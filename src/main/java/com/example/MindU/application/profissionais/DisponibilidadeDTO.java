package com.example.mindu.application.profissionais;

import com.example.mindu.domain.enums.DiaSemana;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class DisponibilidadeDTO {

    @NotNull
    private DiaSemana diaSemana;
    @NotNull
    private LocalTime horaInicio;
    @NotNull
    private LocalTime horaFim;
}