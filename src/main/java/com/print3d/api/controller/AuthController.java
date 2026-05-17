package com.print3d.api.controller;

import com.print3d.api.dto.request.LoginRequest;
import com.print3d.api.dto.response.AuthResponse;
import com.print3d.api.model.Membro;
import com.print3d.api.repository.MembroRepository;
import com.print3d.api.security.JwtUtil;
import com.print3d.api.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MembroRepository membroRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;

    // POST /api/v1/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );

        Membro membro = membroRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        String token = jwtUtil.gerarToken(membro.getEmail());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .email(membro.getEmail())
                .nome(membro.getNome())
                .role(membro.getRole().name())
                .build());
    }

    // POST /api/v1/auth/setup — cria o primeiro admin, bloqueado após isso
    @PostMapping("/setup")
    public ResponseEntity<?> setup(@RequestBody Map<String, String> body) {
        boolean adminExiste = membroRepository.findAll()
                .stream()
                .anyMatch(m -> m.getRole() == Membro.Role.ADMIN);

        if (adminExiste) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Setup já foi realizado. Faça login normalmente."));
        }

        String nome  = body.get("nome");
        String email = body.get("email");
        String senha = body.get("senha");

        if (nome == null || email == null || senha == null || senha.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Nome, email e senha (mín. 6 caracteres) são obrigatórios."));
        }

        if (membroRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Email já cadastrado."));
        }

        Membro admin = Membro.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .role(Membro.Role.ADMIN)
                .status(Membro.Status.ATIVO)
                .dataEntrada(LocalDate.now())
                .build();

        membroRepository.save(admin);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensagem", "Administrador criado com sucesso! Faça login."));
    }

    // POST /api/v1/auth/esqueceu-senha
    // Sempre retorna 200 — não revela se o email existe (evita enumeração de usuários)
    @PostMapping("/esqueceu-senha")
    public ResponseEntity<?> esqueceuSenha(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Email é obrigatório."));
        }
        // Mesmo que o email não exista, retorna 200 por segurança
        passwordResetService.solicitarRecuperacao(email);
        return ResponseEntity.ok(Map.of(
                "mensagem", "Se esse email estiver cadastrado, você receberá um link em breve."
        ));
    }

    // GET /api/v1/auth/validar-token?token=xxx — frontend usa para verificar se o token é válido
    @GetMapping("/validar-token")
    public ResponseEntity<?> validarToken(@RequestParam String token) {
        try {
            String email = passwordResetService.validarToken(token);
            return ResponseEntity.ok(Map.of("email", email, "valido", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage(), "valido", false));
        }
    }

    // POST /api/v1/auth/resetar-senha
    @PostMapping("/resetar-senha")
    public ResponseEntity<?> resetarSenha(@RequestBody Map<String, String> body) {
        String token     = body.get("token");
        String novaSenha = body.get("novaSenha");

        if (token == null || novaSenha == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Token e nova senha são obrigatórios."));
        }

        try {
            passwordResetService.redefinirSenha(token, novaSenha);
            return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso! Faça login."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}