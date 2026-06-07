package com.pettrack.modules.produto.dto.request;

import com.pettrack.modules.produto.domain.enums.CategoriaProduto;
import com.pettrack.modules.produto.domain.enums.EspecieAnimal;
import com.pettrack.modules.produto.domain.enums.TipoArmazenamento;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProdutoRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String nome;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @NotBlank(message = "Código SKU é obrigatório")
    @Size(max = 50, message = "SKU deve ter no máximo 50 caracteres")
    private String codigoSku;

    @Size(max = 20, message = "Código EAN deve ter máximo 20 caracteres")
    private String codigoEan;

    @NotNull(message = "Categoria é obrigatória")
    private CategoriaProduto categoria;

    @NotNull(message = "Espécie animal é obrigatória")
    private EspecieAnimal especieAnimal;

    @NotNull(message = "Tipo de armazenamento é obrigatório")
    private TipoArmazenamento tipoArmazenamento;

    @NotNull(message = "Peso é obrigatório")
    @DecimalMin(value = "0.001", message = "Peso deve ser maior que zero")
    private BigDecimal pesoKg;

    @DecimalMin(value = "0.01", message = "Preço de custo deve ser maior que zero")
    private BigDecimal precoCusto;

    @NotNull(message = "Preço de venda é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero")
    private BigDecimal precoVenda;

    @NotBlank(message = "Fabricante é obrigatório")
    @Size(max = 100, message = "Fabricante deve ter no máximo 100 caracteres")
    private String fabricante;

    private boolean requerReceita = false;

    private boolean controladoAnvisa = false;

    @Positive(message = "Tempo de validade deve ser positivo")
    private Integer tempoValidadeDias;

}