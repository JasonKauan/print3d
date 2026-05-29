package com.print3d.api.service;

import com.print3d.api.model.Configuracao;
import com.print3d.api.model.Membro;
import com.print3d.api.model.MembroConfig;
import com.print3d.api.repository.ConfiguracaoRepository;
import com.print3d.api.repository.MembroConfigRepository;
import com.print3d.api.repository.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfiguracaoService {

    private final ConfiguracaoRepository configuracaoRepository;
    private final MembroConfigRepository membroConfigRepository;
    private final MembroRepository membroRepository;

    // Retorna todas as configs como mapa chave→valor
    public Map<String, String> listarTodas() {
        Map<String, String> mapa = new HashMap<>();
        // Valores padrão — sobrescritos pelo banco se existirem
        mapa.put(Configuracao.PERCENTUAL_REPASSE,      "70");
        mapa.put(Configuracao.MULTIPLICADOR_EXTERNO,   "2.5");
        mapa.put(Configuracao.MULTIPLICADOR_INTERNO,   "1.5");
        mapa.put(Configuracao.ALERTA_FILAMENTO_GRAMAS, "100");
        mapa.put(Configuracao.NOME_ENTIDADE,           "Print3D");
        configuracaoRepository.findAll().forEach(c -> mapa.put(c.getChave(), c.getValor()));
        return mapa;
    }

    // Busca uma config numérica — retorna valor padrão se não existir
    public BigDecimal getBigDecimal(String chave, BigDecimal defaultValor) {
        return configuracaoRepository.findByChave(chave)
                .map(c -> new BigDecimal(c.getValor()))
                .orElse(defaultValor);
    }

    // Busca uma config de texto — retorna valor padrão se não existir
    public String getString(String chave, String defaultValor) {
        return configuracaoRepository.findByChave(chave)
                .map(Configuracao::getValor)
                .orElse(defaultValor);
    }

    // Limite de gramas para alerta de filamento baixo
    public BigDecimal getAlertaFilamentoGramas() {
        return getBigDecimal(Configuracao.ALERTA_FILAMENTO_GRAMAS, new BigDecimal("100"));
    }

    // Percentual global de repasse
    public BigDecimal getPercentualRepasse() {
        return getBigDecimal(Configuracao.PERCENTUAL_REPASSE, new BigDecimal("70"));
    }

    // Multiplicador para venda externa
    public BigDecimal getMultiplicadorExterno() {
        return getBigDecimal(Configuracao.MULTIPLICADOR_EXTERNO, new BigDecimal("2.5"));
    }

    // Multiplicador para venda interna
    public BigDecimal getMultiplicadorInterno() {
        return getBigDecimal(Configuracao.MULTIPLICADOR_INTERNO, new BigDecimal("1.5"));
    }

    // Repasse de um membro específico — usa override se existir, senão usa o global
    public BigDecimal getPercentualRepasseMembro(Long membroId) {
        return membroConfigRepository.findByMembroId(membroId)
                .filter(mc -> mc.getPercentualRepasse() != null)
                .map(MembroConfig::getPercentualRepasse)
                .orElse(getPercentualRepasse());
    }

    // Atualiza ou cria uma config global
    public void atualizar(String chave, String valor) {
        Configuracao config = configuracaoRepository.findByChave(chave)
                .orElse(Configuracao.builder().chave(chave).build());
        config.setValor(valor);
        config.setAtualizadoEm(LocalDateTime.now());
        configuracaoRepository.save(config);
    }

    // Atualiza percentual de repasse de um membro específico
    public void atualizarRepasseMembro(Long membroId, BigDecimal percentual) {
        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        MembroConfig config = membroConfigRepository.findByMembroId(membroId)
                .orElse(MembroConfig.builder().membro(membro).build());

        config.setPercentualRepasse(percentual);
        config.setAtualizadoEm(LocalDateTime.now());
        membroConfigRepository.save(config);
    }

    // Remove override de repasse de um membro — volta ao global
    public void removerRepasseMembro(Long membroId) {
        membroConfigRepository.findByMembroId(membroId)
                .ifPresent(mc -> {
                    mc.setPercentualRepasse(null);
                    mc.setAtualizadoEm(LocalDateTime.now());
                    membroConfigRepository.save(mc);
                });
    }
}