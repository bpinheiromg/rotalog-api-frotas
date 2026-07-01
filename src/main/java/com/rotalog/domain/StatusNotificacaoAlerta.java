package com.rotalog.domain;

/**
 * Enum para o status da notificação de um alerta de manutenção.
 * Substitui as strings hardcodeadas 'ENVIADA', 'PENDENTE', 'FALHA'.
 */
public enum StatusNotificacaoAlerta {

    ENVIADA("Notificação enviada com sucesso ao gestor"),
    PENDENTE("Notificação pendente (api-notificacoes indisponível)"),
    FALHA("Falha permanente ao enviar notificação");

    private final String descricao;

    StatusNotificacaoAlerta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
