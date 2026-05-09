package com.print3d.api.controller;

import com.print3d.api.dto.request.MembroRequest;
import com.print3d.api.dto.response.MembroResponse;
import com.print3d.api.model.Membro;
import com.print3d.api.service.MembroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/membros")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")  // todas as rotas deste controller exigem ADMIN
public class MembroController {

    private final MembroService membroService;

    // GET /api/v1/membros          → lista todos
    // GET /api/v1/membros?status=ATIVO  → filtra por status
    @GetMapping
    public ResponseEntity<List<MembroResponse>> listar(
            @RequestParam(required = false) Membro.Status status) {

        if (status != null) {
            return ResponseEntity.ok(membroService.listarPorStatus(status));
        }
        return ResponseEntity.ok(membroService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembroResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(membroService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<MembroResponse> criar(@Valid @RequestBody MembroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membroService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MembroResponse> atualizar(@PathVariable Long id,
                                                    @Valid @RequestBody MembroRequest request) {
        return ResponseEntity.ok(membroService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        membroService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}