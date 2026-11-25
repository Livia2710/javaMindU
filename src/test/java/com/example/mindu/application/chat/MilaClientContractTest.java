package com.example.mindu.application.chat;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.json.JsonMapper;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MilaClientContractTest {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String RESPONSE = "{\"response\":\"ok\",\"intent\":\"normal\",\"sources\":[],\"missing_info\":false,\"debug_info\":{},\"transcription\":null}";

    @Test
    void sendsTextContractAsJsonWithHistoryTurnFields() throws Exception {
        AtomicReference<String> contentType = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        withServer("/chat", contentType, body, () -> {
            var response = new MilaClient(serverUrl.get(), "", 5).sendMessage(
                    new ChatRequestDTO("Oi", List.of(new ChatRequestDTO.ChatTurnDTO("user", "Olá"))));
            assertEquals("ok", response.path("response").asString());
        });

        assertTrue(contentType.get().startsWith("application/json"));
        var payload = JSON.readTree(body.get());
        assertEquals("Oi", payload.path("message").asString());
        assertEquals(1, payload.path("history").size());
        assertEquals("user", payload.path("history").get(0).path("role").asString());
        assertEquals("Olá", payload.path("history").get(0).path("content").asString());
        assertEquals(2, payload.size());
    }

    @Test
    void normalizesNullHistoryToAnEmptyList() throws Exception {
        AtomicReference<String> contentType = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        withServer("/chat", contentType, body, () -> {
            new MilaClient(serverUrl.get(), "", 5).sendMessage(new ChatRequestDTO("Oi", null));
        });
        assertEquals(0, JSON.readTree(body.get()).path("history").size());
    }

    @Test
    void sendsAudioMultipartContractWithHistory() throws Exception {
        AtomicReference<String> contentType = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        byte[] audioBytes = "short-audio-fixture".getBytes(StandardCharsets.UTF_8);
        withServer("/chat/audio", contentType, body, () -> {
            var response = new MilaClient(serverUrl.get(), "", 5).sendAudio(
                    new MockMultipartFile("file", "sample.wav", "audio/wav", audioBytes),
                    "pergunta", "[{\"role\":\"user\",\"content\":\"anterior\"}]");
            assertEquals("ok", response.path("response").asString());
        });

        assertTrue(contentType.get().startsWith("multipart/form-data;"));
        assertTrue(body.get().contains("name=\"file\"; filename=\"sample.wav\""));
        assertTrue(body.get().contains("name=\"history\""));
        assertTrue(body.get().contains("anterior"));
        assertTrue(body.get().contains("name=\"message\""));
        assertTrue(body.get().contains(new String(audioBytes, StandardCharsets.UTF_8)));
    }

    private final ThreadLocal<String> serverUrl = new ThreadLocal<>();

    private void withServer(String path, AtomicReference<String> contentType,
                            AtomicReference<String> body, ThrowingRunnable action) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(path, exchange -> {
            contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = RESPONSE.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        serverUrl.set("http://127.0.0.1:" + server.getAddress().getPort());
        try {
            action.run();
        } finally {
            server.stop(0);
            serverUrl.remove();
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable { void run() throws Exception; }
}
