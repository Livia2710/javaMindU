package com.example.mindu.domain.service;

import com.example.mindu.application.clientes.DadosSensiveisDTO;
import com.example.mindu.domain.entity.Cliente;
import com.example.mindu.domain.enums.StatusCadastroCliente;

import java.util.List;

public interface ClienteService {
    Cliente cadastrar(String nome, String email, String senha, String empresaId, String codigoMatricula); // RF01, RF02
    Cliente aprovar(String clienteId, boolean aprovado); // RF03
    Cliente completarCadastro(String clienteId, DadosSensiveisDTO dto); // RF04, RN05
    Cliente buscarPorId(String id);
    List<Cliente> listarPorEmpresa(String empresaId, StatusCadastroCliente status);
}