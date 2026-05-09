package com.print3d.api.dto.response;

import com.print3d.api.model.Impressao;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class ImpressaoResponse {
    private Long id;
    private Long membroId;
    private String membroNome;
    private String produtoNome;
    private Integer quantidade;
    private String tempoImpressao;
    private LocalDate dataImpressao;
    private String observacao;
    private LocalDateTime criadoEm;

    public static ImpressaoResponse from(Impressao i) {
        return ImpressaoResponse.builder()
                .id(i.getId())
                .membroId(i.getMembro().getId())
                .membroNome(i.getMembro().getNome())
                .produtoNome(i.getProdutoNome())
                .quantidade(i.getQuantidade())
                .tempoImpressao(i.getTempoImpressao())
                .dataImpressao(i.getDataImpressao())
                .observacao(i.getObservacao())
                .criadoEm(i.getCriadoEm())
                .build();
    }
}