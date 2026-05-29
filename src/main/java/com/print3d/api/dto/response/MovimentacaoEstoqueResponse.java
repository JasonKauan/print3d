package com.print3d.api.dto.response;

import com.print3d.api.model.MovimentacaoEstoque;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class MovimentacaoEstoqueResponse {
    private Long id;
    private MovimentacaoEstoque.TipoItem tipoItem;
    private Long itemId;
    private String itemNome;
    private MovimentacaoEstoque.TipoMovimentacao tipo;
    private BigDecimal quantidade;
    private BigDecimal estoqueAntes;
    private BigDecimal estoqueDepois;
    private String justificativa;
    private String membroNome;
    private LocalDateTime criadoEm;

    public static MovimentacaoEstoqueResponse from(MovimentacaoEstoque m) {
        return MovimentacaoEstoqueResponse.builder()
                .id(m.getId())
                .tipoItem(m.getTipoItem())
                .itemId(m.getItemId())
                .itemNome(m.getItemNome())
                .tipo(m.getTipo())
                .quantidade(m.getQuantidade())
                .estoqueAntes(m.getEstoqueAntes())
                .estoqueDepois(m.getEstoqueDepois())
                .justificativa(m.getJustificativa())
                .membroNome(m.getMembro() != null ? m.getMembro().getNome() : "Sistema")
                .criadoEm(m.getCriadoEm())
                .build();

    }
}