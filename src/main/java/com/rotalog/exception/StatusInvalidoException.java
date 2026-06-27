package com.rotalog.exception;

/**
 * Exceção lançada quando um status não reconhecido é informado.
 */
public class StatusInvalidoException extends VeiculoException {

    public StatusInvalidoException(String message) {
        super(message);
    }

    public StatusInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
