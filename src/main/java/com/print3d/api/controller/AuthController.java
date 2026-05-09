package com.print3d.api.controller;

import com.print3d.api.dto.request.LoginRequest;
import com.print3d.api.dto.response.AuthResponse;
import com.print3d.api.model.Membro;
import com.print3d.api.repository.MembroRepository;
import com.print3d.api.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MembroRepository membroRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        // Delega a autenticação ao Spring Security (verifica email + senha BCrypt)
        // Lança exceção automaticamente se as credenciais estiverem erradas
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );

        // Se chegou aqui, autenticação foi bem-sucedida — busca o membro para montar a resposta
        Membro membro = membroRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        // Gera o token JWT com o email como subject
        String token = jwtUtil.gerarToken(membro.getEmail());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .email(membro.getEmail())
                .nome(membro.getNome())
                .role(membro.getRole().name())
                .build());
    }
}