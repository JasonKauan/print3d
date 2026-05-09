package com.print3d.api.dto.request;

import com.print3d.api.model.Membro;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MembroRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Email(message = "Email inválido")
    private String email;

    private String senha;  // opcional no update

    private Membro.Role role;

    private Membro.Status status;

    private LocalDate dataEntrada;

    private LocalDate dataSaida;
}