package com.example.mindu.application.admin;

import com.example.mindu.application.agendamentos.AgendamentoDTO;
import com.example.mindu.application.agendamentos.AgendamentoMapper;
import com.example.mindu.application.clientes.ClienteDTO;
import com.example.mindu.application.clientes.ClienteMapper;
import com.example.mindu.application.empresas.EmpresaDTO;
import com.example.mindu.application.empresas.EmpresaMapper;
import com.example.mindu.domain.enums.StatusCadastroCliente;
import com.example.mindu.domain.enums.StatusVerificacao;
import com.example.mindu.domain.service.ClienteService;
import com.example.mindu.domain.service.ProfissionalService;
import com.example.mindu.infra.repository.AgendamentoRepository;
import com.example.mindu.infra.repository.EmpresaRepository;
import com.example.mindu.infra.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProfissionalService profissionalService;
    private final ClienteService clienteService;
    private final ClienteMapper clienteMapper;
    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoMapper agendamentoMapper;
    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;
    private final ProfissionalRepository profissionalRepository;

    @GetMapping("/profissionais")
    public ResponseEntity<List<ProfissionalAdminDTO>> profissionais(
            @RequestParam(required = false) StatusVerificacao status) {
        var profissionais = status == null ? profissionalRepository.findAll()
                : profissionalRepository.findByStatus(status);
        return ResponseEntity.ok(profissionais.stream().map(ProfissionalAdminDTO::from).toList());
    }

    @GetMapping("/profissionais/pendentes")
    public ResponseEntity<List<ProfissionalAdminDTO>> profissionaisPendentes() {
        return ResponseEntity.ok(profissionalService.listarPendentes().stream()
                .map(ProfissionalAdminDTO::from).toList());
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteDTO>> clientes(
            @RequestParam(required = false) StatusCadastroCliente status) {
        return ResponseEntity.ok(clienteMapper.toDTOList(clienteService.listarTodos(status)));
    }

    @GetMapping("/empresas")
    public ResponseEntity<List<EmpresaDTO>> empresas() {
        return ResponseEntity.ok(empresaRepository.findAll().stream().map(empresaMapper::toDTO).toList());
    }

    @GetMapping("/agendamentos")
    public ResponseEntity<List<AgendamentoDTO>> agendamentos() {
        return ResponseEntity.ok(agendamentoMapper.toDTOList(agendamentoRepository.findAll()));
    }
}
