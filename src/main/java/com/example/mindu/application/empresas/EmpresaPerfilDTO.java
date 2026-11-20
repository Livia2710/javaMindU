package com.example.mindu.application.empresas;
import jakarta.validation.constraints.Email;
import com.example.mindu.domain.util.EmailNormalizer;
import lombok.Data;
@Data
public class EmpresaPerfilDTO {
    private String nomeFantasia;
    private String responsavel;
    @Email private String email;
    public void setEmail(String email) { this.email = EmailNormalizer.normalizar(email); }
    private String telefone;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
}
