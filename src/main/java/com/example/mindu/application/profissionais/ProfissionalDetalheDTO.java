package com.example.mindu.application.profissionais;

import com.example.mindu.domain.enums.TipoProfissional;
import lombok.Builder;

import java.util.List;

@Builder
public record ProfissionalDetalheDTO(
        String id,
        String nome,
        TipoProfissional tipo,
        List<String> tags,
        List<LocalAtendimentoDTO> locais,
        List<DisponibilidadeDTO> disponibilidadeSemanal,
        String fotoUrl,
        String bannerUrl) {
}
