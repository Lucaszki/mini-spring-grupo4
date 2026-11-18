package br.com.coworking.model;

/**
 * Entidade principal do dominio. Imutavel: para "alterar" uma sala,
 * o Service cria uma nova instancia com o mesmo id.
 */
public class Sala {

    private final String id;
    private final String nome;
    private final Integer capacidadePessoas;

    public Sala(String id, String nome, Integer capacidadePessoas) {
        this.id = id;
        this.nome = nome;
        this.capacidadePessoas = capacidadePessoas;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Integer getCapacidadePessoas() {
        return capacidadePessoas;
    }
}
