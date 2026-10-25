package com.example.mindu.application.clientes;

import com.example.mindu.domain.enums.StatusCadastroCliente;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClienteDTO {
    private String id;
    private String nome;
    private String email;
    private String telefone;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String empresaId;
    private StatusCadastroCliente status;
}