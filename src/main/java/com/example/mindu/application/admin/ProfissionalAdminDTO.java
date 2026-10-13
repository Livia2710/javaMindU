package com.example.mindu.application.admin;

import com.example.mindu.domain.entity.Profissional;
import com.example.mindu.domain.enums.StatusVerificacao;
import com.example.mindu.domain.enums.TipoProfissional;

import java.util.List;

public record ProfissionalAdminDTO(String id, String nome, String email, TipoProfissional tipo,
                                   List<String> tags, StatusVerificacao status) {
    public static ProfissionalAdminDTO from(Profissional profissional) {
        return new ProfissionalAdminDTO(profissional.getId(), profissional.getNome(),
                profissional.getEmail(), profissional.getTipo(), profissional.getTags(), profissional.getStatus());
    }
}
