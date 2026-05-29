package com.print3d.api.service;

import com.print3d.api.dto.response.MovimentacaoEstoqueResponse;
import com.print3d.api.model.*;
import com.print3d.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimentacaoEstoqueService {

    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final FilamentoRepository filamentoRepository;
    private final MembroRepository membroRepository;

    // Lista todas as movimentações
    public List<MovimentacaoEstoqueResponse> listarTodas() {
        return movimentacaoRepository.findAllByOrderByCriadoEmDesc()
                .stream().map(MovimentacaoEstoqueResponse::from).collect(Collectors.toList());
    }

    // Lista por tipo de item (PRODUTO ou FILAMENTO)
    public List<MovimentacaoEstoqueResponse> listarPorTipoItem(MovimentacaoEstoque.TipoItem tipoItem) {
        return movimentacaoRepository.findByTipoItemOrderByCriadoEmDesc(tipoItem)
                .stream().map(MovimentacaoEstoqueResponse::from).collect(Collectors.toList());
    }

    // Lista movimentações de um item específico
    public List<MovimentacaoEstoqueResponse> listarPorItem(Long itemId, MovimentacaoEstoque.TipoItem tipoItem) {
        return movimentacaoRepository.findByItemIdAndTipoItemOrderByCriadoEmDesc(itemId, tipoItem)
                .stream().map(MovimentacaoEstoqueResponse::from).collect(Collectors.toList());
    }

    // ── Registros automáticos ──────────────────────────────────────────────────

    // Produto cadastrado — entrada inicial
    @Async
    public void registrarEntradaProduto(Produto produto, Membro membro) {
        registrar(
                MovimentacaoEstoque.TipoItem.PRODUTO,
                produto.getId(),
                produto.getNome(),
                MovimentacaoEstoque.TipoMovimentacao.ENTRADA_CADASTRO,
                new BigDecimal(produto.getEstoque()),
                BigDecimal.ZERO,
                new BigDecimal(produto.getEstoque()),
                "Cadastro inicial do produto",
                membro
        );
    }

    // Venda registrada — saída de produto
    @Async
    public void registrarSaidaVenda(Produto produto, int quantidade, Membro membro) {
        BigDecimal estoqueAntes = new BigDecimal(produto.getEstoque() + quantidade);
        BigDecimal estoqueDepois = new BigDecimal(produto.getEstoque());
        registrar(
                MovimentacaoEstoque.TipoItem.PRODUTO,
                produto.getId(),
                produto.getNome(),
                MovimentacaoEstoque.TipoMovimentacao.SAIDA_VENDA,
                new BigDecimal(quantidade).negate(),
                estoqueAntes,
                estoqueDepois,
                "Saída por venda",
                membro
        );
    }

    // Impressão finalizada — entrada de peças no estoque
    @Async
    public void registrarEntradaImpressao(Produto produto, int quantidade, Membro membro) {
        BigDecimal estoqueAntes = new BigDecimal(produto.getEstoque() - quantidade);
        BigDecimal estoqueDepois = new BigDecimal(produto.getEstoque());
        registrar(
                MovimentacaoEstoque.TipoItem.PRODUTO,
                produto.getId(),
                produto.getNome(),
                MovimentacaoEstoque.TipoMovimentacao.ENTRADA_IMPRESSAO,
                new BigDecimal(quantidade),
                estoqueAntes,
                estoqueDepois,
                "Entrada por impressão finalizada",
                membro
        );
    }

    // Filamento cadastrado — rolo completo
    @Async
    public void registrarEntradaFilamento(Filamento filamento, Membro membro) {
        registrar(
                MovimentacaoEstoque.TipoItem.FILAMENTO,
                filamento.getId(),
                filamento.getNome(),
                MovimentacaoEstoque.TipoMovimentacao.ENTRADA_CADASTRO,
                filamento.getPesoTotalGramas(),
                BigDecimal.ZERO,
                filamento.getPesoTotalGramas(),
                "Cadastro de novo rolo",
                membro
        );
    }

    // Filamento consumido na impressão
    @Async
    public void registrarConsumoFilamento(Filamento filamento, BigDecimal gramas,
                                          BigDecimal estoqueAntes, Membro membro) {
        registrar(
                MovimentacaoEstoque.TipoItem.FILAMENTO,
                filamento.getId(),
                filamento.getNome(),
                MovimentacaoEstoque.TipoMovimentacao.CONSUMO_FILAMENTO,
                gramas.negate(),
                estoqueAntes,
                filamento.getPesoDisponivelGramas(),
                "Consumo em impressão",
                membro
        );
    }

    // ── Ajuste manual ──────────────────────────────────────────────────────────

    public MovimentacaoEstoqueResponse ajustarProduto(Long produtoId, int novoEstoque,
                                                      String justificativa, String emailMembro) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        Membro membro = membroRepository.findByEmail(emailMembro)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        BigDecimal estoqueAntes  = new BigDecimal(produto.getEstoque());
        BigDecimal estoqueDepois = new BigDecimal(novoEstoque);
        BigDecimal diff          = estoqueDepois.subtract(estoqueAntes);

        MovimentacaoEstoque.TipoMovimentacao tipo = diff.compareTo(BigDecimal.ZERO) >= 0
                ? MovimentacaoEstoque.TipoMovimentacao.AJUSTE_MANUAL_ENTRADA
                : MovimentacaoEstoque.TipoMovimentacao.AJUSTE_MANUAL_SAIDA;

        produto.setEstoque(novoEstoque);
        produtoRepository.save(produto);

        return MovimentacaoEstoqueResponse.from(registrarSync(
                MovimentacaoEstoque.TipoItem.PRODUTO, produtoId, produto.getNome(),
                tipo, diff, estoqueAntes, estoqueDepois, justificativa, membro
        ));
    }

    public MovimentacaoEstoqueResponse ajustarFilamento(Long filamentoId, BigDecimal novoDisponivel,
                                                        String justificativa, String emailMembro) {
        Filamento filamento = filamentoRepository.findById(filamentoId)
                .orElseThrow(() -> new RuntimeException("Filamento não encontrado"));
        Membro membro = membroRepository.findByEmail(emailMembro)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        BigDecimal estoqueAntes  = filamento.getPesoDisponivelGramas();
        BigDecimal diff          = novoDisponivel.subtract(estoqueAntes);

        MovimentacaoEstoque.TipoMovimentacao tipo = diff.compareTo(BigDecimal.ZERO) >= 0
                ? MovimentacaoEstoque.TipoMovimentacao.AJUSTE_MANUAL_ENTRADA
                : MovimentacaoEstoque.TipoMovimentacao.AJUSTE_MANUAL_SAIDA;

        filamento.setPesoDisponivelGramas(novoDisponivel);
        if (novoDisponivel.compareTo(BigDecimal.ZERO) == 0) {
            filamento.setStatus(Filamento.Status.ESGOTADO);
        } else if (filamento.getStatus() == Filamento.Status.ESGOTADO) {
            filamento.setStatus(Filamento.Status.DISPONIVEL);
        }
        filamentoRepository.save(filamento);

        return MovimentacaoEstoqueResponse.from(registrarSync(
                MovimentacaoEstoque.TipoItem.FILAMENTO, filamentoId, filamento.getNome(),
                tipo, diff, estoqueAntes, novoDisponivel, justificativa, membro
        ));
    }

    // ── Internos ──────────────────────────────────────────────────────────────

    @Async
    private void registrar(MovimentacaoEstoque.TipoItem tipoItem, Long itemId, String itemNome,
                           MovimentacaoEstoque.TipoMovimentacao tipo, BigDecimal quantidade,
                           BigDecimal estoqueAntes, BigDecimal estoqueDepois,
                           String justificativa, Membro membro) {
        movimentacaoRepository.save(MovimentacaoEstoque.builder()
                .tipoItem(tipoItem).itemId(itemId).itemNome(itemNome)
                .tipo(tipo).quantidade(quantidade)
                .estoqueAntes(estoqueAntes).estoqueDepois(estoqueDepois)
                .justificativa(justificativa).membro(membro)
                .build());
    }

    private MovimentacaoEstoque registrarSync(MovimentacaoEstoque.TipoItem tipoItem, Long itemId,
                                              String itemNome, MovimentacaoEstoque.TipoMovimentacao tipo,
                                              BigDecimal quantidade, BigDecimal estoqueAntes,
                                              BigDecimal estoqueDepois, String justificativa, Membro membro) {
        return movimentacaoRepository.save(MovimentacaoEstoque.builder()
                .tipoItem(tipoItem).itemId(itemId).itemNome(itemNome)
                .tipo(tipo).quantidade(quantidade)
                .estoqueAntes(estoqueAntes).estoqueDepois(estoqueDepois)
                .justificativa(justificativa).membro(membro)
                .build());
    }
}