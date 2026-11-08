package com.example.mindu.infra.repository;

import com.example.mindu.domain.entity.Matricula;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaRepository extends JpaRepository<Matricula, String> {
    void deleteByEmpresaId(String empresaId);

    // RN04 — valida que o código digitado pelo Cliente pertence àquela Empresa específica
    Optional<Matricula> findByCodigoAndEmpresaId(String codigo, String empresaId);

    Optional<Matricula> findByCodigoIgnoreCase(String codigo);

    List<Matricula> findAllByEmpresaIdOrderByCodigoAsc(String empresaId);

    // RN09 — conta quantas matrículas a Empresa já gerou, pra comparar com o limite do Plano
    long countByEmpresaId(String empresaId);
}
