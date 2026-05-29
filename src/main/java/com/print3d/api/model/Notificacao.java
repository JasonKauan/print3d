package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quem recebe a notificação
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", nullable = false)
    private Membro membro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Column(nullable = false, length = 300)
    private String mensagem;

    // Se false, aparece o contador no sininho
    @Column(nullable = false)
    @Builder.Default
    private Boolean lida = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum Tipo {
        NOVA_VENDA,
        IMPRESSORA_LIBERADA,
        FILAMENTO_BAIXO,
        REPASSE_PAGO,
        REPASSE_PENDENTE
    }
}