package com.pettrack.modules.ecommerce.service;

import com.pettrack.modules.ecommerce.domain.entity.Gaiola;
import com.pettrack.modules.ecommerce.domain.entity.ItemPedido;
import com.pettrack.modules.ecommerce.domain.entity.Pedido;
import com.pettrack.modules.ecommerce.domain.enums.RegiaoCD;
import com.pettrack.modules.ecommerce.domain.enums.StatusGaiola;
import com.pettrack.modules.ecommerce.domain.enums.StatusPedido;
import com.pettrack.modules.ecommerce.dto.request.ItemPedidoRequest;
import com.pettrack.modules.ecommerce.dto.request.PedidoRequest;
import com.pettrack.modules.ecommerce.dto.response.GaiolaResponse;
import com.pettrack.modules.ecommerce.dto.response.ItemPedidoResponse;
import com.pettrack.modules.ecommerce.dto.response.PedidoResponse;
import com.pettrack.modules.ecommerce.repository.GaiolaRepository;
import com.pettrack.modules.ecommerce.repository.ItemPedidoRepository;
import com.pettrack.modules.ecommerce.repository.PedidoRepository;
import com.pettrack.modules.estoque.domain.entity.ItemEstoque;
import com.pettrack.modules.estoque.domain.enums.StatusItemEstoque;
import com.pettrack.modules.estoque.repository.ItemEstoqueRepository;
import com.pettrack.modules.produto.domain.entity.Produto;
import com.pettrack.modules.produto.repository.ProdutoRepository;
import com.pettrack.modules.rastreamento.service.RastreamentoService;
import com.pettrack.shared.exception.NegocioException;
import com.pettrack.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EcommerceService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final GaiolaRepository gaiolaRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemEstoqueRepository itemEstoqueRepository;
    private final RastreamentoService rastreamentoService;

    // ==================== PEDIDOS ====================

    @Transactional
    public PedidoResponse criarPedido(PedidoRequest request) {
        if (pedidoRepository.existsByNumeroPedido(request.getNumeroPedido())) {
            throw new NegocioException("Já existe um pedido com o número: " + request.getNumeroPedido());
        }

        RegiaoCD regiaoCD = determinarRegiaoCD(request.getUfEntrega());

        Pedido pedido = Pedido.builder()
                .numeroPedido(request.getNumeroPedido())
                .clienteNome(request.getClienteNome())
                .clienteCpf(request.getClienteCpf())
                .clienteEmail(request.getClienteEmail())
                .clienteTelefone(request.getClienteTelefone())
                .enderecoEntrega(request.getEnderecoEntrega())
                .cepEntrega(request.getCepEntrega())
                .cidadeEntrega(request.getCidadeEntrega())
                .ufEntrega(request.getUfEntrega())
                .regiaoCD(regiaoCD)
                .observacoes(request.getObservacoes())
                .build();

        BigDecimal valorTotal = BigDecimal.ZERO;
        BigDecimal pesoTotal = BigDecimal.ZERO;

        for (ItemPedidoRequest itemRequest : request.getItens()) {
            Produto produto = produtoRepository.findById(itemRequest.getProdutoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));

            Integer disponivel = itemEstoqueRepository.totalDisponivelPorProduto(produto.getId());
            if (disponivel == null || disponivel < itemRequest.getQuantidade()) {
                throw new NegocioException("Estoque insuficiente para o produto: " + produto.getNome()
                        + ". Disponível: " + (disponivel != null ? disponivel : 0));
            }

            BigDecimal precoTotal = produto.getPrecoVenda()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantidade()));

            ItemPedido item = ItemPedido.builder()
                    .pedido(pedido)
                    .produto(produto)
                    .quantidade(itemRequest.getQuantidade())
                    .precoUnitario(produto.getPrecoVenda())
                    .precoTotal(precoTotal)
                    .build();

            pedido.getItens().add(item);
            valorTotal = valorTotal.add(precoTotal);
            pesoTotal = pesoTotal.add(
                    produto.getPesoKg().multiply(BigDecimal.valueOf(itemRequest.getQuantidade())));
        }

        pedido.setValorTotal(valorTotal);
        pedido.setPesoTotalKg(pesoTotal);

        return toPedidoResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse confirmarPagamento(UUID pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);

        if (pedido.getStatus() != StatusPedido.PEDIDO_CRIADO) {
            throw new NegocioException("Pedido não está aguardando confirmação de pagamento");
        }

        pedido.setStatus(StatusPedido.AGUARDANDO_SEPARACAO);
        log.info("Pagamento confirmado — Pedido: {}, Cliente: {}",
                pedido.getNumeroPedido(), pedido.getClienteNome());

        rastreamentoService.registrar(pedido,
                StatusPedido.PEDIDO_CRIADO,
                StatusPedido.AGUARDANDO_SEPARACAO,
                null,
                "CD — E-commerce",
                "Pagamento confirmado");

        return toPedidoResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse separarPedido(UUID pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_SEPARACAO) {
            throw new NegocioException("Pedido não está aguardando separação");
        }

        pedido.setStatus(StatusPedido.EM_SEPARACAO);

        for (ItemPedido item : pedido.getItens()) {
            List<ItemEstoque> disponiveis = itemEstoqueRepository
                    .findDisponiveisPorProdutoOrdenadosPorValidade(item.getProduto().getId());

            if (disponiveis.isEmpty()) {
                throw new NegocioException("Sem estoque disponível para: " + item.getProduto().getNome());
            }

            ItemEstoque itemEstoque = disponiveis.get(0);
            itemEstoque.setStatus(StatusItemEstoque.RESERVADO);
            itemEstoqueRepository.save(itemEstoque);
            item.setItemEstoque(itemEstoque);
            itemPedidoRepository.save(item);
        }

        pedido.setStatus(StatusPedido.AGUARDANDO_EMBALAGEM);

        rastreamentoService.registrar(pedido,
                StatusPedido.AGUARDANDO_SEPARACAO,
                StatusPedido.AGUARDANDO_EMBALAGEM,
                null,
                "CD — Estoque",
                "Itens separados das baias com FIFO por validade");

        return toPedidoResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse embalarPedido(UUID pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_EMBALAGEM) {
            throw new NegocioException("Pedido não está aguardando embalagem");
        }

        pedido.setStatus(StatusPedido.EMBALADO);

        rastreamentoService.registrar(pedido,
                StatusPedido.AGUARDANDO_EMBALAGEM,
                StatusPedido.EMBALADO,
                null,
                "CD — E-commerce",
                "Pedido embalado e etiquetado");

        return toPedidoResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse adicionarNaGaiola(UUID pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);

        if (pedido.getStatus() != StatusPedido.EMBALADO) {
            throw new NegocioException("Pedido precisa estar embalado para ir à gaiola");
        }

        Gaiola gaiola = gaiolaRepository
                .findByRegiaoCDAndStatus(pedido.getRegiaoCD(), StatusGaiola.ABERTA)
                .orElseThrow(() -> new NegocioException(
                        "Nenhuma gaiola aberta disponível para a região: " + pedido.getRegiaoCD()));

        pedido.setGaiola(gaiola);
        pedido.setStatus(StatusPedido.NA_GAIOLA);

        rastreamentoService.registrar(pedido,
                StatusPedido.EMBALADO,
                StatusPedido.NA_GAIOLA,
                null,
                "CD — E-commerce",
                "Pedido adicionado à gaiola: " + gaiola.getCodigo()
                        + " — Região: " + gaiola.getRegiaoCD());

        return toPedidoResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse cancelarPedido(UUID pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);

        if (pedido.getStatus() == StatusPedido.EM_ROTA_ENTREGA ||
                pedido.getStatus() == StatusPedido.ENTREGUE) {
            pedido.setStatus(StatusPedido.CANCELAMENTO_BLOQUEADO);
            pedidoRepository.save(pedido);
            throw new NegocioException("Cancelamento bloqueado — pedido já está em rota de entrega ou entregue");
        }

        if (pedido.getStatus().name().startsWith("CANCELADO")) {
            throw new NegocioException("Pedido já está cancelado");
        }

        boolean antesDoTransporte = pedido.getStatus() == StatusPedido.PEDIDO_CRIADO
                || pedido.getStatus() == StatusPedido.AGUARDANDO_SEPARACAO
                || pedido.getStatus() == StatusPedido.EM_SEPARACAO
                || pedido.getStatus() == StatusPedido.AGUARDANDO_EMBALAGEM
                || pedido.getStatus() == StatusPedido.EMBALADO
                || pedido.getStatus() == StatusPedido.NA_GAIOLA;

        for (ItemPedido item : pedido.getItens()) {
            if (item.getItemEstoque() != null) {
                item.getItemEstoque().setStatus(StatusItemEstoque.DISPONIVEL);
                itemEstoqueRepository.save(item.getItemEstoque());
            }
        }

        if (antesDoTransporte) {
            pedido.setStatus(StatusPedido.CANCELADO_ESTOQUE);
        } else {
            pedido.setStatus(StatusPedido.CANCELADO_RETORNO_CD);
        }

        return toPedidoResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(UUID id) {
        return toPedidoResponse(buscarPedido(id));
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorNumero(String numeroPedido) {
        Pedido pedido = pedidoRepository.findByNumeroPedido(numeroPedido)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pedido não encontrado: " + numeroPedido));
        return toPedidoResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorStatus(StatusPedido status) {
        if (status == null) {
            return pedidoRepository.findAll()
                    .stream()
                    .map(this::toPedidoResponse)
                    .toList();
        }
        return pedidoRepository.findByStatus(status)
                .stream()
                .map(this::toPedidoResponse)
                .toList();
    }

    // ==================== GAIOLAS ====================

    @Transactional(readOnly = true)
    public List<GaiolaResponse> listarGaiolas() {
        return gaiolaRepository.findAll()
                .stream()
                .map(this::toGaiolaResponse)
                .toList();
    }

    @Transactional
    public GaiolaResponse fecharGaiola(UUID gaiolaId) {
        Gaiola gaiola = gaiolaRepository.findById(gaiolaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gaiola não encontrada"));

        if (gaiola.getStatus() != StatusGaiola.ABERTA) {
            throw new NegocioException("Gaiola não está aberta");
        }

        if (gaiola.getPedidos().isEmpty()) {
            throw new NegocioException("Gaiola está vazia — adicione pedidos antes de fechar");
        }

        gaiola.setStatus(StatusGaiola.FECHADA);

        gaiola.getPedidos().forEach(p -> {
            p.setStatus(StatusPedido.NO_PALLET);
            rastreamentoService.registrar(p,
                    StatusPedido.NA_GAIOLA,
                    StatusPedido.NO_PALLET,
                    null,
                    "CD — Transporte",
                    "Gaiola fechada e destinada ao pallet");
        });

        return toGaiolaResponse(gaiolaRepository.save(gaiola));
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private RegiaoCD determinarRegiaoCD(String uf) {
        return switch (uf.toUpperCase()) {
            case "AM", "PA", "AC", "RO", "RR", "AP", "TO" -> RegiaoCD.NORTE;
            case "BA", "SE", "AL", "PE", "PB", "RN", "CE", "PI", "MA" -> RegiaoCD.NORDESTE;
            case "GO", "MT", "MS", "DF" -> RegiaoCD.CENTRO_OESTE;
            case "RJ", "MG", "ES" -> RegiaoCD.SUDESTE;
            case "SP" -> RegiaoCD.SAO_PAULO;
            case "PR", "SC", "RS" -> RegiaoCD.SUL;
            default -> throw new NegocioException("UF inválida ou não reconhecida: " + uf);
        };
    }

    private Pedido buscarPedido(UUID id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado: " + id));
    }

    private PedidoResponse toPedidoResponse(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .numeroPedido(pedido.getNumeroPedido())
                .clienteNome(pedido.getClienteNome())
                .clienteCpf(pedido.getClienteCpf())
                .clienteEmail(pedido.getClienteEmail())
                .clienteTelefone(pedido.getClienteTelefone())
                .enderecoEntrega(pedido.getEnderecoEntrega())
                .cepEntrega(pedido.getCepEntrega())
                .cidadeEntrega(pedido.getCidadeEntrega())
                .ufEntrega(pedido.getUfEntrega())
                .regiaoCD(pedido.getRegiaoCD())
                .subregiaoFilial(pedido.getSubregiaoFilial())
                .status(pedido.getStatus())
                .valorTotal(pedido.getValorTotal())
                .pesoTotalKg(pedido.getPesoTotalKg())
                .observacoes(pedido.getObservacoes())
                .gaiolaCodigo(pedido.getGaiola() != null ? pedido.getGaiola().getCodigo() : null)
                .itens(pedido.getItens().stream().map(this::toItemResponse).toList())
                .criadoEm(pedido.getCriadoEm())
                .atualizadoEm(pedido.getAtualizadoEm())
                .build();
    }

    private ItemPedidoResponse toItemResponse(ItemPedido item) {
        return ItemPedidoResponse.builder()
                .id(item.getId())
                .produtoId(item.getProduto().getId())
                .produtoNome(item.getProduto().getNome())
                .produtoSku(item.getProduto().getCodigoSku())
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .precoTotal(item.getPrecoTotal())
                .separado(item.getItemEstoque() != null)
                .build();
    }

    private GaiolaResponse toGaiolaResponse(Gaiola gaiola) {
        return GaiolaResponse.builder()
                .id(gaiola.getId())
                .codigo(gaiola.getCodigo())
                .regiaoCD(gaiola.getRegiaoCD())
                .status(gaiola.getStatus())
                .totalPedidos(gaiola.getPedidos().size())
                .criadoEm(gaiola.getCriadoEm())
                .atualizadoEm(gaiola.getAtualizadoEm())
                .build();
    }

}