package com.example.mindu.application.agendamentos;

import com.example.mindu.domain.entity.Agendamento;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgendamentoMapper {

    // Não monta a entidade aqui — cliente/profissional/validações vêm do
    // Service, porque precisam de busca no banco e regra de negócio (RN10).
    // O Mapper só cuida da tradução de saída (Entity → DTO).
    public AgendamentoDTO toDTO(Agendamento agendamento) {
        return AgendamentoDTO.builder()
                .id(agendamento.getId())
                .clienteNome(agendamento.getCliente().getNome())
                .profissionalNome(agendamento.getProfissional().getNome())
                .dataHora(agendamento.getDataHora())
                .status(agendamento.getStatus())
                .build();
    }

    public List<AgendamentoDTO> toDTOList(List<Agendamento> agendamentos) {
        return agendamentos.stream().map(this::toDTO).toList();
    }
}