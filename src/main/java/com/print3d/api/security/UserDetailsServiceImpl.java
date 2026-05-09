package com.print3d.api.security;

import com.print3d.api.model.Membro;
import com.print3d.api.repository.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// Implementação do contrato do Spring Security para carregar usuários
// O Spring chama loadUserByUsername() automaticamente durante a autenticação
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MembroRepository membroRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Busca o membro pelo email — lança exceção se não encontrar
        Membro membro = membroRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Membro não encontrado: " + email));

        // Converte o Role do Membro para o formato que o Spring Security entende
        // "ROLE_" é prefixo obrigatório para o Spring reconhecer como role
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + membro.getRole().name());

        // Retorna o UserDetails com email, senha hash e permissões
        return new User(membro.getEmail(), membro.getSenha(), List.of(authority));
    }
}