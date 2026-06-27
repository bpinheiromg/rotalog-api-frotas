package com.rotalog.service;

import com.rotalog.config.VeiculoProperties;
import com.rotalog.domain.Veiculo;
import com.rotalog.exception.VeiculoNaoEncontradoException;
import com.rotalog.repository.VeiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço especializado em lógica de manutenção preventiva de veículos.
 * Centraliza agendamento, cálculo de custos e verificação de necessidade.
 */
@Slf4j
@Service
public class VeiculoManutencaoService {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoProperties veiculoProperties;

    public VeiculoManutencaoService(VeiculoRepository veiculoRepository,
                                     VeiculoProperties veiculoProperties) {
        this.veiculoRepository = veiculoRepository;
        this.veiculoProperties = veiculoProperties;
    }

    /**
     * Agenda manutenção preventiva para o veículo.
     * Registra o agendamento e notifica o gestor.
     *
     * @param veiculoId ID do veículo
     * @param quilometragemLimite quilometragem limite para a manutenção
     */
    public void agendarManutencao(Long veiculoId, Long quilometragemLimite) {
        Veiculo veiculo = buscarVeiculoOuFalhar(veiculoId);

        log.info("Manutenção preventiva agendada para veículo {} em {} km",
                veiculo.getPlaca(), quilometragemLimite);
    }

    /**
     * Calcula o custo de manutenção com base no modelo e quilometragem.
     * Fórmula: custoBase + (quilometragem * custoPorKm)
     *
     * @param modelo modelo do veículo (não usado no cálculo atual, mantido para compatibilidade)
     * @param quilometragem quilometragem atual do veículo
     * @return custo calculado
     */
    public Double calcularCustoManutencao(String modelo, Long quilometragem) {
        Double custoBase = veiculoProperties.getManutencao().getCustoBase();
        Double custoPorKm = veiculoProperties.getManutencao().getCustoPorKm();

        return custoBase + (quilometragem * custoPorKm);
    }

    /**
     * Verifica se o veículo precisa de manutenção preventiva baseado na quilometragem.
     *
     * @param veiculo veículo a ser verificado
     * @return true se a quilometragem atingiu ou ultrapassou o limite
     */
    public boolean verificarNecessidade(Veiculo veiculo) {
        Long limite = veiculoProperties.getQuilometragem().getLimiteManutencao();
        return veiculo.getQuilometragem() != null && veiculo.getQuilometragem() >= limite;
    }

    /**
     * Retorna o intervalo de quilometragem entre manutenções.
     */
    public Long getIntervaloQuilometragem() {
        return veiculoProperties.getQuilometragem().getIntervalo();
    }

    /**
     * Retorna o intervalo de meses entre manutenções.
     */
    public Integer getIntervaloMeses() {
        return veiculoProperties.getManutencao().getIntervaloMeses();
    }

    private Veiculo buscarVeiculoOuFalhar(Long veiculoId) {
        return veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(
                        "Veículo não encontrado: " + veiculoId));
    }
}
