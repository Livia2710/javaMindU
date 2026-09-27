package com.example.mindu.infra.repository;

import com.example.mindu.domain.entity.Profissional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfissionalRepository extends JpaRepository<Profissional, String>,
        JpaSpecificationExecutor<Profissional> {

    // Login — fallback do AuthController
    Optional<Profissional> findByEmail(String email);

    // RF07 — dar 409 amigável em vez de erro cru de constraint única do banco
    boolean existsByEmail(String email);

    // JpaSpecificationExecutor: habilita o findAll(Specification<Profissional>) que o
    // ProfissionalSpecs (comNome, comEspecialidade, comModalidade, aprovado) usa — RF13/RN06
}