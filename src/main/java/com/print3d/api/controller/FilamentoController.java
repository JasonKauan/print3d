package com.print3d.api.controller;

import com.print3d.api.dto.request.FilamentoRequest;
import com.print3d.api.dto.response.FilamentoResponse;
import com.print3d.api.service.FilamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/filamentos")
@RequiredArgsConstructor
public class FilamentoController {

    private final FilamentoService filamentoService;

    // Todos autenticados podem ver filamentos disponíveis
    @GetMapping("/filamentos")
    public ResponseEntity<List<FilamentoResponse>> listar(
            @RequestParam(required = false) Boolean disponiveis) {
        if (Boolean.TRUE.equals(disponiveis)) {
            return ResponseEntity.ok(filamentoService.listarDisponiveis());
        }
        return ResponseEntity.ok(filamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilamentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(filamentoService.buscarPorId(id));
    }

    // Total investido em filamentos — só admin/dev
    @GetMapping("/total-investido")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<?> totalInvestido() {
        return ResponseEntity.ok(Map.of("total", filamentoService.totalInvestido()));
    }

    // Só admin/dev gerencia filamentos
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<FilamentoResponse> criar(@Valid @RequestBody FilamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(filamentoService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<FilamentoResponse> atualizar(@PathVariable Long id,
                                                       @Valid @RequestBody FilamentoRequest request) {
        return ResponseEntity.ok(filamentoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        filamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}