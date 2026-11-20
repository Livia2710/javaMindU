package com.example.mindu.application.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.example.mindu.domain.util.EmailNormalizer;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank
    @Email
    private String email;

    public void setEmail(String email) {
        this.email = EmailNormalizer.normalizar(email);
    }

    @NotBlank
    private String senha;
}
