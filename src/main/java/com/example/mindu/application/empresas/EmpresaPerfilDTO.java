package com.example.mindu.application.empresas;
import jakarta.validation.constraints.Email;
import lombok.Data;
@Data
public class EmpresaPerfilDTO {
    private String nomeFantasia;
    private String responsavel;
    @Email private String email;
    private String telefone;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
}
