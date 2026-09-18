package br.edu.ial.produtosapi.controller;

import br.edu.ial.produtosapi.dto.ProdutoDTO;
import br.edu.ial.produtosapi.model.Produto;
import br.edu.ial.produtosapi.model.RoleUsuario;
import br.edu.ial.produtosapi.model.Usuario;
import br.edu.ial.produtosapi.repository.ProdutoRepository;
import br.edu.ial.produtosapi.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ProdutoControllerSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        produtoRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Cadastra um usuario ADM e um usuario USER no banco de dados
        usuarioRepository.save(new Usuario("admin", passwordEncoder.encode("admin123"), RoleUsuario.ADM));
        usuarioRepository.save(new Usuario("comum", passwordEncoder.encode("user123"), RoleUsuario.USER));
    }

    @Test
    void devePermitirListarEBuscarProdutosParaQualquerUsuario() throws Exception {
        Produto produto = produtoRepository.save(new Produto("Teclado", "Teclado mecanico", BigDecimal.valueOf(150), 10));

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/produtos/" + produto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Teclado"));
    }

    @Test
    void naoDevePermitirCriarProdutoSemAutenticacao() throws Exception {
        ProdutoDTO dto = new ProdutoDTO(null, "Mouse", "Mouse gamer", BigDecimal.valueOf(80), 5);

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void naoDevePermitirCriarProdutoComRoleUser() throws Exception {
        ProdutoDTO dto = new ProdutoDTO(null, "Mouse", "Mouse gamer", BigDecimal.valueOf(80), 5);

        mockMvc.perform(post("/api/produtos")
                        .with(httpBasic("comum", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirCriarProdutoComRoleAdm() throws Exception {
        ProdutoDTO dto = new ProdutoDTO(null, "Mouse", "Mouse gamer", BigDecimal.valueOf(80), 5);

        mockMvc.perform(post("/api/produtos")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Mouse"));
    }

    @Test
    void naoDevePermitirAtualizarProdutoComRoleUser() throws Exception {
        Produto produto = produtoRepository.save(new Produto("Monitor", "Monitor 24pol", BigDecimal.valueOf(600), 2));
        ProdutoDTO dto = new ProdutoDTO(null, "Monitor Atualizado", "Monitor 27pol", BigDecimal.valueOf(800), 4);

        mockMvc.perform(put("/api/produtos/" + produto.getId())
                        .with(httpBasic("comum", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAtualizarProdutoComRoleAdm() throws Exception {
        Produto produto = produtoRepository.save(new Produto("Monitor", "Monitor 24pol", BigDecimal.valueOf(600), 2));
        ProdutoDTO dto = new ProdutoDTO(null, "Monitor Atualizado", "Monitor 27pol", BigDecimal.valueOf(800), 4);

        mockMvc.perform(put("/api/produtos/" + produto.getId())
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Monitor Atualizado"));
    }

    @Test
    void naoDevePermitirRemoverProdutoComRoleUser() throws Exception {
        Produto produto = produtoRepository.save(new Produto("Headset", "Headset 7.1", BigDecimal.valueOf(250), 3));

        mockMvc.perform(delete("/api/produtos/" + produto.getId())
                        .with(httpBasic("comum", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirRemoverProdutoComRoleAdm() throws Exception {
        Produto produto = produtoRepository.save(new Produto("Headset", "Headset 7.1", BigDecimal.valueOf(250), 3));

        mockMvc.perform(delete("/api/produtos/" + produto.getId())
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
    }
}
