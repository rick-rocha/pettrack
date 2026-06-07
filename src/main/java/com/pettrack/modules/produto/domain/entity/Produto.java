package com.pettrack.modules.produto.domain.entity;

import com.pettrack.modules.produto.domain.enums.CategoriaProduto;
import com.pettrack.modules.produto.domain.enums.EspecieAnimal;
import com.pettrack.modules.produto.domain.enums.TipoArmazenamento;
import com.pettrack.shared.audit.AuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(name = "codigo_sku", nullable = false, unique = true, length = 50)
    private String codigoSku;

    @Column(name = "codigo_ean", length = 20)
    private String codigoEan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaProduto categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "especie_animal", nullable = false)
    private EspecieAnimal especieAnimal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_armazenamento", nullable = false)
    private TipoArmazenamento tipoArmazenamento;

    @Column(name = "peso_kg", nullable = false, precision = 10, scale = 3)
    private BigDecimal pesoKg;

    @Column(name = "preco_custo", precision = 10, scale = 2)
    private BigDecimal precoCusto;

    @Column(name = "preco_venda", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoVenda;

    @Column(nullable = false, length = 100)
    private String fabricante;

    @Builder.Default
    @Column(name = "requer_receita")
    private boolean requerReceita = false;

    @Builder.Default
    @Column(name = "controlado_anvisa")
    private boolean controladoAnvisa = false;

    @Column(name = "tempo_validade_dias")
    private Integer tempoValidadeDias;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = true;

}
