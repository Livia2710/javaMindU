package com.example.mindu.domain.entity;

import com.example.mindu.domain.enums.Genero;
import com.example.mindu.domain.enums.StatusCadastroCliente;
import com.example.mindu.infra.security.CnsConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Cliente extends Usuario{

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @OneToOne
    @JoinColumn(name = "matricula_id", unique = true)
    private Matricula matricula;

    @Convert(converter = CnsConverter.class)
    @Column(name = "cns")
    private String cns;

    private LocalDate dataNascimento;

    private String nomeMae;

    @Enumerated(EnumType.STRING)
    private Genero genero;

    @Enumerated(EnumType.STRING)
    private StatusCadastroCliente status;

}
