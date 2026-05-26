package com.print3d.api.controller;

import com.print3d.api.dto.request.FinalizarImpressoraRequest;
import com.print3d.api.dto.request.ImpressoraRequest;
import com.print3d.api.dto.request.UsarImpressoraRequest;
import com.print3d.api.dto.response.ImpressoraResponse;
import com.print3d.api.model.Impressora;
import com.print3d.api.service.ImpressoraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/impressoras")
@RequiredArgsConstructor
public class ImpressoraController {

    private final ImpressoraService impressoraService;

    // Todos autenticados podem ver as impressoras
    @GetMapping
    public ResponseEntity<List<ImpressoraResponse>> listar(
            @RequestParam(required = false) Boolean livres) {
        if (Boolean.TRUE.equals(livres)) {
            return ResponseEntity.ok(impressoraService.listarLivres());
        }
        return ResponseEntity.ok(impressoraService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImpressoraResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(impressoraService.buscarPorId(id));
    }

    // Só admin/dev cria, edita e deleta impressoras
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<ImpressoraResponse> criar(@Valid @RequestBody ImpressoraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(impressoraService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<ImpressoraResponse> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody ImpressoraRequest request) {
        return ResponseEntity.ok(impressoraService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        impressoraService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // Qualquer membro autenticado pode iniciar o uso de uma impressora livre
    @PostMapping("/{id}/usar")
    public ResponseEntity<ImpressoraResponse> iniciarUso(@PathVariable Long id,
                                                         @Valid @RequestBody UsarImpressoraRequest request,
                                                         Principal principal) {
        return ResponseEntity.ok(impressoraService.iniciarUso(id, principal.getName(), request));
    }

    // Membro finaliza o próprio uso, admin pode finalizar qualquer um
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<ImpressoraResponse> finalizarUso(@PathVariable Long id,
                                                           @RequestBody FinalizarImpressoraRequest request,
                                                           Principal principal) {
        return ResponseEntity.ok(impressoraService.finalizarUso(id, principal.getName(), request));
    }

    // Só admin/dev altera status manualmente (manutenção, liberar forçado)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<ImpressoraResponse> alterarStatus(@PathVariable Long id,
                                                            @RequestBody Map<String, String> body) {
        Impressora.Status novoStatus = Impressora.Status.valueOf(body.get("status"));
        return ResponseEntity.ok(impressoraService.alterarStatus(id, novoStatus));
    }
}