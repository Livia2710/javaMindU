package com.example.mindu.application.profissionais;

import com.example.mindu.domain.entity.Profissional;
import com.example.mindu.domain.enums.Modalidade;
import com.example.mindu.domain.service.ProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService service;
    private final ProfissionalMapper mapper;

    // RF07, RF10 — cadastro. Só fotoCarteirinha é obrigatória (decisão sua:
    // foto/fotoBanner saem completamente daqui). Rota pública (permitAll).
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfissionalDTO> cadastrar(
            @ModelAttribute @Valid ProfissionalCadastroDTO dto,
            @RequestPart("fotoCarteirinha") MultipartFile fotoCarteirinha
    ) throws IOException {
        Profissional profissional = mapper.toEntity(dto, fotoCarteirinha);
        Profissional salvo = service.cadastrar(profissional);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(salvo));
    }

    // RF16 — só o Admin deveria poder chamar isso (hoje ainda não há restrição de
    // role de verdade no SecurityConfig — é a lacuna do TipoUsuario que comentei antes)
    @PatchMapping("/{id}/aprovacao")
    public ResponseEntity<ProfissionalDTO> aprovar(@PathVariable String id, @RequestParam boolean aprovado) {
        return ResponseEntity.ok(mapper.toDTO(service.aprovar(id, aprovado)));
    }

    // RF08 — Profissional pode ter vários locais; cada chamada adiciona um
    @PostMapping("/{id}/locais")
    public ResponseEntity<LocalAtendimentoDTO> adicionarLocal(
            @PathVariable String id, @RequestBody @Valid LocalAtendimentoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionarLocal(id, dto));
    }

    // RF09 — recebe a agenda semanal inteira de uma vez e substitui a anterior
    @PutMapping("/{id}/disponibilidade")
    public ResponseEntity<List<DisponibilidadeDTO>> definirDisponibilidade(
            @PathVariable String id, @RequestBody @Valid List<DisponibilidadeDTO> dtos) {
        return ResponseEntity.ok(service.definirDisponibilidade(id, dtos));
    }

    // Editável depois do cadastro, como combinado — foto e fotoBanner aqui,
    // nunca no cadastrar(). Os dois são opcionais (você pode trocar só um).
    @PatchMapping(value = "/{id}/perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfissionalDTO> atualizarPerfil(
            @PathVariable String id,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam(value = "fotoBanner", required = false) MultipartFile fotoBanner
    ) throws IOException {
        byte[] fotoBytes = (foto != null) ? foto.getBytes() : null;
        byte[] bannerBytes = (fotoBanner != null) ? fotoBanner.getBytes() : null;
        return ResponseEntity.ok(mapper.toDTO(service.atualizarPerfil(id, fotoBytes, bannerBytes)));
    }

    // RF13, RN06 — pesquisa pública de profissionais aprovados
    @GetMapping
    public ResponseEntity<List<ProfissionalDTO>> pesquisar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String especialidade,
            @RequestParam(required = false) Modalidade modalidade
    ) {
        return ResponseEntity.ok(mapper.toDTOList(service.pesquisar(nome, especialidade, modalidade)));
    }
}