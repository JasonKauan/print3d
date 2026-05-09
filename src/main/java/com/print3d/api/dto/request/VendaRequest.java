package com.print3d.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VendaRequest {

    @NotNull(message = "Membro é obrigatório")
    private Long membroId;

    @NotBlank(message = "Nome do produto é obrigatório")
    private String produtoNome;

    @Min(value = 1, message = "Quantidade mínima é 1")
    private Integer quantidade = 1;

    @NotNull(message = "Valor total é obrigatório")
    private BigDecimal valorTotal;

    private LocalDate dataVenda;
}