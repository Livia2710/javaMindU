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
import com.example.mindu.application.contas.SolicitacaoExclusaoStatus;
import com.example.mindu.application.contas.SolicitacaoExclusaoService;
import com.example.mindu.domain.enums.StatusCadastroCliente;
import com.example.mindu.domain.enums.StatusAgendamento;
import com.example.mindu.domain.enums.StatusVerificacao;
import com.example.mindu.domain.service.ClienteService;
import com.example.mindu.domain.service.ProfissionalService;
import com.example.mindu.infra.repository.AgendamentoRepository;
import com.example.mindu.infra.repository.EmpresaRepository;
import com.example.mindu.infra.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
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
            @RequestParam(required = false) StatusVerificacao status,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var profissionais = status == null ? profissionalRepository.findAll()
                : profissionalRepository.findByStatus(status);
        var resultado = profissionais.stream()
                .filter(p -> ativo == null || p.isAtivo() == ativo)
                .map(ProfissionalAdminDTO::from).toList();
        return listar(resultado, page, size);
    }

    @GetMapping("/profissionais/pendentes")
    public ResponseEntity<List<ProfissionalAdminDTO>> profissionaisPendentes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return listar(profissionalService.listarPendentes().stream()
                .map(ProfissionalAdminDTO::from).toList(), page, size);
    }

    @GetMapping(value = "/profissionais/{id}/carteirinha", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> carteirinha(@PathVariable String id) {
        var profissional = profissionalService.buscarPorId(id);
        byte[] imagem = profissional.getFotoCarterinha();
        if (imagem == null || imagem.length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Carteirinha não encontrada");
        }
        return ResponseEntity.ok()
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.IMAGE_PNG)
                .body(imagem);
    }

    @GetMapping(value = "/profissionais/{id}/foto", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> fotoProfissional(@PathVariable String id) {
        var profissional = profissionalService.buscarPorId(id);
        byte[] imagem = profissional.getFoto();
        if (imagem == null || imagem.length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto de perfil não encontrada");
        }
        return ResponseEntity.ok()
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.IMAGE_PNG)
                .body(imagem);
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteDTO>> clientes(
            @RequestParam(required = false) StatusCadastroCliente status,
            @RequestParam(required = false) String empresaId,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var clientes = empresaId == null || empresaId.isBlank()
                ? clienteService.listarTodos(status)
                : clienteService.listarPorEmpresa(empresaId, status);
        var resultado = clientes.stream()
                .filter(c -> ativo == null || c.isAtivo() == ativo)
                .map(clienteMapper::toDTO).toList();
        return listar(resultado, page, size);
    }

    @GetMapping("/empresas")
    public ResponseEntity<List<EmpresaDTO>> empresas(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var resultado = empresaRepository.findAll().stream()
                .filter(e -> ativo == null || e.isAtivo() == ativo)
                .map(empresaMapper::toDTO).toList();
        return listar(resultado, page, size);
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
    public ResponseEntity<List<SolicitacaoExclusaoDTO>> solicitacoesExclusao(
            @RequestParam(required = false) SolicitacaoExclusaoStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var resultado = solicitacaoExclusaoService.listar().stream()
                .filter(s -> status == null || s.status() == status).toList();
        return listar(resultado, page, size);
    }

    @PatchMapping("/solicitacoes-exclusao/{id}")
    public ResponseEntity<SolicitacaoExclusaoDTO> atualizarSolicitacaoExclusao(
            @PathVariable String id, @RequestBody @Valid AtualizarSolicitacaoExclusaoDTO dto) {
        return ResponseEntity.ok(solicitacaoExclusaoService.atualizar(id, dto.getStatus()));
    }

    @GetMapping("/agendamentos")
    public ResponseEntity<List<AgendamentoDTO>> agendamentos(
            @RequestParam(required = false) StatusAgendamento status,
            @RequestParam(required = false) String clienteId,
            @RequestParam(required = false) String profissionalId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var resultado = agendamentoRepository.findAll().stream()
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> clienteId == null || clienteId.isBlank() || a.getCliente().getId().equals(clienteId))
                .filter(a -> profissionalId == null || profissionalId.isBlank()
                        || a.getProfissional().getId().equals(profissionalId))
                .map(agendamentoMapper::toDTO).toList();
        return listar(resultado, page, size);
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

    @PatchMapping("/clientes/{id}/reativacao")
    public ResponseEntity<Void> reativarCliente(@PathVariable String id) {
        exclusaoService.reativarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/empresas/{id}/reativacao")
    public ResponseEntity<Void> reativarEmpresa(@PathVariable String id) {
        exclusaoService.reativarEmpresa(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/profissionais/{id}/reativacao")
    public ResponseEntity<Void> reativarProfissional(@PathVariable String id) {
        exclusaoService.reativarProfissional(id);
        return ResponseEntity.noContent().build();
    }

    private <T> ResponseEntity<List<T>> listar(List<T> registros, Integer page, Integer size) {
        if (page == null && size == null) return ResponseEntity.ok(registros);
        int pagina = page == null ? 0 : page;
        int tamanho = size == null ? 20 : size;
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "page deve ser >= 0 e size deve estar entre 1 e 100");
        }

        long inicio = (long) pagina * tamanho;
        int de = (int) Math.min(inicio, registros.size());
        int ate = Math.min(de + tamanho, registros.size());
        int totalPaginas = (int) Math.ceil((double) registros.size() / tamanho);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", Integer.toString(registros.size()));
        headers.add("X-Page", Integer.toString(pagina));
        headers.add("X-Page-Size", Integer.toString(tamanho));
        headers.add("X-Total-Pages", Integer.toString(totalPaginas));
        return ResponseEntity.ok().headers(headers).body(registros.subList(de, ate));
    }
}
