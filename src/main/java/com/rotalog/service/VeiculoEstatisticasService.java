package com.rotalog.service;

import com.rotalog.domain.Veiculo;
import com.rotalog.repository.VeiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço especializado em cálculo de estatísticas da frota.
 * Centraliza queries e formatação de dados estatísticos.
 */
@Slf4j
@Service
public class VeiculoEstatisticasService {

    private final VeiculoRepository veiculoRepository;

    public VeiculoEstatisticasService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    /**
     * Obtém estatísticas consolidadas da frota em formato JSON.
     * Inclui total de veículos e contagem por status.
     *
     * @return String JSON com estatísticas
     */
    public String obterEstatisticas() {
        long totalVeiculos = veiculoRepository.count();
        long ativos = veiculoRepository.findByStatus("ATIVO").size();
        long inativos = veiculoRepository.findByStatus("INATIVO").size();
        long emManutencao = veiculoRepository.findByStatus("MANUTENCAO").size();

        return String.format(
                "{\"total\": %d, \"ativos\": %d, \"inativos\": %d, \"em_manutencao\": %d}",
                totalVeiculos, ativos, inativos, emManutencao
        );
    }

    /**
     * Obtém estatísticas (mantém nome original para compatibilidade).
     * @deprecated usar {@link #obterEstatisticas()}
     */
    public String obterEstatisticasFreita() {
        return obterEstatisticas();
    }

    /**
     * Obtém lista de veículos por status.
     */
    public List<Veiculo> obterPorStatus(String status) {
        return veiculoRepository.findByStatus(status);
    }
}
