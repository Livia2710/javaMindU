package com.example.mindu.application.profissionais;
import com.example.mindu.domain.enums.TipoProfissional;
import com.example.mindu.domain.util.EmailNormalizer;
import jakarta.validation.constraints.Email;
import lombok.Data;
import java.util.List;
@Data
public class ProfissionalPerfilDTO {
    private String nome;
    @Email private String email;
    public void setEmail(String email) { this.email = EmailNormalizer.normalizar(email); }
    private String telefone;
    private String bio;
    private TipoProfissional tipo;
    private List<String> tags;
}
