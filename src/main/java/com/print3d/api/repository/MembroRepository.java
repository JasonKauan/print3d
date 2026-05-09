package com.print3d.api.repository;

import com.print3d.api.model.Membro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembroRepository extends JpaRepository<Membro, Long> {

    // Busca por email — usado no login para encontrar o usuário
    Optional<Membro> findByEmail(String email);

    // Filtra membros por status (ATIVO ou INATIVO)
    List<Membro> findByStatus(Membro.Status status);

    // Verifica se já existe um membro com esse email (evita duplicatas)
    boolean existsByEmail(String email);
}