package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "impressoes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Impressao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", nullable = false)
    private Membro membro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "impressora_id")
    private Impressora impressora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filamento_id")
    private Filamento filamento;

    @Column(nullable = false, length = 200)
    private String produtoNome;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantidade = 1;

    // Gramas de filamento usados
    @Column(precision = 10, scale = 2)
    private BigDecimal gramasUsadas;

    // Custo calculado: gramasUsadas × filamento.custoPorGrama
    @Column(precision = 10, scale = 2)
    private BigDecimal custoFilamento;

    @Column(length = 50)
    private String tempoImpressao;

    private LocalDate dataImpressao;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}