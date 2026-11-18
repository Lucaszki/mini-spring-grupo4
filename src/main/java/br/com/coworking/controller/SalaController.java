package br.com.coworking.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.coworking.dto.SalaInput;
import br.com.coworking.model.Sala;
import br.com.coworking.service.SalaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/salas")
public class SalaController {

    private final SalaService service;

    // Injecao por construtor: o Spring entrega o SalaService.
    public SalaController(SalaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Sala> cadastrar(@Valid @RequestBody SalaInput input) {
        Sala sala = service.cadastrar(input);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(sala.getId())
                .toUri();
        return ResponseEntity.created(location).body(sala);
    }

    @GetMapping
    public List<Sala> listar(@RequestParam(name = "nome", required = false) String nome) {
        return service.listar(nome);
    }

    @GetMapping("/{id}")
    public Sala buscar(@PathVariable("id") String id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Sala atualizar(@PathVariable("id") String id, @Valid @RequestBody SalaInput input) {
        return service.atualizar(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable("id") String id) {
        service.excluir(id);
    }
}
