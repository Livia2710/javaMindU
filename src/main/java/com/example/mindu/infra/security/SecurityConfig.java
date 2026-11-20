package com.example.mindu.infra.security;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.*;
import org.springframework.beans.factory.annotation.Value ;
import java.util.List;
import java.io.IOException;
import java.time.Duration;

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

                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, exception) -> escreverErro(
                                response, HttpStatus.UNAUTHORIZED, "Não autenticado ou token inválido/expirado"))
                        .accessDeniedHandler((request, response, exception) -> escreverErro(
                                response, HttpStatus.FORBIDDEN, "Sem permissão para este recurso")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v1/auth/login",
                                "/v1/planos",
                                "/error",
                                "/v1/empresas/validar-matricula"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/clientes", "/v1/empresas", "/v1/profissionais").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/profissionais", "/v1/profissionais/*", "/v1/profissionais/*/foto", "/v1/profissionais/*/banner").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/profissionais/*/horarios-disponiveis").permitAll()
                        .requestMatchers("/v1/admin/**", "/v1/profissionais/*/aprovacao").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/v1/profissionais/*/carteirinha").hasRole("PROFISSIONAL")
                        .requestMatchers(HttpMethod.DELETE, "/v1/clientes/*").hasAnyRole("EMPRESA", "ADMIN")
                        .requestMatchers("/v1/conta/**").hasAnyRole("CLIENTE", "EMPRESA", "PROFISSIONAL")
                        .requestMatchers("/v1/auth/me").authenticated()
                        .requestMatchers("/v1/chat/**").hasAnyRole("CLIENTE", "PROFISSIONAL", "EMPRESA", "ADMIN")
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
                        .requestMatchers(HttpMethod.PATCH, "/v1/agendamentos/*/link-online").hasAnyRole("PROFISSIONAL", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/v1/agendamentos/*/realizacao").hasAnyRole("PROFISSIONAL", "ADMIN")
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

    private void escreverErro(HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"erro\":\"" + mensagem + "\",\"status\":" + status.value() + "}");
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${mindu.cors.origens}") List<String> origens) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(origens);
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        c.setExposedHeaders(List.of("X-Total-Count", "X-Page", "X-Page-Size", "X-Total-Pages", "Retry-After"));
        c.setMaxAge(Duration.ofHours(1));
        // O Android nativo (Retrofit/OkHttp) não envia Origin e não depende de CORS;
        // CORS é uma restrição aplicada pelos navegadores, portanto 10.0.2.2 não é origem.
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);
        return src;
    }
}
