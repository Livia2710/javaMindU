package com.example.mindu.application.empresas;

import com.example.mindu.domain.entity.Empresa;
import com.example.mindu.domain.service.EmpresaService;
import com.example.mindu.infra.security.ResourceAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/v1/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService service;
    private final EmpresaMapper mapper;
    private final ResourceAccessService access;

    // RF05 — cadastro de Empresa. Rota pública (permitAll no SecurityConfig).
    @PostMapping
    public ResponseEntity<EmpresaDTO> cadastrar(@RequestBody @Valid EmpresaCadastroDTO dto) {
        Empresa empresa = mapper.toEntity(dto);
        Empresa salva = service.cadastrar(empresa, dto.getPlanoId()); // RN02 validado dentro do Service
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(salva));
    }

    @GetMapping("/validar-matricula")
    public ResponseEntity<?> validarMatricula(@RequestParam String codigo) {
        var matricula = service.validarMatricula(codigo);
        return ResponseEntity.ok(java.util.Map.of("empresaId", matricula.getEmpresa().getId(),
                "nomeFantasia", matricula.getEmpresa().getNomeFantasia()));
    }

    // Consulta de dados da própria Empresa (vagas usadas/disponíveis do plano)
    @GetMapping("/me")
    public ResponseEntity<EmpresaDTO> minhaEmpresa(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(mapper.toDTO(service.buscarPorId(access.empresaIdDoUsuario(email))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaDTO> empresaPorId(@PathVariable String id, @AuthenticationPrincipal String email) {
        access.exigirEmpresaDona(id, email);
        return ResponseEntity.ok(mapper.toDTO(service.buscarPorId(id)));
    }

    @PatchMapping("/me")
    public ResponseEntity<EmpresaDTO> atualizarMinhaEmpresa(@RequestBody @Valid EmpresaPerfilDTO dto, @AuthenticationPrincipal String email) {
        String id = access.empresaIdDoUsuario(email);
        return ResponseEntity.ok(mapper.toDTO(service.atualizarPerfil(id, dto)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmpresaDTO> atualizarEmpresa(@PathVariable String id, @RequestBody @Valid EmpresaPerfilDTO dto, @AuthenticationPrincipal String email) {
        access.exigirEmpresaDona(id, email);
        return ResponseEntity.ok(mapper.toDTO(service.atualizarPerfil(id, dto)));
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<EmpresaDTO> meuDashboard(@AuthenticationPrincipal String email) {
        String id = access.empresaIdDoUsuario(email);
        return ResponseEntity.ok(mapper.toDTO(service.buscarPorId(id)));
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<EmpresaDTO> dashboard(@PathVariable String id, @AuthenticationPrincipal String email) {
        access.exigirEmpresaDona(id, email);
        return ResponseEntity.ok(mapper.toDTO(service.buscarPorId(id)));
    }

    // RF06 — Empresa gera N matrículas para distribuir aos colaboradores
    @PostMapping("/me/matriculas")
    public ResponseEntity<List<MatriculaDTO>> gerarMinhasMatriculas(
            @RequestParam int quantidade, @AuthenticationPrincipal String email) {
        String id = access.empresaIdDoUsuario(email);
        var matriculas = service.gerarMatriculas(id, quantidade);
        return ResponseEntity.ok(mapper.toMatriculaDTOList(matriculas));
    }

    @PostMapping("/{id}/matriculas")
    public ResponseEntity<List<MatriculaDTO>> gerarMatriculas(
            @PathVariable String id, @RequestParam int quantidade, @AuthenticationPrincipal String email) {
        access.exigirEmpresaDona(id, email);
        var matriculas = service.gerarMatriculas(id, quantidade); // RN09 validado dentro do Service
        return ResponseEntity.ok(mapper.toMatriculaDTOList(matriculas));
    }
}
