package br.com.minispring.exemplo.repository;

import br.com.minispring.exemplo.model.Sala;
import java.util.List;
import java.util.Optional;

public interface SalaRepository {
    Sala save(Sala sala);
    List<Sala> findAll();
    List<Sala> findByNomeContainingIgnoreCase(String nome);
    Optional<Sala> findById(String id);
    Sala update(Sala sala);
    boolean delete(String id);
}