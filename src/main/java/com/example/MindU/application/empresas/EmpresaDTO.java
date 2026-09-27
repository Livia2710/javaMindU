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
    private String plano;
    private int matriculasUsadas;
    private int matriculasDisponiveis;
}
