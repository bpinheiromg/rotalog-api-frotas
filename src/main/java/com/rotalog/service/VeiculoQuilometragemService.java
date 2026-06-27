package com.rotalog.service;

import com.rotalog.config.VeiculoProperties;
import com.rotalog.domain.Veiculo;
import com.rotalog.exception.QuilometragemInvalidaException;
import com.rotalog.exception.VeiculoNaoEncontradoException;
import com.rotalog.repository.VeiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço especializado em gestão de quilometragem de veículos.
 * Centraliza operações de atualização e validação de km.
 */
@Slf4j
@Service
public class VeiculoQuilometragemService {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoProperties veiculoProperties;

    public VeiculoQuilometragemService(VeiculoRepository veiculoRepository,
                                        VeiculoProperties veiculoProperties) {
        this.veiculoRepository = veiculoRepository;
        this.veiculoProperties = veiculoProperties;
    }

    /**
     * Atualiza a quilometragem do veículo.
     * Valida se o valor é não-negativo e loga avisos sobre redução.
     *
     * @param veiculoId ID do veículo
     * @param novaQuilometragem novo valor de quilometragem
     * @return o veículo atualizado
     * @throws VeiculoNaoEncontradoException se o veículo não existe
     * @throws QuilometragemInvalidaException se o valor for negativo
     */
    public Veiculo atualizarQuilometragem(Long veiculoId, Long novaQuilometragem) {
        Veiculo veiculo = buscarVeiculoOuFalhar(veiculoId);

        if (novaQuilometragem < 0) {
            throw new QuilometragemInvalidaException("Quilometragem não pode ser negativa");
        }

        if (novaQuilometragem < veiculo.getQuilometragem()) {
            log.warn("AVISO: Quilometragem menor que a atual! veiculoId={}", veiculoId);
        }

        veiculo.setQuilometragem(novaQuilometragem);
        veiculo.setDataAtualizacao(java.time.LocalDateTime.now());

        return veiculoRepository.save(veiculo);
    }

    /**
     * Verifica se a quilometragem atingiu o limite de manutenção.
     */
    public boolean precisaDeManutencao(Veiculo veiculo) {
        Long limite = veiculoProperties.getQuilometragem().getLimiteManutencao();
        return veiculo.getQuilometragem() != null && veiculo.getQuilometragem() >= limite;
    }

    /**
     * Valida se a redução de quilometragem é suspeita.
     */
    public boolean isReducaoSuspeita(Long atual, Long nova) {
        return nova != null && atual != null && nova < atual;
    }

    private Veiculo buscarVeiculoOuFalhar(Long veiculoId) {
        return veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(
                        "Veículo não encontrado: " + veiculoId));
    }
}
