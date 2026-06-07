package com.pettrack.modules.usuario.dto.response;

import com.pettrack.modules.usuario.domain.enums.PerfilUsuario;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private String nome;
    private String email;
    private PerfilUsuario perfil;
    private long expiracaoMs;

}