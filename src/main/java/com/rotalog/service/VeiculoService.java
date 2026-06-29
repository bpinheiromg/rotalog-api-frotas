package com.rotalog.service;

import com.rotalog.domain.Veiculo;
import com.rotalog.domain.VeiculoStatus;
import com.rotalog.exception.VeiculoNaoEncontradoException;
import com.rotalog.repository.VeiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VeiculoService - Serviço de orquestração para operações com veículos.
 * Refatorado: delega validações, notificações e cálculos para serviços especializados.
 */
@Slf4j
@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoValidadorService validadorService;
    private final VeiculoNotificacaoService notificacaoService;
    private final VeiculoQuilometragemService quilometragemService;
    private final VeiculoManutencaoService manutencaoService;
    private final VeiculoEstatisticasService estatisticasService;
    private final VeiculoSincronizacaoService sincronizacaoService;

    public VeiculoService(VeiculoRepository veiculoRepository,
                          VeiculoValidadorService validadorService,
                          VeiculoNotificacaoService notificacaoService,
                          VeiculoQuilometragemService quilometragemService,
                          VeiculoManutencaoService manutencaoService,
                          VeiculoEstatisticasService estatisticasService,
                          VeiculoSincronizacaoService sincronizacaoService) {
        this.veiculoRepository = veiculoRepository;
        this.validadorService = validadorService;
        this.notificacaoService = notificacaoService;
        this.quilometragemService = quilometragemService;
        this.manutencaoService = manutencaoService;
        this.estatisticasService = estatisticasService;
        this.sincronizacaoService = sincronizacaoService;
    }

    /**
     * Lista todos os veículos.
     */
    public List<Veiculo> listarTodos() {
        log.info("Listando todos os veículos");
        return veiculoRepository.findAll();
    }

    /**
     * Busca veículo por ID.
     *
     * @throws VeiculoNaoEncontradoException se não encontrado
     */
    public Veiculo buscarPorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException("Veículo não encontrado: " + id));
    }

    /**
     * Busca veículo por placa.
     *
     * @throws VeiculoNaoEncontradoException se não encontrado
     */
    public Veiculo buscarPorPlaca(String placa) {
        return veiculoRepository.findByPlaca(placa)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(
                        "Veículo não encontrado com placa: " + placa));
    }

    /**
     * Registra um novo veículo no sistema.
     * Valida campos, persiste e envia notificação.
     */
    public Veiculo registrarVeiculo(String placa, String modelo, Integer anoFabricacao) {
        validadorService.validarRegistro(placa, modelo, anoFabricacao);

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(placa.toUpperCase());
        veiculo.setModelo(modelo);
        veiculo.setAnoFabricacao(anoFabricacao);
        veiculo.setStatus(VeiculoStatus.ATIVO.name());
        veiculo.setQuilometragem(0L);
        veiculo.setDataCadastro(LocalDateTime.now());
        veiculo.setDataAtualizacao(LocalDateTime.now());

        Veiculo salvo = veiculoRepository.save(veiculo);
        log.info("Veículo registrado: {} - {}", salvo.getPlaca(), salvo.getModelo());

        notificacaoService.notificarNovoVeiculo(salvo.getPlaca(), salvo.getModelo());

        return salvo;
    }

    /**
     * Atualiza dados do veículo (modelo, ano, quilometragem).
     * Campos null/vazios são mantidos com valor anterior.
     */
    public Veiculo atualizarVeiculo(Long id, String modelo, Integer anoFabricacao, Long quilometragem) {
        Veiculo veiculo = buscarPorId(id);

        if (modelo != null && !modelo.isEmpty()) {
            veiculo.setModelo(modelo);
        }
        if (anoFabricacao != null) {
            veiculo.setAnoFabricacao(anoFabricacao);
        }
        if (quilometragem != null) {
            validadorService.validarReducaoQuilometragem(veiculo.getQuilometragem(), quilometragem);
            veiculo.setQuilometragem(quilometragem);
        }

        veiculo.setDataAtualizacao(LocalDateTime.now());
        return veiculoRepository.save(veiculo);
    }

    /**
     * Atualiza quilometragem do veículo e envia alerta se necessário.
     */
    public Veiculo atualizarQuilometragem(Long veiculoId, Long novaQuilometragem) {
        Veiculo atualizado = quilometragemService.atualizarQuilometragem(veiculoId, novaQuilometragem);

        if (quilometragemService.precisaDeManutencao(atualizado)) {
            notificacaoService.notificarManutencao(atualizado.getPlaca(), novaQuilometragem);
        }

        return atualizado;
    }

    /**
     * Obtém veículos por status.
     *
     * @throws com.rotalog.exception.StatusInvalidoException se status inválido
     */
    public List<Veiculo> obterVeiculosPorStatus(String status) {
        validadorService.validarStatus(status);
        return veiculoRepository.findByStatus(status);
    }

    /**
     * Agenda manutenção preventiva para o veículo.
     */
    public void agendarManutencaoPreventiva(Long veiculoId, Long quilometragemLimite) {
        manutencaoService.agendarManutencao(veiculoId, quilometragemLimite);
        Veiculo veiculo = buscarPorId(veiculoId);
        notificacaoService.notificarManutencaoAgendada(veiculo.getPlaca(), quilometragemLimite);
    }

    /**
     * Calcula custo de manutenção com base na quilometragem.
     */
    public Double calcularCustoManutencao(String modelo, Long quilometragem) {
        return manutencaoService.calcularCustoManutencao(modelo, quilometragem);
    }

    /**
     * Verifica se veículo precisa de manutenção (>= 50000 km por padrão).
     */
    public Boolean precisaDeManutencao(Long veiculoId) {
        Veiculo veiculo = buscarPorId(veiculoId);
        return quilometragemService.precisaDeManutencao(veiculo);
    }

    /**
     * Desativa veículo e envia notificação.
     */
    public Veiculo desativarVeiculo(Long veiculoId) {
        Veiculo veiculo = buscarPorId(veiculoId);
        veiculo.setStatus(VeiculoStatus.INATIVO.name());
        veiculo.setDataAtualizacao(LocalDateTime.now());

        Veiculo desativado = veiculoRepository.save(veiculo);
        log.info("Veículo desativado: {}", veiculo.getPlaca());

        notificacaoService.notificarDesativacao(veiculo.getPlaca());

        return desativado;
    }

    /**
     * Reativa veículo.
     */
    public Veiculo reativarVeiculo(Long veiculoId) {
        Veiculo veiculo = buscarPorId(veiculoId);
        veiculo.setStatus(VeiculoStatus.ATIVO.name());
        veiculo.setDataAtualizacao(LocalDateTime.now());

        log.info("Veículo reativado: {}", veiculo.getPlaca());
        return veiculoRepository.save(veiculo);
    }

    /**
     * Obtém estatísticas de frota em formato JSON.
     */
    public String obterEstatisticasFreita() {
        return estatisticasService.obterEstatisticas();
    }

    /**
     * Sincroniza dados com sistema externo.
     */
    public void sincronizarComSistemaExterno() {
        sincronizacaoService.sincronizar();
    }
}
