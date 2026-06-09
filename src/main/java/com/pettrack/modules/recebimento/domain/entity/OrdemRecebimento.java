package com.pettrack.modules.recebimento.domain.entity;

import com.pettrack.modules.recebimento.domain.enums.StatusRecebimento;
import com.pettrack.modules.usuario.domain.entity.Usuario;
import com.pettrack.shared.audit.AuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ordens_recebimento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdemRecebimento extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_ordem", nullable = false, unique = true, length = 30)
    private String numeroOrdem;

    @Column(name = "nota_fiscal", nullable = false, length = 50)
    private String notaFiscal;

    @Column(nullable = false, length = 150)
    private String fornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRecebimento status = StatusRecebimento.AGUARDANDO_DESCARGA;

    @Column(name = "data_chegada", nullable = false)
    private LocalDateTime dataChegada;

    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    @Column(length = 500)
    private String observacoes;

    @Builder.Default
    @OneToMany(mappedBy = "ordemRecebimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemRecebimento> itens = new ArrayList<>();

}