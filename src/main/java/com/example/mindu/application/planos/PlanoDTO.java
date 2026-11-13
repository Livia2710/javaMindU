package com.example.mindu.application.planos;

import java.math.BigDecimal;

public record PlanoDTO(String id, String nome, Integer vagasTotais, String porteIndicado,
                       BigDecimal precoMensalPorColaborador,
                       Boolean consultasSemLimite) {
}
