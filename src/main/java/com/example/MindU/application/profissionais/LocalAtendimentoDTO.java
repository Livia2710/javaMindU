package com.example.mindu.application.profissionais;

import com.example.mindu.domain.enums.Modalidade;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocalAtendimentoDTO {

    @NotNull
    private Modalidade modalidade;

    // Sem @NotBlank — só é obrigatório quando modalidade != VIRTUAL,
    // validação feita no Service (RF08), não dá pra fazer isso na anotação.
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
}