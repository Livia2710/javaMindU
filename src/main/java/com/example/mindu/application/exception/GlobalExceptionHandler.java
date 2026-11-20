package com.example.mindu.application.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Pattern UNIQUE_CONSTRAINT = Pattern.compile("unique constraint [\\\"']([^\\\"']+)[\\\"']", Pattern.CASE_INSENSITIVE);
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> erroDeStatus(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String mensagem = ex.getReason() == null || ex.getReason().isBlank()
                ? (status instanceof HttpStatus httpStatus ? httpStatus.getReasonPhrase() : "Requisição não concluída")
                : ex.getReason();
        return RespostaErro.resposta(status, mensagem);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> erroDeValidacao(BindException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors())
            campos.putIfAbsent(erro.getField(), mensagemValidacao(erro));
        return RespostaErro.resposta(HttpStatus.BAD_REQUEST, "Erro de validação", campos);
    }

    private String mensagemValidacao(FieldError erro) {
        String mensagem = erro.getDefaultMessage();
        if (mensagem == null) return "Valor inválido";
        if (mensagem.equals("must be a well-formed email address")) return "Informe um e-mail válido";
        if (mensagem.equals("must not be blank")) return "Este campo é obrigatório";
        if (mensagem.equals("must not be null")) return "Este campo é obrigatório";
        if (mensagem.equals("size must be between 8 and 2147483647")) return "A senha deve ter no mínimo 8 caracteres";
        return mensagem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException ex) {
        return RespostaErro.resposta(HttpStatus.BAD_REQUEST, "O JSON enviado é inválido ou está malformado");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tipoDeParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return RespostaErro.resposta(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro: " + ex.getName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> parametroAusente(MissingServletRequestParameterException ex) {
        return RespostaErro.resposta(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: " + ex.getParameterName());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> parteAusente(MissingServletRequestPartException ex) {
        return RespostaErro.resposta(HttpStatus.BAD_REQUEST, "Parte obrigatória ausente: " + ex.getRequestPartName());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> uploadExcedido(MaxUploadSizeExceededException ex) {
        return RespostaErro.resposta(HttpStatus.PAYLOAD_TOO_LARGE, "O arquivo enviado excede o tamanho máximo permitido");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> conflito(DataIntegrityViolationException ex) {
        String campo = campoDaConstraint(ex);
        String mensagem = campo == null
                ? "Dado já cadastrado (e-mail, CNPJ ou matrícula duplicados)"
                : "Já existe um cadastro com este " + campo;
        return RespostaErro.resposta(HttpStatus.CONFLICT, mensagem);
    }

    private String campoDaConstraint(DataIntegrityViolationException ex) {
        Throwable causa = ex.getMostSpecificCause();
        for (Throwable atual = causa; atual != null; atual = atual.getCause()) {
            if (!(atual instanceof SQLException)) continue;
            Matcher matcher = UNIQUE_CONSTRAINT.matcher(atual.getMessage() == null ? "" : atual.getMessage());
            if (!matcher.find()) continue;
            String nome = matcher.group(1).toLowerCase(java.util.Locale.ROOT);
            if (nome.contains("email")) return "e-mail";
            if (nome.contains("cnpj")) return "CNPJ";
            if (nome.contains("matricula")) return "matrícula";
        }
        return null;
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> milaIndisponivel(ResourceAccessException ex) {
        return RespostaErro.resposta(HttpStatus.BAD_GATEWAY,
                "Não foi possível conectar à Mila. Tente novamente em instantes.");
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, Object>> erroDaMila(RestClientResponseException ex) {
        var statusMila = ex.getStatusCode();
        var status = (statusMila.is4xxClientError()
                && statusMila.value() != HttpStatus.UNAUTHORIZED.value()
                && statusMila.value() != HttpStatus.FORBIDDEN.value())
                ? statusMila : HttpStatus.BAD_GATEWAY;
        return RespostaErro.resposta(status, "A Mila não conseguiu processar a solicitação.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> erroInterno(Exception ex) {
        log.error("Erro interno não tratado ao processar a requisição", ex);
        return RespostaErro.resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno. Tente novamente mais tarde.");
    }
}
