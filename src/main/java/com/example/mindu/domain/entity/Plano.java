package com.example.mindu.domain.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plano {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Integer vagasTotais;

    @Column(nullable = false)
    private String porteIndicado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoMensalPorColaborador;

    @Column(nullable = false)
    private Boolean consultasSemLimite;
}
