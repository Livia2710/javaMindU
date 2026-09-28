package com.example.mindu.application.agendamentos;

import com.example.mindu.domain.entity.Agendamento;
import com.example.mindu.domain.service.AgendamentoService;
import com.example.mindu.infra.repository.AgendamentoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService service;
    private final AgendamentoMapper mapper;
    private final AgendamentoRepository agendamentoRepository;

    // RF14 — precisa de token (Cliente logado), não está em permitAll no SecurityConfig
    @PostMapping
    public ResponseEntity<AgendamentoDTO> agendar(@RequestBody @Valid AgendamentoCadastroDTO dto) {
        Agendamento agendamento = service.agendar(dto.getClienteId(), dto.getProfissionalId(), dto.getDataHora());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(agendamento));
    }

    @PatchMapping("/{id}/cancelamento")
    public ResponseEntity<AgendamentoDTO> cancelar(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toDTO(service.cancelar(id)));
    }

    // Existe porque, sem isso, o Cliente nunca sabe o id do próprio agendamento
    // pra poder cancelar (PATCH /{id}/cancelamento já existe, mas de onde viria
    // o id?) — e a tela "meus agendamentos" de qualquer app real precisa disso.
    @GetMapping("/meus")
    public ResponseEntity<List<AgendamentoDTO>> meusAgendamentos(@RequestParam String clienteId) {
        return ResponseEntity.ok(mapper.toDTOList(agendamentoRepository.findByClienteId(clienteId)));
    }
}