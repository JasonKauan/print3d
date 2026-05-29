package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "membro_configs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MembroConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Membro que tem repasse personalizado
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", nullable = false, unique = true)
    private Membro membro;

    // Se null, usa o percentual global da tabela configuracoes
    @Column(precision = 5, scale = 2)
    private BigDecimal percentualRepasse;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime atualizadoEm;
}