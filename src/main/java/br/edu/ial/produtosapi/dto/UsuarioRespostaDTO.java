package br.edu.ial.produtosapi.dto;

import br.edu.ial.produtosapi.model.RoleUsuario;

public record UsuarioRespostaDTO(
        Long id,
        String nome,
        RoleUsuario role
) {}
