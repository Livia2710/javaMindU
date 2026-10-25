package com.example.mindu.domain.service;

import com.example.mindu.domain.entity.Agendamento;

import java.time.LocalDateTime;

public interface AgendamentoService {
    Agendamento agendar(String clienteId, String profissionalId, LocalDateTime dataHora, com.example.mindu.domain.enums.Modalidade modalidade); // RF14, RN10
    Agendamento cancelar(String agendamentoId); // complementa o fluxo, não é RF explícito ainda
    Agendamento reagendar(String agendamentoId, LocalDateTime novaDataHora);
}
