package com.print3d.api.service;

import com.print3d.api.dto.request.FinalizarImpressoraRequest;
import com.print3d.api.dto.request.ImpressoraRequest;
import com.print3d.api.dto.request.UsarImpressoraRequest;
import com.print3d.api.dto.response.FilaImpressaoResponse;
import com.print3d.api.dto.response.ImpressoraResponse;
import com.print3d.api.model.*;
import com.print3d.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImpressoraService {

    private final ImpressoraRepository impressoraRepository;
    private final MembroRepository membroRepository;
    private final ImpressaoRepository impressaoRepository;
    private final FilamentoRepository filamentoRepository;
    private final NotificacaoService notificacaoService;
    private final MovimentacaoEstoqueService movimentacaoService;
    private final ConfiguracaoService configuracaoService;
    private final FilaImpressaoRepository filaRepository;

    public List<ImpressoraResponse> listarTodas() {
        return impressoraRepository.findAllByOrderByNomeAsc()
                .stream().map(ImpressoraResponse::from).collect(Collectors.toList());
    }

    public List<ImpressoraResponse> listarLivres() {
        return impressoraRepository.findByStatus(Impressora.Status.LIVRE)
                .stream().map(ImpressoraResponse::from).collect(Collectors.toList());
    }

    public ImpressoraResponse buscarPorId(Long id) {
        return ImpressoraResponse.from(impressoraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Impressora não encontrada: " + id)));
    }

    public ImpressoraResponse criar(ImpressoraRequest request) {
        Impressora impressora = Impressora.builder()
                .nome(request.getNome())
                .modelo(request.getModelo())
                .observacao(request.getObservacao())
                .status(Impressora.Status.LIVRE)
                .build();
        return ImpressoraResponse.from(impressoraRepository.save(impressora));
    }

    public ImpressoraResponse atualizar(Long id, ImpressoraRequest request) {
        Impressora impressora = impressoraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Impressora não encontrada: " + id));
        impressora.setNome(request.getNome());
        if (request.getModelo() != null)     impressora.setModelo(request.getModelo());
        if (request.getObservacao() != null) impressora.setObservacao(request.getObservacao());
        if (request.getStatus() != null)     impressora.setStatus(request.getStatus());
        return ImpressoraResponse.from(impressoraRepository.save(impressora));
    }

    public void deletar(Long id) {
        if (!impressoraRepository.existsById(id))
            throw new RuntimeException("Impressora não encontrada: " + id);
        impressoraRepository.deleteById(id);
    }

    @Transactional
    public ImpressoraResponse iniciarUso(Long impressoraId, String emailMembro,
                                         UsarImpressoraRequest request) {
        Impressora impressora = impressoraRepository.findById(impressoraId)
                .orElseThrow(() -> new RuntimeException("Impressora não encontrada"));

        if (impressora.getStatus() != Impressora.Status.LIVRE) {
            throw new RuntimeException("Impressora não está livre no momento.");
        }

        Membro membro = membroRepository.findByEmail(emailMembro)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        // Valida filamento se informado
        if (request.getFilamentoId() != null) {
            Filamento filamento = filamentoRepository.findById(request.getFilamentoId())
                    .orElseThrow(() -> new RuntimeException("Filamento não encontrado"));
            if (filamento.getStatus() == Filamento.Status.ESGOTADO) {
                throw new RuntimeException("Filamento selecionado está esgotado.");
            }
            impressora.setFilamentoAtualId(request.getFilamentoId());
        }

        impressora.setStatus(Impressora.Status.OCUPADA);
        impressora.setMembroAtual(membro);
        impressora.setUsoIniciadoEm(LocalDateTime.now());
        impressora.setProdutoEmImpressao(request.getProdutoNome());
        impressora.setQuantidadeEmImpressao(request.getQuantidade());

        return ImpressoraResponse.from(impressoraRepository.save(impressora));
    }

    @Transactional
    public ImpressoraResponse finalizarUso(Long impressoraId, String emailRequisitante,
                                           FinalizarImpressoraRequest request) {
        Impressora impressora = impressoraRepository.findById(impressoraId)
                .orElseThrow(() -> new RuntimeException("Impressora não encontrada"));

        if (impressora.getStatus() != Impressora.Status.OCUPADA) {
            throw new RuntimeException("Impressora não está em uso.");
        }

        Membro requisitante = membroRepository.findByEmail(emailRequisitante)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        boolean isAdmin = requisitante.getRole() == Membro.Role.ADMIN
                || requisitante.getRole() == Membro.Role.DEV;
        boolean isProprioUso = impressora.getMembroAtual() != null
                && impressora.getMembroAtual().getId().equals(requisitante.getId());

        if (!isAdmin && !isProprioUso) {
            throw new RuntimeException("Você só pode finalizar o seu próprio uso.");
        }

        // Resolve filamento e calcula custo
        Filamento filamento = null;
        BigDecimal custoFilamento = null;

        if (impressora.getFilamentoAtualId() != null) {
            filamento = filamentoRepository.findById(impressora.getFilamentoAtualId()).orElse(null);
        }

        if (filamento != null && request.getGramasUsadas() != null
                && request.getGramasUsadas().compareTo(BigDecimal.ZERO) > 0) {
            custoFilamento = request.getGramasUsadas()
                    .multiply(filamento.getCustoPorGrama())
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal estoqueAntes = filamento.getPesoDisponivelGramas();
            BigDecimal novoDisponivel = estoqueAntes.subtract(request.getGramasUsadas());
            if (novoDisponivel.compareTo(BigDecimal.ZERO) < 0) novoDisponivel = BigDecimal.ZERO;
            filamento.setPesoDisponivelGramas(novoDisponivel);
            if (novoDisponivel.compareTo(BigDecimal.ZERO) == 0) {
                filamento.setStatus(Filamento.Status.ESGOTADO);
            }
            filamentoRepository.save(filamento);

            // Registra consumo no histórico de estoque
            movimentacaoService.registrarConsumoFilamento(
                    filamento, request.getGramasUsadas(), estoqueAntes, requisitante);
        }

        // Cria o registro de impressão automaticamente
        Impressao impressao = Impressao.builder()
                .membro(impressora.getMembroAtual())
                .impressora(impressora)
                .filamento(filamento)
                .produtoNome(impressora.getProdutoEmImpressao())
                .quantidade(impressora.getQuantidadeEmImpressao())
                .tempoImpressao(request.getTempoReal())
                .gramasUsadas(request.getGramasUsadas())
                .custoFilamento(custoFilamento)
                .dataImpressao(LocalDate.now())
                .observacao(request.getObservacao())
                .build();
        impressaoRepository.save(impressao);

        // Notifica todos os membros ativos que a impressora foi liberada
        Membro membroQueUsou = impressora.getMembroAtual();
        membroRepository.findByStatus(com.print3d.api.model.Membro.Status.ATIVO)
                .stream()
                .filter(m -> !m.getId().equals(membroQueUsou.getId())) // não notifica quem acabou de usar
                .forEach(m -> notificacaoService.impressoraLiberada(m, impressora.getNome()));

        // Notifica o primeiro da fila que a impressora foi liberada
        filaRepository.findByImpressoraIdOrderByCriadoEmAsc(impressoraId)
                .stream().findFirst()
                .ifPresent(entrada -> notificacaoService.vezNaFila(
                        entrada.getMembro(), impressora.getNome()));

        // Alerta de filamento baixo — threshold configurável
        java.math.BigDecimal alertaGramas = configuracaoService.getAlertaFilamentoGramas();
        if (filamento != null
                && filamento.getPesoDisponivelGramas().compareTo(alertaGramas) < 0
                && filamento.getStatus() != com.print3d.api.model.Filamento.Status.ESGOTADO) {
            // Salva nome e gramas antes do forEach para evitar LazyInitializationException
            String nomeFilamento = filamento.getNome();
            String gramasRestantes = filamento.getPesoDisponivelGramas().toPlainString();
            membroRepository.findByStatus(com.print3d.api.model.Membro.Status.ATIVO)
                    .stream()
                    .filter(m -> m.getRole() == com.print3d.api.model.Membro.Role.ADMIN
                            || m.getRole() == com.print3d.api.model.Membro.Role.DEV)
                    .forEach(m -> notificacaoService.filamentoBaixo(m, nomeFilamento, gramasRestantes));
        }

        // Libera a impressora
        impressora.setStatus(Impressora.Status.LIVRE);
        impressora.setMembroAtual(null);
        impressora.setUsoIniciadoEm(null);
        impressora.setProdutoEmImpressao(null);
        impressora.setQuantidadeEmImpressao(null);
        impressora.setFilamentoAtualId(null);

        return ImpressoraResponse.from(impressoraRepository.save(impressora));
    }

    // ---- Fila de impressão ----

    public List<FilaImpressaoResponse> verFila(Long impressoraId) {
        if (!impressoraRepository.existsById(impressoraId))
            throw new RuntimeException("Impressora não encontrada: " + impressoraId);
        List<FilaImpressao> fila = filaRepository.findByImpressoraIdOrderByCriadoEmAsc(impressoraId);
        java.util.concurrent.atomic.AtomicInteger pos = new java.util.concurrent.atomic.AtomicInteger(1);
        return fila.stream()
                .map(f -> FilaImpressaoResponse.from(f, pos.getAndIncrement()))
                .collect(Collectors.toList());
    }

    @Transactional
    public FilaImpressaoResponse entrarNaFila(Long impressoraId, String emailMembro,
                                               UsarImpressoraRequest request) {
        Impressora impressora = impressoraRepository.findById(impressoraId)
                .orElseThrow(() -> new RuntimeException("Impressora não encontrada"));

        if (impressora.getStatus() == Impressora.Status.LIVRE) {
            throw new RuntimeException("Impressora está livre — use /usar diretamente.");
        }
        if (impressora.getStatus() == Impressora.Status.MANUTENCAO) {
            throw new RuntimeException("Impressora está em manutenção e não pode receber fila.");
        }

        Membro membro = membroRepository.findByEmail(emailMembro)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        if (filaRepository.existsByMembroIdAndImpressoraId(membro.getId(), impressoraId)) {
            throw new RuntimeException("Você já está na fila desta impressora.");
        }

        FilaImpressao entrada = FilaImpressao.builder()
                .impressora(impressora)
                .membro(membro)
                .produtoNome(request.getProdutoNome())
                .quantidade(request.getQuantidade())
                .filamentoId(request.getFilamentoId())
                .build();

        FilaImpressao salvo = filaRepository.save(entrada);
        List<FilaImpressao> filaAtual = filaRepository.findByImpressoraIdOrderByCriadoEmAsc(impressoraId);
        int posicao = filaAtual.indexOf(salvo) + 1;
        return FilaImpressaoResponse.from(salvo, posicao);
    }

    @Transactional
    public void sairDaFila(Long impressoraId, String emailMembro) {
        Membro membro = membroRepository.findByEmail(emailMembro)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));
        if (!filaRepository.existsByMembroIdAndImpressoraId(membro.getId(), impressoraId)) {
            throw new RuntimeException("Você não está na fila desta impressora.");
        }
        filaRepository.deleteByMembroIdAndImpressoraId(membro.getId(), impressoraId);
    }

    @Transactional
    public ImpressoraResponse alterarStatus(Long id, Impressora.Status novoStatus) {
        Impressora impressora = impressoraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Impressora não encontrada"));

        impressora.setStatus(novoStatus);
        if (novoStatus == Impressora.Status.LIVRE || novoStatus == Impressora.Status.MANUTENCAO) {
            impressora.setMembroAtual(null);
            impressora.setUsoIniciadoEm(null);
            impressora.setProdutoEmImpressao(null);
            impressora.setQuantidadeEmImpressao(null);
            impressora.setFilamentoAtualId(null);
        }

        return ImpressoraResponse.from(impressoraRepository.save(impressora));
    }
}