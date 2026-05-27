package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "impressoras")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Impressora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 150)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.LIVRE;

    // Quem está usando atualmente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_atual_id")
    private Membro membroAtual;

    // Qual filamento está sendo usado — guardamos só o ID pra não criar dependência circular
    @Column(name = "filamento_atual_id")
    private Long filamentoAtualId;

    private LocalDateTime usoIniciadoEm;

    @Column(length = 200)
    private String produtoEmImpressao;

    private Integer quantidadeEmImpressao;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum Status {
        LIVRE,
        OCUPADA,
        MANUTENCAO
    }
}