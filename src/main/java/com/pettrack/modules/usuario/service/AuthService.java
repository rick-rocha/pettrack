package com.pettrack.modules.usuario.service;

import com.pettrack.config.security.JwtService;
import com.pettrack.modules.usuario.domain.entity.Usuario;
import com.pettrack.modules.usuario.dto.request.LoginRequest;
import com.pettrack.modules.usuario.dto.response.LoginResponse;
import com.pettrack.modules.usuario.repository.UsuarioRepository;
import com.pettrack.shared.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getSenha()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new NegocioException("Email ou senha inválidos");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NegocioException("Usuário não encontrado"));

        String token = jwtService.gerarToken(usuario);

        return LoginResponse.builder()
                .token(token)
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .perfil(usuario.getPerfil())
                .expiracaoMs(86400000L)
                .build();
    }

}