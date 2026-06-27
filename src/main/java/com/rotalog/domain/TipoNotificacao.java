package com.rotalog.domain;

/**
 * Enum para os tipos de notificação enviados pelo sistema.
 * Substitui as strings hardcodeadas usadas em NotificacaoClient.
 */
public enum TipoNotificacao {

    NOVO_VEICULO("Notificação: novo veículo cadastrado"),
    ALERTA_MANUTENCAO("Alerta: manutenção necessária"),
    MANUTENCAO_AGENDADA("Confirmação: manutenção agendada"),
    VEICULO_DESATIVADO("Notificação: veículo desativado");

    private final String mensagem;

    TipoNotificacao(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMensagem() {
        return mensagem;
    }
}
