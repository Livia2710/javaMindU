package com.example.mindu.application.empresas;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmpresaDTO {
    private String id;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String email;
    private String telefone;
    private String responsavel;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String plano;
    private int matriculasUsadas;
    private int matriculasDisponiveis;
}
