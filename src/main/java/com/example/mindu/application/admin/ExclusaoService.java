package com.example.mindu.application.admin;

import com.example.mindu.domain.entity.Cliente;
import com.example.mindu.infra.repository.ClienteRepository;
import com.example.mindu.infra.repository.EmpresaRepository;
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

    @Transactional
    public void excluirCliente(String id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
        cliente.setAtivo(false);
        cliente.setTokenVersion(cliente.getTokenVersion() + 1);
        clienteRepository.save(cliente);
    }

    @Transactional
    public void excluirEmpresa(String id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada");
        }

        empresaRepository.findById(id).ifPresent(empresa -> {
            empresa.setAtivo(false);
            empresa.setTokenVersion(empresa.getTokenVersion() + 1);
            empresaRepository.save(empresa);
        });
    }

    @Transactional
    public void excluirProfissional(String id) {
        if (!profissionalRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado");
        }

        profissionalRepository.findById(id).ifPresent(profissional -> {
            profissional.setAtivo(false);
            profissional.setTokenVersion(profissional.getTokenVersion() + 1);
            profissionalRepository.save(profissional);
        });
    }
}
