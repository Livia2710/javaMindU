package com.example.mindu.infra.security;

import com.example.mindu.domain.entity.Agendamento;
import com.example.mindu.domain.entity.Cliente;
import com.example.mindu.domain.util.EmailNormalizer;
import com.example.mindu.infra.repository.AgendamentoRepository;
import com.example.mindu.infra.repository.ClienteRepository;
import com.example.mindu.infra.repository.EmpresaRepository;
import com.example.mindu.infra.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class ResourceAccessService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AgendamentoRepository agendamentoRepository;

    public String empresaIdDoUsuario(String email) {
        email = EmailNormalizer.normalizar(email);
        return empresaRepository.findByEmail(email).map(e -> e.getId())
                .orElseThrow(() -> proibido("Conta de empresa não encontrada"));
    }

    public String clienteIdDoUsuario(String email) {
        email = EmailNormalizer.normalizar(email);
        return clienteRepository.findByEmail(email).map(c -> c.getId())
                .orElseThrow(() -> proibido("Conta de cliente não encontrada"));
    }

    public String profissionalIdDoUsuario(String email) {
        email = EmailNormalizer.normalizar(email);
        return profissionalRepository.findByEmail(email).map(p -> p.getId())
                .orElseThrow(() -> proibido("Conta de profissional não encontrada"));
    }

    public void exigirClienteDaConta(String clienteId, String email) {
        exigirClienteDono(clienteId, email);
    }

    public void exigirEmpresaDona(String empresaId, String email) {
        if (isAdmin()) return;
        email = EmailNormalizer.normalizar(email);
        var empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
        if (!EmailNormalizer.normalizar(empresa.getEmail()).equals(email)) throw proibido("Acesso permitido apenas à própria empresa");
    }

    public void exigirProfissionalDono(String profissionalId, String email) {
        if (isAdmin()) return;
        email = EmailNormalizer.normalizar(email);
        var profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado"));
        if (!EmailNormalizer.normalizar(profissional.getEmail()).equals(email)) throw proibido("Acesso permitido apenas ao próprio perfil");
    }

    public void exigirClienteDono(String clienteId, String email) {
        if (isAdmin()) return;
        email = EmailNormalizer.normalizar(email);
        var cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
        if (!EmailNormalizer.normalizar(cliente.getEmail()).equals(email)) throw proibido("Acesso permitido apenas ao próprio cadastro");
    }

    public void exigirEmpresaDonaDoCliente(String clienteId, String email) {
        if (isAdmin()) return;
        email = EmailNormalizer.normalizar(email);
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
        if (cliente.getEmpresa() == null || !EmailNormalizer.normalizar(cliente.getEmpresa().getEmail()).equals(email))
            throw proibido("Cliente não pertence à empresa autenticada");
    }

    public void exigirAcessoAoAgendamento(String agendamentoId, String email) {
        if (isAdmin()) return;
        email = EmailNormalizer.normalizar(email);
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));
        boolean dono = EmailNormalizer.normalizar(agendamento.getCliente().getEmail()).equals(email)
                || EmailNormalizer.normalizar(agendamento.getProfissional().getEmail()).equals(email);
        if (!dono) throw proibido("Agendamento não pertence à conta autenticada");
    }

    public void exigirProfissionalDoAgendamento(String agendamentoId, String email) {
        if (isAdmin()) return;
        email = EmailNormalizer.normalizar(email);
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));
        if (!EmailNormalizer.normalizar(agendamento.getProfissional().getEmail()).equals(email))
            throw proibido("Somente o profissional do atendimento pode definir o link da consulta");
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private ResponseStatusException proibido(String mensagem) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, mensagem);
    }
}
