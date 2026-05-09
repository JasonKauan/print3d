package com.print3d.api.dto.response;

import com.print3d.api.model.Membro;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

// O que a API devolve — nunca expõe a senha
@Data @Builder
public class MembroResponse {
    private Long id;
    private String nome;
    private String email;
    private Membro.Role role;
    private Membro.Status status;
    private LocalDate dataEntrada;
    private LocalDate dataSaida;
    private LocalDateTime criadoEm;

    // Converte a entidade para o DTO de resposta
    public static MembroResponse from(Membro m) {
        return MembroResponse.builder()
                .id(m.getId())
                .nome(m.getNome())
                .email(m.getEmail())
                .role(m.getRole())
                .status(m.getStatus())
                .dataEntrada(m.getDataEntrada())
                .dataSaida(m.getDataSaida())
                .criadoEm(m.getCriadoEm())
                .build();
    }
}