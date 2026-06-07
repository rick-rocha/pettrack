package com.pettrack.modules.produto.repository;

import com.pettrack.modules.produto.domain.entity.Produto;
import com.pettrack.modules.produto.domain.enums.CategoriaProduto;
import com.pettrack.modules.produto.domain.enums.EspecieAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    Optional<Produto> findByCodigoSku(String codigoSku);

    Optional<Produto> findByCodigoEan(String codigoEan);

    @Query("SELECT p FROM Produto p WHERE p.categoria = :categoria")
    List<Produto> findByCategoriaProduto(CategoriaProduto categoria);

    @Query("SELECT p FROM Produto p WHERE p.especieAnimal = :especie")
    List<Produto> findByEspecieAnimal(EspecieAnimal especieAnimal);

    @Query("SELECT p FROM Produto p WHERE p.categoria = :categoria AND p.especieAnimal = :especie")
    List<Produto> findByCategoriaAndEspecieAnimal (
            @Param("categoria") CategoriaProduto categoria,
            @Param("especie") EspecieAnimal especie);

    List<Produto> findByAtivoTrue();

    boolean existsByCodigoSku(String codigoSku);

    boolean existsByCodigoEan(String codigoEan);

}