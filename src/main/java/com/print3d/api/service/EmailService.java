package com.print3d.api.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    // Email de recuperação de senha — enviado com link de reset
    @Async
    public void enviarRecuperacaoSenha(String destinatario, String nome, String linkReset) {
        String assunto = "Recuperação de senha — Print3D";
        String corpo = """
            <div style="font-family:sans-serif;max-width:520px;margin:0 auto">
              <div style="background:#0f1117;padding:24px;border-radius:12px 12px 0 0">
                <h2 style="color:#4f7cff;margin:0">◈ Print3D</h2>
              </div>
              <div style="background:#f9f9f9;padding:24px;border-radius:0 0 12px 12px">
                <h3 style="color:#1e2333">Recuperação de senha</h3>
                <p style="color:#555">Olá, <strong>%s</strong>!</p>
                <p style="color:#555">Recebemos uma solicitação para redefinir sua senha. Clique no botão abaixo para criar uma nova senha:</p>
                <div style="text-align:center;margin:24px 0">
                  <a href="%s"
                     style="background:#4f7cff;color:#fff;padding:12px 28px;border-radius:8px;
                            text-decoration:none;font-weight:bold;font-size:15px;display:inline-block">
                    Redefinir minha senha
                  </a>
                </div>
                <div style="background:#fff3cd;border:1px solid #ffc107;border-radius:8px;padding:12px;margin:16px 0">
                  <p style="margin:0;color:#856404;font-size:13px">
                    ⚠️ Este link expira em <strong>1 hora</strong>.
                    Se você não solicitou a recuperação, ignore este email.
                  </p>
                </div>
                <p style="color:#aaa;font-size:12px;margin-top:20px">
                  Por segurança, nunca compartilhe este link com ninguém.<br>
                  Print3D — Sistema de Gestão de Impressão 3D
                </p>
              </div>
            </div>
            """.formatted(nome, linkReset);
        enviar(destinatario, assunto, corpo);
    }

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
                                       String produto, Integer qtd, java.math.BigDecimal repasse) {
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
                                         String produto, java.math.BigDecimal valor) {
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

    // Relatório mensal individual do membro
    @Async
    public void enviarRelatorioMensal(String destinatario, String nome, String nomeMes,
                                      long impressoes, long pecas,
                                      java.math.BigDecimal vendas, java.math.BigDecimal repasse) {
        String assunto = "Seu resumo de " + nomeMes + " — Print3D";
        String corpo = """
            <div style="font-family:sans-serif;max-width:560px;margin:0 auto">
              <div style="background:#0f1117;padding:24px;border-radius:12px 12px 0 0">
                <h2 style="color:#4f7cff;margin:0">◈ Print3D</h2>
                <p style="color:#888;margin:4px 0 0;font-size:13px">Relatório mensal — %s</p>
              </div>
              <div style="background:#f9f9f9;padding:24px;border-radius:0 0 12px 12px">
                <h3 style="color:#1e2333">Olá, %s! 👋</h3>
                <p style="color:#555">Aqui está um resumo da sua atividade em <strong>%s</strong>:</p>

                <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin:20px 0">
                  <div style="background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:16px;text-align:center">
                    <p style="margin:0;color:#888;font-size:12px;text-transform:uppercase;letter-spacing:.05em">Impressões</p>
                    <p style="margin:6px 0 0;font-size:28px;font-weight:700;color:#4f7cff">%d</p>
                  </div>
                  <div style="background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:16px;text-align:center">
                    <p style="margin:0;color:#888;font-size:12px;text-transform:uppercase;letter-spacing:.05em">Peças produzidas</p>
                    <p style="margin:6px 0 0;font-size:28px;font-weight:700;color:#4f7cff">%d</p>
                  </div>
                  <div style="background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:16px;text-align:center">
                    <p style="margin:0;color:#888;font-size:12px;text-transform:uppercase;letter-spacing:.05em">Vendas</p>
                    <p style="margin:6px 0 0;font-size:22px;font-weight:700;color:#1e2333">R$ %.2f</p>
                  </div>
                  <div style="background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:16px;text-align:center">
                    <p style="margin:0;color:#888;font-size:12px;text-transform:uppercase;letter-spacing:.05em">Repasse gerado</p>
                    <p style="margin:6px 0 0;font-size:22px;font-weight:700;color:#2ecc8a">R$ %.2f</p>
                  </div>
                </div>

                <p style="color:#888;font-size:13px">Acesse o sistema para ver seu extrato detalhado.</p>
                <p style="color:#aaa;font-size:12px;margin-top:20px">Print3D — Relatório gerado automaticamente no 1º dia do mês.</p>
              </div>
            </div>
            """.formatted(nomeMes, nome, nomeMes, impressoes, pecas, vendas, repasse);
        enviar(destinatario, assunto, corpo);
    }

    // Relatório mensal consolidado para ADMINs
    @Async
    public void enviarRelatorioConsolidado(String destinatario, String nome, String nomeMes,
                                           java.math.BigDecimal receitaTotal, long impressoesTotal,
                                           int membrosAtivos, java.math.BigDecimal repassePendente) {
        String assunto = "Relatório consolidado de " + nomeMes + " — Print3D";
        String corpo = """
            <div style="font-family:sans-serif;max-width:560px;margin:0 auto">
              <div style="background:#0f1117;padding:24px;border-radius:12px 12px 0 0">
                <h2 style="color:#4f7cff;margin:0">◈ Print3D</h2>
                <p style="color:#888;margin:4px 0 0;font-size:13px">Consolidado administrativo — %s</p>
              </div>
              <div style="background:#f9f9f9;padding:24px;border-radius:0 0 12px 12px">
                <h3 style="color:#1e2333">Resumo da entidade — %s</h3>
                <p style="color:#555">Olá, <strong>%s</strong>! Confira os números do mês.</p>

                <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin:20px 0">
                  <div style="background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:16px;text-align:center">
                    <p style="margin:0;color:#888;font-size:12px;text-transform:uppercase;letter-spacing:.05em">Receita total</p>
                    <p style="margin:6px 0 0;font-size:22px;font-weight:700;color:#2ecc8a">R$ %.2f</p>
                  </div>
                  <div style="background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:16px;text-align:center">
                    <p style="margin:0;color:#888;font-size:12px;text-transform:uppercase;letter-spacing:.05em">Total impressões</p>
                    <p style="margin:6px 0 0;font-size:28px;font-weight:700;color:#4f7cff">%d</p>
                  </div>
                  <div style="background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:16px;text-align:center">
                    <p style="margin:0;color:#888;font-size:12px;text-transform:uppercase;letter-spacing:.05em">Membros ativos</p>
                    <p style="margin:6px 0 0;font-size:28px;font-weight:700;color:#1e2333">%d</p>
                  </div>
                  <div style="background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:16px;text-align:center">
                    <p style="margin:0;color:#888;font-size:12px;text-transform:uppercase;letter-spacing:.05em">Repasse pendente</p>
                    <p style="margin:6px 0 0;font-size:22px;font-weight:700;color:#f59e0b">R$ %.2f</p>
                  </div>
                </div>

                <p style="color:#888;font-size:13px">Acesse o Painel ADM para detalhes completos e rankings.</p>
                <p style="color:#aaa;font-size:12px;margin-top:20px">Print3D — Relatório gerado automaticamente no 1º dia do mês.</p>
              </div>
            </div>
            """.formatted(nomeMes, nomeMes, nome, receitaTotal, impressoesTotal, membrosAtivos, repassePendente);
        enviar(destinatario, assunto, corpo);
    }

    // Método base — envia o email de fato
    private void enviar(String destinatario, String assunto, String corpo) {
        try {
            log.info("Enviando email para: {}", destinatario);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("Print3D <" + from + ">");
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpo, true);
            mailSender.send(message);
            log.info("Email enviado com sucesso para: {}", destinatario);
        } catch (Exception e) {
            log.error("Erro ao enviar email para {}: {}", destinatario, e.getMessage());
        }
    }
}