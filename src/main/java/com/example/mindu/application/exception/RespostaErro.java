package com.example.mindu.application.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/** Monta o corpo JSON comum usado pelos controllers, filtros e interceptors. */
public final class RespostaErro {

    private RespostaErro() {}

    public static Map<String, Object> corpo(HttpStatusCode status, String mensagem) {
        return corpo(status, mensagem, null);
    }

    public static Map<String, Object> corpo(HttpStatusCode status, String mensagem, Map<String, String> campos) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", mensagem);
        corpo.put("status", status.value());
        if (campos != null && !campos.isEmpty()) corpo.put("campos", campos);
        return corpo;
    }

    public static ResponseEntity<Map<String, Object>> resposta(HttpStatusCode status, String mensagem) {
        return ResponseEntity.status(status).body(corpo(status, mensagem));
    }

    public static ResponseEntity<Map<String, Object>> resposta(HttpStatusCode status, String mensagem,
                                                                Map<String, String> campos) {
        return ResponseEntity.status(status).body(corpo(status, mensagem, campos));
    }
}
