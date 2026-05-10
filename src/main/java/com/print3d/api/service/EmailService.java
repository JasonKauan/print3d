package com.print3d.api.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${resend.from}")
    private String from;

    // Boas-vindas ao novo membro
    @Async
    public void enviarBoasVindas(String destinatario, String nome) {
        String assunto = "Bem-vindo ao Print3D! 🎉";
        String corpo = """
            <div style="font-family:sans-serif;max-width:520px;margin:0 auto">
              <div style="background:#0f1117;padding:24px;border-radius:12px 12px 0 0">
                <h2 style="color:#4f7cff;margin:0">◈ Print3D</h2>
              </div>
              <div style="background:#f9f9f9;padding:24px;border-radius:0 0 12px 12px">
                <h3 style="color:#1e2333">Olá, %s! 👋</h3>
                <p style="color:#555">Sua conta foi criada com sucesso no sistema Print3D.</p>
                <p style="color:#555">Acesse o sistema com seu email e a senha definida pelo administrador.</p>
                <div style="background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:16px;margin:16px 0">
                  <p style="margin:0;color:#888;font-size:13px">Qualquer dúvida, fale com o administrador da entidade.</p>
                </div>
                <p style="color:#aaa;font-size:12px">Print3D — Sistema de Gestão de Impressão 3D</p>
              </div>
            </div>
            """.formatted(nome);
        enviar(destinatario, assunto, corpo);
    }

    // Notificação de nova venda para o produtor
    @Async
    public void enviarNotificacaoVenda(String destinatario, String nome,
                                       String produto, Integer qtd, BigDecimal repasse) {
        String assunto = "Nova venda registrada — " + produto;
        String corpo = """
            <div style="font-family:sans-serif;max-width:520px;margin:0 auto">
              <div style="background:#0f1117;padding:24px;border-radius:12px 12px 0 0">
                <h2 style="color:#4f7cff;margin:0">◈ Print3D</h2>
              </div>
              <div style="background:#f9f9f9;padding:24px;border-radius:0 0 12px 12px">
                <h3 style="color:#1e2333">Nova venda registrada! 🛍️</h3>
                <p style="color:#555">Olá, <strong>%s</strong>! Uma venda do seu produto foi registrada.</p>
                <div style="background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:16px;margin:16px 0">
                  <p style="margin:0 0 8px;color:#333"><strong>Produto:</strong> %s</p>
                  <p style="margin:0 0 8px;color:#333"><strong>Quantidade:</strong> %d unid.</p>
                  <p style="margin:0;font-size:18px;color:#2ecc8a"><strong>Seu repasse (70%%): R$ %.2f</strong></p>
                </div>
                <p style="color:#888;font-size:13px">O repasse será realizado em breve. Acompanhe pelo sistema.</p>
                <p style="color:#aaa;font-size:12px">Print3D — Sistema de Gestão de Impressão 3D</p>
              </div>
            </div>
            """.formatted(nome, produto, qtd, repasse);
        enviar(destinatario, assunto, corpo);
    }

    // Confirmação de repasse pago
    @Async
    public void enviarConfirmacaoRepasse(String destinatario, String nome,
                                         String produto, BigDecimal valor) {
        String assunto = "Repasse realizado — " + produto + " ✅";
        String corpo = """
            <div style="font-family:sans-serif;max-width:520px;margin:0 auto">
              <div style="background:#0f1117;padding:24px;border-radius:12px 12px 0 0">
                <h2 style="color:#4f7cff;margin:0">◈ Print3D</h2>
              </div>
              <div style="background:#f9f9f9;padding:24px;border-radius:0 0 12px 12px">
                <h3 style="color:#1e2333">Repasse confirmado! ✅</h3>
                <p style="color:#555">Olá, <strong>%s</strong>! Seu repasse foi marcado como pago.</p>
                <div style="background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:16px;margin:16px 0">
                  <p style="margin:0 0 8px;color:#333"><strong>Produto:</strong> %s</p>
                  <p style="margin:0;font-size:20px;color:#2ecc8a"><strong>R$ %.2f</strong></p>
                </div>
                <p style="color:#888;font-size:13px">Acesse o sistema para ver seu extrato completo.</p>
                <p style="color:#aaa;font-size:12px">Print3D — Sistema de Gestão de Impressão 3D</p>
              </div>
            </div>
            """.formatted(nome, produto, valor);
        enviar(destinatario, assunto, corpo);
    }

    // Método base — envia o email de fato
    private void enviar(String destinatario, String assunto, String corpo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpo, true); // true = HTML
            mailSender.send(message);
            log.info("Email enviado para {} — {}", destinatario, assunto);
        } catch (Exception e) {
            // Não deixa falha de email quebrar a operação principal
            log.error("Erro ao enviar email para {}: {}", destinatario, e.getMessage());
        }
    }
}