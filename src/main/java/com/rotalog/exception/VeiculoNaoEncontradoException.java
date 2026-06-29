package com.rotalog.exception;

/**
 * Exceção lançada quando um veículo não é encontrado pelo ID ou placa.
 */
public class VeiculoNaoEncontradoException extends VeiculoException {

    public VeiculoNaoEncontradoException(String message) {
        super(message);
    }

    public VeiculoNaoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
