package com.print3d.api.repository;

import com.print3d.api.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findByMembroId(Long membroId);

    // Total de vendas de um membro
    @Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v WHERE v.membro.id = :membroId")
    BigDecimal somarVendasPorMembro(@Param("membroId") Long membroId);

    // Total de repasse já pago a um membro
    @Query("SELECT COALESCE(SUM(v.repasse), 0) FROM Venda v WHERE v.membro.id = :membroId AND v.statusRepasse = 'PAGO'")
    BigDecimal somarRepassePagoPorMembro(@Param("membroId") Long membroId);

    // Total geral de todas as vendas
    @Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v")
    BigDecimal somarTodasVendas();

    // Total de repasse pendente (todos os membros)
    @Query("SELECT COALESCE(SUM(v.repasse), 0) FROM Venda v WHERE v.statusRepasse = 'PENDENTE'")
    BigDecimal somarTodoRepassePendente();

    // Vendas por período (para gráfico mensal)
    @Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim")
    BigDecimal somarVendasPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(v) FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim")
    long contarVendasPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    // Totais por membro em período — usados no relatório mensal
    @Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v WHERE v.membro.id = :membroId AND v.dataVenda BETWEEN :inicio AND :fim")
    java.math.BigDecimal somarVendasMembroPeriodo(@Param("membroId") Long membroId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(v.repasse), 0) FROM Venda v WHERE v.membro.id = :membroId AND v.dataVenda BETWEEN :inicio AND :fim")
    java.math.BigDecimal somarRepasseMembroPeriodo(@Param("membroId") Long membroId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(v) FROM Venda v WHERE v.membro.id = :membroId AND v.dataVenda BETWEEN :inicio AND :fim")
    long contarVendasMembroPeriodo(@Param("membroId") Long membroId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    // Top 5 produtos mais vendidos (nome + quantidade + receita)
    @Query("""
        SELECT new map(
            v.produtoNome as nome,
            SUM(v.quantidade) as totalQuantidade,
            SUM(v.valorTotal) as totalReceita
        )
        FROM Venda v
        GROUP BY v.produtoNome
        ORDER BY SUM(v.quantidade) DESC
        """)
    List<Map<String, Object>> rankingProdutos();
}