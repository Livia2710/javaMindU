package com.example.mindu.application.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record ChatRequestDTO(
        @NotBlank @Size(max = 4000) String message,
        @Size(max = 20) List<@Valid ChatTurnDTO> history
) {
    public record ChatTurnDTO(
            @Pattern(regexp = "user|assistant") String role,
            @NotBlank @Size(max = 4000) String content
    ) {}
}
