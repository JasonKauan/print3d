package com.print3d.api.controller;

import com.print3d.api.dto.response.ProdutoResponse;
import com.print3d.api.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    // Público — qualquer um pode ver o catálogo (configurado no SecurityConfig)
    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listar() {
        return ResponseEntity.ok(produtoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    // multipart/form-data porque vem texto + arquivo (foto) juntos
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdutoResponse> criar(
            @RequestParam String nome,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) BigDecimal preco,
            @RequestParam(required = false) Integer estoque,
            @RequestParam(required = false) MultipartFile foto) throws IOException {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(produtoService.criar(nome, descricao, preco, estoque, foto));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdutoResponse> atualizar(
            @PathVariable Long id,
            @RequestParam String nome,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) BigDecimal preco,
            @RequestParam(required = false) Integer estoque,
            @RequestParam(required = false) MultipartFile foto) throws IOException {

        return ResponseEntity.ok(produtoService.atualizar(id, nome, descricao, preco, estoque, foto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}