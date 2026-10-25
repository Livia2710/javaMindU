package com.example.mindu.application.profissionais;
import com.example.mindu.domain.enums.TipoProfissional;
import jakarta.validation.constraints.Email;
import lombok.Data;
import java.util.List;
@Data
public class ProfissionalPerfilDTO {
    private String nome;
    @Email private String email;
    private String telefone;
    private String bio;
    private TipoProfissional tipo;
    private List<String> tags;
}
