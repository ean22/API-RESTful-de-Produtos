package br.edu.ial.produtosapi.controller;

import br.edu.ial.produtosapi.dto.UsuarioDTO;
import br.edu.ial.produtosapi.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UsuarioDTO>>> listar() {
        List<UsuarioDTO> usuarios = service.listarTodos();

        List<EntityModel<UsuarioDTO>> usuariosEntity =
            usuarios.stream().map(usuario -> EntityModel.of(
                usuario,
                linkTo(methodOn(
                    UsuarioController.class)
                    .buscar(usuario.id()))
                    .withSelfRel()
                )).toList();

        CollectionModel<EntityModel<UsuarioDTO>> colecao = CollectionModel.of(usuariosEntity);

        colecao.add(linkTo(methodOn(UsuarioController.class).listar()).withSelfRel());

        return ResponseEntity.ok(colecao);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioDTO>> buscar(@PathVariable Long id) {
        UsuarioDTO usuario = service.buscarPorId(id);

        EntityModel<UsuarioDTO> usuarioEntity = EntityModel.of(
            usuario,

            linkTo(methodOn(UsuarioController.class)
                .buscar(id))
                .withSelfRel(),

            linkTo(methodOn(UsuarioController.class)
                .remover(id))
                .withRel("deletar"),

            linkTo(methodOn(UsuarioController.class)
                .atualizar(id, null))
                .withRel("atualizar"),

            linkTo(methodOn(UsuarioController.class)
                .listar())
                .withRel("usuarios")
            );

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(usuarioEntity);
    }

    @PostMapping
    public ResponseEntity<EntityModel<UsuarioDTO>> criar(@Valid @RequestBody UsuarioDTO dto) {
        UsuarioDTO usuario = service.criar(dto);

        EntityModel<UsuarioDTO> usuarioEntity = EntityModel.of(
            usuario,
            linkTo(methodOn(UsuarioController.class).buscar(usuario.id())).withSelfRel()
        );

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(usuarioEntity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioDTO>> atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioDTO dto) {
        UsuarioDTO usuario = service.atualizar(id, dto);
        EntityModel<UsuarioDTO> usuarioEntity = EntityModel.of(
            usuario,
            linkTo(methodOn(UsuarioController.class).buscar(id)).withSelfRel()
        );

        return ResponseEntity.ok(usuarioEntity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
