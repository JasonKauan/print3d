package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "filamentos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Filamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ex: "PLA Branco", "PETG Preto"
    @Column(nullable = false, length = 150)
    private String nome;

    // Marca — ex: "Polymaker", "Creality"
    @Column(length = 100)
    private String marca;

    // Cor
    @Column(length = 50)
    private String cor;

    // Tipo — ex: PLA, PETG, ABS, TPU
    @Column(length = 50)
    private String tipo;

    // Peso total do rolo em gramas — ex: 1000g
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoTotalGramas;

    // Quanto ainda tem disponível em gramas
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoDisponivelGramas;

    // Preço pago pelo rolo
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoPago;

    // Calculado automaticamente: precoPago / pesoTotalGramas
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal custoPorGrama;

    // Status do rolo
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.DISPONIVEL;

    // Data de compra
    private LocalDate dataCompra;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum Status {
        DISPONIVEL,  // ainda tem filamento
        ESGOTADO,    // acabou
        RESERVADO    // guardado para um projeto específico
    }
}