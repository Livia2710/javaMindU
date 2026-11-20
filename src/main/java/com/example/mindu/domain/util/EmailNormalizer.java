package com.example.mindu.domain.util;

import java.util.Locale;

/** Padroniza e-mails para que cadastro, login e consultas usem a mesma chave. */
public final class EmailNormalizer {

    private EmailNormalizer() {}

    public static String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
