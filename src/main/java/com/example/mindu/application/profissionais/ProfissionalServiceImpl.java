package com.example.mindu.application.profissionais;

import com.example.mindu.domain.entity.Disponibilidade;
import com.example.mindu.domain.entity.LocalAtendimento;
import com.example.mindu.domain.entity.Profissional;
import com.example.mindu.domain.enums.Modalidade;
import com.example.mindu.domain.enums.StatusVerificacao;
import com.example.mindu.domain.service.ProfissionalService;
import com.example.mindu.infra.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfissionalServiceImpl implements ProfissionalService {

    private final ProfissionalRepository repository;
    private final ProfissionalMapper mapper;
    private final PasswordEncoder passwordEncoder;

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
    public Profissional atualizarPerfil(String id, byte[] foto, byte[] fotoBanner) {
        Profissional profissional = buscarPorId(id);

        // Só atualiza o que veio preenchido — permite trocar só a foto sem
        // mexer no banner, e vice-versa.
        if (foto != null) {
            profissional.setFoto(foto);
        }
        if (fotoBanner != null) {
            profissional.setFotoBanner(fotoBanner);
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
}