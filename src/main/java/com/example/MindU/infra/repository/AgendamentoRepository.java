package com.example.mindu.infra.repository;

import com.example.mindu.domain.entity.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, String> {
    // RN10 — busca os agendamentos do Profissional naquele dia, pra checar conflito de horário
    List<Agendamento> findByProfissionalIdAndDataHoraBetween(
            String profissionalId, LocalDateTime inicio, LocalDateTime fim);

}