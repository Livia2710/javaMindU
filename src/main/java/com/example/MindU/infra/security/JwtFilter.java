package com.example.mindu.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor // gera o construtor que injeta o JwtService automaticamente
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Não tem header, ou não começa com "Bearer " → deixa passar sem autenticar.
        // Não é erro aqui — quem decide se a rota PRECISA de autenticação é o
        // SecurityConfig, não o filtro. O filtro só "tenta autenticar se der".
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // passa pro próximo da cadeia (outro filtro, ou o Controller)
            return;
        }

        String token = header.substring(7); // remove os 7 caracteres de "Bearer " e sobra só o token

        if (jwtService.tokenValido(token)) {
            String email = jwtService.extrairEmail(token);

            // Isso é o "avisa pro Spring Security que esse usuário está autenticado".
            // Os 3 parâmetros são: principal (quem é — aqui, o email), credentials
            // (senha — null porque já validamos via token, não precisa mais dela),
            // authorities (permissões/roles — List.of() = nenhuma, por isso hoje
            // o Security não distingue Cliente de Admin, como comentei antes).
            var auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        // Se o token for inválido, simplesmente não autentica ninguém — a requisição
        // segue "anônima", e cai na regra do SecurityConfig (que vai barrar com 401
        // se a rota não for permitAll()).

        filterChain.doFilter(request, response); // sempre deixa a requisição seguir
    }
}