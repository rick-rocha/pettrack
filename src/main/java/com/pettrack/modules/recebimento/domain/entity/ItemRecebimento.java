package com.pettrack.modules.recebimento.domain.entity;

import com.pettrack.modules.estoque.domain.entity.Baia;
import com.pettrack.modules.produto.domain.entity.Produto;
import com.pettrack.modules.recebimento.domain.enums.StatusItemRecebimento;
import com.pettrack.shared.audit.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "itens_recebimento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRecebimento extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_recebimento_id", nullable = false)
    private OrdemRecebimento ordemRecebimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baia_destino_id")
    private Baia baiaDestino;

    @Column(name = "quantidade_esperada", nullable = false)
    private Integer quantidadeEsperada;

    @Column(name = "quantidade_recebida")
    private Integer quantidadeRecebida;

    @Column(name = "numero_lote", length = 50)
    private String numeroLote;

    @Column(name = "data_fabricacao")
    private LocalDate dataFabricacao;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusItemRecebimento status = StatusItemRecebimento.PENDENTE;

    @Column(length = 300)
    private String observacoes;

}