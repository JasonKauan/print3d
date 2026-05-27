package com.print3d.api.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FinalizarImpressoraRequest {
    // Tempo real que levou — ex: "3h45min"
    private String tempoReal;
    // Gramas reais usadas — desconta do estoque do filamento
    private BigDecimal gramasUsadas;
    // Observações finais
    private String observacao;
}