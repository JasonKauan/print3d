package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produtos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome do produto no catálogo — obrigatório
    @Column(nullable = false, length = 200)
    private String nome;

    // Descrição livre do produto
    @Column(columnDefinition = "TEXT")
    private String descricao;

    // URL da foto armazenada no Cloudinary — obrigatória
    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    // Preço de venda em reais
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal preco = BigDecimal.ZERO;

    // Quantidade disponível em estoque
    @Column(nullable = false)
    @Builder.Default
    private Integer estoque = 0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}