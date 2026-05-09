package com.print3d.api.dto.response;

import com.print3d.api.model.Venda;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class VendaResponse {
    private Long id;
    private Long membroId;
    private String membroNome;
    private String produtoNome;
    private Integer quantidade;
    private BigDecimal valorTotal;
    private BigDecimal repasse;
    private LocalDate dataVenda;
    private Venda.StatusRepasse statusRepasse;
    private LocalDateTime criadoEm;

    public static VendaResponse from(Venda v) {
        return VendaResponse.builder()
                .id(v.getId())
                .membroId(v.getMembro().getId())
                .membroNome(v.getMembro().getNome())
                .produtoNome(v.getProdutoNome())
                .quantidade(v.getQuantidade())
                .valorTotal(v.getValorTotal())
                .repasse(v.getRepasse())
                .dataVenda(v.getDataVenda())
                .statusRepasse(v.getStatusRepasse())
                .criadoEm(v.getCriadoEm())
                .build();
    }
}