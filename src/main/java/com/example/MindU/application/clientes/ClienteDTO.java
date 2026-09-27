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
    private String empresaId;
    private StatusCadastroCliente status;
}