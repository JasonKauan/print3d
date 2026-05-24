package com.print3d.api.dto.response;

import com.print3d.api.model.Impressora;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class ImpressoraResponse {
    private Long id;
    private String nome;
    private String modelo;
    private Impressora.Status status;
    private String observacao;

    // Quem está usando — null se livre
    private Long membroAtualId;
    private String membroAtualNome;
    private String membroAtualFoto;
    private LocalDateTime usoIniciadoEm;
    private String produtoEmImpressao;
    private Integer quantidadeEmImpressao;

    private LocalDateTime criadoEm;

    public static ImpressoraResponse from(Impressora i) {
        return ImpressoraResponse.builder()
                .id(i.getId())
                .nome(i.getNome())
                .modelo(i.getModelo())
                .status(i.getStatus())
                .observacao(i.getObservacao())
                .membroAtualId(i.getMembroAtual() != null ? i.getMembroAtual().getId() : null)
                .membroAtualNome(i.getMembroAtual() != null ? i.getMembroAtual().getNome() : null)
                .membroAtualFoto(i.getMembroAtual() != null ? i.getMembroAtual().getFotoUrl() : null)
                .usoIniciadoEm(i.getUsoIniciadoEm())
                .produtoEmImpressao(i.getProdutoEmImpressao())
                .quantidadeEmImpressao(i.getQuantidadeEmImpressao())
                .criadoEm(i.getCriadoEm())
                .build();
    }
}