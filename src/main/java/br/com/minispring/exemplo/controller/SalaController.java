package br.com.minispring.exemplo.controller;

import br.com.minispring.exemplo.model.Sala;
import br.com.minispring.exemplo.model.SalaInput;
import br.com.minispring.exemplo.service.SalaService;
import br.com.minispring.framework.annotation.*;
import br.com.minispring.framework.web.HttpResult;

import java.util.List;

@RestController
public class SalaController {

    private final SalaService service;

    public SalaController(SalaService service) {
        this.service = service;
    }

    @Route(method = "POST", path = "/salas")
    public HttpResult cadastrar(@RequestBody SalaInput input) {
        Sala sala = service.cadastrar(input);
        return HttpResult.created("/salas/" + sala.getId(), sala);
    }

    @Route(method = "GET", path = "/salas")
    public HttpResult listar(@RequestParam(value = "nome") String nome) {
        List<Sala> salas = service.listar(nome);
        return HttpResult.ok(salas);
    }

    @Route(method = "GET", path = "/salas/{id}")
    public HttpResult buscarPorId(@PathVariable("id") String id) {
        Sala sala = service.buscarPorId(id);
        return HttpResult.ok(sala);
    }

    @Route(method = "PUT", path = "/salas/{id}")
    public HttpResult atualizar(@PathVariable("id") String id, @RequestBody SalaInput input) {
        Sala sala = service.atualizar(id, input);
        return HttpResult.ok(sala);
    }

    @Route(method = "DELETE", path = "/salas/{id}")
    public HttpResult excluir(@PathVariable("id") String id) {
        service.excluir(id);
        return HttpResult.noContent();
    }
}