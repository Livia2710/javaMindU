package com.example.mindu.application.auth;

import com.example.mindu.domain.entity.Cliente;
import com.example.mindu.domain.entity.TokenRecuperacaoSenha;
import com.example.mindu.infra.mail.EmailRecuperacaoSenhaService;
import com.example.mindu.infra.repository.AdminRepository;
import com.example.mindu.infra.repository.ClienteRepository;
import com.example.mindu.infra.repository.EmpresaRepository;
import com.example.mindu.infra.repository.ProfissionalRepository;
import com.example.mindu.infra.repository.TokenRecuperacaoSenhaRepository;
import com.example.mindu.infra.security.JwtFilter;
import com.example.mindu.infra.security.JwtService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RecuperacaoSenhaServiceTest {

    private ClienteRepository clienteRepository;
    private EmpresaRepository empresaRepository;
    private ProfissionalRepository profissionalRepository;
    private AdminRepository adminRepository;
    private TokenRecuperacaoSenhaRepository tokenRepository;
    private JavaMailSender mailSender;
    private ApplicationEventPublisher eventPublisher;
    private PasswordEncoder passwordEncoder;
    private RecuperacaoSenhaService service;
    private Cliente usuario;

    @BeforeEach
    void configurar() throws Exception {
        clienteRepository = mock(ClienteRepository.class);
        empresaRepository = mock(EmpresaRepository.class);
        profissionalRepository = mock(ProfissionalRepository.class);
        adminRepository = mock(AdminRepository.class);
        tokenRepository = mock(TokenRecuperacaoSenhaRepository.class);
        mailSender = mock(JavaMailSender.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        passwordEncoder = new BCryptPasswordEncoder();
        when(tokenRepository.save(any(TokenRecuperacaoSenha.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenRepository.invalidarPendentesDoUsuario(anyString(), any(LocalDateTime.class))).thenReturn(0);
        when(tokenRepository.invalidarOutrosPendentes(anyString(), anyString(), any(LocalDateTime.class))).thenReturn(0);

        service = new RecuperacaoSenhaService(clienteRepository, empresaRepository, profissionalRepository,
                adminRepository, tokenRepository, passwordEncoder, eventPublisher);
        ReflectionTestUtils.setField(service, "urlRedefinicao", "http://localhost:5173/redefinir-senha");

        usuario = new Cliente();
        usuario.setId("usuario-teste");
        usuario.setEmail("ana@example.com");
        usuario.setSenha("hash-anterior");
        usuario.setTokenVersion(0);
        usuario.setAtivo(true);
    }

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void solicitacaoParaContaExistenteEnfileiraLinkEArmazenaSomenteHash() {
        when(clienteRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));

        var resposta = service.solicitar("  Ana@Example.com ");

        assertEquals("Se o e-mail estiver cadastrado, enviaremos um link para redefinir sua senha.",
                resposta.get("mensagem"));
        var tokenCaptor = org.mockito.ArgumentCaptor.forClass(TokenRecuperacaoSenha.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        TokenRecuperacaoSenha armazenado = tokenCaptor.getValue();
        assertEquals(15, java.time.Duration.between(armazenado.getCriadoEm(), armazenado.getExpiraEm()).toMinutes());
        assertEquals(64, armazenado.getHashToken().length());

        var eventoCaptor = org.mockito.ArgumentCaptor.forClass(RecuperacaoSenhaSolicitadaEvent.class);
        verify(eventPublisher).publishEvent(eventoCaptor.capture());
        String link = eventoCaptor.getValue().link();
        Matcher matcher = Pattern.compile("[?&]token=([A-Za-z0-9_-]+)").matcher(link);
        assertTrue(matcher.find(), "O evento de envio deve conter o token apenas no link destinado ao e-mail");
        assertNotEquals(matcher.group(1), armazenado.getHashToken());
        assertEquals(hash(matcher.group(1)), armazenado.getHashToken());
        verifyNoInteractions(mailSender);
    }

    @Test
    void emailInexistenteRecebeMesmaRespostaSemEnviarMensagem() {
        when(clienteRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));
        configurarEmailInexistente("nao-existe@example.com");

        var existente = service.solicitar("ana@example.com");
        var inexistente = service.solicitar("nao-existe@example.com");

        assertEquals(existente, inexistente);
        verify(eventPublisher).publishEvent(any(RecuperacaoSenhaSolicitadaEvent.class));
        verifyNoInteractions(mailSender);
    }

    @Test
    void emailDeRecuperacaoUsaNomeMindUTextoEValidadeDeQuinzeMinutos() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(
                new MimeMessage(Session.getInstance(new Properties())));
        EmailRecuperacaoSenhaService emailService = new EmailRecuperacaoSenhaService(
                mailSender, tokenRepository, "nao-responder@mindu.test");
        var evento = new RecuperacaoSenhaSolicitadaEvent("ana@example.com",
                "http://localhost:5173/redefinir-senha?token=token-de-teste", "token-id", usuario.getId());

        emailService.enviar(evento);

        var mensagemCaptor = org.mockito.ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(mensagemCaptor.capture());
        assertEquals("Redefinição de senha — MindU", mensagemCaptor.getValue().getSubject());
        String conteudo = (String) mensagemCaptor.getValue().getContent();
        assertTrue(conteudo.contains("MindU"));
        assertTrue(conteudo.contains("15 minutos"));
        assertTrue(conteudo.contains("token-de-teste"));
    }

    @Test
    void falhaSmtpNaoPropagaEInvalidaTokenNaoEntregue() {
        when(mailSender.createMimeMessage()).thenReturn(
                new MimeMessage(Session.getInstance(new Properties())));
        EmailRecuperacaoSenhaService emailService = new EmailRecuperacaoSenhaService(
                mailSender, tokenRepository, "nao-responder@mindu.test");
        doThrow(new MailSendException("erro smtp interno")).when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> emailService.enviar(new RecuperacaoSenhaSolicitadaEvent(
                "ana@example.com", "https://example.test/redefinir?token=segredo", "token-id", usuario.getId())));
        verify(tokenRepository).invalidarSePendente(eq("token-id"), any(LocalDateTime.class));
    }

    @Test
    void redefinicaoValidaTrocaSenhaConsomeTokenEIncrementaVersaoJwt() throws Exception {
        String tokenCru = "token-valido-de-teste";
        TokenRecuperacaoSenha token = token(tokenCru, LocalDateTime.now().plusMinutes(10));
        when(tokenRepository.findByHashTokenForUpdate(hash(tokenCru))).thenReturn(Optional.of(token));
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-only-secret-at-least-32-bytes-long");
        ReflectionTestUtils.setField(jwtService, "expiracaoMs", 3_600_000L);
        String tokenJwtAnterior = jwtService.gerarToken(usuario.getEmail(), "CLIENTE", usuario.getTokenVersion());

        service.redefinir(tokenCru, "NovaSenha123!");

        assertTrue(passwordEncoder.matches("NovaSenha123!", usuario.getSenha()));
        assertEquals(1, usuario.getTokenVersion());
        assertNotNull(token.getUtilizadoEm());
        assertEquals(0, jwtService.parsearClaims(tokenJwtAnterior).get("ver", Integer.class));

        JwtFilter filtro = new JwtFilter(jwtService, clienteRepository, empresaRepository,
                profissionalRepository, adminRepository);
        when(clienteRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenJwtAnterior);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filtro.doFilter(request, response, (req, res) -> { });
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "O filtro deve rejeitar JWTs cuja versão ficou anterior à conta");
        verify(tokenRepository).invalidarOutrosPendentes(eq(usuario.getId()), anyString(), any(LocalDateTime.class));
    }

    @Test
    void tokenExpiradoOuJaUtilizadoNaoPodeRedefinirSenha() {
        String tokenCru = "token-expirado";
        when(tokenRepository.findByHashTokenForUpdate(hash(tokenCru)))
                .thenReturn(Optional.of(token(tokenCru, LocalDateTime.now().minusSeconds(1))));
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.redefinir(tokenCru, "NovaSenha123!")).getStatusCode());

        String tokenUsadoCru = "token-ja-usado";
        TokenRecuperacaoSenha tokenUsado = token(tokenUsadoCru, LocalDateTime.now().plusMinutes(5));
        tokenUsado.setUtilizadoEm(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByHashTokenForUpdate(hash(tokenUsadoCru))).thenReturn(Optional.of(tokenUsado));
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.redefinir(tokenUsadoCru, "NovaSenha123!")).getStatusCode());
        assertEquals("hash-anterior", usuario.getSenha());
        assertEquals(0, usuario.getTokenVersion());
    }

    @Test
    void novaSenhaComMenosDeOitoCaracteresFalhaNaValidacao() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        RedefinirSenhaDTO dto = new RedefinirSenhaDTO();
        dto.setToken("token");
        dto.setNovaSenha("curta");
        assertTrue(validator.validate(dto).stream().anyMatch(violacao ->
                violacao.getPropertyPath().toString().equals("novaSenha")));
    }

    private TokenRecuperacaoSenha token(String tokenCru, LocalDateTime expiraEm) {
        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
        token.setId("token-id-" + tokenCru);
        token.setUsuario(usuario);
        token.setHashToken(hash(tokenCru));
        token.setCriadoEm(LocalDateTime.now().minusMinutes(1));
        token.setExpiraEm(expiraEm);
        return token;
    }

    private void configurarEmailInexistente(String email) {
        when(clienteRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(empresaRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(profissionalRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(adminRepository.findByEmail(email)).thenReturn(Optional.empty());
    }

    private String hash(String token) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
