package com.example.mindu.application.empresas;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private BigDecimal precoMensalPorColaborador;
    private Boolean consultasSemLimite;
    private BigDecimal valorMensalEstimado;
    private LocalDate inicioVigenciaPlano;
    private LocalDate fimVigenciaPlano;
    private Integer diaPagamento;
}
