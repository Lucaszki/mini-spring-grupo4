package br.com.minispring.exemplo.service;

import br.com.minispring.exemplo.model.Sala;
import br.com.minispring.exemplo.model.SalaInput;
import br.com.minispring.exemplo.repository.SalaRepository;
import br.com.minispring.framework.annotation.Service;
import br.com.minispring.framework.web.HttpException;

import java.util.List;

@Service
public class SalaService {

    private final SalaRepository repository;

    public SalaService(SalaRepository repository) {
        this.repository = repository;
    }

    public Sala cadastrar(SalaInput input) {
        validarRegraNegocio(input.capacidadePessoas());
        Sala sala = new Sala(null, input.nome(), input.capacidadePessoas());
        return repository.save(sala);
    }

    public List<Sala> listar(String nome) {
        if (nome != null && !nome.trim().isEmpty()) {
            return repository.findByNomeContainingIgnoreCase(nome);
        }
        return repository.findAll();
    }

    public Sala buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new HttpException(404, "Sala não encontrada."));
    }

    public Sala atualizar(String id, SalaInput input) {
        validarRegraNegocio(input.capacidadePessoas());

        Sala salaExistente = buscarPorId(id);
        salaExistente.setNome(input.nome());
        salaExistente.setCapacidadePessoas(input.capacidadePessoas());

        return repository.update(salaExistente);
    }

    public void excluir(String id) {
        buscarPorId(id);
        repository.delete(id);
    }

    private void validarRegraNegocio(Integer capacidade) {
        if (capacidade > 100) {
            throw new HttpException(422, "A capacidade máxima da sala não pode ultrapassar 100 pessoas.");
        }
    }
}