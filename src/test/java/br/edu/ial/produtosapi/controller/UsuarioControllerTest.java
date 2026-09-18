package br.edu.ial.produtosapi.controller;

import br.edu.ial.produtosapi.dto.UsuarioRequisicaoDTO;
import br.edu.ial.produtosapi.dto.UsuarioRespostaDTO;
import br.edu.ial.produtosapi.model.RoleUsuario;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class UsuarioControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

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
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
        usuarioRepository.deleteAll();
    }


    @Test
    void deveCriarUsuarioComSenhaCriptografadaEComRoleAdmSemRetornarSenhaNaResponse() throws Exception {
        UsuarioRequisicaoDTO dto = new UsuarioRequisicaoDTO("Administrador", "senhaSecreta123", RoleUsuario.ADM);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Administrador")))
                .andExpect(jsonPath("$.role", is("ADM")))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$._links.self.href", notNullValue()));

        var usuarios = usuarioRepository.findAll();
        org.junit.jupiter.api.Assertions.assertEquals(1, usuarios.size());
        var usuarioSalvo = usuarios.get(0);
        org.junit.jupiter.api.Assertions.assertEquals("Administrador", usuarioSalvo.getNome());
        org.junit.jupiter.api.Assertions.assertEquals(RoleUsuario.ADM, usuarioSalvo.getRole());
        assertTrue(passwordEncoder.matches("senhaSecreta123", usuarioSalvo.getSenha()));
    }

    @Test
    void deveCriarUsuarioComRoleUserSemRetornarSenhaNaResponse() throws Exception {
        UsuarioRequisicaoDTO dto = new UsuarioRequisicaoDTO("Usuario Normal", "minhasenha", RoleUsuario.USER);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Usuario Normal")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.senha").doesNotExist());

        var usuarios = usuarioRepository.findAll();
        org.junit.jupiter.api.Assertions.assertEquals(1, usuarios.size());
        assertTrue(passwordEncoder.matches("minhasenha", usuarios.get(0).getSenha()));
    }

    @Test
    void deveListarUsuariosSemExporSenhas() throws Exception {
        UsuarioRequisicaoDTO user1 = new UsuarioRequisicaoDTO("User 1", "pass1", RoleUsuario.ADM);
        UsuarioRequisicaoDTO user2 = new UsuarioRequisicaoDTO("User 2", "pass2", RoleUsuario.USER);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1))).andExpect(status().isOk());

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2))).andExpect(status().isOk());

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.usuarioRespostaDTOList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.usuarioRespostaDTOList[0].senha").doesNotExist())
                .andExpect(jsonPath("$._embedded.usuarioRespostaDTOList[1].senha").doesNotExist())
                .andExpect(jsonPath("$._links.self.href", notNullValue()));
    }

    @Test
    void deveBuscarUsuarioPorIdSemExporSenha() throws Exception {
        UsuarioRequisicaoDTO user = new UsuarioRequisicaoDTO("User Teste", "pass123", RoleUsuario.USER);

        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UsuarioRespostaDTO criado = objectMapper.readValue(response, UsuarioRespostaDTO.class);

        mockMvc.perform(get("/api/usuarios/" + criado.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(criado.id().intValue())))
                .andExpect(jsonPath("$.nome", is("User Teste")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.deletar.href", notNullValue()))
                .andExpect(jsonPath("$._links.atualizar.href", notNullValue()));
    }

    @Test
    void deveAtualizarUsuarioSemExporSenha() throws Exception {
        UsuarioRequisicaoDTO user = new UsuarioRequisicaoDTO("User Antigo", "antigaSenha", RoleUsuario.USER);

        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UsuarioRespostaDTO criado = objectMapper.readValue(response, UsuarioRespostaDTO.class);

        UsuarioRequisicaoDTO atualizacao = new UsuarioRequisicaoDTO("User Atualizado", "novaSenha123", RoleUsuario.ADM);

        mockMvc.perform(put("/api/usuarios/" + criado.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("User Atualizado")))
                .andExpect(jsonPath("$.role", is("ADM")))
                .andExpect(jsonPath("$.senha").doesNotExist());

        var usuarioAtualizado = usuarioRepository.findById(criado.id()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("User Atualizado", usuarioAtualizado.getNome());
        org.junit.jupiter.api.Assertions.assertEquals(RoleUsuario.ADM, usuarioAtualizado.getRole());
        assertTrue(passwordEncoder.matches("novaSenha123", usuarioAtualizado.getSenha()));
    }

    @Test
    void deveRemoverUsuario() throws Exception {
        UsuarioRequisicaoDTO user = new UsuarioRequisicaoDTO("User Remover", "pass123", RoleUsuario.USER);

        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UsuarioRespostaDTO criado = objectMapper.readValue(response, UsuarioRespostaDTO.class);

        mockMvc.perform(delete("/api/usuarios/" + criado.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/" + criado.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarErroQuandoCamposInvalidos() throws Exception {
        UsuarioRequisicaoDTO invalido = new UsuarioRequisicaoDTO("", "", null);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", is("Erro de validacao")))
                .andExpect(jsonPath("$.campos.nome", notNullValue()))
                .andExpect(jsonPath("$.campos.senha", notNullValue()))
                .andExpect(jsonPath("$.campos.role", notNullValue()));
    }
}
