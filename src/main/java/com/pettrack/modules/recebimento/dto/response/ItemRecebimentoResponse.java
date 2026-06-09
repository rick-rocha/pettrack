package com.pettrack.modules.recebimento.dto.response;

import com.pettrack.modules.recebimento.domain.enums.StatusItemRecebimento;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ItemRecebimentoResponse {

    private UUID id;
    private UUID produtoId;
    private String produtoNome;
    private String produtoSku;
    private UUID baiaDestinoId;
    private String baiaDestinoCodigo;
    private Integer quantidadeEsperada;
    private Integer quantidadeRecebida;
    private String numeroLote;
    private LocalDate dataFabricacao;
    private LocalDate dataValidade;
    private StatusItemRecebimento status;
    private String observacoes;
    private LocalDateTime criadoEm;

}