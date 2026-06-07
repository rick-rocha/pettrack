package com.pettrack.modules.produto.dto.response;

import com.pettrack.modules.produto.domain.enums.CategoriaProduto;
import com.pettrack.modules.produto.domain.enums.EspecieAnimal;
import com.pettrack.modules.produto.domain.enums.TipoArmazenamento;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProdutoResponse {

    private UUID id;
    private String nome;
    private String descricao;
    private String codigoSku;
    private String codigoEan;
    private CategoriaProduto categoria;
    private EspecieAnimal especieAnimal;
    private TipoArmazenamento tipoArmazenamento;
    private BigDecimal pesoKg;
    private BigDecimal precoVenda;
    private String fabricante;
    private boolean requerReceita;
    private boolean controladoAnvisa;
    private Integer tempoValidadeDias;
    private boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

}