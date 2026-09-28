package com.example.mindu.application.clientes;

import com.example.mindu.domain.entity.Cliente;
import com.example.mindu.domain.entity.Empresa;
import com.example.mindu.domain.entity.Matricula;
import com.example.mindu.domain.enums.StatusCadastroCliente;
import com.example.mindu.domain.service.ClienteService;
import com.example.mindu.infra.repository.ClienteRepository;
import com.example.mindu.infra.repository.EmpresaRepository;
import com.example.mindu.infra.repository.MatriculaRepository;
import com.example.mindu.infra.security.CriptografiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final MatriculaRepository matriculaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Cliente cadastrar(String nome, String email, String senha, String empresaId, String codigoMatricula) {
        // RN01 — e-mail não pode se repetir
        if (clienteRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        // RN04 — o código só é válido se pertencer a ESSA empresa específica
        Matricula matricula = matriculaRepository.findByCodigoAndEmpresaId(codigoMatricula, empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Matrícula inválida"));

        // RN04/RN01 — matrícula não pode já ter sido usada por outro colaborador
        if (matricula.isUtilizada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula já utilizada");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        Cliente cliente = Cliente.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(senha)) // RNF01
                .empresa(empresa)
                .matricula(matricula)
                .status(StatusCadastroCliente.AGUARDANDO_APROVACAO) // RF03 — nasce pendente
                .build();

        // Marca a matrícula como usada ANTES de salvar o Cliente — os dois fazem
        // parte da mesma transação (@Transactional): ou os dois persistem, ou nenhum.
        matricula.setUtilizada(true);
        matriculaRepository.save(matricula);

        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public Cliente aprovar(String clienteId, boolean aprovado) {
        Cliente cliente = buscarPorId(clienteId);
        // RF03 — quem chama esse método é a Empresa, decidindo aceitar ou recusar
        cliente.setStatus(aprovado ? StatusCadastroCliente.APROVADO : StatusCadastroCliente.REJEITADO);
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public Cliente completarCadastro(String clienteId, DadosSensiveisDTO dto) {
        Cliente cliente = buscarPorId(clienteId);

        // RN05 — só quem já foi aprovado pela Empresa pode preencher dados sensíveis
        if (cliente.getStatus() != StatusCadastroCliente.APROVADO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cliente ainda não foi aprovado pela empresa");
        }

        cliente.setCns(dto.getCns()); // RNF02
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setNomeMae(dto.getNomeMae());
        cliente.setGenero(dto.getGenero());

        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente buscarPorId(String id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    // Filtra em memória — lista de Clientes por Empresa nunca deveria ser grande
    // o suficiente pra precisar de Specification (diferente do Profissional/RF13)
    @Override
    public List<Cliente> listarPorEmpresa(String empresaId, StatusCadastroCliente status) {
        List<Cliente> clientes = clienteRepository.findByEmpresaId(empresaId);
        if (status == null) return clientes;
        return clientes.stream().filter(c -> c.getStatus() == status).toList();
    }
}