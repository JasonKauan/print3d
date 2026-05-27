package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
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

    // Quem está usando atualmente — null quando LIVRE ou MANUTENCAO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_atual_id")
    private Membro membroAtual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filamento_atual_id")
    private Long FilamentoAtualID;

    // Quando o uso atual começou
    private LocalDateTime usoIniciadoEm;

    // Produto sendo impresso no momento
    @Column(length = 200)
    private String produtoEmImpressao;

    // Quantidade sendo impressa no momento
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