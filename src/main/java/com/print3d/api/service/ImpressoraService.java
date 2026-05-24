package com.print3d.api.service;

import com.print3d.api.dto.request.FinalizarImpressoraRequest;
import com.print3d.api.dto.request.ImpressoraRequest;
import com.print3d.api.dto.request.UsarImpressoraRequest;
import com.print3d.api.dto.response.ImpressoraResponse;
import com.print3d.api.model.Impressao;
import com.print3d.api.model.Impressora;
import com.print3d.api.model.Membro;
import com.print3d.api.repository.ImpressaoRepository;
import com.print3d.api.repository.ImpressoraRepository;
import com.print3d.api.repository.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // Admin pode mudar status manualmente (ex: colocar em manutenção)
        if (request.getStatus() != null)     impressora.setStatus(request.getStatus());
        return ImpressoraResponse.from(impressoraRepository.save(impressora));
    }

    public void deletar(Long id) {
        if (!impressoraRepository.existsById(id))
            throw new RuntimeException("Impressora não encontrada: " + id);
        impressoraRepository.deleteById(id);
    }

    // Membro inicia o uso de uma impressora livre
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

        impressora.setStatus(Impressora.Status.OCUPADA);
        impressora.setMembroAtual(membro);
        impressora.setUsoIniciadoEm(LocalDateTime.now());
        impressora.setProdutoEmImpressao(request.getProdutoNome());
        impressora.setQuantidadeEmImpressao(request.getQuantidade());

        return ImpressoraResponse.from(impressoraRepository.save(impressora));
    }

    // Membro ou admin finaliza o uso — cria registro de impressão automaticamente
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

        // Membro só pode finalizar o próprio uso
        boolean isAdmin = requisitante.getRole() == Membro.Role.ADMIN
                || requisitante.getRole() == Membro.Role.DEV;
        boolean isProprioUso = impressora.getMembroAtual() != null
                && impressora.getMembroAtual().getId().equals(requisitante.getId());

        if (!isAdmin && !isProprioUso) {
            throw new RuntimeException("Você só pode finalizar o seu próprio uso.");
        }

        // Cria o registro de impressão automaticamente
        Impressao impressao = Impressao.builder()
                .membro(impressora.getMembroAtual())
                .impressora(impressora)
                .produtoNome(impressora.getProdutoEmImpressao())
                .quantidade(impressora.getQuantidadeEmImpressao())
                .tempoImpressao(request.getTempoReal())
                .dataImpressao(LocalDate.now())
                .observacao(request.getObservacao())
                .build();
        impressaoRepository.save(impressao);

        // Libera a impressora
        impressora.setStatus(Impressora.Status.LIVRE);
        impressora.setMembroAtual(null);
        impressora.setUsoIniciadoEm(null);
        impressora.setProdutoEmImpressao(null);
        impressora.setQuantidadeEmImpressao(null);

        return ImpressoraResponse.from(impressoraRepository.save(impressora));
    }

    // Admin coloca em manutenção ou libera forçado
    @Transactional
    public ImpressoraResponse alterarStatus(Long id, Impressora.Status novoStatus) {
        Impressora impressora = impressoraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Impressora não encontrada"));

        impressora.setStatus(novoStatus);

        // Se liberando forçado, limpa o membro atual
        if (novoStatus == Impressora.Status.LIVRE || novoStatus == Impressora.Status.MANUTENCAO) {
            impressora.setMembroAtual(null);
            impressora.setUsoIniciadoEm(null);
            impressora.setProdutoEmImpressao(null);
            impressora.setQuantidadeEmImpressao(null);
        }

        return ImpressoraResponse.from(impressoraRepository.save(impressora));
    }
}