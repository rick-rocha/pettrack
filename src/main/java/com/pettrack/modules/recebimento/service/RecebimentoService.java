package com.pettrack.modules.recebimento.service;

import com.pettrack.modules.estoque.service.EstoqueService;
import com.pettrack.modules.estoque.dto.request.ItemEstoqueRequest;
import com.pettrack.modules.estoque.domain.entity.Baia;
import com.pettrack.modules.estoque.repository.BaiaRepository;
import com.pettrack.modules.produto.domain.entity.Produto;
import com.pettrack.modules.produto.repository.ProdutoRepository;
import com.pettrack.modules.recebimento.domain.entity.ItemRecebimento;
import com.pettrack.modules.recebimento.domain.entity.OrdemRecebimento;
import com.pettrack.modules.recebimento.domain.enums.StatusItemRecebimento;
import com.pettrack.modules.recebimento.domain.enums.StatusRecebimento;
import com.pettrack.modules.recebimento.dto.request.ConferirItemRequest;
import com.pettrack.modules.recebimento.dto.request.OrdemRecebimentoRequest;
import com.pettrack.modules.recebimento.dto.response.ItemRecebimentoResponse;
import com.pettrack.modules.recebimento.dto.response.OrdemRecebimentoResponse;
import com.pettrack.modules.recebimento.repository.ItemRecebimentoRepository;
import com.pettrack.modules.recebimento.repository.OrdemRecebimentoRepository;
import com.pettrack.shared.exception.NegocioException;
import com.pettrack.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecebimentoService {

    private final OrdemRecebimentoRepository ordemRepository;
    private final ItemRecebimentoRepository itemRepository;
    private final ProdutoRepository produtoRepository;
    private final BaiaRepository baiaRepository;
    private final EstoqueService estoqueService;

    @Transactional
    public OrdemRecebimentoResponse criarOrdem(OrdemRecebimentoRequest request) {
        if (ordemRepository.existsByNumeroOrdem(request.getNumeroOrdem())) {
            throw new NegocioException("Já existe uma ordem com o número: " + request.getNumeroOrdem());
        }

        OrdemRecebimento ordem = OrdemRecebimento.builder()
                .numeroOrdem(request.getNumeroOrdem())
                .notaFiscal(request.getNotaFiscal())
                .fornecedor(request.getFornecedor())
                .dataChegada(request.getDataChegada())
                .observacoes(request.getObservacoes())
                .build();

        request.getItens().forEach(itemRequest -> {
            Produto produto = produtoRepository.findById(itemRequest.getProdutoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto não encontrado: " + itemRequest.getProdutoId()));

            ItemRecebimento item = ItemRecebimento.builder()
                    .ordemRecebimento(ordem)
                    .produto(produto)
                    .quantidadeEsperada(itemRequest.getQuantidadeEsperada())
                    .observacoes(itemRequest.getObservacoes())
                    .build();

            ordem.getItens().add(item);
        });

        return toOrdemResponse(ordemRepository.save(ordem));
    }

    @Transactional
    public OrdemRecebimentoResponse iniciarConferencia(UUID ordemId) {
        OrdemRecebimento ordem = buscarOrdem(ordemId);

        if (ordem.getStatus() != StatusRecebimento.AGUARDANDO_DESCARGA) {
            throw new NegocioException("Ordem não está aguardando descarga");
        }

        ordem.setStatus(StatusRecebimento.EM_CONFERENCIA);
        return toOrdemResponse(ordemRepository.save(ordem));
    }

    @Transactional
    public ItemRecebimentoResponse conferirItem(UUID ordemId, UUID itemId,
                                                ConferirItemRequest request) {
        OrdemRecebimento ordem = buscarOrdem(ordemId);

        if (ordem.getStatus() != StatusRecebimento.EM_CONFERENCIA &&
                ordem.getStatus() != StatusRecebimento.CONFERIDO_PARCIAL) {
            throw new NegocioException("Ordem não está em conferência");
        }

        ItemRecebimento item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado"));

        if (item.getStatus() != StatusItemRecebimento.PENDENTE) {
            throw new NegocioException("Item já foi conferido");
        }

        Baia baia = baiaRepository.findById(request.getBaiaDestinoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Baia não encontrada"));

        item.setQuantidadeRecebida(request.getQuantidadeRecebida());
        item.setBaiaDestino(baia);
        item.setNumeroLote(request.getNumeroLote());
        item.setDataFabricacao(request.getDataFabricacao());
        item.setDataValidade(request.getDataValidade());
        item.setObservacoes(request.getObservacoes());

        if (request.getQuantidadeRecebida().equals(item.getQuantidadeEsperada())) {
            item.setStatus(StatusItemRecebimento.APROVADO);
        } else {
            item.setStatus(StatusItemRecebimento.DIVERGENTE);
            log.warn("Divergência no recebimento — Ordem: {}, Produto: {}, Esperado: {}, Recebido: {}",
                    ordem.getNumeroOrdem(),
                    item.getProduto().getNome(),
                    item.getQuantidadeEsperada(),
                    request.getQuantidadeRecebida());
        }

        itemRepository.save(item);
        atualizarStatusOrdem(ordem);

        if (item.getStatus() == StatusItemRecebimento.APROVADO ||
                item.getStatus() == StatusItemRecebimento.DIVERGENTE) {
            ItemEstoqueRequest estoqueRequest = ItemEstoqueRequest.builder()
                    .produtoId(item.getProduto().getId())
                    .baiaId(baia.getId())
                    .numeroLote(request.getNumeroLote())
                    .dataFabricacao(request.getDataFabricacao())
                    .dataValidade(request.getDataValidade())
                    .quantidade(request.getQuantidadeRecebida())
                    .notaFiscalEntrada(ordem.getNotaFiscal())
                    .build();

            estoqueService.entrarEstoque(estoqueRequest);
        }

        return toItemResponse(item);
    }

    @Transactional
    public OrdemRecebimentoResponse finalizarOrdem(UUID ordemId) {
        OrdemRecebimento ordem = buscarOrdem(ordemId);

        long pendentes = itemRepository.countItensPendentesPorOrdem(ordemId);
        if (pendentes > 0) {
            throw new NegocioException("Existem " + pendentes + " itens pendentes de conferência");
        }

        ordem.setStatus(StatusRecebimento.FINALIZADO);
        ordem.setDataFinalizacao(LocalDateTime.now());
        return toOrdemResponse(ordemRepository.save(ordem));
    }

    @Transactional(readOnly = true)
    public OrdemRecebimentoResponse buscarPorId(UUID id) {
        return toOrdemResponse(buscarOrdem(id));
    }

    @Transactional(readOnly = true)
    public List<OrdemRecebimentoResponse> listarPorStatus(StatusRecebimento status) {
        if (status == null) {
            return ordemRepository.findAll()
                    .stream()
                    .map(this::toOrdemResponse)
                    .toList();
        }
        return ordemRepository.findByStatus(status)
                .stream()
                .map(this::toOrdemResponse)
                .toList();
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private OrdemRecebimento buscarOrdem(UUID id) {
        return ordemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Ordem de recebimento não encontrada: " + id));
    }

    private void atualizarStatusOrdem(OrdemRecebimento ordem) {
        long pendentes = itemRepository.countItensPendentesPorOrdem(ordem.getId());
        if (pendentes == 0) {
            ordem.setStatus(StatusRecebimento.CONFERIDO_TOTAL);
        } else {
            ordem.setStatus(StatusRecebimento.CONFERIDO_PARCIAL);
        }
        ordemRepository.save(ordem);
    }

    private OrdemRecebimentoResponse toOrdemResponse(OrdemRecebimento ordem) {
        return OrdemRecebimentoResponse.builder()
                .id(ordem.getId())
                .numeroOrdem(ordem.getNumeroOrdem())
                .notaFiscal(ordem.getNotaFiscal())
                .fornecedor(ordem.getFornecedor())
                .responsavelNome(ordem.getResponsavel() != null
                        ? ordem.getResponsavel().getNome() : null)
                .status(ordem.getStatus())
                .dataChegada(ordem.getDataChegada())
                .dataFinalizacao(ordem.getDataFinalizacao())
                .observacoes(ordem.getObservacoes())
                .itens(ordem.getItens().stream().map(this::toItemResponse).toList())
                .criadoEm(ordem.getCriadoEm())
                .atualizadoEm(ordem.getAtualizadoEm())
                .build();
    }

    private ItemRecebimentoResponse toItemResponse(ItemRecebimento item) {
        return ItemRecebimentoResponse.builder()
                .id(item.getId())
                .produtoId(item.getProduto().getId())
                .produtoNome(item.getProduto().getNome())
                .produtoSku(item.getProduto().getCodigoSku())
                .baiaDestinoId(item.getBaiaDestino() != null
                        ? item.getBaiaDestino().getId() : null)
                .baiaDestinoCodigo(item.getBaiaDestino() != null
                        ? item.getBaiaDestino().getCodigo() : null)
                .quantidadeEsperada(item.getQuantidadeEsperada())
                .quantidadeRecebida(item.getQuantidadeRecebida())
                .numeroLote(item.getNumeroLote())
                .dataFabricacao(item.getDataFabricacao())
                .dataValidade(item.getDataValidade())
                .status(item.getStatus())
                .observacoes(item.getObservacoes())
                .criadoEm(item.getCriadoEm())
                .build();

    }

}