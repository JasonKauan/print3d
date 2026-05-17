package com.print3d.api.repository;

import com.print3d.api.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    // Remove tokens antigos do mesmo membro antes de criar um novo
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.membro.id = :membroId")
    void deleteByMembroId(Long membroId);

    // Limpeza de tokens expirados — pode ser chamado periodicamente
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiracao < :agora")
    void deleteExpirados(LocalDateTime agora);
}