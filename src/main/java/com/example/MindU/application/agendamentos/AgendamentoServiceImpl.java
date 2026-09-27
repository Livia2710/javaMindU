package com.example.mindu.application.agendamentos;

import com.example.mindu.domain.entity.Agendamento;
import com.example.mindu.domain.entity.Cliente;
import com.example.mindu.domain.entity.Profissional;
import com.example.mindu.domain.enums.DiaSemana;
import com.example.mindu.domain.enums.StatusAgendamento;
import com.example.mindu.domain.enums.StatusVerificacao;
import com.example.mindu.domain.service.AgendamentoService;
import com.example.mindu.infra.repository.AgendamentoRepository;
import com.example.mindu.infra.repository.ClienteRepository;
import com.example.mindu.infra.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoServiceImpl implements AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    @Override
    @Transactional
    public Agendamento agendar(String clienteId, String profissionalId, LocalDateTime dataHora) {

        // Validação 1 (RN10) — profissional existe
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado"));

        // Validação 2 (RN10) — não dá pra marcar consulta com quem não foi aprovado pelo Admin
        if (profissional.getStatus() != StatusVerificacao.APROVADO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Profissional não está aprovado");
        }

        // Validação 3 (RN10) — o horário pedido precisa estar dentro de algum
        // bloco de disponibilidade que o Profissional cadastrou (módulo 12/RF09)
        boolean dentroDaDisponibilidade = profissional.getDisponibilidades().stream()
                .anyMatch(d -> d.getDiaSemana() == diaSemanaDe(dataHora)
                        && !dataHora.toLocalTime().isBefore(d.getHoraInicio())
                        && !dataHora.toLocalTime().isAfter(d.getHoraFim()));

        if (!dentroDaDisponibilidade) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Horário fora da disponibilidade do profissional");
        }

        // Validação 4 (RN10) — não pode ter outro agendamento ativo no mesmo
        // horário exato. Busca só os agendamentos DAQUELE dia (mais barato que
        // trazer o histórico inteiro do Profissional) e filtra em memória.
        LocalDateTime inicioDoDia = dataHora.toLocalDate().atStartOfDay();
        LocalDateTime fimDoDia = inicioDoDia.plusDays(1);
        List<Agendamento> agendamentosDoDia = agendamentoRepository
                .findByProfissionalIdAndDataHoraBetween(profissionalId, inicioDoDia, fimDoDia);

        boolean conflito = agendamentosDoDia.stream()
                .anyMatch(a -> a.getStatus() != StatusAgendamento.CANCELADO
                        && a.getDataHora().equals(dataHora));

        if (conflito) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário já ocupado");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        Agendamento agendamento = Agendamento.builder()
                .cliente(cliente)
                .profissional(profissional)
                .dataHora(dataHora)
                .status(StatusAgendamento.AGENDADO)
                .build();

        return agendamentoRepository.save(agendamento);
    }

    @Override
    @Transactional
    public Agendamento cancelar(String agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        return agendamentoRepository.save(agendamento);
    }

    // java.time.DayOfWeek (SEGUNDA-FEIRA em inglês, MONDAY...) não é o mesmo
    // enum que o seu DiaSemana (que você criou em português) — esse método
    // faz a ponte entre os dois.
    private DiaSemana diaSemanaDe(LocalDateTime dataHora) {
        DayOfWeek dia = dataHora.getDayOfWeek();
        return switch (dia) {
            case MONDAY -> DiaSemana.SEGUNDA;
            case TUESDAY -> DiaSemana.TERCA;
            case WEDNESDAY -> DiaSemana.QUARTA;
            case THURSDAY -> DiaSemana.QUINTA;
            case FRIDAY -> DiaSemana.SEXTA;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }
}