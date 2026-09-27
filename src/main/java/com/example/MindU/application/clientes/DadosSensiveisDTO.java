package com.example.mindu.application.clientes;

import com.example.mindu.domain.enums.Genero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DadosSensiveisDTO {

    @NotBlank
    private String cns; // vem em texto puro no DTO, o Service criptografa antes de salvar

    @NotNull
    private LocalDate dataNascimento;

    @NotBlank
    private String nomeMae;

    @NotNull
    private Genero genero;
}