package com.example.mindu.application.agendamentos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LinkConsultaDTO {
    @NotBlank
    @Size(max = 2048)
    @Pattern(regexp = "https://.+", message = "O link da consulta deve usar HTTPS")
    private String linkOnline;
}
