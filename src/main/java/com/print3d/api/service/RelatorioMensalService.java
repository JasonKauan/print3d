package com.print3d.api.service;

import com.print3d.api.model.Membro;
import com.print3d.api.repository.ImpressaoRepository;
import com.print3d.api.repository.MembroRepository;
import com.print3d.api.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class RelatorioMensalService {

    private final MembroRepository membroRepository;
    private final VendaRepository vendaRepository;
    private final ImpressaoRepository impressaoRepository;
    private final EmailService emailService;

    // Executa todo dia 1º do mês às 8h
    @Scheduled(cron = "0 0 8 1 * *")
    public void enviarRelatoriosMensais() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusMonths(1).withDayOfMonth(1);
        LocalDate fim    = hoje.withDayOfMonth(1).minusDays(1);

        String nomeMesRaw = inicio.format(
                DateTimeFormatter.ofPattern("MMMM 'de' yyyy", new Locale("pt", "BR")));
        final String nomeMes = Character.toUpperCase(nomeMesRaw.charAt(0)) + nomeMesRaw.substring(1);

        log.info("Enviando relatórios mensais referentes a {}", nomeMes);

        List<Membro> ativos = membroRepository.findByStatus(Membro.Status.ATIVO);

        // Relatório individual para cada membro com email
        for (Membro membro : ativos) {
            if (membro.getEmail() == null || membro.getEmail().isBlank()) continue;
            try {
                long impressoes = impressaoRepository.contarPorMembroEPeriodo(membro.getId(), inicio, fim);
                long pecas      = impressaoRepository.somarPecasPorMembroEPeriodo(membro.getId(), inicio, fim);
                BigDecimal vendas   = vendaRepository.somarVendasMembroPeriodo(membro.getId(), inicio, fim);
                BigDecimal repasse  = vendaRepository.somarRepasseMembroPeriodo(membro.getId(), inicio, fim);

                emailService.enviarRelatorioMensal(
                        membro.getEmail(), membro.getNome(), nomeMes,
                        impressoes, pecas, vendas, repasse);
            } catch (Exception e) {
                log.error("Erro ao enviar relatório para {}: {}", membro.getEmail(), e.getMessage());
            }
        }

        // Relatório consolidado para ADMIN e DEV
        BigDecimal receitaTotal    = vendaRepository.somarVendasPorPeriodo(inicio, fim);
        long impressoesTotal       = impressaoRepository.contarPorPeriodo(inicio, fim);
        int membrosAtivos          = ativos.size();
        BigDecimal repassePendente = vendaRepository.somarTodoRepassePendente();

        ativos.stream()
                .filter(m -> m.getRole() == Membro.Role.ADMIN || m.getRole() == Membro.Role.DEV)
                .filter(m -> m.getEmail() != null && !m.getEmail().isBlank())
                .forEach(admin -> {
                    try {
                        emailService.enviarRelatorioConsolidado(
                                admin.getEmail(), admin.getNome(), nomeMes,
                                receitaTotal, impressoesTotal, membrosAtivos, repassePendente);
                    } catch (Exception e) {
                        log.error("Erro ao enviar consolidado para {}: {}", admin.getEmail(), e.getMessage());
                    }
                });

        log.info("Relatórios mensais de {} enviados com sucesso.", nomeMes);
    }
}
