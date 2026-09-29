package com.example.mindu.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Empresa extends Usuario {

    @Column(nullable = false, unique = true)
    private String razaoSocial;

    @Column(nullable = false)
    private String nomeFantasia;

    private String inscricaoEstadual;

    @Column(nullable = false, unique = true)
    private String cnpj;

    @Column(nullable = false)
    private String responsavel;

    @Column(nullable = false)
    private String cep;

    @Column(nullable = false)
    private String logradouro;

    private String numero;
    private String complemento;

    @Column(nullable = false)
    private String bairro;

    @Column(nullable = false)
    private String cidade;

    @Column(nullable = false)
    private String estado;

    @ManyToOne
    @JoinColumn(name = "plano_id")
    private Plano plano;
}
