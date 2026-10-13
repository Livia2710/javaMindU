package com.example.mindu.application.clientes;

import com.example.mindu.domain.entity.Cliente;
import com.example.mindu.domain.enums.StatusCadastroCliente;
import com.example.mindu.domain.service.ClienteService;
import com.example.mindu.infra.security.ResourceAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;
    private final ClienteMapper mapper;
    private final ResourceAccessService access;

    // RF01, RF02, RF03 — cadastro inicial. Rota pública (permitAll no SecurityConfig).
    @PostMapping
    public ResponseEntity<ClienteDTO> cadastrar(@RequestBody @Valid ClienteCadastroDTO dto) {
        Cliente cliente = service.cadastrar(dto.getNome(), dto.getEmail(), dto.getSenha(),
                dto.getEmpresaId(), dto.getMatricula());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(cliente));
    }

    @GetMapping("/me")
    public ResponseEntity<ClienteDTO> minhaConta(@AuthenticationPrincipal String email) {
        String clienteId = access.clienteIdDoUsuario(email);
        return ResponseEntity.ok(mapper.toDTO(service.buscarPorId(clienteId)));
    }

    @PatchMapping("/me/dados-sensiveis")
    public ResponseEntity<ClienteDTO> completarMeusDados(@RequestBody @Valid DadosSensiveisDTO dto,
                                                          @AuthenticationPrincipal String email) {
        String clienteId = access.clienteIdDoUsuario(email);
        return ResponseEntity.ok(mapper.toDTO(service.completarCadastro(clienteId, dto)));
    }

    // RF03 — chamado pela Empresa logada (precisa de token — não está em permitAll)
    @PatchMapping("/{id}/aprovacao")
    public ResponseEntity<ClienteDTO> aprovar(@PathVariable String id, @RequestParam boolean aprovado,
                                               @AuthenticationPrincipal String email) {
        access.exigirEmpresaDonaDoCliente(id, email);
        return ResponseEntity.ok(mapper.toDTO(service.aprovar(id, aprovado)));
    }

    // RF04 — só funciona se status já for APROVADO (RN05, validado no Service)
    @PatchMapping("/{id}/dados-sensiveis")
    public ResponseEntity<ClienteDTO> completarCadastro(@PathVariable String id,
                                                        @RequestBody @Valid DadosSensiveisDTO dto,
                                                        @AuthenticationPrincipal String email) {
        access.exigirClienteDono(id, email);
        return ResponseEntity.ok(mapper.toDTO(service.completarCadastro(id, dto)));
    }

    // Existe porque a Empresa precisa SABER quem está pendente antes de poder
    // aprovar (RF03) — sem isso, o PATCH /{id}/aprovacao não tem como ser
    // chamado, já que ninguém sabe o id do Cliente de antemão.
    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listar(
            @RequestParam(required = false) StatusCadastroCliente status,
            @AuthenticationPrincipal String email) {
        if (access.isAdmin()) return ResponseEntity.ok(mapper.toDTOList(service.listarTodos(status)));
        String empresaId = access.empresaIdDoUsuario(email);
        return ResponseEntity.ok(mapper.toDTOList(service.listarPorEmpresa(empresaId, status)));
    }


}
