package com.print3d.api.dto.response;

import com.print3d.api.model.Notificacao;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class NotificacaoResponse {
    private Long id;
    private Notificacao.Tipo tipo;
    private String mensagem;
    private Boolean lida;
    private LocalDateTime criadoEm;

    public static NotificacaoResponse from(Notificacao n) {
        return NotificacaoResponse.builder()
                .id(n.getId())
                .tipo(n.getTipo())
                .mensagem(n.getMensagem())
                .lida(n.getLida())
                .criadoEm(n.getCriadoEm())
                .build();
    }
}