package com.example.mindu.application.profissionais;

import com.example.mindu.domain.entity.Profissional;
import com.example.mindu.domain.enums.Modalidade;
import com.example.mindu.domain.enums.StatusVerificacao;
import com.example.mindu.domain.service.ProfissionalService;
import com.example.mindu.infra.security.ResourceAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService service;
    private final ProfissionalMapper mapper;
    private final ResourceAccessService access;

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
            @PathVariable String id, @RequestBody @Valid LocalAtendimentoDTO dto,
            @AuthenticationPrincipal String email) {
        access.exigirProfissionalDono(id, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionarLocal(id, dto));
    }

    // RF09 — recebe a agenda semanal inteira de uma vez e substitui a anterior
    @PutMapping("/{id}/disponibilidade")
    public ResponseEntity<List<DisponibilidadeDTO>> definirDisponibilidade(
            @PathVariable String id, @RequestBody @Valid List<DisponibilidadeDTO> dtos,
            @AuthenticationPrincipal String email) {
        access.exigirProfissionalDono(id, email);
        return ResponseEntity.ok(service.definirDisponibilidade(id, dtos));
    }

    // Editável depois do cadastro, como combinado — foto e fotoBanner aqui,
    // nunca no cadastrar(). Os dois são opcionais (você pode trocar só um).
    @PatchMapping(value = "/{id}/perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfissionalDTO> atualizarPerfil(
            @PathVariable String id,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam(value = "fotoBanner", required = false) MultipartFile fotoBanner,
            @AuthenticationPrincipal String email
    ) throws IOException {
        access.exigirProfissionalDono(id, email);
        byte[] fotoBytes = (foto != null) ? foto.getBytes() : null;
        byte[] bannerBytes = (fotoBanner != null) ? fotoBanner.getBytes() : null;
        return ResponseEntity.ok(mapper.toDTO(service.atualizarPerfil(id, fotoBytes,
                foto == null ? null : foto.getContentType(), bannerBytes,
                fotoBanner == null ? null : fotoBanner.getContentType())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalDetalheDTO> detalhes(@PathVariable String id) {
        Profissional profissional = service.buscarPorId(id);
        if (profissional.getStatus() != StatusVerificacao.APROVADO) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(mapper.toDetalheDTO(profissional));
    }

    @PostMapping("/{id}/horarios-disponiveis")
    public ResponseEntity<List<java.time.LocalDateTime>> horariosDisponiveis(
            @PathVariable String id, @RequestBody @Valid ConsultaHorariosDTO consulta) {
        return ResponseEntity.ok(service.horariosDisponiveis(id, consulta.getData()));
    }

    @GetMapping(value = "/{id}/foto")
    public ResponseEntity<byte[]> foto(@PathVariable String id) {
        Profissional profissional = service.buscarPorId(id);
        if (profissional.getStatus() != StatusVerificacao.APROVADO) return ResponseEntity.notFound().build();
        if (profissional.getFoto() == null || profissional.getFoto().length == 0)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(mediaType(profissional.getFotoContentType()))
                .body(profissional.getFoto());
    }

    @GetMapping(value = "/{id}/banner")
    public ResponseEntity<byte[]> banner(@PathVariable String id) {
        Profissional profissional = service.buscarPorId(id);
        if (profissional.getStatus() != StatusVerificacao.APROVADO) return ResponseEntity.notFound().build();
        if (profissional.getFotoBanner() == null || profissional.getFotoBanner().length == 0)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(mediaType(profissional.getFotoBannerContentType()))
                .body(profissional.getFotoBanner());
    }

    private MediaType mediaType(String value) {
        if (value == null || !value.startsWith("image/")) return MediaType.APPLICATION_OCTET_STREAM;
        try { return MediaType.parseMediaType(value); }
        catch (IllegalArgumentException ignored) { return MediaType.APPLICATION_OCTET_STREAM; }
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
