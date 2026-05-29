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
    private final EmailService emailService;
    private final ConfiguracaoService configuracaoService;
    private final NotificacaoService notificacaoService;

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

        // Busca o percentual de repasse do membro — usa override se existir, senão usa o global
        // Ex: global = 70%, membro específico pode ter 65% ou 75%
        BigDecimal percentual = configuracaoService
                .getPercentualRepasseMembro(membro.getId())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        BigDecimal repasse = request.getValorTotal()
                .multiply(percentual)
                .setScale(2, RoundingMode.HALF_UP);

        Venda venda = Venda.builder()
                .membro(membro)
                .produtoNome(request.getProdutoNome())
                .quantidade(request.getQuantidade())
                .valorTotal(request.getValorTotal())
                .repasse(repasse)
                .dataVenda(request.getDataVenda())
                .statusRepasse(Venda.StatusRepasse.PENDENTE)
                .build();

        VendaResponse response = VendaResponse.from(vendaRepository.save(venda));

        if (membro.getEmail() != null) {
            emailService.enviarNotificacaoVenda(
                    membro.getEmail(), membro.getNome(),
                    request.getProdutoNome(), request.getQuantidade(), repasse
            );
        }

        // Notificação in-app
        notificacaoService.novaVenda(membro, request.getProdutoNome(),
                "R$ " + repasse.toPlainString());

        return response;
    }

    public VendaResponse atualizarStatus(Long id, Venda.StatusRepasse novoStatus) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada: " + id));

        venda.setStatusRepasse(novoStatus);
        VendaResponse response = VendaResponse.from(vendaRepository.save(venda));

        if (novoStatus == Venda.StatusRepasse.PAGO
                && venda.getMembro().getEmail() != null) {
            emailService.enviarConfirmacaoRepasse(
                    venda.getMembro().getEmail(),
                    venda.getMembro().getNome(),
                    venda.getProdutoNome(),
                    venda.getRepasse()
            );
            // Notificação in-app de repasse pago
            notificacaoService.repassePago(venda.getMembro(), venda.getProdutoNome(),
                    "R$ " + venda.getRepasse().toPlainString());
        }

        return response;
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

    // Calcula o resumo usando o percentual personalizado de cada membro
    private ResumoFinanceiroResponse calcularResumo(Membro membro) {
        BigDecimal totalVendas = vendaRepository.somarVendasPorMembro(membro.getId());

        // Usa o repasse individual do membro — global se não tiver override
        BigDecimal percentual = configuracaoService
                .getPercentualRepasseMembro(membro.getId())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        BigDecimal totalRepasse  = totalVendas.multiply(percentual).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPago     = vendaRepository.somarRepassePagoPorMembro(membro.getId());
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