package com.print3d.api.service;

import com.print3d.api.dto.request.MembroRequest;
import com.print3d.api.dto.response.MembroResponse;
import com.print3d.api.model.Membro;
import com.print3d.api.repository.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembroService {

    private final MembroRepository membroRepository;
    private final PasswordEncoder passwordEncoder;

    public List<MembroResponse> listarTodos() {
        return membroRepository.findAll()
                .stream()
                .map(MembroResponse::from)
                .collect(Collectors.toList());
    }

    public List<MembroResponse> listarPorStatus(Membro.Status status) {
        return membroRepository.findByStatus(status)
                .stream()
                .map(MembroResponse::from)
                .collect(Collectors.toList());
    }

    public MembroResponse buscarPorId(Long id) {
        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado: " + id));
        return MembroResponse.from(membro);
    }

    public MembroResponse criar(MembroRequest request) {
        if (membroRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + request.getEmail());
        }

        // Senha é obrigatória na criação — membro precisa conseguir fazer login
        if (request.getSenha() == null || request.getSenha().isBlank()) {
            throw new RuntimeException("Senha é obrigatória para criar um membro.");
        }

        Membro membro = Membro.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(request.getRole() != null ? request.getRole() : Membro.Role.MEMBRO)
                .status(request.getStatus() != null ? request.getStatus() : Membro.Status.ATIVO)
                .dataEntrada(request.getDataEntrada())
                .dataSaida(request.getDataSaida())
                .build();

        return MembroResponse.from(membroRepository.save(membro));
    }

    public MembroResponse atualizar(Long id, MembroRequest request) {
        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado: " + id));

        membro.setNome(request.getNome());
        if (request.getEmail() != null) membro.setEmail(request.getEmail());
        // Só atualiza senha se veio uma nova no request
        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            membro.setSenha(passwordEncoder.encode(request.getSenha()));
        }
        if (request.getRole() != null)   membro.setRole(request.getRole());
        if (request.getStatus() != null) membro.setStatus(request.getStatus());
        membro.setDataEntrada(request.getDataEntrada());
        membro.setDataSaida(request.getDataSaida());

        return MembroResponse.from(membroRepository.save(membro));
    }

    public void deletar(Long id) {
        if (!membroRepository.existsById(id)) {
            throw new RuntimeException("Membro não encontrado: " + id);
        }
        membroRepository.deleteById(id);
    }
}