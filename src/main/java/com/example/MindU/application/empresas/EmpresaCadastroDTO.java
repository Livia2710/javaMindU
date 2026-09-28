package com.example.mindu.application.empresas;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpresaCadastroDTO {

    @NotBlank
    private String razaoSocial;
    @NotBlank
    private String nomeFantasia;
    private String inscricaoEstadual; // opcional, sem @NotBlank

    @NotBlank
    private String cnpj;
    @NotBlank
    private String responsavel;

    // Endereço já vem preenchido pelo front (via ViaCEP) — o backend só valida
    // que não chegou vazio, não consulta CEP nenhum aqui.
    @NotBlank
    private String cep;
    @NotBlank
    private String logradouro;
    private String numero;
    private String complemento;
    @NotBlank
    private String bairro;
    @NotBlank
    private String cidade;
    @NotBlank
    private String estado;

    @NotBlank
    @Email
    private String email;
    private String telefone;

    @NotBlank
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    private String senha; // chega em texto puro, o Service que faz o BCrypt

    @NotBlank
    private String planoId; // RF05 — Empresa escolhe o plano no cadastro
}