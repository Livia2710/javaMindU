package com.example.mindu.application.auth;

import com.example.mindu.domain.util.EmailNormalizer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SolicitarRecuperacaoSenhaDTO {

    @NotBlank
    @Email
    private String email;

    public void setEmail(String email) {
        this.email = EmailNormalizer.normalizar(email);
    }
}
