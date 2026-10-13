package com.example.mindu.application.profissionais;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ConsultaHorariosDTO {
    @NotNull
    private LocalDate data;
}
