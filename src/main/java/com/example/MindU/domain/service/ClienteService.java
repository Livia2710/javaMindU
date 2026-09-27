package com.example.mindu.domain.service;

import com.example.mindu.application.clientes.DadosSensiveisDTO;
import com.example.mindu.domain.entity.Cliente;

public interface ClienteService {
    Cliente cadastrar(String nome, String email, String senha, String empresaId, String codigoMatricula); // RF01, RF02
    Cliente aprovar(String clienteId, boolean aprovado); // RF03
    Cliente completarCadastro(String clienteId, DadosSensiveisDTO dto); // RF04, RN05
    Cliente buscarPorId(String id);
}