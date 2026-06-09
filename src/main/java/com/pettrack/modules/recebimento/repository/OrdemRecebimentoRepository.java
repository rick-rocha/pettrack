package com.pettrack.modules.recebimento.repository;

import com.pettrack.modules.recebimento.domain.entity.OrdemRecebimento;
import com.pettrack.modules.recebimento.domain.enums.StatusRecebimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdemRecebimentoRepository extends JpaRepository<OrdemRecebimento, UUID> {

    Optional<OrdemRecebimento> findByNumeroOrdem(String numeroOrdem);

    List<OrdemRecebimento> findByStatus(StatusRecebimento status);

    boolean existsByNumeroOrdem(String numeroOrdem);

}