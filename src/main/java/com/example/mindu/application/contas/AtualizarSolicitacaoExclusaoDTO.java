package com.example.mindu.application.contas;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtualizarSolicitacaoExclusaoDTO {
    @NotNull
    private SolicitacaoExclusaoStatus status;
}
