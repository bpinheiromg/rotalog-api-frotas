package com.rotalog.exception;

/**
 * Exceção lançada quando o modelo do veículo é nulo ou vazio.
 */
public class ModeloInvalidoException extends VeiculoException {

    public ModeloInvalidoException(String message) {
        super(message);
    }

    public ModeloInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
