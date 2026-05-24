package com.print3d.api.dto.request;

import com.print3d.api.model.Impressora;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImpressoraRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String modelo;
    private String observacao;
    private Impressora.Status status;
}