package com.print3d.api.dto.response;

import com.print3d.api.model.FilaImpressao;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class FilaImpressaoResponse {

    private Long id;
    private Long impressoraId;
    private String impressoraNome;
    private Long membroId;
    private String membroNome;
    private String membroFoto;
    private String produtoNome;
    private Integer quantidade;
    private Long filamentoId;
    private Integer posicao;
    private LocalDateTime criadoEm;

    public static FilaImpressaoResponse from(FilaImpressao f, int posicao) {
        return FilaImpressaoResponse.builder()
                .id(f.getId())
                .impressoraId(f.getImpressora().getId())
                .impressoraNome(f.getImpressora().getNome())
                .membroId(f.getMembro().getId())
                .membroNome(f.getMembro().getNome())
                .membroFoto(f.getMembro().getFotoUrl())
                .produtoNome(f.getProdutoNome())
                .quantidade(f.getQuantidade())
                .filamentoId(f.getFilamentoId())
                .posicao(posicao)
                .criadoEm(f.getCriadoEm())
                .build();
    }
}
