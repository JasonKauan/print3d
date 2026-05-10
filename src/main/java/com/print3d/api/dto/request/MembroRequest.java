package com.print3d.api.dto.request;

import com.print3d.api.model.Membro;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MembroRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    // Na criação: obrigatória (validada no service)
    // No update: opcional — se vier vazia, mantém a senha atual
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;

    private Membro.Role role;

    private Membro.Status status;

    private LocalDate dataEntrada;

    private LocalDate dataSaida;
}