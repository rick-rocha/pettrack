package com.pettrack.modules.transporte.service;

import com.pettrack.modules.ecommerce.domain.entity.Gaiola;
import com.pettrack.modules.ecommerce.domain.enums.StatusGaiola;
import com.pettrack.modules.ecommerce.domain.enums.StatusPedido;
import com.pettrack.modules.ecommerce.repository.GaiolaRepository;
import com.pettrack.modules.ecommerce.repository.PedidoRepository;
import com.pettrack.modules.filial.domain.entity.Filial;
import com.pettrack.modules.filial.repository.FilialRepository;
import com.pettrack.modules.rastreamento.service.RastreamentoService;
import com.pettrack.modules.transporte.domain.entity.Pallet;
import com.pettrack.modules.transporte.domain.entity.PalletGaiola;
import com.pettrack.modules.transporte.domain.entity.Viagem;
import com.pettrack.modules.transporte.domain.entity.ViagemPallet;
import com.pettrack.modules.transporte.domain.enums.StatusPallet;
import com.pettrack.modules.transporte.domain.enums.StatusViagem;
import com.pettrack.modules.transporte.dto.request.AdicionarGaiolaPalletRequest;
import com.pettrack.modules.transporte.dto.request.PalletRequest;
import com.pettrack.modules.transporte.dto.request.ViagemRequest;
import com.pettrack.modules.transporte.dto.response.PalletResponse;
import com.pettrack.modules.transporte.dto.response.ViagemResponse;
import com.pettrack.modules.transporte.repository.PalletRepository;
import com.pettrack.modules.transporte.repository.ViagemRepository;
import com.pettrack.modules.transportadora.domain.entity.Veiculo;
import com.pettrack.modules.transportadora.repository.VeiculoRepository;
import com.pettrack.modules.usuario.domain.entity.Usuario;
import com.pettrack.modules.usuario.repository.UsuarioRepository;
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
public class TransporteService {

    private final PalletRepository palletRepository;
    private final ViagemRepository viagemRepository;
    private final GaiolaRepository gaiolaRepository;
    private final PedidoRepository pedidoRepository;
    private final VeiculoRepository veiculoRepository;
    private final FilialRepository filialRepository;
    private final UsuarioRepository usuarioRepository;
    private final RastreamentoService rastreamentoService;

    // ==================== PALLETS ====================

    @Transactional
    public PalletResponse criarPallet(PalletRequest request) {
        if (palletRepository.existsByCodigoPallet(request.getCodigoPallet())) {
            throw new NegocioException("Já existe um pallet com o código: "
                    + request.getCodigoPallet());
        }

        Pallet pallet = Pallet.builder()
                .codigoPallet(request.getCodigoPallet())
                .regiaoDestino(request.getRegiaoDestino())
                .pesoMaximoKg(request.getPesoMaximoKg())
                .observacoes(request.getObservacoes())
                .build();

        return toPalletResponse(palletRepository.save(pallet));
    }

    @Transactional
    public PalletResponse adicionarGaiola(UUID palletId,
                                          AdicionarGaiolaPalletRequest request) {
        Pallet pallet = buscarPallet(palletId);

        if (pallet.getStatus() != StatusPallet.EM_MONTAGEM) {
            throw new NegocioException("Pallet não está em montagem");
        }

        Gaiola gaiola = gaiolaRepository.findById(request.getGaiolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gaiola não encontrada"));

        if (gaiola.getStatus() != StatusGaiola.FECHADA) {
            throw new NegocioException("Gaiola precisa estar fechada para ser adicionada ao pallet");
        }

        if (!pallet.getRegiaoDestino().equals(gaiola.getRegiaoCD())) {
            throw new NegocioException("Região da gaiola (" + gaiola.getRegiaoCD()
                    + ") não corresponde à região do pallet (" + pallet.getRegiaoDestino() + ")");
        }

        double pesoGaiola = gaiola.getPedidos().stream()
                .mapToDouble(p -> p.getPesoTotalKg().doubleValue())
                .sum();

        if (!pallet.aceitaPeso(java.math.BigDecimal.valueOf(pesoGaiola))) {
            throw new NegocioException(String.format(
                    "Pallet excederia o peso máximo. Máximo: %.2fkg, Atual: %.2fkg, Gaiola: %.2fkg",
                    pallet.getPesoMaximoKg().doubleValue(),
                    pallet.getPesoAtualKg().doubleValue(),
                    pesoGaiola));
        }

        PalletGaiola palletGaiola = PalletGaiola.builder()
                .pallet(pallet)
                .gaiola(gaiola)
                .build();

        pallet.getGaiolas().add(palletGaiola);
        pallet.setPesoAtualKg(pallet.getPesoAtualKg()
                .add(java.math.BigDecimal.valueOf(pesoGaiola)));

        gaiola.setStatus(StatusGaiola.NO_PALLET);
        gaiolaRepository.save(gaiola);

        return toPalletResponse(palletRepository.save(pallet));
    }

    @Transactional
    public PalletResponse validarPallet(UUID palletId, UUID usuarioId) {
        Pallet pallet = buscarPallet(palletId);

        if (pallet.getStatus() != StatusPallet.EM_MONTAGEM) {
            throw new NegocioException("Pallet não está em montagem");
        }

        if (pallet.getGaiolas().isEmpty()) {
            throw new NegocioException("Pallet está vazio — adicione gaiolas antes de validar");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        pallet.setStatus(StatusPallet.VALIDADO);
        pallet.setValidadoPor(usuario);

        log.info("Pallet validado — Código: {}, Peso: {}kg, Região: {}, Validado por: {}",
                pallet.getCodigoPallet(),
                pallet.getPesoAtualKg(),
                pallet.getRegiaoDestino(),
                usuario.getNome());

        return toPalletResponse(palletRepository.save(pallet));
    }

    @Transactional(readOnly = true)
    public PalletResponse buscarPorId(UUID id) {
        return toPalletResponse(buscarPallet(id));
    }

    @Transactional(readOnly = true)
    public List<PalletResponse> listarPorStatus(StatusPallet status) {
        if (status == null) {
            return palletRepository.findAll().stream()
                    .map(this::toPalletResponse).toList();
        }
        return palletRepository.findByStatus(status).stream()
                .map(this::toPalletResponse).toList();
    }

    // ==================== VIAGENS ====================

    @Transactional
    public ViagemResponse criarViagem(ViagemRequest request) {
        if (viagemRepository.existsByCodigoViagem(request.getCodigoViagem())) {
            throw new NegocioException("Já existe uma viagem com o código: "
                    + request.getCodigoViagem());
        }

        Veiculo veiculo = veiculoRepository.findById(request.getVeiculoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado"));

        if (!veiculo.isDisponivel()) {
            throw new NegocioException("Veículo não está disponível");
        }

        Filial filial = filialRepository.findById(request.getFilialDestinoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Filial não encontrada"));

        Viagem viagem = Viagem.builder()
                .codigoViagem(request.getCodigoViagem())
                .veiculo(veiculo)
                .filialDestino(filial)
                .regiaoCD(filial.getRegiao())
                .observacoes(request.getObservacoes())
                .build();

        veiculo.setDisponivel(false);
        veiculoRepository.save(veiculo);

        return toViagemResponse(viagemRepository.save(viagem));
    }

    @Transactional
    public ViagemResponse adicionarPallet(UUID viagemId, UUID palletId) {
        Viagem viagem = buscarViagem(viagemId);
        Pallet pallet = buscarPallet(palletId);

        if (viagem.getStatus() != StatusViagem.AGUARDANDO_CARREGAMENTO &&
                viagem.getStatus() != StatusViagem.EM_CARREGAMENTO) {
            throw new NegocioException("Viagem não está em fase de carregamento");
        }

        if (pallet.getStatus() != StatusPallet.VALIDADO) {
            throw new NegocioException("Pallet precisa estar validado para ser carregado");
        }

        if (!pallet.getRegiaoDestino().equals(viagem.getRegiaoCD())) {
            throw new NegocioException("Região do pallet não corresponde à região da viagem");
        }

        ViagemPallet viagemPallet = ViagemPallet.builder()
                .viagem(viagem)
                .pallet(pallet)
                .build();

        viagem.getPallets().add(viagemPallet);
        viagem.setStatus(StatusViagem.EM_CARREGAMENTO);
        pallet.setStatus(StatusPallet.CARREGADO);
        palletRepository.save(pallet);

        return toViagemResponse(viagemRepository.save(viagem));
    }

    @Transactional
    public ViagemResponse partirViagem(UUID viagemId) {
        Viagem viagem = buscarViagem(viagemId);

        if (viagem.getStatus() != StatusViagem.EM_CARREGAMENTO) {
            throw new NegocioException("Viagem não está em carregamento");
        }

        if (viagem.getPallets().isEmpty()) {
            throw new NegocioException("Viagem sem pallets carregados");
        }

        viagem.setStatus(StatusViagem.EM_TRANSITO);
        viagem.setDataSaida(LocalDateTime.now());

        viagem.getPallets().forEach(vp -> {
            vp.getPallet().setStatus(StatusPallet.EM_TRANSITO);
            palletRepository.save(vp.getPallet());
            vp.getPallet().getGaiolas().forEach(pg ->
                    pg.getGaiola().getPedidos().forEach(pedido -> {
                        pedido.setStatus(StatusPedido.EM_TRANSITO_CD_FILIAL);
                        pedidoRepository.save(pedido);
                        rastreamentoService.registrar(pedido,
                                StatusPedido.NO_PALLET,
                                StatusPedido.EM_TRANSITO_CD_FILIAL,
                                null,
                                "Em trânsito — " + viagem.getVeiculo().getPlaca(),
                                "Caminhão partiu para filial: "
                                        + viagem.getFilialDestino().getNome());
                    }));
        });

        log.info("Viagem partiu — Código: {}, Filial: {}, Pallets: {}",
                viagem.getCodigoViagem(),
                viagem.getFilialDestino().getNome(),
                viagem.getPallets().size());

        return toViagemResponse(viagemRepository.save(viagem));
    }

    @Transactional
    public ViagemResponse registrarChegada(UUID viagemId) {
        Viagem viagem = buscarViagem(viagemId);

        if (viagem.getStatus() != StatusViagem.EM_TRANSITO) {
            throw new NegocioException("Viagem não está em trânsito");
        }

        viagem.setStatus(StatusViagem.CHEGOU_FILIAL);
        viagem.setDataChegada(LocalDateTime.now());

        viagem.getPallets().forEach(vp -> {
            vp.getPallet().getGaiolas().forEach(pg ->
                    pg.getGaiola().getPedidos().forEach(pedido -> {
                        pedido.setStatus(StatusPedido.RECEBIDO_NA_FILIAL);
                        pedidoRepository.save(pedido);
                        rastreamentoService.registrar(pedido,
                                StatusPedido.EM_TRANSITO_CD_FILIAL,
                                StatusPedido.RECEBIDO_NA_FILIAL,
                                null,
                                viagem.getFilialDestino().getNome(),
                                "Caminhão chegou na filial");
                    }));
        });

        viagem.getVeiculo().setDisponivel(true);
        veiculoRepository.save(viagem.getVeiculo());

        return toViagemResponse(viagemRepository.save(viagem));
    }

    @Transactional(readOnly = true)
    public List<ViagemResponse> listarPorStatus(StatusViagem status) {
        if (status == null) {
            return viagemRepository.findAll().stream()
                    .map(this::toViagemResponse).toList();
        }
        return viagemRepository.findByStatus(status).stream()
                .map(this::toViagemResponse).toList();
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private Pallet buscarPallet(UUID id) {
        return palletRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pallet não encontrado: " + id));
    }

    private Viagem buscarViagem(UUID id) {
        return viagemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Viagem não encontrada: " + id));
    }

    private PalletResponse toPalletResponse(Pallet pallet) {
        return PalletResponse.builder()
                .id(pallet.getId())
                .codigoPallet(pallet.getCodigoPallet())
                .regiaoDestino(pallet.getRegiaoDestino())
                .transportadoraNome(pallet.getTransportadora() != null
                        ? pallet.getTransportadora().getNome() : null)
                .pesoMaximoKg(pallet.getPesoMaximoKg())
                .pesoAtualKg(pallet.getPesoAtualKg())
                .percentualOcupacao(pallet.percentualOcupacao())
                .status(pallet.getStatus())
                .validadoPorNome(pallet.getValidadoPor() != null
                        ? pallet.getValidadoPor().getNome() : null)
                .observacoes(pallet.getObservacoes())
                .totalGaiolas(pallet.getGaiolas().size())
                .criadoEm(pallet.getCriadoEm())
                .atualizadoEm(pallet.getAtualizadoEm())
                .build();
    }

    private ViagemResponse toViagemResponse(Viagem viagem) {
        return ViagemResponse.builder()
                .id(viagem.getId())
                .codigoViagem(viagem.getCodigoViagem())
                .veiculoPlaca(viagem.getVeiculo().getPlaca())
                .transportadoraNome(viagem.getVeiculo().getTransportadora().getNome())
                .filialDestinoNome(viagem.getFilialDestino().getNome())
                .regiaoCD(viagem.getRegiaoCD())
                .status(viagem.getStatus())
                .dataSaida(viagem.getDataSaida())
                .dataChegada(viagem.getDataChegada())
                .observacoes(viagem.getObservacoes())
                .totalPallets(viagem.getPallets().size())
                .criadoEm(viagem.getCriadoEm())
                .atualizadoEm(viagem.getAtualizadoEm())
                .build();
    }

}