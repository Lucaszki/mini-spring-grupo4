package br.com.coworking.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

/**
 * Testes de integracao: sobem o contexto completo do Spring Boot e chamam
 * a API pelo MockMvc (sem abrir porta de rede).
 * O contexto e recriado a cada teste para que os dados em memoria comecem vazios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SalaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static String json(String nome, int capacidade) {
        return "{\"nome\":\"" + nome + "\",\"capacidadePessoas\":" + capacidade + "}";
    }

    private String criarSala(String nome, int capacidade) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(nome, capacidade)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
    }

    static Stream<String> corposInvalidos() {
        return Stream.of(
                "{\"nome\":\"\",\"capacidadePessoas\":10}",
                "{\"nome\":\"   \",\"capacidadePessoas\":10}",
                "{\"nome\":\"Sala\",\"capacidadePessoas\":0}",
                "{\"nome\":\"Sala\",\"capacidadePessoas\":-5}",
                "{\"nome\":\"Sala\"}",
                "{\"capacidadePessoas\":10}",
                "{\"id\":\"x\",\"nome\":\"Sala\",\"capacidadePessoas\":10}",
                "{quebrado",
                "");
    }

    // ---------- 201 / 200 / 204 ----------

    @Test
    void cadastrarRetorna201ComLocationEIdGerado() throws Exception {
        mockMvc.perform(post("/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Sala Master", 25)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/salas/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Sala Master"))
                .andExpect(jsonPath("$.capacidadePessoas").value(25));
    }

    @Test
    void capacidadeNoLimiteDeCemEAceita() throws Exception {
        criarSala("Auditorio", 100);
    }

    @Test
    void listarRetorna200ComTodasAsSalas() throws Exception {
        criarSala("Sala Master", 25);
        criarSala("Sala Azul", 10);

        mockMvc.perform(get("/salas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void filtroPorNomeIgnoraMaiusculasEMinusculas() throws Exception {
        criarSala("Sala Master", 25);
        criarSala("Sala Azul", 10);

        mockMvc.perform(get("/salas").param("nome", "MASTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Sala Master"));

        mockMvc.perform(get("/salas").param("nome", "sala"))
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/salas").param("nome", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void buscarPorIdRetorna200() throws Exception {
        String id = criarSala("Sala Master", 25);

        mockMvc.perform(get("/salas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Sala Master"));
    }

    @Test
    void atualizarRetorna200EPreservaOId() throws Exception {
        String id = criarSala("Sala Master", 25);

        mockMvc.perform(put("/salas/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Sala Reformada", 30)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Sala Reformada"))
                .andExpect(jsonPath("$.capacidadePessoas").value(30));
    }

    @Test
    void excluirRetorna204ESalaDeixaDeExistir() throws Exception {
        String id = criarSala("Sala Master", 25);

        mockMvc.perform(delete("/salas/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/salas/{id}", id))
                .andExpect(status().isNotFound());
    }

    // ---------- 400 ----------

    @ParameterizedTest
    @MethodSource("corposInvalidos")
    void entradaInvalidaNoCadastroRetorna400(String corpo) throws Exception {
        mockMvc.perform(post("/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/salas"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @ParameterizedTest
    @MethodSource("corposInvalidos")
    void mesmasValidacoesNaAtualizacaoRetornam400(String corpo) throws Exception {
        String id = criarSala("Sala Original", 20);

        mockMvc.perform(put("/salas/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/salas/{id}", id))
                .andExpect(jsonPath("$.nome").value("Sala Original"))
                .andExpect(jsonPath("$.capacidadePessoas").value(20));
    }

    @Test
    void validacaoListaTodosOsErros() throws Exception {
        mockMvc.perform(post("/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erros", hasSize(2)));
    }

    // ---------- 422 ----------

    @Test
    void capacidadeAcimaDeCemNoCadastroRetorna422() throws Exception {
        mockMvc.perform(post("/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Sala Gigante", 150)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("100 pessoas")));

        mockMvc.perform(get("/salas"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void capacidadeAcimaDeCemNaAtualizacaoRetorna422ENaoAlteraOsDados() throws Exception {
        String id = criarSala("Sala Original", 20);

        mockMvc.perform(put("/salas/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Sala Hackeada", 999)))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/salas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Sala Original"))
                .andExpect(jsonPath("$.capacidadePessoas").value(20));
    }

    // ---------- 404 ----------

    @Test
    void idInexistenteRetorna404EmGetPutEDelete() throws Exception {
        mockMvc.perform(get("/salas/{id}", "nao-existe"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/salas/{id}", "nao-existe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Sala", 5)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/salas/{id}", "nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rotaInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/rota-que-nao-existe"))
                .andExpect(status().isNotFound());
    }

    // ---------- 405 / 415 ----------

    @Test
    void metodoNaoPermitidoRetorna405() throws Exception {
        mockMvc.perform(patch("/salas").content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void contentTypeNaoSuportadoRetorna415() throws Exception {
        mockMvc.perform(post("/salas")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("oi"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
