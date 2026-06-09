package com.pettrack.modules.recebimento.dto.response;

import com.pettrack.modules.recebimento.domain.enums.StatusRecebimento;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OrdemRecebimentoResponse {

    private UUID id;
    private String numeroOrdem;
    private String notaFiscal;
    private String fornecedor;
    private String responsavelNome;
    private StatusRecebimento status;
    private LocalDateTime dataChegada;
    private LocalDateTime dataFinalizacao;
    private String observacoes;
    private List<ItemRecebimentoResponse> itens;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

}