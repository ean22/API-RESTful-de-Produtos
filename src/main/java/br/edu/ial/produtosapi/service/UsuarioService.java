package br.edu.ial.produtosapi.service;

import br.edu.ial.produtosapi.dto.UsuarioDTO;
import br.edu.ial.produtosapi.exception.ResourceNotFoundException;
import br.edu.ial.produtosapi.model.Usuario;
import br.edu.ial.produtosapi.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public UsuarioDTO buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario nao encontrado com id: " + id));

        return converterParaDTO(usuario);
    }

    public UsuarioDTO criar(UsuarioDTO dto) {
        String senhaCriptografada = passwordEncoder.encode(dto.senha());
        Usuario usuario = new Usuario(
            dto.nome(),
            senhaCriptografada,
            dto.role()
        );

        return converterParaDTO(repository.save(usuario));
    }

    public UsuarioDTO atualizar(Long id, UsuarioDTO dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario nao encontrado com id: " + id));

        usuario.setNome(dto.nome());
        usuario.setRole(dto.role());
        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        } else {
            throw new IllegalArgumentException("Senha nao pode ser nula ou vazia");
        }

        return converterParaDTO(repository.save(usuario));
    }

    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario nao encontrado com id: " + id);
        }
        repository.deleteById(id);
    }

    private UsuarioDTO converterParaDTO(Usuario usuario) {
        return new UsuarioDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getSenha(),
            usuario.getRole()
        );
    }
}
