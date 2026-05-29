package com.print3d.api.controller;

import com.print3d.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
public class AdminDashboardController {

    private final VendaRepository vendaRepository;
    private final ImpressaoRepository impressaoRepository;
    private final MembroRepository membroRepository;
    private final FilamentoRepository filamentoRepository;
    private final ImpressoraRepository impressoraRepository;

    // Métricas gerais do sistema
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Map<String, Object> dados = new LinkedHashMap<>();

        // Membros
        dados.put("totalMembros",  membroRepository.count());
        dados.put("membrosAtivos", membroRepository.findByStatus(
                com.print3d.api.model.Membro.Status.ATIVO).size());

        // Impressoras
        dados.put("totalImpressoras", impressoraRepository.count());
        dados.put("impressorasLivres", impressoraRepository
                .findByStatus(com.print3d.api.model.Impressora.Status.LIVRE).size());
        dados.put("impressorasOcupadas", impressoraRepository
                .findByStatus(com.print3d.api.model.Impressora.Status.OCUPADA).size());

        // Filamentos
        dados.put("totalFilamentos", filamentoRepository.count());
        dados.put("totalInvestidoFilamento", filamentoRepository.totalInvestido());

        // Vendas
        dados.put("totalVendas", vendaRepository.count());
        dados.put("receitaTotal", vendaRepository.somarTodasVendas());
        dados.put("repassePendente", vendaRepository.somarTodoRepassePendente());

        // Impressões
        dados.put("totalImpressoes", impressaoRepository.count());

        return ResponseEntity.ok(dados);
    }

    // Vendas agrupadas por mês — últimos 6 meses
    @GetMapping("/vendas-por-mes")
    public ResponseEntity<List<Map<String, Object>>> vendasPorMes() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM/yy", new Locale("pt", "BR"));

        for (int i = 5; i >= 0; i--) {
            LocalDate mes = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDate fimMes = mes.plusMonths(1).minusDays(1);

            BigDecimal total = vendaRepository.somarVendasPorPeriodo(mes, fimMes);
            long qtd = vendaRepository.contarVendasPorPeriodo(mes, fimMes);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("mes", mes.format(fmt));
            item.put("total", total);
            item.put("quantidade", qtd);
            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }

    // Impressões agrupadas por mês — últimos 6 meses
    @GetMapping("/impressoes-por-mes")
    public ResponseEntity<List<Map<String, Object>>> impressoesPorMes() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM/yy", new Locale("pt", "BR"));

        for (int i = 5; i >= 0; i--) {
            LocalDate mes = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDate fimMes = mes.plusMonths(1).minusDays(1);

            long qtd = impressaoRepository.contarPorPeriodo(mes, fimMes);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("mes", mes.format(fmt));
            item.put("quantidade", qtd);
            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }

    // Top 5 produtos mais vendidos
    @GetMapping("/ranking-produtos")
    public ResponseEntity<List<Map<String, Object>>> rankingProdutos() {
        return ResponseEntity.ok(vendaRepository.rankingProdutos());
    }

    // Top 5 membros mais produtivos (por impressões)
    @GetMapping("/ranking-membros")
    public ResponseEntity<List<Map<String, Object>>> rankingMembros() {
        return ResponseEntity.ok(impressaoRepository.rankingMembros());
    }

    // Custo total de filamento vs receita total
    @GetMapping("/filamento-vs-receita")
    public ResponseEntity<Map<String, Object>> filamentoVsReceita() {
        BigDecimal custoFilamento = filamentoRepository.totalInvestido();
        BigDecimal receita = vendaRepository.somarTodasVendas();
        BigDecimal lucro = receita.subtract(custoFilamento);
        BigDecimal margem = receita.compareTo(BigDecimal.ZERO) > 0
                ? lucro.divide(receita, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("custoFilamento", custoFilamento);
        dados.put("receita", receita);
        dados.put("lucro", lucro);
        dados.put("margemPercent", margem.setScale(1, RoundingMode.HALF_UP));
        return ResponseEntity.ok(dados);
    }
}