package com.example.mindu.application.admin;

import com.example.mindu.domain.entity.Cliente;
import com.example.mindu.infra.repository.AgendamentoRepository;
import com.example.mindu.infra.repository.ClienteRepository;
import com.example.mindu.infra.repository.EmpresaRepository;
import com.example.mindu.infra.repository.MatriculaRepository;
import com.example.mindu.infra.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ExclusaoService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final MatriculaRepository matriculaRepository;
    private final AgendamentoRepository agendamentoRepository;

    @Transactional
    public void excluirCliente(String id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
        agendamentoRepository.deleteByClienteId(id);
        clienteRepository.delete(cliente);
    }

    @Transactional
    public void excluirEmpresa(String id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada");
        }

        for (Cliente cliente : clienteRepository.findByEmpresaId(id)) {
            agendamentoRepository.deleteByClienteId(cliente.getId());
            clienteRepository.delete(cliente);
        }
        matriculaRepository.deleteByEmpresaId(id);
        empresaRepository.deleteById(id);
    }

    @Transactional
    public void excluirProfissional(String id) {
        if (!profissionalRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado");
        }

        agendamentoRepository.deleteByProfissionalId(id);
        profissionalRepository.deleteById(id);
    }
}
