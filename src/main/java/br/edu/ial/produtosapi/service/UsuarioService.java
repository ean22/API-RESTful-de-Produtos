package br.edu.ial.produtosapi.service;

import br.edu.ial.produtosapi.dto.UsuarioRequisicaoDTO;
import br.edu.ial.produtosapi.dto.UsuarioRespostaDTO;
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

    public List<UsuarioRespostaDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public UsuarioRespostaDTO buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario nao encontrado com id: " + id));

        return converterParaDTO(usuario);
    }

    public UsuarioRespostaDTO criar(UsuarioRequisicaoDTO dto) {
        String senhaCriptografada = passwordEncoder.encode(dto.senha());
        Usuario usuario = new Usuario(
            dto.nome(),
            senhaCriptografada,
            dto.role()
        );

        return converterParaDTO(repository.save(usuario));
    }

    public UsuarioRespostaDTO atualizar(Long id, UsuarioRequisicaoDTO dto) {
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

    private UsuarioRespostaDTO converterParaDTO(Usuario usuario) {
        return new UsuarioRespostaDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getRole()
        );
    }
}
