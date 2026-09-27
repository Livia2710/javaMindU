package com.example.mindu.application.agendamentos;

import com.example.mindu.domain.entity.Agendamento;
import com.example.mindu.domain.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService service;
    private final AgendamentoMapper mapper;

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
}