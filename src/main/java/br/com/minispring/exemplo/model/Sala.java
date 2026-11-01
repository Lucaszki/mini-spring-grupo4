package br.com.minispring.exemplo.model;

public class Sala {
    private String id;
    private String nome;
    private Integer capacidadePessoas;

    public Sala() {}

    public Sala(String id, String nome, Integer capacidadePessoas) {
        this.id = id;
        this.nome = nome;
        this.capacidadePessoas = capacidadePessoas;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Integer getCapacidadePessoas() { return capacidadePessoas; }
    public void setCapacidadePessoas(Integer capacidadePessoas) { this.capacidadePessoas = capacidadePessoas; }
}