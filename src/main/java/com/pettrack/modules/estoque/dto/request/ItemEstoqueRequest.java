package com.pettrack.modules.estoque.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ItemEstoqueRequest {

    @NotNull(message = "Produto é obrigatório")
    private UUID produtoId;

    @NotNull(message = "Baia é obrigatória")
    private UUID baiaId;

    @NotBlank(message = "Número do lote é obrigatório")
    @Size(max = 50, message = "Número do lote deve ter no máximo 50 caracteres")
    private String numeroLote;

    private LocalDate dataFabricacao;

    private LocalDate dataValidade;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser maior que zero")
    private Integer quantidade;

    @Size(max = 100, message = "Número de série deve ter no máximo 100 caracteres")
    private String numeroSerie;

    @Size(max = 50, message = "Nota fiscal deve ter no máximo 50 caracteres")
    private String notaFiscalEntrada;

}