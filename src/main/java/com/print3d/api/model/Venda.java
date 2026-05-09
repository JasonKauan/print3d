package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Qual membro produziu o item vendido
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", nullable = false)
    private Membro membro;

    // Nome do produto vendido (texto livre — mesma razão que em Impressao)
    @Column(nullable = false, length = 200)
    private String produtoNome;

    // Quantas unidades foram vendidas nessa venda
    @Column(nullable = false)
    @Builder.Default
    private Integer quantidade = 1;

    // Valor total da venda (preço × quantidade)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    // 70% do valorTotal — calculado automaticamente no service
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal repasse;

    // Data em que a venda aconteceu
    private LocalDate dataVenda;

    // Status do repasse ao produtor
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusRepasse statusRepasse = StatusRepasse.PENDENTE;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum StatusRepasse {
        PENDENTE, PAGO
    }
}