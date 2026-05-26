package com.print3d.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FilamentoRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String marca;
    private String cor;
    private String tipo;

    @NotNull(message = "Peso total é obrigatório")
    @Positive(message = "Peso deve ser positivo")
    private BigDecimal pesoTotalGramas;

    @NotNull(message = "Preço pago é obrigatório")
    @Positive(message = "Preço deve ser positivo")
    private BigDecimal precoPago;

    private LocalDate dataCompra;
}