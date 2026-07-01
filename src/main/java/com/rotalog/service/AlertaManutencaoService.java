package com.rotalog.service;

import com.rotalog.domain.AlertaManutencao;
import com.rotalog.domain.StatusNotificacaoAlerta;
import com.rotalog.domain.TipoAlerta;
import com.rotalog.domain.Veiculo;
import com.rotalog.dto.NotificacaoResponse;
import com.rotalog.repository.AlertaManutencaoRepository;
import com.rotalog.repository.VeiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Serviço de verificação de elegibilidade e geração de alertas de manutenção preventiva.
 *
 * Varre veículos ATIVO elegíveis por quilometragem ou por tempo sem manutenção,
 * dispara a notificação ao gestor e registra o alerta com o status retornado
 * pela api-notificacoes (ENVIADA em sucesso, PENDENTE em falha).
 *
 * FIXME: processa tudo em memória — para frotas grandes, usar paginação/stream.
 * FIXME: sem idempotência forte — a checagem de alerta pendente é a única guarda.
 */
@Slf4j
@Service
public class AlertaManutencaoService {

    private final VeiculoRepository veiculoRepository;
    private final AlertaManutencaoRepository alertaManutencaoRepository;
    private final VeiculoNotificacaoService veiculoNotificacaoService;
    private final VeiculoManutencaoService veiculoManutencaoService;

    public AlertaManutencaoService(VeiculoRepository veiculoRepository,
                                  AlertaManutencaoRepository alertaManutencaoRepository,
                                  VeiculoNotificacaoService veiculoNotificacaoService,
                                  VeiculoManutencaoService veiculoManutencaoService) {
        this.veiculoRepository = veiculoRepository;
        this.alertaManutencaoRepository = alertaManutencaoRepository;
        this.veiculoNotificacaoService = veiculoNotificacaoService;
        this.veiculoManutencaoService = veiculoManutencaoService;
    }

    /**
     * Verifica todos os veículos elegíveis e gera alertas para cada um.
     *
     * @return lista de alertas gerados nesta execução (vazia se nenhum).
     */
    @Transactional
    public List<AlertaManutencao> verificarVeiculosElegiveis() {
        Long limiteKm = veiculoManutencaoService.getVerificarNecessidadeLimite();
        Integer intervaloMeses = veiculoManutencaoService.getIntervaloMeses();

        List<Veiculo> elegiveisKm = veiculoRepository.findElegiveisPorQuilometragem(limiteKm);
        List<Veiculo> elegiveisTempo = veiculoRepository.findElegiveisPorTempoSemManutencao(intervaloMeses);

        // Merge sem duplicatas por id do veículo
        Set<Long> idsProcessados = new HashSet<>();
        List<AlertaManutencao> alertas = new ArrayList<>();

        for (Veiculo v : elegiveisKm) {
            if (idsProcessados.add(v.getId())) {
                adicionarSeNaoNulo(alertas, processarAlerta(v, TipoAlerta.QUILOMETRAGEM, limiteKm, intervaloMeses));
            }
        }
        for (Veiculo v : elegiveisTempo) {
            if (idsProcessados.add(v.getId())) {
                adicionarSeNaoNulo(alertas, processarAlerta(v, TipoAlerta.TEMPO, limiteKm, intervaloMeses));
            }
        }

        if (alertas.isEmpty()) {
            log.info("Nenhum veículo elegível para manutenção preventiva encontrado.");
        } else {
            log.info("Verificação de manutenção preventiva concluída: {} alerta(s) gerado(s).", alertas.size());
        }
        return alertas;
    }

    private void adicionarSeNaoNulo(List<AlertaManutencao> lista, AlertaManutencao alerta) {
        if (alerta != null) {
            lista.add(alerta);
        }
    }

    private AlertaManutencao processarAlerta(Veiculo veiculo, TipoAlerta tipoAlerta,
                                              Long limiteKm, Integer intervaloMeses) {
        if (alertaManutencaoRepository.existsByVeiculoIdAndStatusNotificacaoNotEnviada(veiculo.getId())) {
            log.debug("Veículo {} já possui alerta pendente/falha. Ignorando.", veiculo.getPlaca());
            return null;
        }

        AlertaManutencao alerta = new AlertaManutencao(veiculo.getId(), tipoAlerta);
        alerta.setQuilometragemAtual(veiculo.getQuilometragem());
        alerta.setLimiteQuilometragem(limiteKm);
        alerta.setIntervaloMeses(intervaloMeses);

        try {
            Optional<NotificacaoResponse> resposta = veiculoNotificacaoService.notificarManutencao(
                    veiculo.getPlaca(), veiculo.getQuilometragem());

            if (resposta.isPresent() && resposta.get().getId() != null) {
                alerta.setStatusNotificacao(StatusNotificacaoAlerta.ENVIADA);
                alerta.setNotificacaoId(resposta.get().getId());
                log.info("Alerta de manutenção registrado como ENVIADA: veiculo={}, notificacaoId={}",
                        veiculo.getPlaca(), resposta.get().getId());
            } else {
                alerta.setStatusNotificacao(StatusNotificacaoAlerta.PENDENTE);
                log.warn("Alerta de manutenção registrado como PENDENTE (resposta ausente): veiculo={}",
                        veiculo.getPlaca());
            }
        } catch (Exception e) {
            alerta.setStatusNotificacao(StatusNotificacaoAlerta.PENDENTE);
            log.error("Falha ao notificar api-notificacoes. Alerta registrado como PENDENTE: veiculo={}, erro={}",
                    veiculo.getPlaca(), e.getMessage());
        }

        return alertaManutencaoRepository.save(alerta);
    }
}
