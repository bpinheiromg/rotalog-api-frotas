package com.rotalog.service;

import com.rotalog.config.VeiculoProperties;
import com.rotalog.domain.Veiculo;
import com.rotalog.domain.VeiculoStatus;
import com.rotalog.exception.AnoFabricacaoInvalidoException;
import com.rotalog.exception.CampoObrigatorioException;
import com.rotalog.exception.ModeloInvalidoException;
import com.rotalog.exception.PlacaInvalidaException;
import com.rotalog.exception.QuilometragemInvalidaException;
import com.rotalog.exception.StatusInvalidoException;
import com.rotalog.exception.VeiculoDuplicadoException;
import com.rotalog.exception.VeiculoJaEmManutencaoException;
import com.rotalog.repository.VeiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Serviço especializado em validações de veículos.
 * Centraliza toda a lógica de validação antes de operações de escrita.
 */
@Slf4j
@Service
public class VeiculoValidadorService {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoProperties veiculoProperties;

    public VeiculoValidadorService(VeiculoRepository veiculoRepository,
                                   VeiculoProperties veiculoProperties) {
        this.veiculoRepository = veiculoRepository;
        this.veiculoProperties = veiculoProperties;
    }

    /**
     * Valida todos os campos para registro de um novo veículo.
     * @throws PlacaInvalidaException se a placa for inválida
     * @throws VeiculoDuplicadoException se a placa já existir
     * @throws ModeloInvalidoException se o modelo for nulo/vazio
     * @throws AnoFabricacaoInvalidoException se o ano estiver fora do intervalo
     */
    public void validarRegistro(String placa, String modelo, Integer anoFabricacao) {
        validarPlaca(placa);
        validarDuplicidadePlaca(placa);
        validarModelo(modelo);
        validarAnoFabricacao(anoFabricacao);
    }

    /**
     * Valida formato da placa (não nula, não vazia, tamanho correto).
     */
    public void validarPlaca(String placa) {
        if (placa == null || placa.isEmpty()) {
            throw new CampoObrigatorioException("Placa é obrigatória");
        }
        if (placa.length() != veiculoProperties.getPlaca().getTamanho()) {
            throw new PlacaInvalidaException(
                    "Placa deve ter " + veiculoProperties.getPlaca().getTamanho() + " caracteres");
        }
    }

    /**
     * Verifica se a placa já está cadastrada.
     */
    public void validarDuplicidadePlaca(String placa) {
        Optional<Veiculo> existente = veiculoRepository.findByPlaca(placa);
        if (existente.isPresent()) {
            throw new VeiculoDuplicadoException("Veículo com placa " + placa + " já existe");
        }
    }

    /**
     * Valida o modelo do veículo.
     */
    public void validarModelo(String modelo) {
        if (modelo == null || modelo.isEmpty()) {
            throw new ModeloInvalidoException("Modelo é obrigatório");
        }
    }

    /**
     * Valida o ano de fabricação.
     */
    public void validarAnoFabricacao(Integer anoFabricacao) {
        if (anoFabricacao == null) {
            throw new AnoFabricacaoInvalidoException("Ano de fabricação é obrigatório");
        }
        int minimo = veiculoProperties.getAnoFabricacao().getMinimo();
        int maximo = veiculoProperties.getAnoFabricacao().getMaximo();
        if (anoFabricacao < minimo || anoFabricacao > maximo) {
            throw new AnoFabricacaoInvalidoException(
                    "Ano de fabricação inválido: deve estar entre " + minimo + " e " + maximo);
        }
    }

    /**
     * Valida a quilometragem (não pode ser negativa).
     */
    public void validarQuilometragem(Long quilometragem) {
        if (quilometragem == null) {
            throw new QuilometragemInvalidaException("Quilometragem é obrigatória");
        }
        if (quilometragem < 0) {
            throw new QuilometragemInvalidaException("Quilometragem não pode ser negativa");
        }
    }

    /**
     * Valida se a quilometragem é decrescente (apenas loga, não bloqueia).
     */
    public void validarReducaoQuilometragem(Long atual, Long nova) {
        if (nova != null && atual != null && nova < atual) {
            log.warn("Tentativa de reduzir quilometragem: {} -> {}", atual, nova);
        }
    }

    /**
     * Valida o status do veículo.
     */
    public void validarStatus(String status) {
        try {
            VeiculoStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new StatusInvalidoException("Status inválido: " + status);
        }
    }

    /**
     * Valida se o veículo pode receber manutenção (não está já em manutenção).
     */
    public void validarDisponibilidadeParaManutencao(Veiculo veiculo) {
        if (veiculo.getStatus().equals(VeiculoStatus.MANUTENCAO.name())) {
            throw new VeiculoJaEmManutencaoException(
                    "Veículo " + veiculo.getPlaca() + " já está em manutenção");
        }
    }
}
