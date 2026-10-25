package com.example.mindu.application.agendamentos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.mindu.domain.enums.Modalidade;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgendamentoCadastroDTO {

    private String clienteId;

    @NotBlank
    private String profissionalId;

    @NotNull
    private LocalDateTime dataHora;

    @NotNull
    private Modalidade modalidade;
}
