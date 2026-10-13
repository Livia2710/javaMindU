package com.example.mindu.infra.repository;

import com.example.mindu.domain.entity.Profissional;
import com.example.mindu.domain.enums.Modalidade;
import com.example.mindu.domain.enums.StatusVerificacao;
import com.example.mindu.infra.repository.specs.ProfissionalSpecs;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfissionalRepository extends JpaRepository<Profissional, String>,
        JpaSpecificationExecutor<Profissional> { // habilita o findAll(Specification<>) usado abaixo

    Optional<Profissional> findByEmail(String email); // login

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Profissional p where p.id = :id")
    Optional<Profissional> findByIdForUpdate(@Param("id") String id);

    boolean existsByEmail(String email); // RF07 — 409 amigável em vez de erro cru de constraint

    List<Profissional> findByStatus(StatusVerificacao status);

    // RF13 — monta o WHERE dinamicamente: cada filtro só entra se veio preenchido,
    // e a RN06 (só aprovados aparecem) entra sempre, sem condicional.
    default List<Profissional> search(String nome, String especialidade, Modalidade modalidade) {
        Specification<Profissional> spec = Specification.where(ProfissionalSpecs.aprovado());

        if (StringUtils.hasText(nome)) {
            spec = spec.and(ProfissionalSpecs.comNome(nome));
        }
        if (StringUtils.hasText(especialidade)) {
            spec = spec.and(ProfissionalSpecs.comEspecialidade(especialidade));
        }
        if (modalidade != null) {
            spec = spec.and(ProfissionalSpecs.comModalidade(modalidade));
        }

        return findAll(spec);
    }
}
