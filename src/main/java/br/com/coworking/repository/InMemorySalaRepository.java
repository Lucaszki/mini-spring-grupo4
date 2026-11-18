package br.com.coworking.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.coworking.model.Sala;

/**
 * Implementacao em memoria. Os metodos sao synchronized porque o bean e um
 * singleton compartilhado entre as threads do Tomcat.
 * LinkedHashMap mantem a ordem de insercao na listagem.
 */
@Repository
public class InMemorySalaRepository implements SalaRepository {

    private final Map<String, Sala> salas = new LinkedHashMap<>();

    @Override
    public synchronized Sala save(Sala sala) {
        String id = UUID.randomUUID().toString();
        Sala nova = new Sala(id, sala.getNome(), sala.getCapacidadePessoas());
        salas.put(id, nova);
        return nova;
    }

    @Override
    public synchronized List<Sala> findAll() {
        return new ArrayList<>(salas.values());
    }

    @Override
    public synchronized List<Sala> findByNomeContainingIgnoreCase(String nome) {
        String termo = nome.toLowerCase(Locale.ROOT);
        return salas.values().stream()
                .filter(sala -> sala.getNome().toLowerCase(Locale.ROOT).contains(termo))
                .toList();
    }

    @Override
    public synchronized Optional<Sala> findById(String id) {
        return Optional.ofNullable(salas.get(id));
    }

    @Override
    public synchronized Sala update(Sala sala) {
        salas.put(sala.getId(), sala);
        return sala;
    }

    @Override
    public synchronized boolean delete(String id) {
        return salas.remove(id) != null;
    }
}
