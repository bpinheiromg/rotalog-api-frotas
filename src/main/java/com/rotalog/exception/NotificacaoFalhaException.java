package com.rotalog.exception;

/**
 * Exceção lançada quando o envio de notificação falha.
 */
public class NotificacaoFalhaException extends VeiculoException {

    public NotificacaoFalhaException(String message) {
        super(message);
    }

    public NotificacaoFalhaException(String message, Throwable cause) {
        super(message, cause);
    }
}
