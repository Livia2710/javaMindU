package com.example.mindu.application.admin;

import com.example.mindu.application.agendamentos.AgendamentoDTO;
import com.example.mindu.application.agendamentos.AgendamentoMapper;
import com.example.mindu.application.clientes.ClienteDTO;
import com.example.mindu.application.clientes.ClienteMapper;
import com.example.mindu.application.empresas.EmpresaDTO;
import com.example.mindu.application.empresas.EmpresaMapper;
import com.example.mindu.application.empresas.ContratoPlanoDTO;
import com.example.mindu.application.contas.AtualizarSolicitacaoExclusaoDTO;
import com.example.mindu.application.contas.SolicitacaoExclusaoDTO;
import com.example.mindu.application.contas.SolicitacaoExclusaoService;
import com.example.mindu.domain.enums.StatusCadastroCliente;
import com.example.mindu.domain.enums.StatusVerificacao;
import com.example.mindu.domain.service.ClienteService;
import com.example.mindu.domain.service.ProfissionalService;
import com.example.mindu.infra.repository.AgendamentoRepository;
import com.example.mindu.infra.repository.EmpresaRepository;
import com.example.mindu.infra.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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
    private final ExclusaoService exclusaoService;
    private final SolicitacaoExclusaoService solicitacaoExclusaoService;

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

    @PatchMapping("/empresas/{id}/contrato")
    public ResponseEntity<EmpresaDTO> atualizarContrato(@PathVariable String id,
                                                         @RequestBody @Valid ContratoPlanoDTO dto) {
        var empresa = empresaRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
        if (dto.getFimVigencia() != null && dto.getFimVigencia().isBefore(dto.getInicioVigencia())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O fim da vigência não pode ser anterior ao início");
        }
        empresa.setInicioVigenciaPlano(dto.getInicioVigencia());
        empresa.setFimVigenciaPlano(dto.getFimVigencia());
        empresa.setDiaPagamento(dto.getDiaPagamento());
        return ResponseEntity.ok(empresaMapper.toDTO(empresaRepository.save(empresa)));
    }

    @GetMapping("/solicitacoes-exclusao")
    public ResponseEntity<List<SolicitacaoExclusaoDTO>> solicitacoesExclusao() {
        return ResponseEntity.ok(solicitacaoExclusaoService.listar());
    }

    @PatchMapping("/solicitacoes-exclusao/{id}")
    public ResponseEntity<SolicitacaoExclusaoDTO> atualizarSolicitacaoExclusao(
            @PathVariable String id, @RequestBody @Valid AtualizarSolicitacaoExclusaoDTO dto) {
        return ResponseEntity.ok(solicitacaoExclusaoService.atualizar(id, dto.getStatus()));
    }

    @GetMapping("/agendamentos")
    public ResponseEntity<List<AgendamentoDTO>> agendamentos() {
        return ResponseEntity.ok(agendamentoMapper.toDTOList(agendamentoRepository.findAll()));
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> excluirCliente(@PathVariable String id) {
        exclusaoService.excluirCliente(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/empresas/{id}")
    public ResponseEntity<Void> excluirEmpresa(@PathVariable String id) {
        exclusaoService.excluirEmpresa(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/profissionais/{id}")
    public ResponseEntity<Void> excluirProfissional(@PathVariable String id) {
        exclusaoService.excluirProfissional(id);
        return ResponseEntity.noContent().build();
    }
}
