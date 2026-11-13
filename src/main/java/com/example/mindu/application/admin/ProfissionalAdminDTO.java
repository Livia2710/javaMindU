package com.example.mindu.application.admin;

import com.example.mindu.domain.entity.Profissional;
import com.example.mindu.domain.enums.StatusVerificacao;
import com.example.mindu.domain.enums.TipoProfissional;

import java.util.List;
import java.time.LocalDateTime;

public record ProfissionalAdminDTO(String id, String nome, String email, TipoProfissional tipo,
                                   List<String> tags, StatusVerificacao status,
                                   LocalDateTime carteirinhaEnviadaEm, LocalDateTime prazoVerificacaoAte) {
    public static ProfissionalAdminDTO from(Profissional profissional) {
        return new ProfissionalAdminDTO(profissional.getId(), profissional.getNome(),
                profissional.getEmail(), profissional.getTipo(), profissional.getTags(), profissional.getStatus(),
                profissional.getCarteirinhaEnviadaEm(), profissional.getCarteirinhaEnviadaEm() == null
                        ? null : profissional.getCarteirinhaEnviadaEm().plusHours(72));
    }
}
