package com.example.mindu.domain.entity;

import com.example.mindu.domain.enums.Modalidade;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional profissional;;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidade modalidade;

    // endereço só faz sentido se modalidade != VIRTUAL — validar isso no Service, não aqui
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;

}
