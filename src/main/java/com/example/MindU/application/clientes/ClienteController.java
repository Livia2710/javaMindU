package com.example.mindu.application.clientes;

import com.example.mindu.domain.entity.Cliente;
import com.example.mindu.domain.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;
    private final ClienteMapper mapper;

    // RF01, RF02, RF03 — cadastro inicial. Rota pública (permitAll no SecurityConfig).
    @PostMapping
    public ResponseEntity<ClienteDTO> cadastrar(@RequestBody @Valid ClienteCadastroDTO dto) {
        Cliente cliente = service.cadastrar(dto.getNome(), dto.getEmail(), dto.getSenha(),
                dto.getEmpresaId(), dto.getMatricula());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(cliente));
    }

    // RF03 — chamado pela Empresa logada (precisa de token — não está em permitAll)
    @PatchMapping("/{id}/aprovacao")
    public ResponseEntity<ClienteDTO> aprovar(@PathVariable String id, @RequestParam boolean aprovado) {
        return ResponseEntity.ok(mapper.toDTO(service.aprovar(id, aprovado)));
    }

    // RF04 — só funciona se status já for APROVADO (RN05, validado no Service)
    @PatchMapping("/{id}/dados-sensiveis")
    public ResponseEntity<ClienteDTO> completarCadastro(@PathVariable String id,
                                                        @RequestBody @Valid DadosSensiveisDTO dto) {
        return ResponseEntity.ok(mapper.toDTO(service.completarCadastro(id, dto)));
    }
}