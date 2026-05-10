package com.print3d.api.controller;

import com.print3d.api.dto.request.VendaRequest;
import com.print3d.api.dto.response.VendaResponse;
import com.print3d.api.model.Membro;
import com.print3d.api.model.Venda;
import com.print3d.api.repository.MembroRepository;
import com.print3d.api.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;
    private final MembroRepository membroRepository;

    // ADMIN vê tudo ou filtra por membro_id
    // MEMBRO só vê as próprias vendas
    @GetMapping
    public ResponseEntity<List<VendaResponse>> listar(
            @RequestParam(name = "membro_id", required = false) Long membroId,
            Principal principal) {

        Membro requisitante = membroRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        if (requisitante.getRole() == Membro.Role.ADMIN) {
            if (membroId != null) return ResponseEntity.ok(vendaService.listarPorMembro(membroId));
            return ResponseEntity.ok(vendaService.listarTodas());
        }

        return ResponseEntity.ok(vendaService.listarPorMembro(requisitante.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.buscarPorId(id));
    }

    // ADMIN vê resumo geral ou de um membro específico
    // MEMBRO vê só o próprio resumo
    @GetMapping("/resumo")
    public ResponseEntity<?> resumo(
            @RequestParam(name = "membro_id", required = false) Long membroId,
            Principal principal) {

        Membro requisitante = membroRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        if (requisitante.getRole() == Membro.Role.ADMIN) {
            if (membroId != null) return ResponseEntity.ok(vendaService.resumoPorMembro(membroId));
            return ResponseEntity.ok(vendaService.resumoGeral());
        }

        return ResponseEntity.ok(vendaService.resumoPorMembro(requisitante.getId()));
    }

    // Só ADMIN registra venda
    @PostMapping
    public ResponseEntity<VendaResponse> criar(@Valid @RequestBody VendaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendaService.criar(request));
    }

    // Só ADMIN altera status do repasse
    @PatchMapping("/{id}/status")
    public ResponseEntity<VendaResponse> atualizarStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Venda.StatusRepasse novoStatus = Venda.StatusRepasse.valueOf(body.get("status"));
        return ResponseEntity.ok(vendaService.atualizarStatus(id, novoStatus));
    }
}