package com.print3d.api.dto.response;

import com.print3d.api.model.Produto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class ProdutoResponse {
    private Long id;
    private String nome;
    private String descricao;
    private String fotoUrl;
    private BigDecimal preco;
    private Integer estoque;
    private LocalDateTime criadoEm;

    public static ProdutoResponse from(Produto p) {
        return ProdutoResponse.builder()
                .id(p.getId())
                .nome(p.getNome())
                .descricao(p.getDescricao())
                .fotoUrl(p.getFotoUrl())
                .preco(p.getPreco())
                .estoque(p.getEstoque())
                .criadoEm(p.getCriadoEm())
                .build();
    }
}