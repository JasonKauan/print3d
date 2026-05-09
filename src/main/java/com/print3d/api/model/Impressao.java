package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
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

    // Quem fez a impressão — FK para membros
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", nullable = false)
    private Membro membro;

    // Nome do produto impresso (texto livre, não FK de produto)
    // Assim não quebra histórico se um produto for deletado do catálogo
    @Column(nullable = false, length = 200)
    private String produtoNome;

    // Quantas unidades foram impressas
    @Column(nullable = false)
    @Builder.Default
    private Integer quantidade = 1;

    // Tempo de impressão em formato livre: "4h", "1h30min"
    @Column(length = 50)
    private String tempoImpressao;

    // Data em que a impressão foi realizada
    private LocalDate dataImpressao;

    // Campo livre para anotações extras
    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}