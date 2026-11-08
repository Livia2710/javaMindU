package com.example.mindu.application.clientes;

import com.example.mindu.domain.enums.Genero;

import java.time.LocalDate;

public record DadosSensiveisResponseDTO(
        String cns,
        LocalDate dataNascimento,
        String nomeMae,
        Genero genero
) {
}
