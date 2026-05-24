package com.print3d.api.dto.request;

import lombok.Data;

@Data
public class FinalizarImpressoraRequest {
    // Tempo real que levou — ex: "3h45min"
    private String tempoReal;
    // Observações finais
    private String observacao;
}