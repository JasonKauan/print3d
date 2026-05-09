package com.print3d.api.repository;

import com.print3d.api.model.Impressao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImpressaoRepository extends JpaRepository<Impressao, Long> {

    // Todas as impressões de um membro específico
    List<Impressao> findByMembroId(Long membroId);

    // Ordenado por data mais recente primeiro
    List<Impressao> findAllByOrderByDataImpressaoDesc();

    // Impressões de um membro ordenadas pela mais recente
    List<Impressao> findByMembroIdOrderByDataImpressaoDesc(Long membroId);
}