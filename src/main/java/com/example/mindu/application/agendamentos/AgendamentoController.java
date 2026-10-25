package com.example.mindu.application.agendamentos;

import com.example.mindu.domain.entity.Agendamento;
import com.example.mindu.domain.service.AgendamentoService;
import com.example.mindu.infra.repository.AgendamentoRepository;
import com.example.mindu.infra.security.ResourceAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.time.YearMonth;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService service;
    private final AgendamentoMapper mapper;
    private final AgendamentoRepository agendamentoRepository;
    private final ResourceAccessService access;

    // RF14 — precisa de token (Cliente logado), não está em permitAll no SecurityConfig
    @PostMapping
    public ResponseEntity<AgendamentoDTO> agendar(@RequestBody @Valid AgendamentoCadastroDTO dto,
                                                   @AuthenticationPrincipal String email) {
        String clienteId = access.isAdmin() ? dto.getClienteId() : access.clienteIdDoUsuario(email);
        if (clienteId == null || clienteId.isBlank())
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "clienteId é obrigatório para o Admin");
        Agendamento agendamento = service.agendar(clienteId, dto.getProfissionalId(), dto.getDataHora(), dto.getModalidade());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(agendamento));
    }

    @PatchMapping("/{id}/cancelamento")
    public ResponseEntity<AgendamentoDTO> cancelar(@PathVariable String id, @AuthenticationPrincipal String email) {
        access.exigirAcessoAoAgendamento(id, email);
        return ResponseEntity.ok(mapper.toDTO(service.cancelar(id)));
    }

    @PatchMapping("/{id}/horario")
    public ResponseEntity<AgendamentoDTO> reagendar(@PathVariable String id,
                                                   @RequestBody @Valid ReagendarDTO dto,
                                                   @AuthenticationPrincipal String email) {
        access.exigirAcessoAoAgendamento(id, email);
        return ResponseEntity.ok(mapper.toDTO(service.reagendar(id, dto.getDataHora())));
    }

    // Existe porque, sem isso, o Cliente nunca sabe o id do próprio agendamento
    // pra poder cancelar (PATCH /{id}/cancelamento já existe, mas de onde viria
    // o id?) — e a tela "meus agendamentos" de qualquer app real precisa disso.
    @GetMapping("/meus")
    public ResponseEntity<List<AgendamentoDTO>> meusAgendamentos(@AuthenticationPrincipal String email, @RequestParam(required = false) YearMonth data) {
        if (data == null) {
            if (access.isAdmin()) return ResponseEntity.ok(mapper.toDTOList(agendamentoRepository.findAll()));
            return ResponseEntity.ok(mapper.toDTOList(agendamentoRepository.findByClienteId(access.clienteIdDoUsuario(email))));
        }
        LocalDateTime inicio = data.atDay(1).atStartOfDay();
        LocalDateTime fim = data.plusMonths(1).atDay(1).atStartOfDay();
        if (access.isAdmin()) return ResponseEntity.ok(mapper.toDTOList(agendamentoRepository.findAll().stream().filter(a -> !a.getDataHora().isBefore(inicio) && a.getDataHora().isBefore(fim)).toList()));
        return ResponseEntity.ok(mapper.toDTOList(agendamentoRepository.findByClienteIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(access.clienteIdDoUsuario(email), inicio, fim)));
    }

    @GetMapping("/minha-agenda")
    public ResponseEntity<List<AgendamentoDTO>> minhaAgenda(@AuthenticationPrincipal String email, @RequestParam(required = false) YearMonth data) {
        String profissionalId = access.profissionalIdDoUsuario(email);
        if (data == null) return ResponseEntity.ok(mapper.toDTOList(agendamentoRepository.findByProfissionalId(profissionalId)));
        LocalDateTime inicio = data.atDay(1).atStartOfDay();
        LocalDateTime fim = data.plusMonths(1).atDay(1).atStartOfDay();
        return ResponseEntity.ok(mapper.toDTOList(agendamentoRepository.findByProfissionalIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(profissionalId, inicio, fim)));
    }
}
