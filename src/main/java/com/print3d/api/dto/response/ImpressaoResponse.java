package com.print3d.api.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ImpressaoResponse {

    @NotNull(message = "Membro é obrigatório")
    private Long membroId;

    @NotBlank(message = "Nome do produto é obrigatório")
    private String produtoNome;

    @Min(value = 1, message = "Quantidade mínima é 1")
    private Integer quantidade = 1;

    private String tempoImpressao;

    private LocalDate dataImpressao;

    private String observacao;
}