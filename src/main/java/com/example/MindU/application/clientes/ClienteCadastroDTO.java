package com.example.mindu.application.clientes;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClienteCadastroDTO {

    @NotBlank
    private String nome;

    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String senha;

    @NotBlank
    private String empresaId;

    @NotBlank
    private String matricula; // código digitado pelo colaborador (RN04)
}