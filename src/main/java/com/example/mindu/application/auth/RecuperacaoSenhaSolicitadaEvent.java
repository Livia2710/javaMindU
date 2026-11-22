package com.example.mindu.application.auth;

/** Evento publicado após criar o token; o envio ocorre somente depois do commit. */
public record RecuperacaoSenhaSolicitadaEvent(String destinatario, String link, String tokenId, String usuarioId) {
}
