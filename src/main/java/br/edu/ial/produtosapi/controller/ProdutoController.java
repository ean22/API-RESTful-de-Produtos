package br.edu.ial.produtosapi.controller;

import br.edu.ial.produtosapi.dto.ProdutoDTO;
import br.edu.ial.produtosapi.service.ProdutoService;
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
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ProdutoDTO>>> listar() {
        List<ProdutoDTO> produtos = service.listarTodos();
        
        List<EntityModel<ProdutoDTO>> produtosEntity = 
            produtos.stream().map(produto -> EntityModel.of(
                produto, 
                linkTo(methodOn(
                    ProdutoController.class)
                    .buscar(produto.id()))
                    .withSelfRel()
                )).toList();

        CollectionModel<EntityModel<ProdutoDTO>> colecao = CollectionModel.of(produtosEntity);

        colecao.add(linkTo(methodOn(ProdutoController.class).listar()).withSelfRel());

        return ResponseEntity.ok(colecao);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ProdutoDTO>> buscar(@PathVariable Long id) {
        ProdutoDTO produto = service.buscarPorId(id);
        
        EntityModel<ProdutoDTO> produtoEntity = EntityModel.of(
            produto, 
           
            linkTo(methodOn(ProdutoController.class)
                .buscar(id))
                .withSelfRel(),

           
            linkTo(methodOn(ProdutoController.class)
                .remover(id))
                .withRel("deletar"),

         
            linkTo(methodOn(ProdutoController.class)
                .atualizar(id, null))
                .withRel("atualizar"),

            linkTo(methodOn(ProdutoController.class)
                .listar())
                .withRel("produtos")
              
            );

        return  ResponseEntity
            .status(HttpStatus.OK)
            .body(produtoEntity);
    }

    @PostMapping
    public ResponseEntity<EntityModel<ProdutoDTO>> criar (@Valid @RequestBody ProdutoDTO dto) {
        ProdutoDTO produto = service.criar(dto);

        EntityModel<ProdutoDTO> produtoEntity = EntityModel.of(
            produto,
            linkTo(methodOn(ProdutoController.class).buscar(produto.id())).withSelfRel()
        );
        

       return ResponseEntity
            .status(HttpStatus.OK)
            .body(produtoEntity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ProdutoDTO>> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoDTO dto) {
        
        ProdutoDTO produto = service.atualizar(id, dto);
        EntityModel<ProdutoDTO> produtoEntity = EntityModel.of(
            produto,
            linkTo(methodOn(ProdutoController.class).buscar(id)).withSelfRel()
        );
        
        return ResponseEntity.ok(produtoEntity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping ("/seed")
    public ResponseEntity<List<ProdutoDTO>> seed() {
        return ResponseEntity.ok(service.seed());
    }
}
