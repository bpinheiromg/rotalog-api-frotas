package com.rotalog.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * DTO que representa a resposta da api-notificacoes ao criar uma notificação.
 * Espelha o shape retornado por POST /api/notificacoes (entidade Notificacao).
 *
 * FIXME: Apenas um subconjundo dos campos é usado (id, status).
 * FIXME: Sem validação @JsonIgnoreProperties para novos campos desconhecidos.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificacaoResponse {

    private Long id;
    private String tipo;
    private String canal;
    private String destinatario;
    private String assunto;
    private String mensagem;
    private String status; // ENVIADO | PENDENTE | FALHA
    private Integer tentativas;
    private Integer maxTentativas;
    private String erroMensagem;
    private String servicoOrigem;
    private String referenciaId;
    private String dataCriacao;
    private String dataEnvio;
    private String dataAtualizacao;

    // FIXME: Sem toString/log Sanitization
}
