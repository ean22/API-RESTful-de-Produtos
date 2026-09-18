package br.edu.ial.produtosapi.controller;

import br.edu.ial.produtosapi.model.Produto;
import br.edu.ial.produtosapi.repository.ProdutoRepository;
import br.edu.ial.produtosapi.service.ServicoRateLimit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ProdutoControllerRateLimitTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ServicoRateLimit servicoRateLimit;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        produtoRepository.deleteAll();
        servicoRateLimit.limparBaldes();

        produtoRepository.save(new Produto("Notebook", "Notebook Gamer", BigDecimal.valueOf(4500), 5));
    }

    @Test
    void devePermitirAteCincoRequisicoesERejeitarASextaComStatus429() throws Exception {
        String ipTeste = "192.168.1.100";

        // As primeiras 5 requisições devem passar com 200 OK
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(get("/api/produtos")
                            .with(request -> {
                                request.setRemoteAddr(ipTeste);
                                return request;
                            }))
                    .andExpect(status().isOk());
        }

        // A 6ª requisição imediata deve ser bloqueada com 429 Too Many Requests
        mockMvc.perform(get("/api/produtos")
                        .with(request -> {
                            request.setRemoteAddr(ipTeste);
                            return request;
                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.erro").value("Muitas requisicoes"))
                .andExpect(jsonPath("$.mensagem").value("Limite de requisicoes excedido. Maximo permitido: 5 requisicoes por minuto."));
    }

    @Test
    void ipsDiferentesDevemPossuirBaldesDeTokensIndependentes() throws Exception {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        // Esgota o limite do ip1 (5 requisições)
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(get("/api/produtos")
                            .with(request -> {
                                request.setRemoteAddr(ip1);
                                return request;
                            }))
                    .andExpect(status().isOk());
        }

        // ip1 agora recebe 429
        mockMvc.perform(get("/api/produtos")
                        .with(request -> {
                            request.setRemoteAddr(ip1);
                            return request;
                        }))
                .andExpect(status().isTooManyRequests());

        // ip2 deve conseguir realizar requisição normalmente (200 OK)
        mockMvc.perform(get("/api/produtos")
                        .with(request -> {
                            request.setRemoteAddr(ip2);
                            return request;
                        }))
                .andExpect(status().isOk());
    }
}
