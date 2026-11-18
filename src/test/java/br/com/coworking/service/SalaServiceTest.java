package br.com.coworking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.coworking.dto.SalaInput;
import br.com.coworking.exception.RegraNegocioException;
import br.com.coworking.exception.SalaNaoEncontradaException;
import br.com.coworking.model.Sala;
import br.com.coworking.repository.InMemorySalaRepository;

/**
 * Teste unitario SEM Spring: so e possivel porque a injecao e por construtor.
 */
class SalaServiceTest {

    private SalaService service;

    @BeforeEach
    void setUp() {
        service = new SalaService(new InMemorySalaRepository());
    }

    @Test
    void cadastrarGeraIdAutomaticamente() {
        Sala sala = service.cadastrar(new SalaInput("Sala Master", 25));

        assertNotNull(sala.getId());
        assertEquals("Sala Master", sala.getNome());
        assertEquals(25, sala.getCapacidadePessoas());
    }

    @Test
    void capacidadeNoLimiteDeCemEAceita() {
        Sala sala = service.cadastrar(new SalaInput("Auditório", 100));

        assertEquals(100, sala.getCapacidadePessoas());
    }

    @Test
    void capacidadeAcimaDeCemLancaRegraDeNegocioENaoSalva() {
        assertThrows(RegraNegocioException.class,
                () -> service.cadastrar(new SalaInput("Gigante", 101)));

        assertTrue(service.listar(null).isEmpty());
    }

    @Test
    void atualizarPreservaOId() {
        Sala criada = service.cadastrar(new SalaInput("Sala A", 10));

        Sala atualizada = service.atualizar(criada.getId(), new SalaInput("Sala B", 20));

        assertEquals(criada.getId(), atualizada.getId());
        assertEquals("Sala B", atualizada.getNome());
        assertEquals(20, atualizada.getCapacidadePessoas());
    }

    @Test
    void atualizacaoRejeitadaNaoAlteraOsDados() {
        Sala criada = service.cadastrar(new SalaInput("Sala A", 10));

        assertThrows(RegraNegocioException.class,
                () -> service.atualizar(criada.getId(), new SalaInput("Hackeada", 999)));

        Sala armazenada = service.buscarPorId(criada.getId());
        assertEquals("Sala A", armazenada.getNome());
        assertEquals(10, armazenada.getCapacidadePessoas());
    }

    @Test
    void buscarAtualizarEExcluirInexistenteLancamNaoEncontrada() {
        assertThrows(SalaNaoEncontradaException.class, () -> service.buscarPorId("x"));
        assertThrows(SalaNaoEncontradaException.class,
                () -> service.atualizar("x", new SalaInput("Sala", 5)));
        assertThrows(SalaNaoEncontradaException.class, () -> service.excluir("x"));
    }

    @Test
    void filtroIgnoraMaiusculasEMinusculas() {
        service.cadastrar(new SalaInput("Sala Master", 25));
        service.cadastrar(new SalaInput("Sala Azul", 10));

        assertEquals(1, service.listar("MASTER").size());
        assertEquals(1, service.listar("azul").size());
        assertEquals(2, service.listar("sala").size());
        assertEquals(0, service.listar("xyz").size());
    }

    @Test
    void filtroVazioOuNuloListaTudo() {
        service.cadastrar(new SalaInput("Sala Master", 25));
        service.cadastrar(new SalaInput("Sala Azul", 10));

        assertEquals(2, service.listar(null).size());
        assertEquals(2, service.listar("").size());
        assertEquals(2, service.listar("   ").size());
    }

    @Test
    void excluirRemoveASala() {
        Sala criada = service.cadastrar(new SalaInput("Sala A", 10));

        service.excluir(criada.getId());

        assertThrows(SalaNaoEncontradaException.class,
                () -> service.buscarPorId(criada.getId()));
    }

    @Test
    void cadastrosConcorrentesNaoPerdemRegistros() throws Exception {
        int total = 50;
        ExecutorService pool = Executors.newFixedThreadPool(10);
        List<Future<Sala>> futuros = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            final int numero = i;
            Callable<Sala> tarefa = () -> service.cadastrar(new SalaInput("Sala " + numero, 10));
            futuros.add(pool.submit(tarefa));
        }

        Set<String> ids = new HashSet<>();
        for (Future<Sala> futuro : futuros) {
            ids.add(futuro.get().getId());
        }
        pool.shutdown();

        assertEquals(total, ids.size());
        assertEquals(total, service.listar(null).size());
    }
}
