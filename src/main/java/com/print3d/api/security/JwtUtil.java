package com.print3d.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Chave secreta lida do application.properties
    @Value("${jwt.secret}")
    private String secret;

    // Tempo de expiração em milissegundos (24h = 86400000)
    @Value("${jwt.expiration}")
    private Long expiration;

    // Converte a string do secret em uma chave criptográfica segura
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Gera um token JWT com o email do usuário como "subject"
    // O subject é o identificador principal gravado dentro do token
    public String gerarToken(String email) {
        return Jwts.builder()
                .subject(email)                                       // quem é o dono do token
                .issuedAt(new Date())                                 // quando foi gerado
                .expiration(new Date(System.currentTimeMillis() + expiration)) // quando expira
                .signWith(getSigningKey())                            // assina com a chave secreta
                .compact();                                           // serializa para string
    }

    // Extrai o email (subject) de dentro do token
    public String extrairEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // verifica a assinatura
                .build()
                .parseSignedClaims(token)     // decodifica o token
                .getPayload()
                .getSubject();               // retorna o subject (email)
    }

    // Verifica se o token é válido:
    // 1. O email dentro do token bate com o usuário logado?
    // 2. O token ainda não expirou?
    public boolean tokenValido(String token, UserDetails userDetails) {
        String email = extrairEmail(token);
        return email.equals(userDetails.getUsername()) && !tokenExpirado(token);
    }

    // Verifica se a data de expiração já passou
    private boolean tokenExpirado(String token) {
        Date expDate = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return expDate.before(new Date());
    }
}