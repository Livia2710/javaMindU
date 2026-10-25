package com.example.mindu.application.clientes;
import jakarta.validation.constraints.Email;
import lombok.Data;
@Data
public class ClientePerfilDTO {
    private String nome;
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
