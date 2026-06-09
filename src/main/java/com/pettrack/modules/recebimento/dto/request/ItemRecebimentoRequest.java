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
public class ItemRecebimentoRequest {

    @NotNull(message = "Produto é obrigatório")
    private UUID produtoId;

    @NotNull(message = "Quantidade esperada é obrigatória")
    @Positive(message = "Quantidade esperada deve ser maior que zero")
    private Integer quantidadeEsperada;

    @Size(max = 300, message = "Observações devem ter no máximo 300 caracteres")
    private String observacoes;

}