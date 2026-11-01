package br.com.minispring.exemplo.repository;

import br.com.minispring.exemplo.model.Sala;
import br.com.minispring.framework.annotation.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class InMemorySalaRepository implements SalaRepository {

    private final List<Sala> salas = new ArrayList<>();

    @Override
    public Sala save(Sala sala) {
        sala.setId(UUID.randomUUID().toString()); // Gera o ID automaticamente
        salas.add(sala);
        return sala;
    }

    @Override
    public List<Sala> findAll() {
        return new ArrayList<>(salas);
    }

    @Override
    public List<Sala> findByNomeContainingIgnoreCase(String nome) {
        return salas.stream()
                .filter(s -> s.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Sala> findById(String id) {
        return salas.stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    @Override
    public Sala update(Sala sala) {
        for (int i = 0; i < salas.size(); i++) {
            if (salas.get(i).getId().equals(sala.getId())) {
                salas.set(i, sala);
                return sala;
            }
        }
        return null;
    }

    @Override
    public boolean delete(String id) {
        return salas.removeIf(s -> s.getId().equals(id));
    }
}