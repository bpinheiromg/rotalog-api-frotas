package com.rotalog.exception;

/**
 * Exceção lançada ao tentar registrar um veículo com placa já existente.
 */
public class VeiculoDuplicadoException extends VeiculoException {

    public VeiculoDuplicadoException(String message) {
        super(message);
    }

    public VeiculoDuplicadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
