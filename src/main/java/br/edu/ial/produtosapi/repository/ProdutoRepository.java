package br.edu.ial.produtosapi.repository;

import br.edu.ial.produtosapi.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByNomeContainingIgnoreCase(String nome);
    List<Produto> findByEstoqueLessThan(Integer quantidade);
}
