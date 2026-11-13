package com.example.mindu.application.contas;

import com.example.mindu.domain.entity.*;
import com.example.mindu.infra.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SolicitacaoExclusaoService {
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AdminRepository adminRepository;
    private final SolicitacaoExclusaoContaRepository requestRepository;

    @Transactional
    public SolicitacaoExclusaoDTO solicitar(String email) {
        Usuario usuario = buscarPorEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
        if (usuario instanceof Admin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solicitações de exclusão de contas administrativas devem ser tratadas pelo suporte");
        }
        var aberta = requestRepository.findFirstByUsuarioIdAndStatusInOrderBySolicitadaEmDesc(
                usuario.getId(), List.of(SolicitacaoExclusaoStatus.PENDENTE, SolicitacaoExclusaoStatus.EM_ANALISE));
        if (aberta.isPresent()) return SolicitacaoExclusaoDTO.from(aberta.get());

        SolicitacaoExclusaoConta request = new SolicitacaoExclusaoConta();
        request.setUsuarioId(usuario.getId());
        request.setEmail(usuario.getEmail());
        request.setTipoConta(tipoConta(usuario));
        request.setSolicitadaEm(LocalDateTime.now());
        usuario.setAtivo(false);
        usuario.setTokenVersion(usuario.getTokenVersion() + 1);
        salvar(usuario);
        return SolicitacaoExclusaoDTO.from(requestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoExclusaoDTO> listar() {
        return requestRepository.findAll().stream().map(SolicitacaoExclusaoDTO::from).toList();
    }

    @Transactional
    public SolicitacaoExclusaoDTO atualizar(String requestId, SolicitacaoExclusaoStatus status) {
        SolicitacaoExclusaoConta request = requestRepository.findById(requestId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
        request.setStatus(status);
        if (status == SolicitacaoExclusaoStatus.RECUSADA) {
            buscarPorEmail(request.getEmail()).ifPresent(usuario -> {
                usuario.setAtivo(true);
                usuario.setTokenVersion(usuario.getTokenVersion() + 1);
                salvar(usuario);
            });
        }
        return SolicitacaoExclusaoDTO.from(requestRepository.save(request));
    }

    private Optional<Usuario> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email).map(Usuario.class::cast)
                .or(() -> empresaRepository.findByEmail(email).map(Usuario.class::cast))
                .or(() -> profissionalRepository.findByEmail(email).map(Usuario.class::cast))
                .or(() -> adminRepository.findByEmail(email).map(Usuario.class::cast));
    }

    private String tipoConta(Usuario usuario) {
        if (usuario instanceof Cliente) return "CLIENTE";
        if (usuario instanceof Empresa) return "EMPRESA";
        if (usuario instanceof Profissional) return "PROFISSIONAL";
        return "ADMIN";
    }

    private void salvar(Usuario usuario) {
        if (usuario instanceof Cliente cliente) clienteRepository.save(cliente);
        else if (usuario instanceof Empresa empresa) empresaRepository.save(empresa);
        else if (usuario instanceof Profissional profissional) profissionalRepository.save(profissional);
        else if (usuario instanceof Admin admin) adminRepository.save(admin);
    }
}
