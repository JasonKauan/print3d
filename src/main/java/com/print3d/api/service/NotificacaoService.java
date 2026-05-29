package com.print3d.api.service;

import com.print3d.api.dto.response.NotificacaoResponse;
import com.print3d.api.model.Membro;
import com.print3d.api.model.Notificacao;
import com.print3d.api.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    // Cria uma notificação para um membro — assíncrono para não travar a resposta
    @Async
    public void criar(Membro membro, Notificacao.Tipo tipo, String mensagem) {
        Notificacao notificacao = Notificacao.builder()
                .membro(membro)
                .tipo(tipo)
                .mensagem(mensagem)
                .build();
        notificacaoRepository.save(notificacao);
    }

    // Lista todas as notificações do membro
    public List<NotificacaoResponse> listarPorMembro(Long membroId) {
        return notificacaoRepository
                .findByMembroIdOrderByCriadoEmDesc(membroId)
                .stream()
                .map(NotificacaoResponse::from)
                .collect(Collectors.toList());
    }

    // Conta as não lidas — para o badge
    public long contarNaoLidas(Long membroId) {
        return notificacaoRepository.countByMembroIdAndLidaFalse(membroId);
    }

    // Marca uma notificação como lida
    public void marcarComoLida(Long notificacaoId, Long membroId) {
        Notificacao n = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));

        // Garante que o membro só pode marcar as próprias notificações
        if (!n.getMembro().getId().equals(membroId)) {
            throw new RuntimeException("Acesso negado.");
        }

        n.setLida(true);
        notificacaoRepository.save(n);
    }

    // Marca todas as notificações do membro como lidas
    public void marcarTodasComoLidas(Long membroId) {
        notificacaoRepository.marcarTodasComoLidas(membroId);
    }

    // ── Métodos de conveniência para disparar notificações ──

    public void novaVenda(Membro membro, String produto, String valor) {
        criar(membro, Notificacao.Tipo.NOVA_VENDA,
                String.format("Nova venda de \"%s\" registrada — repasse: %s", produto, valor));
    }

    public void impressoraLiberada(Membro membro, String impressora) {
        criar(membro, Notificacao.Tipo.IMPRESSORA_LIBERADA,
                String.format("A impressora \"%s\" foi liberada e está disponível!", impressora));
    }

    public void filamentoBaixo(Membro membro, String filamento, String gramas) {
        criar(membro, Notificacao.Tipo.FILAMENTO_BAIXO,
                String.format("Filamento \"%s\" com estoque baixo: %s gramas restantes.", filamento, gramas));
    }

    public void repassePago(Membro membro, String produto, String valor) {
        criar(membro, Notificacao.Tipo.REPASSE_PAGO,
                String.format("Seu repasse de %s pela venda de \"%s\" foi pago!", valor, produto));
    }
}