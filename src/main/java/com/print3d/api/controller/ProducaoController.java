package com.print3d.api.controller;

import com.print3d.api.service.ProducaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/producao")
@RequiredArgsConstructor
public class ProducaoController {

    private final ProducaoService producaoService;

    // Todos autenticados podem calcular o custo de produção
    @GetMapping("/calcular")
    public ResponseEntity<Map<String, Object>> calcular(
            @RequestParam Long filamentoId,
            @RequestParam BigDecimal gramas) {

        if (gramas.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Gramas deve ser maior que zero."));
        }

        return ResponseEntity.ok(producaoService.calcularCusto(filamentoId, gramas));
    }
}