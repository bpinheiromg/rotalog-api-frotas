package com.rotalog.controller;

import com.rotalog.domain.AlertaManutencao;
import com.rotalog.repository.AlertaManutencaoRepository;
import com.rotalog.service.AlertaManutencaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AlertaManutencaoController - expõe endpoints de alertas de manutenção preventiva.
 *
 * <ul>
 *     <li>GET /veiculos/alertas-manutencao — executa a verificação de elegibilidade
 *         e retorna os alertas recém-gerados.</li>
 *     <li>GET /veiculos/alertas-manutencao/listar — lista os alertas já registrados
 *         no banco (opcionalmente filtrados por status_notificacao).</li>
 * </ul>
 *
 * FIXME: sem Spring Security — endpoint de disparo de verificação deve ser protegido/Admin.
 * FIXME: sem cache — chamadas repetidas refazem o processamento.
 * FIXME: sem paginação na listagem.
 */
@Slf4j
@RestController
@RequestMapping("/veiculos/alertas-manutencao")
public class AlertaManutencaoController {

    private final AlertaManutencaoService alertaManutencaoService;
    private final AlertaManutencaoRepository alertaManutencaoRepository;

    public AlertaManutencaoController(AlertaManutencaoService alertaManutencaoService,
                                     AlertaManutencaoRepository alertaManutencaoRepository) {
        this.alertaManutencaoService = alertaManutencaoService;
        this.alertaManutencaoRepository = alertaManutencaoRepository;
    }

    /**
     * Executa a verificação de veículos elegíveis e gera os alertas.
     * Retorna a lista de alertas criados nesta execução.
     */
    @GetMapping
    public ResponseEntity<?> verificarElegiveis() {
        try {
            List<AlertaManutencao> alertas = alertaManutencaoService.verificarVeiculosElegiveis();
            return ResponseEntity.ok(alertas);
        } catch (RuntimeException e) {
            log.error("Erro ao verificar veículos elegíveis: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("erro", "Falha ao verificar alertas de manutenção: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Lista alertas já registrados no banco.
     *
     * @param status filtro opcional por status_notificacao (ENVIADA, PENDENTE, FALHA).
     */
    @GetMapping("/listar")
    public ResponseEntity<List<AlertaManutencao>> listarAlertas(
            @RequestParam(value = "status", required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(alertaManutencaoRepository.findByStatusNotificacao(
                    com.rotalog.domain.StatusNotificacaoAlerta.valueOf(status.toUpperCase())));
        }
        return ResponseEntity.ok(alertaManutencaoRepository.findAllOrderByDataAlertaDesc());
    }
}
