package com.example.mindu.application.profissionais;

import com.example.mindu.domain.entity.Disponibilidade;
import com.example.mindu.domain.entity.LocalAtendimento;
import com.example.mindu.domain.entity.Profissional;
import com.example.mindu.domain.entity.Agendamento;
import com.example.mindu.domain.enums.Modalidade;
import com.example.mindu.domain.enums.StatusVerificacao;
import com.example.mindu.domain.service.ProfissionalService;
import com.example.mindu.infra.repository.ProfissionalRepository;
import com.example.mindu.infra.repository.AgendamentoRepository;
import com.example.mindu.domain.enums.DiaSemana;
import com.example.mindu.domain.enums.StatusAgendamento;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ProfissionalServiceImpl implements ProfissionalService {

    private final ProfissionalRepository repository;
    private final ProfissionalMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final AgendamentoRepository agendamentoRepository;

    @Value("${mindu.agendamento.duracao-minutos:50}")
    private int duracaoConsultaMinutos;

    @Value("${mindu.agendamento.fuso-horario:America/Sao_Paulo}")
    private String fusoHorario;

    @Override
    @Transactional
    public Profissional cadastrar(Profissional profissional) {
        // RF07 — e-mail não pode duplicar (o unique=true da coluna já garantiria,
        // mas isso dá um 409 amigável em vez de estourar erro cru do banco)
        if (repository.existsByEmail(profissional.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        profissional.setSenha(passwordEncoder.encode(profissional.getSenha())); // RNF01
        profissional.setStatus(StatusVerificacao.PENDENTE); // RF10 — nasce pendente de aprovação
        return repository.save(profissional);
    }

    @Override
    @Transactional
    public Profissional aprovar(String id, boolean aprovado) {
        Profissional p = buscarPorId(id);
        p.setStatus(aprovado ? StatusVerificacao.APROVADO : StatusVerificacao.REJEITADO); // RF16
        return repository.save(p);
    }

    @Override
    public Profissional buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado"));
    }

    @Override
    @Transactional
    public LocalAtendimentoDTO adicionarLocal(String profissionalId, LocalAtendimentoDTO dto) {
        Profissional profissional = buscarPorId(profissionalId);

        // RF08 — endereço só é obrigatório quando NÃO for atendimento virtual
        if (dto.getModalidade() != Modalidade.VIRTUAL) {
            if (dto.getCep() == null || dto.getLogradouro() == null || dto.getCidade() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Endereço é obrigatório para atendimento presencial ou híbrido");
            }
        }

        LocalAtendimento local = mapper.toEntity(dto, profissional);

        // Graças ao cascade=ALL no Profissional.locaisAtendimento, adicionar na
        // lista e salvar o Profissional já persiste o novo LocalAtendimento junto.
        profissional.getLocaisAtendimento().add(local);
        repository.save(profissional);

        return mapper.toDTO(local);
    }

    @Override
    @Transactional
    public List<DisponibilidadeDTO> definirDisponibilidade(String profissionalId, List<DisponibilidadeDTO> dtos) {
        Profissional profissional = buscarPorId(profissionalId);

        for (int i = 0; i < dtos.size(); i++) {
            DisponibilidadeDTO janela = dtos.get(i);
            if (!janela.getHoraInicio().isBefore(janela.getHoraFim()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "horaInicio precisa ser anterior a horaFim");
            for (int j = i + 1; j < dtos.size(); j++) {
                DisponibilidadeDTO outra = dtos.get(j);
                boolean mesmoDia = janela.getDiaSemana() == outra.getDiaSemana();
                boolean sobreposta = janela.getHoraInicio().isBefore(outra.getHoraFim())
                        && janela.getHoraFim().isAfter(outra.getHoraInicio());
                if (mesmoDia && sobreposta)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Janelas de disponibilidade do mesmo dia não podem se sobrepor");
            }
        }

        // RF09 — "substituir tudo": limpa a lista antiga (orphanRemoval apaga do
        // banco os que saíram) e recria do zero com o que veio no request.
        profissional.getDisponibilidades().clear();

        List<Disponibilidade> novas = dtos.stream()
                .map(dto -> mapper.toEntity(dto, profissional))
                .toList();
        profissional.getDisponibilidades().addAll(novas);

        repository.save(profissional);

        return novas.stream().map(mapper::toDTO).toList();
    }

    @Override
    @Transactional
    public Profissional atualizarPerfil(String id, byte[] foto, String fotoContentType,
                                        byte[] fotoBanner, String bannerContentType) {
        Profissional profissional = buscarPorId(id);

        // Só atualiza o que veio preenchido — permite trocar só a foto sem
        // mexer no banner, e vice-versa.
        if (foto != null) {
            profissional.setFoto(foto);
            profissional.setFotoContentType(fotoContentType);
        }
        if (fotoBanner != null) {
            profissional.setFotoBanner(fotoBanner);
            profissional.setFotoBannerContentType(bannerContentType);
        }

        return repository.save(profissional);
    }

    @Override
    public List<Profissional> pesquisar(String nome, String especialidade, Modalidade modalidade) {
        return repository.search(nome, especialidade, modalidade); // RF13, RN06
    }

    public List<Profissional> listarPendentes() {
        return repository.findByStatus(StatusVerificacao.PENDENTE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> horariosDisponiveis(String profissionalId, LocalDate data) {
        Profissional profissional = buscarPorId(profissionalId);
        if (profissional.getStatus() != StatusVerificacao.APROVADO)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado");

        DiaSemana dia = diaSemana(data.getDayOfWeek());
        List<Agendamento> agendamentos = agendamentoRepository.findByProfissionalIdAndDataHoraBetween(
                profissionalId, data.atStartOfDay(), data.plusDays(1).atStartOfDay());
        LocalDateTime agora = LocalDateTime.now(ZoneId.of(fusoHorario));
        List<LocalDateTime> horarios = new java.util.ArrayList<>();
        for (Disponibilidade janela : profissional.getDisponibilidades()) {
            if (janela.getDiaSemana() != dia) continue;
            LocalTime inicio = janela.getHoraInicio();
            while (!inicio.plusMinutes(duracaoConsultaMinutos).isAfter(janela.getHoraFim())) {
                LocalDateTime inicioConsulta = LocalDateTime.of(data, inicio);
                LocalDateTime fimConsulta = inicioConsulta.plusMinutes(duracaoConsultaMinutos);
                boolean ocupado = agendamentos.stream().anyMatch(a -> a.getStatus() != StatusAgendamento.CANCELADO
                        && inicioConsulta.isBefore(a.getDataHora().plusMinutes(duracaoConsultaMinutos))
                        && fimConsulta.isAfter(a.getDataHora()));
                if (!ocupado && inicioConsulta.isAfter(agora)) horarios.add(inicioConsulta);
                inicio = inicio.plusMinutes(duracaoConsultaMinutos);
            }
        }
        return horarios.stream().distinct().sorted().toList();
    }

    private DiaSemana diaSemana(DayOfWeek dia) {
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
