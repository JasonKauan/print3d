package com.print3d.api.repository;

import com.print3d.api.model.Impressora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImpressoraRepository extends JpaRepository<Impressora, Long> {
    List<Impressora> findByStatus(Impressora.Status status);
    List<Impressora> findAllByOrderByNomeAsc();
    boolean existsByMembroAtualIdAndStatus(Long membroId, Impressora.Status status);
}