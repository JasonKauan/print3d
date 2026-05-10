package com.print3d.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.print3d.api.dto.request.MembroRequest;
import com.print3d.api.dto.response.MembroResponse;
import com.print3d.api.model.Membro;
import com.print3d.api.repository.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembroService {

    private final MembroRepository membroRepository;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;

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

    public MembroResponse buscarPorEmail(String email) {
        Membro membro = membroRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado: " + email));
        return MembroResponse.from(membro);
    }

    public MembroResponse criar(MembroRequest request) {
        if (membroRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + request.getEmail());
        }

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

    public MembroResponse atualizar(Long id, MembroRequest request, String emailRequisitante) {
        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado: " + id));

        Membro requisitante = membroRepository.findByEmail(emailRequisitante)
                .orElseThrow(() -> new RuntimeException("Requisitante não encontrado"));

        // Só o primeiro admin cadastrado (menor id) pode rebaixar outros admins para MEMBRO
        if (membro.getRole() == Membro.Role.ADMIN
                && request.getRole() == Membro.Role.MEMBRO) {

            Long primeiroAdminId = membroRepository.findAll()
                    .stream()
                    .filter(m -> m.getRole() == Membro.Role.ADMIN)
                    .mapToLong(Membro::getId)
                    .min()
                    .orElse(-1L);

            if (!requisitante.getId().equals(primeiroAdminId)) {
                throw new RuntimeException("Apenas o administrador principal pode rebaixar outros admins.");
            }
        }

        membro.setNome(request.getNome());
        if (request.getEmail() != null) membro.setEmail(request.getEmail());
        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            membro.setSenha(passwordEncoder.encode(request.getSenha()));
        }
        if (request.getRole() != null)   membro.setRole(request.getRole());
        if (request.getStatus() != null) membro.setStatus(request.getStatus());
        membro.setDataEntrada(request.getDataEntrada());
        membro.setDataSaida(request.getDataSaida());

        return MembroResponse.from(membroRepository.save(membro));
    }

    public void deletar(Long id, String emailRequisitante) {
        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado: " + id));

        Membro requisitante = membroRepository.findByEmail(emailRequisitante)
                .orElseThrow(() -> new RuntimeException("Requisitante não encontrado"));

        // Admin não pode se deletar
        if (membro.getId().equals(requisitante.getId())) {
            throw new RuntimeException("Você não pode remover sua própria conta.");
        }

        membroRepository.deleteById(id);
    }

    // Membro troca a própria senha — valida a senha atual antes
    public void trocarSenha(String email, String senhaAtual, String novaSenha) {
        Membro membro = membroRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        if (!passwordEncoder.matches(senhaAtual, membro.getSenha())) {
            throw new RuntimeException("Senha atual incorreta.");
        }

        if (novaSenha == null || novaSenha.length() < 6) {
            throw new RuntimeException("Nova senha deve ter no mínimo 6 caracteres.");
        }

        membro.setSenha(passwordEncoder.encode(novaSenha));
        membroRepository.save(membro);
    }

    // Membro atualiza a própria foto de perfil
    public MembroResponse atualizarFoto(String email, MultipartFile foto) throws IOException {
        Membro membro = membroRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        // Upload para o Cloudinary na pasta perfis/
        Map<?, ?> resultado = cloudinary.uploader().upload(
                foto.getBytes(),
                ObjectUtils.asMap("folder", "print3d/perfis")
        );

        membro.setFotoUrl((String) resultado.get("secure_url"));
        return MembroResponse.from(membroRepository.save(membro));
    }
}