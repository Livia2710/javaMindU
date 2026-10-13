package com.example.mindu.application.agendamentos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReagendarDTO {
    @NotNull
    private LocalDateTime dataHora;
}
