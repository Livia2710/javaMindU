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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoServiceImpl implements AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    @Value("${mindu.agendamento.duracao-minutos:50}")
    private int duracaoConsultaMinutos;

    @Value("${mindu.agendamento.fuso-horario:America/Sao_Paulo}")
    private String fusoHorario;

    @Override
    @Transactional
    public Agendamento agendar(String clienteId, String profissionalId, LocalDateTime dataHora) {

        // Validação 1 (RN10) — profissional existe
        Profissional profissional = profissionalRepository.findByIdForUpdate(profissionalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado"));

        validarHorario(profissional, dataHora, null);

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
        if (cliente.getStatus() != com.example.mindu.domain.enums.StatusCadastroCliente.APROVADO)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cliente ainda não foi aprovado pela empresa");

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
    public Agendamento reagendar(String agendamentoId, LocalDateTime novaDataHora) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));
        if (agendamento.getStatus() == StatusAgendamento.CANCELADO)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agendamento cancelado não pode ser reagendado");

        Profissional profissional = profissionalRepository.findByIdForUpdate(agendamento.getProfissional().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado"));
        validarHorario(profissional, novaDataHora, agendamentoId);
        agendamento.setDataHora(novaDataHora);
        return agendamentoRepository.save(agendamento);
    }

    private void validarHorario(Profissional profissional, LocalDateTime dataHora, String agendamentoIgnorado) {
        if (!dataHora.isAfter(LocalDateTime.now(ZoneId.of(fusoHorario))))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O horário precisa estar no futuro");
        if (profissional.getStatus() != StatusVerificacao.APROVADO)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Profissional não está aprovado");

        boolean dentroDaDisponibilidade = profissional.getDisponibilidades().stream()
                .anyMatch(d -> d.getDiaSemana() == diaSemanaDe(dataHora)
                        && !dataHora.toLocalTime().isBefore(d.getHoraInicio())
                        && !dataHora.toLocalTime().plusMinutes(duracaoConsultaMinutos).isAfter(d.getHoraFim())
                        && Duration.between(d.getHoraInicio(), dataHora.toLocalTime()).getSeconds()
                                % (duracaoConsultaMinutos * 60L) == 0
                        && dataHora.getSecond() == 0 && dataHora.getNano() == 0);
        if (!dentroDaDisponibilidade)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Horário fora da disponibilidade ou do intervalo de agendamento");

        LocalDateTime inicioDoDia = dataHora.toLocalDate().atStartOfDay();
        List<Agendamento> agendamentosDoDia = agendamentoRepository.findByProfissionalIdAndDataHoraBetween(
                profissional.getId(), inicioDoDia, inicioDoDia.plusDays(1));
        boolean conflito = agendamentosDoDia.stream()
                .filter(a -> !a.getId().equals(agendamentoIgnorado))
                .anyMatch(a -> a.getStatus() != StatusAgendamento.CANCELADO
                        && dataHora.isBefore(a.getDataHora().plusMinutes(duracaoConsultaMinutos))
                        && dataHora.plusMinutes(duracaoConsultaMinutos).isAfter(a.getDataHora()));
        if (conflito) throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário já ocupado");
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
