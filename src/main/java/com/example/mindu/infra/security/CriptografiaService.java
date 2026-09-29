package com.example.mindu.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CriptografiaService {

    @Value("${mindu.criptografia.chave}")
    private String chave;

    private static final int TAMANHO_IV = 12;
    private static final int TAMANHO_TAG = 128;

    public String criptografar(String texto) {
        try {
            byte[] iv = new byte[TAMANHO_IV];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(chave.getBytes(), "AES"),
                    new GCMParameterSpec(TAMANHO_TAG, iv));

            byte[] encriptado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encriptado.length);
            buffer.put(iv).put(encriptado);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar", e);
        }
    }

    public String descriptografar(String textoCriptografado) {
        try {
            byte[] dados = Base64.getDecoder().decode(textoCriptografado);
            ByteBuffer buffer = ByteBuffer.wrap(dados);

            byte[] iv = new byte[TAMANHO_IV];
            buffer.get(iv);
            byte[] encriptado = new byte[buffer.remaining()];
            buffer.get(encriptado);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(chave.getBytes(), "AES"),
                    new GCMParameterSpec(TAMANHO_TAG, iv));

            return new String(cipher.doFinal(encriptado), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar", e);
        }
    }
}