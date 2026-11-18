package br.com.coworking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.coworking.dto.SalaInput;
import br.com.coworking.exception.RegraNegocioException;
import br.com.coworking.exception.SalaNaoEncontradaException;
import br.com.coworking.model.Sala;
import br.com.coworking.repository.SalaRepository;

@Service
public class SalaService {

    private static final int CAPACIDADE_MAXIMA = 100;

    private final SalaRepository repository;

    // Injecao por construtor: o Spring entrega a implementacao de SalaRepository.
    public SalaService(SalaRepository repository) {
        this.repository = repository;
    }

    public Sala cadastrar(SalaInput input) {
        validarRegraNegocio(input);
        return repository.save(new Sala(null, input.nome(), input.capacidadePessoas()));
    }

    public List<Sala> listar(String nome) {
        if (nome == null || nome.isBlank()) {
            return repository.findAll();
        }
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    public Sala buscarPorId(String id) {
        return repository.findById(id).orElseThrow(SalaNaoEncontradaException::new);
    }

    public Sala atualizar(String id, SalaInput input) {
        // A regra de negocio e verificada ANTES de qualquer alteracao:
        // uma operacao rejeitada nao modifica os dados armazenados.
        validarRegraNegocio(input);
        Sala existente = buscarPorId(id);
        Sala atualizada = new Sala(existente.getId(), input.nome(), input.capacidadePessoas());
        return repository.update(atualizada);
    }

    public void excluir(String id) {
        buscarPorId(id);
        repository.delete(id);
    }

    private void validarRegraNegocio(SalaInput input) {
        if (input.capacidadePessoas() > CAPACIDADE_MAXIMA) {
            throw new RegraNegocioException(
                    "A capacidade máxima da sala não pode ultrapassar "
                            + CAPACIDADE_MAXIMA + " pessoas.");
        }
    }
}
