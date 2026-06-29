package com.rotalog.service;

import com.rotalog.exception.VeiculoNaoEncontradoException;
import com.rotalog.repository.VeiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço especializado em integração com sistemas externos.
 * Placeholder para futura implementação com retry e circuit breaker.
 */
@Slf4j
@Service
public class VeiculoSincronizacaoService {

    private final VeiculoRepository veiculoRepository;

    public VeiculoSincronizacaoService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    /**
     * Inicia sincronização com sistema externo.
     * TODO: Implementar integração real com retry logic e circuit breaker.
     */
    public void sincronizar() {
        log.info("Sincronização com sistema externo iniciada");

        // TODO: Implementar integração real
        // TODO: Adicionar retry logic
        // TODO: Adicionar circuit breaker
    }

    /**
     * Sincroniza um veículo específico com o sistema externo.
     *
     * @param veiculoId ID do veículo a sincronizar
     * @throws VeiculoNaoEncontradoException se o veículo não existe
     */
    public void sincronizarVeiculo(Long veiculoId) {
        veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(
                        "Veículo não encontrado: " + veiculoId));

        log.info("Sincronização do veículo {} iniciada", veiculoId);

        // TODO: Implementar integração real
    }
}
