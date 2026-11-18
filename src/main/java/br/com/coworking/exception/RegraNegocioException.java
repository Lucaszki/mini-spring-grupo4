package br.com.coworking.exception;

/** Lancada quando uma regra de negocio e violada (vira HTTP 422). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
