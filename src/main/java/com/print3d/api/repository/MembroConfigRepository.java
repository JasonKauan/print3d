package com.print3d.api.repository;

import com.print3d.api.model.MembroConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MembroConfigRepository extends JpaRepository<MembroConfig, Long> {
    Optional<MembroConfig> findByMembroId(Long membroId);
}