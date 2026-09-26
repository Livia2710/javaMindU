package com.example.mindu.domain.entity;

import com.example.mindu.domain.enums.Genero;
import com.example.mindu.domain.enums.StatusVerificacao;
import com.example.mindu.domain.enums.TipoProfissional;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Profissional extends Usuario{

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoProfissional tipo;

    @ElementCollection
    @CollectionTable(name = "profissional_tags", joinColumns = @JoinColumn(name = "profissional_id"))
    @Column(name = "tag")
    private List<String> tags;

    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    private Genero genero;

    private String cep;

    @Lob
    private byte[] foto;

    @Lob
    private byte[] fotoBanner;

    @Lob
    @Column(nullable = false)
    private byte[] fotoCarterinha;

    @Enumerated(EnumType.STRING)
    private StatusVerificacao status;

    @OneToMany(mappedBy = "profissional", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocalAtendimento> locaisAtendimento;

    @OneToMany(mappedBy = "profissional", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Disponibilidade> disponibilidades;

}
