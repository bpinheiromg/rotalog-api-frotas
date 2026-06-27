package com.rotalog.exception;

/**
 * Exceção lançada quando a quilometragem fornecida é inválida (negativa ou decrescente).
 */
public class QuilometragemInvalidaException extends VeiculoException {

    public QuilometragemInvalidaException(String message) {
        super(message);
    }

    public QuilometragemInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
