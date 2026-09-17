package br.edu.ial.produtosapi.dto;

import br.edu.ial.produtosapi.model.RoleUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioDTO(
        Long id,

        @NotBlank(message = "O nome e obrigatorio")
        String nome,

        @NotBlank(message = "A senha e obrigatoria")
        String senha,

        @NotNull(message = "A role e obrigatoria")
        RoleUsuario role
) {}
