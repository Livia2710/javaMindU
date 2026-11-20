package com.example.mindu.application.profissionais;

import com.example.mindu.domain.enums.TipoProfissional;
import com.example.mindu.domain.util.EmailNormalizer;
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

    public void setEmail(String email) {
        this.email = EmailNormalizer.normalizar(email);
    }
    @NotBlank
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    private String senha;
    private TipoProfissional tipo;
    private List<String> tags;
}
