package com.pettrack.modules.recebimento.repository;

import com.pettrack.modules.recebimento.domain.entity.ItemRecebimento;
import com.pettrack.modules.recebimento.domain.enums.StatusItemRecebimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRecebimentoRepository extends JpaRepository<ItemRecebimento, UUID> {

    List<ItemRecebimento> findByOrdemRecebimentoId(UUID ordemId);

    List<ItemRecebimento> findByOrdemRecebimentoIdAndStatus(
            UUID ordemId, StatusItemRecebimento status);

    @Query("""
            SELECT COUNT(i) FROM ItemRecebimento i
            WHERE i.ordemRecebimento.id = :ordemId
            AND i.status = 'PENDENTE'
            """)
    long countItensPendentesPorOrdem(@Param("ordemId") UUID ordemId);

}