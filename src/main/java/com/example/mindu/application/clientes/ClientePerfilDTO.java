package com.example.mindu.application.clientes;
import jakarta.validation.constraints.Email;
import com.example.mindu.domain.util.EmailNormalizer;
import lombok.Data;
@Data
public class ClientePerfilDTO {
    private String nome;
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
