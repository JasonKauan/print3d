package com.print3d.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

// Resposta do login — o token que o frontend vai guardar e enviar em cada request
@Data @Builder @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String nome;
    private String role;
}