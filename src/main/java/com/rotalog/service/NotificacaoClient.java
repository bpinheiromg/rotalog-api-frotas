package com.rotalog.service;

import com.rotalog.dto.NotificacaoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * NotificacaoClient - HTTP client for api-notificacoes
 *
 * FIXME: URL hardcoded
 * FIXME: Sem circuit breaker
 * FIXME: Sem retry logic
 * FIXME: Sem timeout configurável
 * FIXME: RestTemplate instanciado manualmente em vez de ser Bean
 */
@Slf4j
@Component
public class NotificacaoClient {

    // FIXME: URL hardcoded - deveria estar em application.properties
    private static final String NOTIFICACAO_API_URL = "http://localhost:5000";

    // FIXME: RestTemplate criado manualmente - deveria ser @Bean injetado
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${notificacao.api-url:" + NOTIFICACAO_API_URL + "}")
    private String notificacaoApiUrl;

    /**
     * Envia notificação para api-notificacoes e retorna a resposta.
     *
     * A resposta inclui o id criado e o status da notificação, necessários
     * para o registro do alerta de manutenção preventiva.
     *
     * @return NotificacaoResponse com id e status, ou null se a chamada falhar
     *
     * FIXME: Sem retry
     * FIXME: Sem fallback
     * FIXME: Sem validação de resposta completa
     */
    public NotificacaoResponse enviarNotificacao(String tipo, String destinatario, String mensagem) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("tipo", tipo);
            body.put("destinatario", destinatario);
            body.put("mensagem", mensagem);
            body.put("canal", "email"); // FIXME: canal hardcoded
            body.put("servicoOrigem", "api-frotas"); // FIXME: hardcoded

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            String url = notificacaoApiUrl + "/api/notificacoes";
            log.info("Enviando notificação para {}: tipo={}, destinatario={}", url, tipo, destinatario);

            NotificacaoResponse response = restTemplate.postForObject(url, request, NotificacaoResponse.class);

            if (response != null) {
                log.info("Notificação enviada com sucesso: tipo={}, id={}, status={}",
                        tipo, response.getId(), response.getStatus());
            } else {
                log.warn("Resposta nula da api-notificacoes: tipo={}", tipo);
            }
            return response;
        } catch (Exception e) {
            // FIXME: Engolindo exceção - apenas logando
            log.error("Erro ao enviar notificação: tipo={}, erro={}", tipo, e.getMessage());
            // FIXME: Sem retry, sem fallback, sem dead letter queue
            throw e; // re-throw para o chamador decidir o que fazer
        }
    }

    /**
     * Envia notificação SMS
     * 
     * FIXME: Código duplicado com enviarNotificacao
     * FIXME: Deveria ser o mesmo método com canal diferente
     */
    public void enviarSms(String destinatario, String mensagem) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("tipo", "SMS");
            body.put("destinatario", destinatario);
            body.put("mensagem", mensagem);
            body.put("canal", "sms");

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            String url = NOTIFICACAO_API_URL + "/api/notificacoes";
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            log.error("Erro ao enviar SMS: {}", e.getMessage());
        }
    }
}
