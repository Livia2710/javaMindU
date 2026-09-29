package com.example.mindu.application.empresas;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatriculaDTO {
    private String id;
    private String codigo;
    private boolean utilizada;
}