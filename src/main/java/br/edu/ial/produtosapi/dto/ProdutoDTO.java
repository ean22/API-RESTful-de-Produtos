package br.edu.ial.produtosapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ProdutoDTO(
        Long id,
        
        @NotBlank(message = "O nome e obrigatorio") String nome,
        String descricao,
        
        @Positive(message = "O preco deve ser positivo") BigDecimal preco,
        Integer estoque
) {}
