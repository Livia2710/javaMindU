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
import java.util.ArrayList;

@Entity
@Table
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Profissional extends Usuario{

    @Enumerated(EnumType.STRING)
    private TipoProfissional tipo;

    @Column(length = 2000)
    private String bio;

    @ElementCollection
    @CollectionTable(name = "profissional_tags", joinColumns = @JoinColumn(name = "profissional_id"))
    @Column(name = "tag")
    private List<String> tags;

    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    private Genero genero;

    private String cep;

    @Column(columnDefinition = "bytea")
    private byte[] foto;

    private String fotoContentType;

    @Column(columnDefinition = "bytea")
    private byte[] fotoBanner;

    private String fotoBannerContentType;

    @Column(columnDefinition = "bytea")
    private byte[] fotoCarterinha;

    @Enumerated(EnumType.STRING)
    private StatusVerificacao status;

    @OneToMany(mappedBy = "profissional", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.Builder.Default
    private List<LocalAtendimento> locaisAtendimento = new ArrayList<>();

    @OneToMany(mappedBy = "profissional", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.Builder.Default
    private List<Disponibilidade> disponibilidades = new ArrayList<>();

}
