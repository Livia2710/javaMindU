package com.example.mindu.application.profissionais;

import com.example.mindu.domain.enums.TipoProfissional;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

// Não é @RequestBody (o cadastro é multipart/form-data), então não recebe os
// arquivos aqui — eles chegam separados, como MultipartFile, no Controller.
@Data
public class ProfissionalCadastroDTO {

    @NotBlank
    private String nome;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    private String senha;
    @NotNull
    private TipoProfissional tipo;
    @NotEmpty
    private List<String> tags;
}