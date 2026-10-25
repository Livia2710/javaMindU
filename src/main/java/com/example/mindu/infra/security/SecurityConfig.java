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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.*;
import org.springframework.beans.factory.annotation.Value ;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                // CSRF protege formulários HTML com sessão de navegador. Sua API é
                // stateless (sem sessão) e usa token, então esse ataque não se aplica
                // do mesmo jeito — desligar é padrão em API REST pura.
                .csrf(csrf -> csrf.disable())

                // Diz pro Spring "nunca crie sessão HTTP" — cada requisição se
                // autentica sozinha via token (é o JwtFilter que faz isso acima).
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/auth/login", "/v1/planos", "/error", "/v1/empresas/validar-matricula").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/clientes", "/v1/empresas", "/v1/profissionais").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/profissionais", "/v1/profissionais/*", "/v1/profissionais/*/foto", "/v1/profissionais/*/banner").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/profissionais/*/horarios-disponiveis").permitAll()
                        .requestMatchers("/v1/admin/**", "/v1/profissionais/*/aprovacao").hasRole("ADMIN")
                        .requestMatchers("/v1/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/profissionais/conta/perfil").hasRole("PROFISSIONAL")
                        .requestMatchers("/v1/profissionais/*/locais", "/v1/profissionais/*/disponibilidade", "/v1/profissionais/*/perfil").hasAnyRole("PROFISSIONAL", "ADMIN")
                        .requestMatchers("/v1/empresas/me/**", "/v1/empresas/me").hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.GET, "/v1/empresas/*").hasAnyRole("EMPRESA", "ADMIN")
                        .requestMatchers("/v1/empresas/**").hasAnyRole("EMPRESA", "ADMIN")
                        .requestMatchers("/v1/clientes/me/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/v1/clientes/me", "/v1/clientes/*").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/clientes").hasAnyRole("EMPRESA", "ADMIN")
                        .requestMatchers("/v1/clientes/*/aprovacao").hasAnyRole("EMPRESA", "ADMIN")
                        .requestMatchers("/v1/clientes/*/dados-sensiveis").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/agendamentos").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/agendamentos/meus").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/agendamentos/minha-agenda").hasRole("PROFISSIONAL")
                        .requestMatchers("/v1/agendamentos/*/cancelamento", "/v1/agendamentos/*/horario").hasAnyRole("CLIENTE", "PROFISSIONAL", "ADMIN")
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

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${mindu.cors.origens}") List<String> origens) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(origens);
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);
        return src;
    }
}
