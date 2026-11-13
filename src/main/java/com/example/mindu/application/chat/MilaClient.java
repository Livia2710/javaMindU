package com.example.mindu.application.chat;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class MilaClient {
    private final RestClient client;
    private final String apiKey;

    public MilaClient(
            @Value("${mindu.mila.url}") String baseUrl,
            @Value("${mindu.mila.api-key:}") String apiKey,
            @Value("${mindu.mila.timeout-seconds:60}") long timeoutSeconds
    ) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeoutSeconds)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.apiKey = apiKey;
    }

    public JsonNode sendMessage(ChatRequestDTO request) {
        List<Map<String, String>> history = request.history() == null ? List.of() : request.history().stream()
                .map(turn -> Map.of("role", turn.role(), "content", turn.content()))
                .toList();
        return client.post().uri("/chat")
                .headers(this::addAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", request.message(), "history", history))
                .retrieve().body(JsonNode.class);
    }

    public JsonNode sendAudio(MultipartFile file, String message, String history) throws IOException {
        ByteArrayResource audio = new ByteArrayResource(file.getBytes()) {
            @Override public String getFilename() { return file.getOriginalFilename() == null ? "audio" : file.getOriginalFilename(); }
        };
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(file.getContentType()));
        form.add("file", new HttpEntity<>(audio, fileHeaders));
        if (message != null && !message.isBlank()) form.add("message", message);
        form.add("history", history == null || history.isBlank() ? "[]" : history);
        return client.post().uri("/chat/audio")
                .headers(this::addAuth)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(form)
                .retrieve().body(JsonNode.class);
    }

    private void addAuth(HttpHeaders headers) {
        if (apiKey != null && !apiKey.isBlank()) headers.set("X-Mila-API-Key", apiKey);
    }
}
