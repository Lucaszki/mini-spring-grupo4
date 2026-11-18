package br.com.coworking.exception;

/** Lancada quando nao existe sala com o id informado (vira HTTP 404). */
public class SalaNaoEncontradaException extends RuntimeException {

    public SalaNaoEncontradaException() {
        super("Sala não encontrada.");
    }
}
