package br.com.coworking.repository;

import java.util.List;
import java.util.Optional;

import br.com.coworking.model.Sala;

/** Contrato de acesso aos dados. O Service depende desta interface. */
public interface SalaRepository {

    Sala save(Sala sala);

    List<Sala> findAll();

    List<Sala> findByNomeContainingIgnoreCase(String nome);

    Optional<Sala> findById(String id);

    Sala update(Sala sala);

    boolean delete(String id);
}
