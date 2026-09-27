package com.example.mindu.application.clientes;

import com.example.mindu.domain.entity.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    // Note que empresa, matricula e status NÃO são setados aqui — dependem de
    // busca no banco (RN04) e de regra de negócio (RF03), então ficam por
    // conta do Service, não do mapper.
    public Cliente toEntity(ClienteCadastroDTO dto) {
        return Cliente.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(dto.getSenha()) // ainda texto puro — Service faz o encode
                .build();
    }

    public ClienteDTO toDTO(Cliente cliente) {
        return ClienteDTO.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .email(cliente.getEmail())
                .empresaId(cliente.getEmpresa() != null ? cliente.getEmpresa().getId() : null)
                .status(cliente.getStatus())
                .build();
    }
}