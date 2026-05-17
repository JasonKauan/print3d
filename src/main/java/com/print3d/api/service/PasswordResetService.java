package com.print3d.api.service;

import com.print3d.api.model.Membro;
import com.print3d.api.model.PasswordResetToken;
import com.print3d.api.repository.MembroRepository;
import com.print3d.api.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final MembroRepository membroRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // URL base do frontend — usada para montar o link do email
    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // Solicita recuperação de senha
    // Sempre retorna sucesso — não revela se o email existe ou não (segurança)
    public void solicitarRecuperacao(String email) {
        membroRepository.findByEmail(email).ifPresent(membro -> {

            // Remove tokens anteriores do mesmo membro
            tokenRepository.deleteByMembroId(membro.getId());

            // Gera token UUID aleatório e seguro
            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .membro(membro)
                    .expiracao(LocalDateTime.now().plusHours(1)) // expira em 1 hora
                    .build();

            tokenRepository.save(resetToken);

            // Monta o link que vai no email
            String link = frontendUrl + "/resetar-senha?token=" + token;
            emailService.enviarRecuperacaoSenha(membro.getEmail(), membro.getNome(), link);

            log.info("Token de recuperação gerado para: {}", email);
        });
    }

    // Valida o token — retorna o email do membro se válido
    public String validarToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido ou inexistente."));

        if (resetToken.getUsado()) {
            throw new RuntimeException("Este link já foi utilizado. Solicite um novo.");
        }

        if (resetToken.isExpirado()) {
            throw new RuntimeException("Este link expirou. Solicite um novo.");
        }

        return resetToken.getMembro().getEmail();
    }

    // Redefine a senha usando o token
    public void redefinirSenha(String token, String novaSenha) {
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new RuntimeException("A senha deve ter no mínimo 6 caracteres.");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido ou inexistente."));

        if (resetToken.getUsado()) {
            throw new RuntimeException("Este link já foi utilizado. Solicite um novo.");
        }

        if (resetToken.isExpirado()) {
            throw new RuntimeException("Este link expirou. Solicite um novo.");
        }

        // Atualiza a senha do membro
        Membro membro = resetToken.getMembro();
        membro.setSenha(passwordEncoder.encode(novaSenha));
        membroRepository.save(membro);

        // Marca o token como usado — não pode ser reutilizado
        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        log.info("Senha redefinida com sucesso para: {}", membro.getEmail());
    }
}