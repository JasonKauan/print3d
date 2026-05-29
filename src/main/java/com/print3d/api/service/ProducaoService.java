package com.print3d.api.service;

import com.print3d.api.model.Filamento;
import com.print3d.api.repository.FilamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProducaoService {

    private final FilamentoRepository filamentoRepository;
    private final ConfiguracaoService configuracaoService;

    // Calcula o custo de produção baseado no filamento e gramas
    public Map<String, Object> calcularCusto(Long filamentoId, BigDecimal gramas) {
        Filamento filamento = filamentoRepository.findById(filamentoId)
                .orElseThrow(() -> new RuntimeException("Filamento não encontrado"));

        // Custo do filamento = gramas × custo por grama
        BigDecimal custoFilamento = gramas
                .multiply(filamento.getCustoPorGrama())
                .setScale(2, RoundingMode.HALF_UP);

        // Preço sugerido interno = custo × multiplicador interno
        BigDecimal multInterno = configuracaoService.getMultiplicadorInterno();
        BigDecimal precoInterno = custoFilamento
                .multiply(multInterno)
                .setScale(2, RoundingMode.HALF_UP);

        // Preço sugerido externo = custo × multiplicador externo
        BigDecimal multExterno = configuracaoService.getMultiplicadorExterno();
        BigDecimal precoExterno = custoFilamento
                .multiply(multExterno)
                .setScale(2, RoundingMode.HALF_UP);

        // Margem de lucro
        BigDecimal lucroInterno = precoInterno.subtract(custoFilamento);
        BigDecimal lucroExterno = precoExterno.subtract(custoFilamento);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("filamentoNome",       filamento.getNome());
        resultado.put("filamentoCor",        filamento.getCor());
        resultado.put("custoPorGrama",       filamento.getCustoPorGrama());
        resultado.put("gramasUsadas",        gramas);
        resultado.put("custoFilamento",      custoFilamento);
        resultado.put("multiplicadorInterno", multInterno);
        resultado.put("multiplicadorExterno", multExterno);
        resultado.put("precoSugeridoInterno", precoInterno);
        resultado.put("precoSugeridoExterno", precoExterno);
        resultado.put("lucroInterno",         lucroInterno);
        resultado.put("lucroExterno",         lucroExterno);

        return resultado;
    }
}