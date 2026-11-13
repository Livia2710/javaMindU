package com.example.mindu.infra.repository;

import com.example.mindu.application.contas.SolicitacaoExclusaoStatus;
import com.example.mindu.domain.entity.SolicitacaoExclusaoConta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitacaoExclusaoContaRepository extends JpaRepository<SolicitacaoExclusaoConta, String> {
    Optional<SolicitacaoExclusaoConta> findFirstByUsuarioIdAndStatusInOrderBySolicitadaEmDesc(
            String usuarioId, List<SolicitacaoExclusaoStatus> statuses);
}
