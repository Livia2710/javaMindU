package com.example.mindu.application.empresas;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ContratoPlanoDTO {
    @NotNull
    private LocalDate inicioVigencia;

    private LocalDate fimVigencia;

    @NotNull
    @Min(1)
    @Max(28)
    private Integer diaPagamento;
}
