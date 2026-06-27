package com.rotalog.exception;

/**
 * Exceção base abstrata para operações do domínio de veículos.
 * Todas as exceções específicas herdam desta classe.
 */
public abstract class VeiculoException extends RuntimeException {

    public VeiculoException(String message) {
        super(message);
    }

    public VeiculoException(String message, Throwable cause) {
        super(message, cause);
    }
}
