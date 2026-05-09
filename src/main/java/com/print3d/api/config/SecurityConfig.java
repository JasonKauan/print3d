package com.print3d.api.config;

import com.print3d.api.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // habilita @PreAuthorize nos controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Desabilita CSRF — APIs REST stateless não precisam
                .csrf(AbstractHttpConfigurer::disable)

                // Configura CORS para aceitar requisições do frontend React
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Regras de acesso por rota
                .authorizeHttpRequests(auth -> auth

                        // Rotas públicas — não precisam de token
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Catálogo público — qualquer um pode ver os produtos
                        .requestMatchers(HttpMethod.GET, "/api/v1/produtos/**").permitAll()

                        // Só ADMIN pode gerenciar membros e ver financeiro completo
                        .requestMatchers("/api/v1/membros/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/vendas/resumo").hasRole("ADMIN")

                        // Qualquer usuário autenticado pode registrar impressões e ver vendas próprias
                        .anyRequest().authenticated()
                )

                // Stateless — sem sessão no servidor, cada request traz o token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Registra o provider de autenticação (DB + BCrypt)
                .authenticationProvider(authenticationProvider())

                // Insere o JwtFilter ANTES do filtro padrão de usuário/senha do Spring
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    // Configura como o Spring vai autenticar: busca no DB e compara com BCrypt
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // onde buscar o usuário
        provider.setPasswordEncoder(passwordEncoder());      // como verificar a senha
        return provider;
    }

    // BCrypt é o padrão atual para hash de senhas — nunca guarda senha em texto puro
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager é usado pelo AuthController para fazer o login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // CORS: permite que o frontend React (localhost:5173) chame a API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Em produção, troca pelo domínio real do frontend
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // aplica para todas as rotas
        return source;
    }
}