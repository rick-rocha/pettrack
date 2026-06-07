package com.pettrack.modules.usuario.repository;

import com.pettrack.modules.usuario.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByMatricula(String matricula);

    boolean existsByEmail(String email);

    boolean existsByMatricula(String matricula);

}