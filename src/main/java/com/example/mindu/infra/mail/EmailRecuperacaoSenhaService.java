package com.example.mindu.infra.mail;

import com.example.mindu.application.auth.RecuperacaoSenhaSolicitadaEvent;
import com.example.mindu.infra.repository.TokenRecuperacaoSenhaRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

@Service
public class EmailRecuperacaoSenhaService {

    private static final Logger log = LoggerFactory.getLogger(EmailRecuperacaoSenhaService.class);
    private final JavaMailSender mailSender;
    private final TokenRecuperacaoSenhaRepository tokenRepository;
    private final String remetente;

    public EmailRecuperacaoSenhaService(JavaMailSender mailSender,
                                        TokenRecuperacaoSenhaRepository tokenRepository,
                                        @Value("${SMTP_FROM}") String remetente) {
        this.mailSender = mailSender;
        this.tokenRepository = tokenRepository;
        this.remetente = remetente;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviar(RecuperacaoSenhaSolicitadaEvent evento) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, false, "UTF-8");
            helper.setFrom(remetente, "MindU");
            helper.setTo(evento.destinatario());
            helper.setSubject("Redefinição de senha — MindU");
            helper.setText("Olá!\n\nRecebemos uma solicitação para redefinir a senha da sua conta MindU.\n" +
                    "Acesse o link abaixo para escolher uma nova senha. Ele é válido por 15 minutos e pode ser usado uma única vez:\n\n" +
                    evento.link() + "\n\nSe você não solicitou a redefinição, ignore este e-mail. Sua senha atual continuará funcionando.");
            mailSender.send(mensagem);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            falhaNoEnvio(evento, ex);
        } catch (MailException ex) {
            falhaNoEnvio(evento, ex);
        }
    }

    private void falhaNoEnvio(RecuperacaoSenhaSolicitadaEvent evento, Exception ex) {
        tokenRepository.invalidarSePendente(evento.tokenId(), LocalDateTime.now());
        // Não registrar endereço, link, token nem resposta completa do servidor SMTP.
        log.error("Falha ao enviar e-mail de recuperação para usuário {} ({}).",
                evento.usuarioId(), ex.getClass().getSimpleName());
    }
}
