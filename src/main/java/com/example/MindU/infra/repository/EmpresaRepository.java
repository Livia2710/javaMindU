package com.example.mindu.infra.repository;

import com.example.mindu.domain.entity.Empresa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, String> {
    // RN02 — impede CNPJ duplicado no cadastro de Empresa
    boolean existsByCnpj(String cnpj);

    // Login — fallback do AuthController
    Optional<Empresa> findByEmail(String email);
}