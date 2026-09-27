package com.example.mindu.infra.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CriptografiaService {

    // 16 caracteres = AES-128. Depois mova pro application.yml (@Value), nunca
    // deixe uma chave real hardcoded — por enquanto, "funciona primeiro".
    private static final String CHAVE = "0123456789abcdef";

    public String criptografar(String texto) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(CHAVE.getBytes(), "AES"));
            byte[] encriptado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encriptado); // vira String legível pra salvar no banco
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar", e);
        }
    }

    public String descriptografar(String textoCriptografado) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(CHAVE.getBytes(), "AES"));
            byte[] decodificado = Base64.getDecoder().decode(textoCriptografado);
            return new String(cipher.doFinal(decodificado), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar", e);
        }
    }
}