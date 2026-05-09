package com.print3d.api.controller;

import com.print3d.api.dto.request.VendaRequest;
import com.print3d.api.dto.response.ResumoFinanceiroResponse;
import com.print3d.api.dto.response.VendaResponse;
import com.print3d.api.model.Venda;
import com.print3d.api.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    // GET /api/v1/vendas              → lista todas
    // GET /api/v1/vendas?membro_id=1  → filtra por membro
    @GetMapping
    public ResponseEntity<List<VendaResponse>> listar(
            @RequestParam(name = "membro_id", required = false) Long membroId) {

        if (membroId != null) {
            return ResponseEntity.ok(vendaService.listarPorMembro(membroId));
        }
        return ResponseEntity.ok(vendaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.buscarPorId(id));
    }

    // GET /api/v1/vendas/resumo              → resumo de todos os membros
    // GET /api/v1/vendas/resumo?membro_id=1  → resumo de um membro específico
    @GetMapping("/resumo")
    public ResponseEntity<?> resumo(
            @RequestParam(name = "membro_id", required = false) Long membroId) {

        if (membroId != null) {
            return ResponseEntity.ok(vendaService.resumoPorMembro(membroId));
        }
        return ResponseEntity.ok(vendaService.resumoGeral());
    }

    @PostMapping
    public ResponseEntity<VendaResponse> criar(@Valid @RequestBody VendaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendaService.criar(request));
    }

    // PATCH /api/v1/vendas/{id}/status
    // Body: { "status": "PAGO" } ou { "status": "PENDENTE" }
    @PatchMapping("/{id}/status")
    public ResponseEntity<VendaResponse> atualizarStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Venda.StatusRepasse novoStatus = Venda.StatusRepasse.valueOf(body.get("status"));
        return ResponseEntity.ok(vendaService.atualizarStatus(id, novoStatus));
    }
}