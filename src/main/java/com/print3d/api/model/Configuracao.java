package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracoes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Configuracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chave única da configuração — ex: "PERCENTUAL_REPASSE"
    @Column(nullable = false, unique = true, length = 100)
    private String chave;

    // Valor armazenado como string — convertido conforme necessário
    @Column(nullable = false, length = 255)
    private String valor;

    // Descrição legível para o admin
    @Column(length = 255)
    private String descricao;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime atualizadoEm;

    // Chaves conhecidas do sistema
    public static final String PERCENTUAL_REPASSE       = "PERCENTUAL_REPASSE";       // default: 70
    public static final String MULTIPLICADOR_EXTERNO    = "MULTIPLICADOR_EXTERNO";    // default: 2.5
    public static final String MULTIPLICADOR_INTERNO    = "MULTIPLICADOR_INTERNO";    // default: 1.5
    public static final String ALERTA_FILAMENTO_GRAMAS  = "ALERTA_FILAMENTO_GRAMAS";  // default: 100
    public static final String NOME_ENTIDADE            = "NOME_ENTIDADE";            // default: Print3D

    // Chaves que contêm valores não-numéricos
    public static final java.util.Set<String> CHAVES_TEXTO = java.util.Set.of(NOME_ENTIDADE);
}