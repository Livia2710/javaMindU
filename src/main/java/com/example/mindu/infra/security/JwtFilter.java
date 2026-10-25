package com.example.mindu.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.example.mindu.domain.entity.Usuario;
import io.jsonwebtoken.JwtException;
import com.example.mindu.infra.repository.ClienteRepository;
import com.example.mindu.infra.repository.EmpresaRepository;
import com.example.mindu.infra.repository.ProfissionalRepository;
import com.example.mindu.infra.repository.AdminRepository;
import java.util.Optional;
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
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AdminRepository adminRepository;

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

        try {
            if (jwtService.tokenValido(token)) {
                String email = jwtService.extrairEmail(token);
                int tokenVersion = jwtService.extrairTokenVersion(token);
                Usuario usuario = clienteRepository.findByEmail(email).map(Usuario.class::cast)
                        .or(() -> empresaRepository.findByEmail(email).map(Usuario.class::cast))
                        .or(() -> profissionalRepository.findByEmail(email).map(Usuario.class::cast))
                        .or(() -> adminRepository.findByEmail(email).map(Usuario.class::cast))
                        .orElse(null);
                if (usuario != null && usuario.getTokenVersion() == tokenVersion) {
                    String role = jwtService.extrairRole(token);
                    var auth = new UsernamePasswordAuthenticationToken(email, null,
                            List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }
        // Se o token for inválido, simplesmente não autentica ninguém — a requisição
        // segue "anônima", e cai na regra do SecurityConfig (que vai barrar com 401
        // se a rota não for permitAll()).

        filterChain.doFilter(request, response); // sempre deixa a requisição seguir
    }
}
