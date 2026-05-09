package com.print3d.api.controller;

import com.print3d.api.dto.request.ImpressaoRequest;
import com.print3d.api.dto.response.ImpressaoResponse;
import com.print3d.api.service.ImpressaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/impressoes")
@RequiredArgsConstructor
public class ImpressaoController {

    private final ImpressaoService impressaoService;

    // GET /api/v1/impressoes              → lista todas
    // GET /api/v1/impressoes?membro_id=1  → filtra por membro
    @GetMapping
    public ResponseEntity<List<ImpressaoResponse>> listar(
            @RequestParam(name = "membro_id", required = false) Long membroId) {

        if (membroId != null) {
            return ResponseEntity.ok(impressaoService.listarPorMembro(membroId));
        }
        return ResponseEntity.ok(impressaoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImpressaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(impressaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ImpressaoResponse> criar(@Valid @RequestBody ImpressaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(impressaoService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImpressaoResponse> atualizar(@PathVariable Long id,
                                                       @Valid @RequestBody ImpressaoRequest request) {
        return ResponseEntity.ok(impressaoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        impressaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}