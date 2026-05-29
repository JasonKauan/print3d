package com.print3d.api.repository;

import com.print3d.api.model.FilaImpressao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilaImpressaoRepository extends JpaRepository<FilaImpressao, Long> {

    List<FilaImpressao> findByImpressoraIdOrderByCriadoEmAsc(Long impressoraId);

    Optional<FilaImpressao> findByMembroIdAndImpressoraId(Long membroId, Long impressoraId);

    boolean existsByMembroIdAndImpressoraId(Long membroId, Long impressoraId);

    void deleteByMembroIdAndImpressoraId(Long membroId, Long impressoraId);
}
