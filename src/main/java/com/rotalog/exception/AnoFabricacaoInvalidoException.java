package com.rotalog.exception;

/**
 * Exceção lançada quando o ano de fabricação está fora do intervalo aceitável.
 */
public class AnoFabricacaoInvalidoException extends VeiculoException {

    public AnoFabricacaoInvalidoException(String message) {
        super(message);
    }

    public AnoFabricacaoInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
