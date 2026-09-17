package br.edu.ial.produtosapi.controller;

import br.edu.ial.produtosapi.dto.UsuarioRequisicaoDTO;
import br.edu.ial.produtosapi.dto.UsuarioRespostaDTO;
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
    public ResponseEntity<CollectionModel<EntityModel<UsuarioRespostaDTO>>> listar() {
        List<UsuarioRespostaDTO> usuarios = service.listarTodos();

        List<EntityModel<UsuarioRespostaDTO>> usuariosEntity =
            usuarios.stream().map(usuario -> EntityModel.of(
                usuario,
                linkTo(methodOn(
                    UsuarioController.class)
                    .buscar(usuario.id()))
                    .withSelfRel()
                )).toList();

        CollectionModel<EntityModel<UsuarioRespostaDTO>> colecao = CollectionModel.of(usuariosEntity);

        colecao.add(linkTo(methodOn(UsuarioController.class).listar()).withSelfRel());

        return ResponseEntity.ok(colecao);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioRespostaDTO>> buscar(@PathVariable Long id) {
        UsuarioRespostaDTO usuario = service.buscarPorId(id);

        EntityModel<UsuarioRespostaDTO> usuarioEntity = EntityModel.of(
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
    public ResponseEntity<EntityModel<UsuarioRespostaDTO>> criar(@Valid @RequestBody UsuarioRequisicaoDTO dto) {
        UsuarioRespostaDTO usuario = service.criar(dto);

        EntityModel<UsuarioRespostaDTO> usuarioEntity = EntityModel.of(
            usuario,
            linkTo(methodOn(UsuarioController.class).buscar(usuario.id())).withSelfRel()
        );

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(usuarioEntity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioRespostaDTO>> atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequisicaoDTO dto) {
        UsuarioRespostaDTO usuario = service.atualizar(id, dto);
        EntityModel<UsuarioRespostaDTO> usuarioEntity = EntityModel.of(
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
