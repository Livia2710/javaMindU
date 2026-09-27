package com.example.mindu.infra.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {

    // Chave usada pra ASSINAR o token — não criptografa o conteúdo, garante que
    // ninguém alterou o token sem você perceber (é uma "assinatura", não um "cadeado").
    private static final String SECRET = "coloque-uma-chave-bem-grande-e-secreta-aqui-depois-mova-para-o-yml";

    // RN08: token expira em 60 minutos (60 * 60 * 1000 ms = 1h)
    private static final long EXPIRACAO_MS = 60 * 60 * 1000;

    // Converte a String SECRET numa SecretKey no formato que a lib jjwt exige
    // pra assinar/verificar com o algoritmo HMAC-SHA.
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // Monta o token: quem é o dono (subject = email), quando foi emitido,
    // quando expira, e assina tudo isso com a chave.
    public String gerarToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACAO_MS))
                .signWith(getKey())
                .compact(); // "compact" = serializa tudo numa única String (o token final)
    }

    // Faz o caminho inverso: pega o token, verifica a assinatura (se alguém mexeu
    // no token, a verificação falha e lança exceção), e devolve o email de dentro dele.
    public String extrairEmail(String token) {
        return Jwts.parser().verifyWith(getKey()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    // Só checa se o token é válido (assinatura correta + não expirado), sem
    // precisar do email — usado pelo JwtFilter antes de confiar no token.
    public boolean tokenValido(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException cobre: assinatura inválida, token expirado, malformado...
            // IllegalArgumentException cobre: token vazio/null.
            return false;
        }
    }
}