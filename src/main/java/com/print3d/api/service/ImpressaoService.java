package com.print3d.api.service;

import com.print3d.api.dto.request.ImpressaoRequest;
import com.print3d.api.dto.response.ImpressaoResponse;
import com.print3d.api.model.Filamento;
import com.print3d.api.model.Impressao;
import com.print3d.api.model.Impressora;
import com.print3d.api.model.Membro;
import com.print3d.api.repository.FilamentoRepository;
import com.print3d.api.repository.ImpressaoRepository;
import com.print3d.api.repository.ImpressoraRepository;
import com.print3d.api.repository.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImpressaoService {

    private final ImpressaoRepository impressaoRepository;
    private final MembroRepository membroRepository;
    private final ImpressoraRepository impressoraRepository;
    private final FilamentoRepository filamentoRepository;

    public List<ImpressaoResponse> listarTodas() {
        return impressaoRepository.findAllByOrderByDataImpressaoDesc()
                .stream()
                .map(ImpressaoResponse::from)
                .collect(Collectors.toList());
    }

    public List<ImpressaoResponse> listarPorMembro(Long membroId) {
        return impressaoRepository.findByMembroIdOrderByDataImpressaoDesc(membroId)
                .stream()
                .map(ImpressaoResponse::from)
                .collect(Collectors.toList());
    }

    public ImpressaoResponse buscarPorId(Long id) {
        return ImpressaoResponse.from(impressaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Impressão não encontrada: " + id)));
    }

    public ImpressaoResponse criar(ImpressaoRequest request) {
        Membro membro = membroRepository.findById(request.getMembroId())
                .orElseThrow(() -> new RuntimeException("Membro não encontrado: " + request.getMembroId()));

        // Resolve impressora — opcional
        Impressora impressora = null;
        if (request.getImpressoraId() != null) {
            impressora = impressoraRepository.findById(request.getImpressoraId()).orElse(null);
        }

        // Resolve filamento e calcula custo — opcional
        Filamento filamento = null;
        BigDecimal custoFilamento = null;
        if (request.getFilamentoId() != null) {
            filamento = filamentoRepository.findById(request.getFilamentoId()).orElse(null);
            if (filamento != null && request.getGramasUsadas() != null
                    && request.getGramasUsadas().compareTo(BigDecimal.ZERO) > 0) {
                custoFilamento = request.getGramasUsadas()
                        .multiply(filamento.getCustoPorGrama())
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        Impressao impressao = Impressao.builder()
                .membro(membro)
                .impressora(impressora)
                .filamento(filamento)
                .produtoNome(request.getProdutoNome())
                .quantidade(request.getQuantidade())
                .gramasUsadas(request.getGramasUsadas())
                .custoFilamento(custoFilamento)
                .tempoImpressao(request.getTempoImpressao())
                .dataImpressao(request.getDataImpressao())
                .observacao(request.getObservacao())
                .build();

        return ImpressaoResponse.from(impressaoRepository.save(impressao));
    }

    public ImpressaoResponse atualizar(Long id, ImpressaoRequest request) {
        Impressao impressao = impressaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Impressão não encontrada: " + id));

        if (request.getMembroId() != null) {
            Membro membro = membroRepository.findById(request.getMembroId())
                    .orElseThrow(() -> new RuntimeException("Membro não encontrado"));
            impressao.setMembro(membro);
        }

        impressao.setProdutoNome(request.getProdutoNome());
        if (request.getQuantidade() != null)     impressao.setQuantidade(request.getQuantidade());
        if (request.getTempoImpressao() != null) impressao.setTempoImpressao(request.getTempoImpressao());
        if (request.getDataImpressao() != null)  impressao.setDataImpressao(request.getDataImpressao());
        if (request.getObservacao() != null)     impressao.setObservacao(request.getObservacao());

        return ImpressaoResponse.from(impressaoRepository.save(impressao));
    }

    public void deletar(Long id) {
        if (!impressaoRepository.existsById(id)) {
            throw new RuntimeException("Impressão não encontrada: " + id);
        }
        impressaoRepository.deleteById(id);
    }
}