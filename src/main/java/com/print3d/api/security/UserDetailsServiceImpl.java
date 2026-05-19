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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MembroRepository membroRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Membro membro = membroRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Membro não encontrado: " + email));

        // Bloqueia login de membro inativo
        if (membro.getStatus() == Membro.Status.INATIVO) {
            throw new UsernameNotFoundException("Conta inativa. Entre em contato com o administrador.");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // DEV tem todas as permissões: ROLE_DEV + ROLE_ADMIN + ROLE_MEMBRO
        // ADMIN tem: ROLE_ADMIN + ROLE_MEMBRO
        // MEMBRO tem: ROLE_MEMBRO
        switch (membro.getRole()) {
            case DEV -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_DEV"));
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_MEMBRO"));
            }
            case ADMIN -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_MEMBRO"));
            }
            case MEMBRO -> authorities.add(new SimpleGrantedAuthority("ROLE_MEMBRO"));
        }

        return new User(membro.getEmail(), membro.getSenha(), authorities);
    }
}