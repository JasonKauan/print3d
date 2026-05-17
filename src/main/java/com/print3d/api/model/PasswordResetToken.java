package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Token UUID único enviado no email
    @Column(nullable = false, unique = true, length = 100)
    private String token;

    // Membro que solicitou a recuperação
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", nullable = false)
    private Membro membro;

    // Expira em 1 hora após a criação
    @Column(nullable = false)
    private LocalDateTime expiracao;

    // Marca se já foi usado — token de uso único
    @Column(nullable = false)
    @Builder.Default
    private Boolean usado = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(expiracao);
    }
}