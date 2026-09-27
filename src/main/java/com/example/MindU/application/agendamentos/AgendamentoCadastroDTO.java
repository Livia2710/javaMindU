package com.example.mindu.application.agendamentos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgendamentoCadastroDTO {

    @NotBlank
    private String clienteId;

    @NotBlank
    private String profissionalId;

    @NotNull
    @Future // não deixa agendar em data/hora que já passou
    private LocalDateTime dataHora;
}