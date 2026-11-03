package br.com.minispring;

import br.com.minispring.framework.MiniApplication;
import br.com.minispring.framework.context.AppConfig;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;

class ApiIntegrationTest {
    private MiniApplication app;
    private HttpClient client;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach void start() throws Exception {
        // Porta 0 = Jetty escolhe uma porta livre; cada teste sobe sua própria instância,
        // isolando o repositório em memória entre os testes.
        app = MiniApplication.start("br.com.minispring.exemplo", new AppConfig(new Properties(), Map.of()), 0);
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }
    @AfterEach void stop() throws Exception {
        if (client != null) client.close();
        if (app != null) app.close();
    }
    private HttpRequest request(String method, String path, String body, String contentType) {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + app.port() + path)).timeout(Duration.ofSeconds(10));
        if (contentType != null) builder.header("Content-Type", contentType);
        return builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build();
    }
    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        return client.send(request(method, path, body, body == null ? null : "application/json"), HttpResponse.BodyHandlers.ofString());
    }

    @Test void fullCrudThroughRealJetty() throws Exception {
        var created = send("POST", "/salas", "{\"nome\":\"Sala Azul\",\"capacidadePessoas\":10}");
        assertEquals(201, created.statusCode());
        assertTrue(created.headers().firstValue("Content-Type").orElseThrow().startsWith("application/json"));
        assertFalse(created.headers().firstValue("X-Request-Id").orElseThrow().isBlank());
        String location = created.headers().firstValue("Location").orElseThrow();
        var criada = json.readTree(created.body());
        String id = criada.get("id").asText();
        assertFalse(id.isBlank());
        assertTrue(location.endsWith(id));

        var buscada = json.readTree(send("GET", location, null).body());
        assertEquals("Sala Azul", buscada.get("nome").asText());
        assertEquals(10, buscada.get("capacidadePessoas").asInt());

        assertEquals(1, json.readTree(send("GET", "/salas?nome=AZUL", null).body()).size());
        assertEquals(0, json.readTree(send("GET", "/salas?nome=auditorio", null).body()).size());

        var updated = send("PUT", location, "{\"nome\":\"Sala Azul Reformada\",\"capacidadePessoas\":20}");
        assertEquals(200, updated.statusCode());
        var atualizada = json.readTree(updated.body());
        assertEquals("Sala Azul Reformada", atualizada.get("nome").asText());
        assertEquals(20, atualizada.get("capacidadePessoas").asInt());
        assertEquals(id, atualizada.get("id").asText()); // id precisa ser preservado na atualização

        var removed = send("DELETE", location, null);
        assertEquals(204, removed.statusCode());
        assertEquals("", removed.body());

        assertEquals(404, send("GET", location, null).statusCode());
        assertEquals(404, send("DELETE", location, null).statusCode());
        assertEquals(404, send("PUT", location, "{\"nome\":\"Sala\",\"capacidadePessoas\":5}").statusCode());
    }

    @Test void rejectsInvalidInputsWithStructuredErrors() throws Exception {
        var invalid = send("POST", "/salas", "{\"nome\":\" \",\"capacidadePessoas\":0}");
        assertEquals(400, invalid.statusCode());
        var error = json.readTree(invalid.body());
        assertEquals(2, error.get("details").size()); // nome em branco + capacidade não positiva
        assertEquals(invalid.headers().firstValue("X-Request-Id").orElseThrow(), error.get("requestId").asText());

        for (String body : List.of("{broken", "null", "", "{}",
                "{\"nome\":\"Sala\",\"capacidadePessoas\":1.5}",
                "{\"nome\":\"Sala\",\"capacidadePessoas\":\"10\"}", "{} {}",
                "{\"nome\":\"Sala\",\"capacidadePessoas\":10,\"id\":\"x\"}")) {
            assertEquals(400, send("POST", "/salas", body).statusCode(), body);
        }

        assertEquals(422, send("POST", "/salas", "{\"nome\":\"Auditório\",\"capacidadePessoas\":101}").statusCode());
        assertEquals(415, client.send(request("POST", "/salas", "{}", "text/plain"), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(413, send("POST", "/salas", "x".repeat(1_048_577)).statusCode());
    }

    @Test void appliesSameValidationsOnUpdateAsOnCreate() throws Exception {
        var created = send("POST", "/salas", "{\"nome\":\"Sala Validação\",\"capacidadePessoas\":10}");
        String location = created.headers().firstValue("Location").orElseThrow();

        var nomeEmBranco = send("PUT", location, "{\"nome\":\" \",\"capacidadePessoas\":10}");
        assertEquals(400, nomeEmBranco.statusCode());
        assertEquals(1, json.readTree(nomeEmBranco.body()).get("details").size());

        var capacidadeInvalida = send("PUT", location, "{\"nome\":\"Sala Validação\",\"capacidadePessoas\":0}");
        assertEquals(400, capacidadeInvalida.statusCode());

        var acimaDoLimite = send("PUT", location, "{\"nome\":\"Sala Validação\",\"capacidadePessoas\":101}");
        assertEquals(422, acimaDoLimite.statusCode());

        // Nenhuma das tentativas inválidas pode ter alterado o registro original.
        var aindaOriginal = json.readTree(send("GET", location, null).body());
        assertEquals("Sala Validação", aindaOriginal.get("nome").asText());
        assertEquals(10, aindaOriginal.get("capacidadePessoas").asInt());
    }

    @Test void rejectedOperationDoesNotChangeStoredData() throws Exception {
        var created = send("POST", "/salas", "{\"nome\":\"Sala Original\",\"capacidadePessoas\":10}");
        String location = created.headers().firstValue("Location").orElseThrow();

        // Tentativa de atualização inválida (capacidade acima do limite) não pode alterar o registro.
        assertEquals(422, send("PUT", location, "{\"nome\":\"Sala Hackeada\",\"capacidadePessoas\":999}").statusCode());

        var aindaOriginal = json.readTree(send("GET", location, null).body());
        assertEquals("Sala Original", aindaOriginal.get("nome").asText());
        assertEquals(10, aindaOriginal.get("capacidadePessoas").asInt());
    }

    @Test void distinguishesRouteErrorsAndSendsAllowHeader() throws Exception {
        assertEquals(404, send("GET", "/inexistente", null).statusCode());

        var semId = send("PATCH", "/salas", "{}");
        assertEquals(405, semId.statusCode());
        assertEquals("GET, POST", semId.headers().firstValue("Allow").orElseThrow());

        var comId = send("PATCH", "/salas/qualquer-id", "{}");
        assertEquals(405, comId.statusCode());
        assertEquals("DELETE, GET, PUT", comId.headers().firstValue("Allow").orElseThrow());
    }

    @Test void concurrentRequestsProduceUniqueIds() throws Exception {
        var futures = new ArrayList<CompletableFuture<HttpResponse<String>>>();
        for (int i = 0; i < 30; i++) futures.add(client.sendAsync(
            request("POST", "/salas", "{\"nome\":\"Sala Concorrente\",\"capacidadePessoas\":5}", "application/json"), HttpResponse.BodyHandlers.ofString()));
        var ids = new HashSet<String>();
        for (var future : futures) {
            var response = future.join();
            assertEquals(201, response.statusCode());
            ids.add(json.readTree(response.body()).get("id").asText());
        }
        assertEquals(30, ids.size()); // UUIDs não podem colidir mesmo sob concorrência
        assertEquals(30, json.readTree(send("GET", "/salas", null).body()).size());
    }
}