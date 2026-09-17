package br.edu.ial.produtosapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome e obrigatorio")
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank(message = "A senha e obrigatoria")
    @Column(nullable = false)
    private String senha;

    @NotNull(message = "A role e obrigatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleUsuario role;

    public Usuario() {}

    public Usuario(String nome, String senha, RoleUsuario role) {
        this.nome = nome;
        this.senha = senha;
        this.role = role;
    }
}
