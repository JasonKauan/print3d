package com.print3d.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsarImpressoraRequest {

    @NotBlank(message = "Nome do produto é obrigatório")
    private String produtoNome;

    @Min(value = 1, message = "Quantidade mínima é 1")
    private Integer quantidade = 1;
}