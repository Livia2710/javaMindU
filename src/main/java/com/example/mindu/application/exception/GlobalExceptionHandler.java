package com.example.mindu.application.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> conflito(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", "Dado já cadastrado (e-mail, CNPJ ou matrícula duplicados)"));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, String>> milaIndisponivel(ResourceAccessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("erro", "Não foi possível conectar à Mila. Tente novamente em instantes."));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, String>> erroDaMila(RestClientResponseException ex) {
        org.springframework.http.HttpStatusCode status = ex.getStatusCode().is4xxClientError()
                ? ex.getStatusCode() : HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status)
                .body(Map.of("erro", "A Mila não conseguiu processar a solicitação.", "statusMila", Integer.toString(ex.getStatusCode().value())));
    }
}
