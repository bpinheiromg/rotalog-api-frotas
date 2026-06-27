package com.rotalog.service;

import com.rotalog.domain.TipoNotificacao;
import com.rotalog.exception.NotificacaoFalhaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Serviço especializado em envio de notificações relacionadas a veículos.
 * Wrapper sobre NotificacaoClient com tratamento de falhas.
 */
@Slf4j
@Service
public class VeiculoNotificacaoService {

    private final NotificacaoClient notificacaoClient;

    @Value("${notificacao.email-gestor}")
    private String emailGestor;

    public VeiculoNotificacaoService(NotificacaoClient notificacaoClient) {
        this.notificacaoClient = notificacaoClient;
    }

    /**
     * Notifica sobre novo veículo cadastrado.
     * Falhas são logadas mas não propagadas (operação já foi concluída).
     */
    public void notificarNovoVeiculo(String placa, String modelo) {
        try {
            notificacaoClient.enviarNotificacao(
                    TipoNotificacao.NOVO_VEICULO.name(),
                    emailGestor,
                    "Novo veículo cadastrado: " + placa + " - " + modelo
            );
        } catch (Exception e) {
            log.error("Erro ao enviar notificação de novo veículo: {}", e.getMessage());
        }
    }

    /**
     * Notifica sobre alerta de manutenção preventiva.
     */
    public void notificarManutencao(String placa, Long quilometragem) {
        try {
            notificacaoClient.enviarNotificacao(
                    TipoNotificacao.ALERTA_MANUTENCAO.name(),
                    emailGestor,
                    "Veículo " + placa + " atingiu " + quilometragem + " km. Agendar manutenção preventiva."
            );
        } catch (Exception e) {
            log.error("Falha ao enviar alerta de manutenção: {}", e.getMessage());
        }
    }

    /**
     * Notifica sobre manutenção agendada.
     */
    public void notificarManutencaoAgendada(String placa, Long quilometragemLimite) {
        try {
            notificacaoClient.enviarNotificacao(
                    TipoNotificacao.MANUTENCAO_AGENDADA.name(),
                    emailGestor,
                    "Manutenção preventiva agendada para veículo " + placa + " em " + quilometragemLimite + " km"
            );
        } catch (Exception e) {
            log.error("Falha ao notificar agendamento de manutenção: {}", e.getMessage());
        }
    }

    /**
     * Notifica sobre desativação de veículo.
     */
    public void notificarDesativacao(String placa) {
        try {
            notificacaoClient.enviarNotificacao(
                    TipoNotificacao.VEICULO_DESATIVADO.name(),
                    emailGestor,
                    "Veículo " + placa + " foi desativado"
            );
        } catch (Exception e) {
            log.error("Falha ao notificar desativação: {}", e.getMessage());
        }
    }
}
