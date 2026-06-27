package com.rotalog.exception;

/**
 * Exceção lançada quando um campo obrigatório está nulo ou vazio.
 */
public class CampoObrigatorioException extends VeiculoException {

    public CampoObrigatorioException(String message) {
        super(message);
    }

    public CampoObrigatorioException(String message, Throwable cause) {
        super(message, cause);
    }
}
