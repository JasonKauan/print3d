package com.print3d.api.service;

import com.print3d.api.dto.request.VendaRequest;
import com.print3d.api.dto.response.ResumoFinanceiroResponse;
import com.print3d.api.dto.response.VendaResponse;
import com.print3d.api.model.Membro;
import com.print3d.api.model.Venda;
import com.print3d.api.repository.MembroRepository;
import com.print3d.api.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final MembroRepository membroRepository;

    // Percentual de repasse ao produtor — 70%
    private static final BigDecimal PERCENTUAL_REPASSE = new BigDecimal("0.70");

    public List<VendaResponse> listarTodas() {
        return vendaRepository.findAll()
                .stream()
                .map(VendaResponse::from)
                .collect(Collectors.toList());
    }

    public List<VendaResponse> listarPorMembro(Long membroId) {
        return vendaRepository.findByMembroId(membroId)
                .stream()
                .map(VendaResponse::from)
                .collect(Collectors.toList());
    }

    public VendaResponse buscarPorId(Long id) {
        return VendaResponse.from(vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada: " + id)));
    }

    public VendaResponse criar(VendaRequest request) {
        Membro membro = membroRepository.findById(request.getMembroId())
                .orElseThrow(() -> new RuntimeException("Membro não encontrado: " + request.getMembroId()));

        // Calcula o repasse automaticamente: 70% do valor total
        // RoundingMode.HALF_UP = arredonda para cima no meio (padrão financeiro)
        BigDecimal repasse = request.getValorTotal()
                .multiply(PERCENTUAL_REPASSE)
                .setScale(2, RoundingMode.HALF_UP);

        Venda venda = Venda.builder()
                .membro(membro)
                .produtoNome(request.getProdutoNome())
                .quantidade(request.getQuantidade())
                .valorTotal(request.getValorTotal())
                .repasse(repasse)             // já calculado
                .dataVenda(request.getDataVenda())
                .statusRepasse(Venda.StatusRepasse.PENDENTE)  // sempre começa pendente
                .build();

        return VendaResponse.from(vendaRepository.save(venda));
    }

    // Só atualiza o status do repasse (PENDENTE ↔ PAGO)
    public VendaResponse atualizarStatus(Long id, Venda.StatusRepasse novoStatus) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada: " + id));

        venda.setStatusRepasse(novoStatus);
        return VendaResponse.from(vendaRepository.save(venda));
    }

    // Resumo financeiro de todos os membros ativos
    public List<ResumoFinanceiroResponse> resumoGeral() {
        return membroRepository.findByStatus(Membro.Status.ATIVO)
                .stream()
                .map(this::calcularResumo)
                .collect(Collectors.toList());
    }

    // Resumo financeiro de um membro específico
    public ResumoFinanceiroResponse resumoPorMembro(Long membroId) {
        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado: " + membroId));
        return calcularResumo(membro);
    }

    // Calcula o resumo financeiro de um membro usando as queries do repository
    private ResumoFinanceiroResponse calcularResumo(Membro membro) {
        BigDecimal totalVendas  = vendaRepository.somarVendasPorMembro(membro.getId());
        BigDecimal totalRepasse = totalVendas.multiply(PERCENTUAL_REPASSE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPago    = vendaRepository.somarRepassePagoPorMembro(membro.getId());
        BigDecimal totalPendente = totalRepasse.subtract(totalPago);

        return ResumoFinanceiroResponse.builder()
                .membroId(membro.getId())
                .membroNome(membro.getNome())
                .totalVendas(totalVendas)
                .totalRepasse(totalRepasse)
                .totalPago(totalPago)
                .totalPendente(totalPendente)
                .build();
    }
}