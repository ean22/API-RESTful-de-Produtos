package br.edu.ial.produtosapi.service;

import br.edu.ial.produtosapi.dto.ProdutoDTO;
import br.edu.ial.produtosapi.exception.ResourceNotFoundException;
import br.edu.ial.produtosapi.model.Produto;
import br.edu.ial.produtosapi.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ProdutoService {
    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public List<ProdutoDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProdutoDTO buscarPorId(Long id) {
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produto nao encontrado com id: " + id));
       
        return toDTO(produto);
    }

    public ProdutoDTO criar(ProdutoDTO dto) {
        Produto produto = new Produto(
            dto.nome(), 
            dto.descricao(), 
            dto.preco(), 
            dto.estoque()
        );
        
        return toDTO(repository.save(produto));
    }

    public ProdutoDTO atualizar(Long id, ProdutoDTO dto) {
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produto nao encontrado com id: " + id));
        
        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setEstoque(dto.estoque());
        
        return toDTO(repository.save(produto));
    }

    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Produto nao encontrado com id: " + id);
        }
        repository.deleteById(id);
    }

    private ProdutoDTO toDTO(Produto produto) {
        return new ProdutoDTO(produto.getId(),
            produto.getNome(), 
            produto.getDescricao(), 
            produto.getPreco(), 
            produto.getEstoque()
        );
    }

    public List<ProdutoDTO> seed() {
        List<ProdutoDTO> produtos = new ArrayList<>(List.of(
            new ProdutoDTO(0L,"laranja", "alimento", BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10.0, 500.0)), ThreadLocalRandom.current().nextInt(1, 101)),
            new ProdutoDTO(0L,"banana", "alimento", BigDecimal.valueOf(2), ThreadLocalRandom.current().nextInt(1, 101)),
            new ProdutoDTO(0L,"ovo", "alimento", BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10.0, 500.0)), ThreadLocalRandom.current().nextInt(1, 101)),
            new ProdutoDTO(0L,"controle", "eletronico", BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10.0, 500.0)), ThreadLocalRandom.current().nextInt(1, 101)),
            new ProdutoDTO(0L,"calça", "vestimenta", BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10.0, 500.0)), ThreadLocalRandom.current().nextInt(1, 101)),
            new ProdutoDTO(0L,"camisa", "vestimenta", BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10.0, 500.0)), ThreadLocalRandom.current().nextInt(1, 101)),
            new ProdutoDTO(0L,"short", "vestimenta", BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10.0, 500.0)), ThreadLocalRandom.current().nextInt(1, 101)),
            new ProdutoDTO(0L,"tenis", "vestimenta", BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10.0, 500.0)), ThreadLocalRandom.current().nextInt(1, 101)),
            new ProdutoDTO(0L,"cardarço", "vestimenta", BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10.0, 500.0)), ThreadLocalRandom.current().nextInt(1, 101)),
            new ProdutoDTO(0L,"achocolatado", "alimento", BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10.0, 500.0)), ThreadLocalRandom.current().nextInt(1, 101))
        ));

        List<ProdutoDTO> produtosSalvos = new ArrayList<>();
        
        produtos.forEach(p -> produtosSalvos.add(criar(p)));

        return produtosSalvos;
    }
}
