package com.print3d.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

// Resumo financeiro de um membro específico — usado na tela de financeiro
@Data @Builder
public class ResumoFinanceiroResponse {
    private Long membroId;
    private String membroNome;
    private BigDecimal totalVendas;      // soma de todos os valorTotal
    private BigDecimal totalRepasse;     // soma de todos os repasses (70%)
    private BigDecimal totalPago;        // soma dos repasses com status=PAGO
    private BigDecimal totalPendente;    // totalRepasse - totalPago
}