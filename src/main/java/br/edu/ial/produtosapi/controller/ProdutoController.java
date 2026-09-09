package br.edu.ial.produtosapi.controller;

import br.edu.ial.produtosapi.dto.ProdutoDTO;
import br.edu.ial.produtosapi.service.ProdutoService;
import jakarta.validation.Valid;

import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProdutoDTO>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public EntityModel<ProdutoDTO> buscar(@PathVariable Long id) {
        ProdutoDTO produto = service.buscarPorId(id);
        
        return EntityModel.of(
            produto,
            linkTo(methodOn(ProdutoController.class).buscar(id)).withSelfRel()
        );
    }

    @PostMapping
    public ResponseEntity<ProdutoDTO> criar (@Valid @RequestBody ProdutoDTO dto) {
        ProdutoDTO criado = service.criar(dto);
        return ResponseEntity.created(URI.create("/api/produtos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoDTO> atualizar(@Valid @PathVariable Long id, @RequestBody ProdutoDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping ("/seed")
    public ResponseEntity<List<ProdutoDTO>> seed() {
        List<ProdutoDTO> produtos = service.seed();

        return ResponseEntity.created(produtos);
    }
}
