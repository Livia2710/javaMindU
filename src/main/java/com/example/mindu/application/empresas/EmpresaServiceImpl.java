package com.example.mindu.application.empresas;

import com.example.mindu.domain.entity.Empresa;
import com.example.mindu.domain.entity.Matricula;
import com.example.mindu.domain.entity.Plano;
import com.example.mindu.domain.service.EmpresaService;
import com.example.mindu.infra.repository.EmpresaRepository;
import com.example.mindu.infra.repository.MatriculaRepository;
import com.example.mindu.infra.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository repository;
    private final PlanoRepository planoRepository;
    private final MatriculaRepository matriculaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Empresa cadastrar(Empresa empresa, String planoId) {
        // RN02 — CNPJ não pode se repetir
        if (repository.existsByCnpj(empresa.getCnpj())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ já cadastrado");
        }

        // RF05 — o plano escolhido precisa existir
        Plano plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano não encontrado"));

        empresa.setPlano(plano);
        empresa.setSenha(passwordEncoder.encode(empresa.getSenha())); // RNF01 — nunca salvar senha em texto puro
        return repository.save(empresa);
    }

    @Override
    @Transactional
    public Empresa atualizarPerfil(String id, EmpresaPerfilDTO dto) {
        Empresa e = buscarPorId(id);
        if (dto.getNomeFantasia()!=null) e.setNomeFantasia(dto.getNomeFantasia());
        if (dto.getResponsavel()!=null) e.setResponsavel(dto.getResponsavel());
        if (dto.getEmail()!=null) e.setEmail(dto.getEmail());
        if (dto.getTelefone()!=null) e.setTelefone(dto.getTelefone());
        if (dto.getCep()!=null) e.setCep(dto.getCep()); if (dto.getLogradouro()!=null) e.setLogradouro(dto.getLogradouro());
        if (dto.getNumero()!=null) e.setNumero(dto.getNumero()); if (dto.getComplemento()!=null) e.setComplemento(dto.getComplemento());
        if (dto.getBairro()!=null) e.setBairro(dto.getBairro()); if (dto.getCidade()!=null) e.setCidade(dto.getCidade()); if (dto.getEstado()!=null) e.setEstado(dto.getEstado());
        return repository.save(e);
    }

    @Override
    public Matricula validarMatricula(String codigo) {
        Matricula m = matriculaRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matrícula inválida"));
        if (m.isUtilizada()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula já utilizada");
        return m;
    }

    @Override
    public Empresa buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
    }

    @Override
    @Transactional
    public List<Matricula> gerarMatriculas(String empresaId, int quantidade) {
        Empresa empresa = buscarPorId(empresaId);

        // RN09 — soma o que já foi gerado antes + o que está sendo pedido agora,
        // e compara com o limite de vagas do plano contratado.
        long jaGeradas = matriculaRepository.countByEmpresaId(empresaId);
        if (jaGeradas + quantidade > empresa.getPlano().getVagasTotais()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limite de matrículas do plano excedido");
        }

        // RF06 — gera N códigos únicos, todos começando como "não utilizada"
        List<Matricula> novas = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            novas.add(Matricula.builder()
                    .empresa(empresa)
                    .codigo(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .utilizada(false)
                    .build());
        }

        // @Transactional garante que ou todas as matrículas são salvas, ou nenhuma
        return matriculaRepository.saveAll(novas);
    }
}