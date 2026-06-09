package com.pettrack.modules.recebimento.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrdemRecebimentoRequest {

    @NotBlank(message = "Número da ordem é obrigatório")
    @Size(max = 30, message = "Número da ordem deve ter no máximo 30 caracteres")
    private String numeroOrdem;

    @NotBlank(message = "Nota fiscal é obrigatória")
    @Size(max = 50, message = "Nota fiscal deve ter no máximo 50 caracteres")
    private String notaFiscal;

    @NotBlank(message = "Fornecedor é obrigatório")
    @Size(max = 150, message = "Fornecedor deve ter no máximo 150 caracteres")
    private String fornecedor;

    @NotNull(message = "Data de chegada é obrigatória")
    private LocalDateTime dataChegada;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;

    @Valid
    @NotEmpty(message = "A ordem deve ter pelo menos um item")
    private List<ItemRecebimentoRequest> itens;

}
