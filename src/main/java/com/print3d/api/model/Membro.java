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

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(unique = true, length = 200)
    private String email;

    @Column(length = 255)
    private String senha;

    // DEV > ADMIN > MEMBRO
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.MEMBRO;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ATIVO;

    private LocalDate dataEntrada;
    private LocalDate dataSaida;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum Role {
        DEV, ADMIN, MEMBRO
    }

    public enum Status {
        ATIVO, INATIVO
    }
}