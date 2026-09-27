package com.example.mindu.application.agendamentos;

import com.example.mindu.domain.enums.StatusAgendamento;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AgendamentoDTO {
    private String id;
    private String clienteNome;
    private String profissionalNome;
    private LocalDateTime dataHora;
    private StatusAgendamento status;
}