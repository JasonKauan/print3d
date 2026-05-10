package com.print3d.api.controller;

import com.print3d.api.dto.request.MembroRequest;
import com.print3d.api.dto.response.MembroResponse;
import com.print3d.api.model.Membro;
import com.print3d.api.service.MembroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/membros")
@RequiredArgsConstructor
public class MembroController {

    private final MembroService membroService;

    @GetMapping

    public ResponseEntity<List<MembroResponse>> listar(
            @RequestParam(required = false) Membro.Status status) {
        if (status != null) return ResponseEntity.ok(membroService.listarPorStatus(status));
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
                                                    @Valid @RequestBody MembroRequest request,
                                                    Principal principal) {
        return ResponseEntity.ok(membroService.atualizar(id, request, principal.getName()));
    }

    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deletar(@PathVariable Long id, Principal principal) {
        membroService.deletar(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/v1/membros/minha-senha — qualquer membro autenticado troca a própria senha
    @PatchMapping("/minha-senha")
    public ResponseEntity<?> trocarSenha(@RequestBody Map<String, String> body,
                                         Principal principal) {
        membroService.trocarSenha(
                principal.getName(),
                body.get("senhaAtual"),
                body.get("novaSenha")
        );
        return ResponseEntity.ok(Map.of("mensagem", "Senha atualizada com sucesso!"));
    }

    // PATCH /api/v1/membros/minha-foto — qualquer membro autenticado atualiza a própria foto
    @PatchMapping(value = "/minha-foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MembroResponse> atualizarFoto(@RequestParam MultipartFile foto,
                                                        Principal principal) throws IOException {
        return ResponseEntity.ok(membroService.atualizarFoto(principal.getName(), foto));
    }

    // GET /api/v1/membros/me — retorna os dados do próprio membro autenticado
    @GetMapping("/me")
    public ResponseEntity<MembroResponse> me(Principal principal) {
        return ResponseEntity.ok(membroService.buscarPorEmail(principal.getName()));
    }
}