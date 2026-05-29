package com.print3d.api.controller;

import com.print3d.api.service.ConfiguracaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/configuracoes")
@RequiredArgsConstructor
public class ConfiguracaoController {

    private final ConfiguracaoService configuracaoService;

    // Todos autenticados podem ver as configs (para usar nos cálculos)
    @GetMapping
    public ResponseEntity<Map<String, String>> listar() {
        return ResponseEntity.ok(configuracaoService.listarTodas());
    }

    // Só ADMIN/DEV pode alterar configs globais
    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<?> atualizar(@RequestBody Map<String, String> body) {
        body.forEach((chave, valor) -> {
            try {
                new BigDecimal(valor); // valida que é número
                configuracaoService.atualizar(chave, valor);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Valor inválido para " + chave + ": " + valor);
            }
        });
        return ResponseEntity.ok(Map.of("mensagem", "Configurações atualizadas!"));
    }

    // Override de repasse por membro — só ADMIN/DEV
    @PatchMapping("/membros/{membroId}/repasse")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<?> atualizarRepasseMembro(@PathVariable Long membroId,
                                                    @RequestBody Map<String, String> body) {
        String valor = body.get("percentual");
        if (valor == null) return ResponseEntity.badRequest().body(Map.of("erro", "Campo 'percentual' obrigatório."));

        BigDecimal percentual = new BigDecimal(valor);
        if (percentual.compareTo(BigDecimal.ZERO) < 0 || percentual.compareTo(new BigDecimal("100")) > 0) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Percentual deve estar entre 0 e 100."));
        }

        configuracaoService.atualizarRepasseMembro(membroId, percentual);
        return ResponseEntity.ok(Map.of("mensagem", "Repasse do membro atualizado!"));
    }

    // Remove override — volta ao global
    @DeleteMapping("/membros/{membroId}/repasse")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<?> removerRepasseMembro(@PathVariable Long membroId) {
        configuracaoService.removerRepasseMembro(membroId);
        return ResponseEntity.ok(Map.of("mensagem", "Repasse personalizado removido. Voltando ao padrão global."));
    }
}