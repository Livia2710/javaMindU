package com.example.mindu.application.auth;

import com.example.mindu.domain.entity.TokenRecuperacaoSenha;
import com.example.mindu.domain.entity.Usuario;
import com.example.mindu.domain.util.EmailNormalizer;
import com.example.mindu.infra.repository.AdminRepository;
import com.example.mindu.infra.repository.ClienteRepository;
import com.example.mindu.infra.repository.EmpresaRepository;
import com.example.mindu.infra.repository.ProfissionalRepository;
import com.example.mindu.infra.repository.TokenRecuperacaoSenhaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String MENSAGEM_SOLICITACAO =
            "Se o e-mail estiver cadastrado, enviaremos um link para redefinir sua senha.";
    private static final int VALIDADE_MINUTOS = 15;

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AdminRepository adminRepository;
    private final TokenRecuperacaoSenhaRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${MINDU_RESET_PASSWORD_URL}")
    private String urlRedefinicao;

    @Transactional
    public Map<String, String> solicitar(String emailRecebido) {
        String email = EmailNormalizer.normalizar(emailRecebido);
        Optional<Usuario> usuarioEncontrado = buscarPorEmail(email);
        if (usuarioEncontrado.isEmpty() || !usuarioEncontrado.get().isAtivo())
            return respostaSolicitacao();

        Usuario usuario = usuarioEncontrado.get();
        LocalDateTime agora = LocalDateTime.now();
        tokenRepository.invalidarPendentesDoUsuario(usuario.getId(), agora);

        String tokenCru = gerarToken();
        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
        token.setUsuario(usuario);
        token.setHashToken(hashToken(tokenCru));
        token.setCriadoEm(agora);
        token.setExpiraEm(agora.plusMinutes(VALIDADE_MINUTOS));
        tokenRepository.save(token);

        String link = UriComponentsBuilder.fromUriString(urlRedefinicao)
                .queryParam("token", tokenCru)
                .build()
                .encode()
                .toUriString();
        // O listener envia o e-mail depois do commit e em outra thread, sem criar
        // diferenças de tempo perceptíveis entre e-mails cadastrados e inexistentes.
        eventPublisher.publishEvent(new RecuperacaoSenhaSolicitadaEvent(
                usuario.getEmail(), link, token.getId(), usuario.getId()));
        return respostaSolicitacao();
    }

    @Transactional
    public void redefinir(String tokenCru, String novaSenha) {
        LocalDateTime agora = LocalDateTime.now();
        TokenRecuperacaoSenha token = tokenRepository.findByHashTokenForUpdate(hashToken(tokenCru))
                .orElseThrow(this::tokenInvalido);

        if (token.getUtilizadoEm() != null || token.getInvalidadoEm() != null
                || !token.getExpiraEm().isAfter(agora) || !token.getUsuario().isAtivo())
            throw tokenInvalido();

        Usuario usuario = token.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setTokenVersion(usuario.getTokenVersion() + 1);
        token.setUtilizadoEm(agora);
        tokenRepository.invalidarOutrosPendentes(usuario.getId(), token.getId(), agora);
        // Usuario e token estão gerenciados nesta transação; alterações são persistidas juntas.
    }

    private Optional<Usuario> buscarPorEmail(String email) {
        Optional<Usuario> cliente = clienteRepository.findByEmail(email).map(Usuario.class::cast);
        if (cliente.isPresent()) return cliente;

        Optional<Usuario> empresa = empresaRepository.findByEmail(email).map(Usuario.class::cast);
        if (empresa.isPresent()) return empresa;

        Optional<Usuario> profissional = profissionalRepository.findByEmail(email).map(Usuario.class::cast);
        if (profissional.isPresent()) return profissional;

        return adminRepository.findByEmail(email).map(Usuario.class::cast);
    }

    private Map<String, String> respostaSolicitacao() {
        Map<String, String> resposta = new LinkedHashMap<>();
        resposta.put("mensagem", MENSAGEM_SOLICITACAO);
        return resposta;
    }

    private String gerarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 não está disponível", ex);
        }
    }

    private ResponseStatusException tokenInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Link de recuperação inválido ou expirado. Solicite um novo link.");
    }
}
