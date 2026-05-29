package com.print3d.api.repository;

import com.print3d.api.model.Impressao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface ImpressaoRepository extends JpaRepository<Impressao, Long> {

    // Listagem ordenada por data — usada no ImpressaoService
    List<Impressao> findAllByOrderByDataImpressaoDesc();

    // Listagem por membro ordenada por data — usada no ImpressaoService
    List<Impressao> findByMembroIdOrderByDataImpressaoDesc(Long membroId);

    // Listagem por membro sem ordenação — usada no AdminDashboardController
    List<Impressao> findByMembroId(Long membroId);

    // Impressões por período — usada no dashboard
    @Query("SELECT COUNT(i) FROM Impressao i WHERE i.dataImpressao BETWEEN :inicio AND :fim")
    long contarPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    // Totais por membro em período — usados no relatório mensal
    @Query("SELECT COUNT(i) FROM Impressao i WHERE i.membro.id = :membroId AND i.dataImpressao BETWEEN :inicio AND :fim")
    long contarPorMembroEPeriodo(@Param("membroId") Long membroId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(i.quantidade), 0) FROM Impressao i WHERE i.membro.id = :membroId AND i.dataImpressao BETWEEN :inicio AND :fim")
    long somarPecasPorMembroEPeriodo(@Param("membroId") Long membroId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    // Top membros mais produtivos — ranking do painel ADM
    @Query("""
        SELECT new map(
            i.membro.nome as nome,
            i.membro.fotoUrl as fotoUrl,
            COUNT(i) as totalImpressoes,
            SUM(i.quantidade) as totalPecas
        )
        FROM Impressao i
        GROUP BY i.membro.id, i.membro.nome, i.membro.fotoUrl
        ORDER BY COUNT(i) DESC
        """)
    List<Map<String, Object>> rankingMembros();
}