package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes_estoque")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tipo do item movimentado
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoItem tipoItem;

    // ID do produto ou filamento
    @Column(nullable = false)
    private Long itemId;

    // Nome do item — salvo para histórico mesmo se o item for deletado
    @Column(nullable = false, length = 200)
    private String itemNome;

    // Tipo da movimentação
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    // Quantidade movimentada — positivo para entrada, negativo para saída
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidade;

    // Estoque antes da movimentação
    @Column(precision = 10, scale = 2)
    private BigDecimal estoqueAntes;

    // Estoque após a movimentação
    @Column(precision = 10, scale = 2)
    private BigDecimal estoqueDepois;

    // Justificativa — obrigatória para ajustes manuais
    @Column(columnDefinition = "TEXT")
    private String justificativa;

    // Quem gerou a movimentação
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id")
    private Membro membro;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum TipoItem {
        PRODUTO,
        FILAMENTO
    }

    public enum TipoMovimentacao {
        ENTRADA_CADASTRO,     // produto ou filamento cadastrado
        ENTRADA_IMPRESSAO,    // peças produzidas ao finalizar impressão
        SAIDA_VENDA,          // peças vendidas
        AJUSTE_MANUAL_ENTRADA,
        AJUSTE_MANUAL_SAIDA,
        CONSUMO_FILAMENTO     // gramas descontadas ao finalizar impressão
    }
}