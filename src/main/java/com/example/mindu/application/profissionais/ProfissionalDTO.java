package com.example.mindu.application.profissionais;

import com.example.mindu.domain.enums.StatusVerificacao;
import com.example.mindu.domain.enums.TipoProfissional;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfissionalDTO {
    private String id;
    private String nome;
    private String email;
    private TipoProfissional tipo;
    private List<String> tags;
    private StatusVerificacao status;
    // Sem foto/fotoBanner/fotoCarteirinha aqui — byte[] não deveria trafegar
    // dentro de um JSON de listagem; se precisar exibir, cria endpoint próprio
    // tipo GET /v1/profissionais/{id}/foto devolvendo a imagem crua, igual ImageLite.
}