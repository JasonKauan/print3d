package com.print3d.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// OncePerRequestFilter garante que esse filtro roda UMA VEZ por requisição
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Pega o header Authorization da requisição
        // Exemplo de valor: "Bearer eyJhbGciOiJIUzI1NiJ9..."
        String authHeader = request.getHeader("Authorization");

        // Se não tem header ou não começa com "Bearer ", passa adiante sem autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Remove o prefixo "Bearer " para ficar só o token
        String token = authHeader.substring(7);

        try {
            // Extrai o email gravado dentro do token
            String email = jwtUtil.extrairEmail(token);

            // Só processa se o email foi extraído e o usuário ainda não está autenticado
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Busca o usuário no banco pelo email
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Valida o token (email bate + não expirou)
                if (jwtUtil.tokenValido(token, userDetails)) {

                    // Cria o objeto de autenticação com as permissões do usuário
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,                        // credentials = null (já autenticado)
                                    userDetails.getAuthorities() // permissões/roles
                            );

                    // Anexa detalhes da requisição (IP, session) ao token de autenticação
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Registra o usuário como autenticado no contexto de segurança do Spring
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token inválido ou expirado — simplesmente não autentica
            // O Spring Security vai retornar 401 automaticamente nas rotas protegidas
        }

        // Passa para o próximo filtro da cadeia
        filterChain.doFilter(request, response);
    }
}