package com.print3d.api.repository;

import com.print3d.api.model.Filamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FilamentoRepository extends JpaRepository<Filamento, Long> {

    List<Filamento> findByStatus(Filamento.Status status);

    List<Filamento> findAllByOrderByNomeAsc();

    // Total gasto em filamentos
    @Query("SELECT COALESCE(SUM(f.precoPago), 0) FROM Filamento f")
    BigDecimal totalInvestido();

    // Filamentos disponíveis com estoque
    @Query("SELECT f FROM Filamento f WHERE f.pesoDisponivelGramas > 0 ORDER BY f.nome")
    List<Filamento> findDisponiveis();
}