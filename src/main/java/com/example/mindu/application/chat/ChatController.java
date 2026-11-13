package com.example.mindu.application.chat;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/v1/chat")
public class ChatController {
    private final MilaClient milaClient;

    public ChatController(MilaClient milaClient) {
        this.milaClient = milaClient;
    }

    @PostMapping
    public JsonNode chat(@Valid @RequestBody ChatRequestDTO request) {
        return milaClient.sendMessage(request);
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public JsonNode audio(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) @Size(max = 4000) String message,
            @RequestParam(required = false) String history
    ) throws IOException {
        return milaClient.sendAudio(file, message, history);
    }
}
