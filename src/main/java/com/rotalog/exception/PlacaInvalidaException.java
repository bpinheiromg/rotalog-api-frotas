package com.rotalog.exception;

/**
 * Exceção lançada quando a placa fornecida não atende aos requisitos de formato.
 */
public class PlacaInvalidaException extends VeiculoException {

    public PlacaInvalidaException(String message) {
        super(message);
    }

    public PlacaInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
