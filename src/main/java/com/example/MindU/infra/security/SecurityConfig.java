package com.example.mindu.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF protege formulários HTML com sessão de navegador. Sua API é
                // stateless (sem sessão) e usa token, então esse ataque não se aplica
                // do mesmo jeito — desligar é padrão em API REST pura.
                .csrf(csrf -> csrf.disable())

                // Diz pro Spring "nunca crie sessão HTTP" — cada requisição se
                // autentica sozinha via token (é o JwtFilter que faz isso acima).
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Rotas que um visitante SEM token pode acessar: login e os
                        // 3 cadastros públicos (Cliente, Empresa, Profissional).
                        .requestMatchers("/v1/auth/**", "/v1/clientes", "/v1/empresas", "/v1/profissionais").permitAll()
                        // Qualquer outra rota exige estar autenticado (token válido).
                        .anyRequest().authenticated()
                )

                // Insere o JwtFilter ANTES do filtro padrão de autenticação do Spring
                // Security — precisa rodar cedo, porque é ele quem popula o
                // SecurityContextHolder que a regra ".authenticated()" acima vai checar.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // RNF01 — nunca salvar senha em texto puro. BCrypt gera um hash com
        // "salt" embutido automaticamente (duas senhas iguais geram hashes diferentes).
        return new BCryptPasswordEncoder();
    }
}