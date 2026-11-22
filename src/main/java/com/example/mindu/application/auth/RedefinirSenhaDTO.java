package com.example.mindu.application.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RedefinirSenhaDTO {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8)
    private String novaSenha;
}
