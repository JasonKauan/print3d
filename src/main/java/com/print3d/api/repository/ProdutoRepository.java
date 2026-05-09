package com.print3d.api.repository;

import com.print3d.api.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Busca produtos pelo nome (case-insensitive, busca parcial)
    List<Produto> findByNomeContainingIgnoreCase(String nome);

    // Busca produtos com estoque acima de zero
    List<Produto> findByEstoqueGreaterThan(Integer estoque);
}