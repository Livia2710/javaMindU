package com.example.mindu.infra.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// @Converter avisa o JPA "essa classe sabe transformar esse campo pro formato
// de banco e de volta". @Component é o que permite injetar o CriptografiaService
// aqui dentro — sem ele, o Hibernate tentaria instanciar a classe sozinho, sem
// passar pelo Spring, e o "private final CriptografiaService" ficaria null.
@Converter
@Component
@RequiredArgsConstructor
public class CnsConverter implements AttributeConverter<String, String> {

    private final CriptografiaService criptografiaService;

    // Roda sozinho, toda vez que o Hibernate for fazer INSERT/UPDATE nesse campo
    @Override
    public String convertToDatabaseColumn(String cnsTextoPuro) {
        if (cnsTextoPuro == null) return null;
        return criptografiaService.criptografar(cnsTextoPuro);
    }

    // Roda sozinho, toda vez que o Hibernate fizer SELECT desse campo
    @Override
    public String convertToEntityAttribute(String cnsCriptografado) {
        if (cnsCriptografado == null) return null;
        return criptografiaService.descriptografar(cnsCriptografado);
    }
}