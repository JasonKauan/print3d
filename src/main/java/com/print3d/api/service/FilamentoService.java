package com.print3d.api.service;

import com.print3d.api.dto.request.FilamentoRequest;
import com.print3d.api.dto.response.FilamentoResponse;
import com.print3d.api.model.Filamento;
import com.print3d.api.repository.FilamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilamentoService {

    private final FilamentoRepository filamentoRepository;
    private final MovimentacaoEstoqueService movimentacaoService;

    public List<FilamentoResponse> listarTodos() {
        return filamentoRepository.findAllByOrderByNomeAsc()
                .stream().map(FilamentoResponse::from).collect(Collectors.toList());
    }

    public List<FilamentoResponse> listarDisponiveis() {
        return filamentoRepository.findDisponiveis()
                .stream().map(FilamentoResponse::from).collect(Collectors.toList());
    }

    public FilamentoResponse buscarPorId(Long id) {
        return FilamentoResponse.from(filamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Filamento não encontrado: " + id)));
    }

    public FilamentoResponse criar(FilamentoRequest request) {
        BigDecimal custoPorGrama = request.getPrecoPago()
                .divide(request.getPesoTotalGramas(), 4, RoundingMode.HALF_UP);

        Filamento filamento = Filamento.builder()
                .nome(request.getNome())
                .marca(request.getMarca())
                .cor(request.getCor())
                .tipo(request.getTipo())
                .pesoTotalGramas(request.getPesoTotalGramas())
                .pesoDisponivelGramas(request.getPesoTotalGramas())
                .precoPago(request.getPrecoPago())
                .custoPorGrama(custoPorGrama)
                .dataCompra(request.getDataCompra())
                .status(Filamento.Status.DISPONIVEL)
                .build();

        Filamento salvo = filamentoRepository.save(filamento);

        // Registra entrada do rolo no histórico de estoque
        movimentacaoService.registrarEntradaFilamento(salvo, null);

        return FilamentoResponse.from(salvo);
    }

    public FilamentoResponse atualizar(Long id, FilamentoRequest request) {
        Filamento filamento = filamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Filamento não encontrado: " + id));

        BigDecimal custoPorGrama = request.getPrecoPago()
                .divide(request.getPesoTotalGramas(), 4, RoundingMode.HALF_UP);

        filamento.setNome(request.getNome());
        if (request.getMarca() != null)      filamento.setMarca(request.getMarca());
        if (request.getCor() != null)        filamento.setCor(request.getCor());
        if (request.getTipo() != null)       filamento.setTipo(request.getTipo());
        if (request.getDataCompra() != null) filamento.setDataCompra(request.getDataCompra());
        filamento.setPesoTotalGramas(request.getPesoTotalGramas());
        filamento.setPrecoPago(request.getPrecoPago());
        filamento.setCustoPorGrama(custoPorGrama);

        return FilamentoResponse.from(filamentoRepository.save(filamento));
    }

    public void descontarGramas(Filamento filamento, BigDecimal gramas) {
        BigDecimal novoDisponivel = filamento.getPesoDisponivelGramas().subtract(gramas);
        if (novoDisponivel.compareTo(BigDecimal.ZERO) < 0) novoDisponivel = BigDecimal.ZERO;
        filamento.setPesoDisponivelGramas(novoDisponivel);
        if (novoDisponivel.compareTo(BigDecimal.ZERO) == 0) {
            filamento.setStatus(Filamento.Status.ESGOTADO);
        }
        filamentoRepository.save(filamento);
    }

    public void deletar(Long id) {
        if (!filamentoRepository.existsById(id))
            throw new RuntimeException("Filamento não encontrado: " + id);
        filamentoRepository.deleteById(id);
    }

    public BigDecimal totalInvestido() {
        return filamentoRepository.totalInvestido();
    }
}