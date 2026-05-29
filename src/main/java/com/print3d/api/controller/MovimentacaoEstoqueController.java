package com.print3d.api.controller;

import com.print3d.api.dto.response.MovimentacaoEstoqueResponse;
import com.print3d.api.model.MovimentacaoEstoque;
import com.print3d.api.service.MovimentacaoEstoqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/estoque")
@RequiredArgsConstructor
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueService movimentacaoService;

    // Todos autenticados podem ver o histórico
    @GetMapping
    public ResponseEntity<List<MovimentacaoEstoqueResponse>> listar(
            @RequestParam(required = false) MovimentacaoEstoque.TipoItem tipo) {
        if (tipo != null) return ResponseEntity.ok(movimentacaoService.listarPorTipoItem(tipo));
        return ResponseEntity.ok(movimentacaoService.listarTodas());
    }

    // Histórico de um item específico
    @GetMapping("/{tipoItem}/{itemId}")
    public ResponseEntity<List<MovimentacaoEstoqueResponse>> listarPorItem(
            @PathVariable MovimentacaoEstoque.TipoItem tipoItem,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(movimentacaoService.listarPorItem(itemId, tipoItem));
    }

    // Ajuste manual de produto — só ADMIN/DEV
    @PostMapping("/ajuste/produto/{produtoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<MovimentacaoEstoqueResponse> ajustarProduto(
            @PathVariable Long produtoId,
            @RequestBody Map<String, String> body,
            Principal principal) {

        int novoEstoque = Integer.parseInt(body.get("novoEstoque"));
        String justificativa = body.getOrDefault("justificativa", "Ajuste manual");

        return ResponseEntity.ok(
                movimentacaoService.ajustarProduto(produtoId, novoEstoque, justificativa, principal.getName())
        );
    }

    // Ajuste manual de filamento — só ADMIN/DEV
    @PostMapping("/ajuste/filamento/{filamentoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<MovimentacaoEstoqueResponse> ajustarFilamento(
            @PathVariable Long filamentoId,
            @RequestBody Map<String, String> body,
            Principal principal) {

        BigDecimal novoDisponivel = new BigDecimal(body.get("novoDisponivel"));
        String justificativa = body.getOrDefault("justificativa", "Ajuste manual");

        return ResponseEntity.ok(
                movimentacaoService.ajustarFilamento(filamentoId, novoDisponivel, justificativa, principal.getName())
        );
    }
}