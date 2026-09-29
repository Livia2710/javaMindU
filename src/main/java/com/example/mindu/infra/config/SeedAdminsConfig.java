package com.example.mindu.infra.config;

import com.example.mindu.domain.entity.Admin;
import com.example.mindu.infra.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class SeedAdminsConfig {

    @Bean
    public CommandLineRunner seedAdmins(AdminRepository repo, PasswordEncoder encoder,
                                        @Value("${mindu.admin.senha-padrao:}") String senhaPadrao) {
        return args -> {
            if (senhaPadrao.isBlank()) return; // variável de ambiente não setada — não faz nada, sem quebrar a app

            List<String> nomes = List.of("Livia", "Sabrina", "Manuela", "Tinin");
            for (String nome : nomes) {
                String email = nome.toLowerCase() + "@mindu.local";
                if (!repo.existsByEmail(email)) {
                    repo.save(Admin.builder()
                            .nome(nome)
                            .email(email)
                            .senha(encoder.encode(senhaPadrao))
                            .build());
                }
            }
        };
    }
}