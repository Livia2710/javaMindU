package com.example.mindu.application.auth;

import com.example.mindu.domain.entity.*;
import com.example.mindu.infra.repository.AdminRepository;
import com.example.mindu.infra.repository.ClienteRepository;
import com.example.mindu.infra.repository.EmpresaRepository;
import com.example.mindu.infra.repository.ProfissionalRepository;
import com.example.mindu.infra.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    //Como o classe Usuario é a abstrata, estou usando os Repository do Cliente, Empresa, Profissional e Admin.
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO dto) {

        // Um helper que tenta os 4
        Usuario usuario = buscarPorEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (usuario.getBloqueadoAte() != null) {
            if (usuario.getBloqueadoAte().isAfter(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.LOCKED, "Conta bloqueada. Tente novamente mais tarde.");
            }
            usuario.setBloqueadoAte(null);
            usuario.setTentativasLogin(0); // bloqueio expirou: recomeça a contagem
        }

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            usuario.setTentativasLogin(usuario.getTentativasLogin() + 1);
            if (usuario.getTentativasLogin() >= 5) {
                usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(15)); // RN07
            }
            salvar(usuario); // antes: usuarioRepository.save(usuario)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        usuario.setTentativasLogin(0);
        usuario.setBloqueadoAte(null);
        salvar(usuario);

        String token = jwtService.gerarToken(usuario.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }

    // LER dá pra fazer devolvendo como "Usuario" — como Cliente/Empresa/
    // Profissional/Admin TODOS estendem Usuario, tanto faz qual apareceu,
    // pra pegar senha/tentativasLogin/bloqueadoAte o tipo Usuario já basta.
    // Testa um repository de cada vez, na ordem, e para no primeiro que achar.
    private Optional<Usuario> buscarPorEmail(String email) {
        Optional<Usuario> cliente = clienteRepository.findByEmail(email).map(Usuario.class::cast);
        if (cliente.isPresent()) return cliente;

        Optional<Usuario> empresa = empresaRepository.findByEmail(email).map(Usuario.class::cast);
        if (empresa.isPresent()) return empresa;

        Optional<Usuario> profissional = profissionalRepository.findByEmail(email).map(Usuario.class::cast);
        if (profissional.isPresent()) return profissional;

        return adminRepository.findByEmail(email).map(Usuario.class::cast);
    }

    // SALVAR é o contrário do problema: aqui SIM precisa saber a tabela exata,
    // porque o Hibernate não consegue fazer UPDATE numa entidade abstrata sem
    // saber qual tabela filha tocar. Descobre o tipo real em tempo de execução
    // (instanceof) e chama o repository certo.
    private void salvar(Usuario usuario) {
        if (usuario instanceof Cliente c) clienteRepository.save(c);
        else if (usuario instanceof Empresa e) empresaRepository.save(e);
        else if (usuario instanceof Profissional p) profissionalRepository.save(p);
        else if (usuario instanceof Admin a) adminRepository.save(a);
    }
}