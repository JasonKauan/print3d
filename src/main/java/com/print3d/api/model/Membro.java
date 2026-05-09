package com.print3d.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "membros")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Membro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome completo do membro — obrigatório
    @Column(nullable = false, length = 150)
    private String nome;

    // E-mail para contato — único por membro
    @Column(unique = true, length = 200)
    private String email;

    // Senha hash para login no sistema
    @Column(length = 255)
    private String senha;

    // Perfil de acesso: ADMIN pode tudo, MEMBRO vê só o próprio
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.MEMBRO;

    // Status atual na entidade
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ATIVO;

    // Data que entrou na entidade
    private LocalDate dataEntrada;

    // Data que saiu — null enquanto ainda é membro
    private LocalDate dataSaida;

    // Preenchido automaticamente na criação
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum Role {
        ADMIN, MEMBRO
    }

    public enum Status {
        ATIVO, INATIVO
    }
}