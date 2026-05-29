package com.print3d.api.repository;

import com.print3d.api.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findAllByOrderByCriadoEmDesc();

    List<MovimentacaoEstoque> findByTipoItemOrderByCriadoEmDesc(MovimentacaoEstoque.TipoItem tipoItem);

    List<MovimentacaoEstoque> findByItemIdAndTipoItemOrderByCriadoEmDesc(
            Long itemId, MovimentacaoEstoque.TipoItem tipoItem);
}