package com.print3d.api.controller;

import com.print3d.api.dto.response.NotificacaoResponse;
import com.print3d.api.repository.MembroRepository;
import com.print3d.api.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final MembroRepository membroRepository;

    // Lista todas as notificações do membro logado
    @GetMapping
    public ResponseEntity<List<NotificacaoResponse>> listar(Principal principal) {
        Long membroId = membroRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"))
                .getId();
        return ResponseEntity.ok(notificacaoService.listarPorMembro(membroId));
    }

    // Conta as não lidas — para o badge do sininho
    @GetMapping("/nao-lidas")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(Principal principal) {
        Long membroId = membroRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"))
                .getId();
        return ResponseEntity.ok(Map.of("total", notificacaoService.contarNaoLidas(membroId)));
    }

    // Marca uma notificação como lida
    @PatchMapping("/{id}/lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id, Principal principal) {
        Long membroId = membroRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"))
                .getId();
        notificacaoService.marcarComoLida(id, membroId);
        return ResponseEntity.ok().build();
    }

    // Marca todas como lidas
    @PatchMapping("/todas-lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(Principal principal) {
        Long membroId = membroRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"))
                .getId();
        notificacaoService.marcarTodasComoLidas(membroId);
        return ResponseEntity.ok().build();
    }
}