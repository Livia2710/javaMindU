package com.example.mindu.application.contas;

import java.time.LocalDateTime;

public record SolicitacaoExclusaoDTO(String id, String usuarioId, String email, String tipoConta,
                                     LocalDateTime solicitadaEm, SolicitacaoExclusaoStatus status) {
    public static SolicitacaoExclusaoDTO from(com.example.mindu.domain.entity.SolicitacaoExclusaoConta request) {
        return new SolicitacaoExclusaoDTO(request.getId(), request.getUsuarioId(), request.getEmail(),
                request.getTipoConta(), request.getSolicitadaEm(), request.getStatus());
    }
}
