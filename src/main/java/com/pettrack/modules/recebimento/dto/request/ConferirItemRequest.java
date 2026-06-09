package com.pettrack.modules.recebimento.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ConferirItemRequest {

    @NotNull(message = "Quantidade recebida é obrigatória")
    @Positive(message = "Quantidade recebida deve ser maior que zero")
    private Integer quantidadeRecebida;

    @NotNull(message = "Baia de destino é obrigatória")
    private UUID baiaDestinoId;

    @NotNull(message = "Número do lote é obrigatório")
    @Size(max = 50, message = "Número do lote deve ter no máximo 50 caracteres")
    private String numeroLote;

    private LocalDate dataFabricacao;

    private LocalDate dataValidade;

    @Size(max = 300, message = "Observações devem ter no máximo 300 caracteres")
    private String observacoes;

}