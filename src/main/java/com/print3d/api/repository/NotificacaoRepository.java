package com.print3d.api.repository;

import com.print3d.api.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    // Todas as notificações do membro, mais recentes primeiro
    List<Notificacao> findByMembroIdOrderByCriadoEmDesc(Long membroId);

    // Só as não lidas
    List<Notificacao> findByMembroIdAndLidaFalseOrderByCriadoEmDesc(Long membroId);

    // Conta as não lidas — para o badge do sininho
    long countByMembroIdAndLidaFalse(Long membroId);

    // Marca todas como lidas de uma vez
    @Modifying
    @Transactional
    @Query("UPDATE Notificacao n SET n.lida = true WHERE n.membro.id = :membroId")
    void marcarTodasComoLidas(@Param("membroId") Long membroId);
}