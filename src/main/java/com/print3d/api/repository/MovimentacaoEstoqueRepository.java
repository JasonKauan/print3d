package com.print3d.api.repository;

import com.print3d.api.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findAllByOrderByCriadoEmDesc();

    List<MovimentacaoEstoque> findByTipoItemOrderByCriadoEmDesc(MovimentacaoEstoque.TipoItem tipoItem);

    List<MovimentacaoEstoque> findByItemIdAndTipoItemOrderByCriadoEmDesc(
            Long itemId, MovimentacaoEstoque.TipoItem tipoItem);

    // Total consumido de um filamento em um período (retorna valor positivo)
    @Query("""
        SELECT COALESCE(ABS(SUM(m.quantidade)), 0)
        FROM MovimentacaoEstoque m
        WHERE m.itemId = :filamentoId
          AND m.tipoItem = 'FILAMENTO'
          AND m.tipo = 'CONSUMO_FILAMENTO'
          AND m.criadoEm >= :desde
        """)
    BigDecimal somarConsumoFilamento(@Param("filamentoId") Long filamentoId,
                                     @Param("desde") LocalDateTime desde);
}