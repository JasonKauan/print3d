package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fila_impressao",
        uniqueConstraints = @UniqueConstraint(columnNames = {"impressora_id", "membro_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FilaImpressao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "impressora_id", nullable = false)
    private Impressora impressora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", nullable = false)
    private Membro membro;

    @Column(length = 200)
    private String produtoNome;

    private Integer quantidade;

    private Long filamentoId;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}
