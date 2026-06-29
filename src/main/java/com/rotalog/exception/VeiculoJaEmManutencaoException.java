package com.rotalog.exception;

/**
 * Exceção lançada quando se tenta agendar manutenção para veículo que já está em manutenção.
 */
public class VeiculoJaEmManutencaoException extends VeiculoException {

    public VeiculoJaEmManutencaoException(String message) {
        super(message);
    }

    public VeiculoJaEmManutencaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
