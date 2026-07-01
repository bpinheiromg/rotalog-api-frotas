package com.rotalog.service;

import com.rotalog.domain.AlertaManutencao;
import com.rotalog.domain.StatusNotificacaoAlerta;
import com.rotalog.domain.TipoAlerta;
import com.rotalog.domain.Veiculo;
import com.rotalog.dto.NotificacaoResponse;
import com.rotalog.repository.AlertaManutencaoRepository;
import com.rotalog.repository.VeiculoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaManutencaoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private AlertaManutencaoRepository alertaManutencaoRepository;

    @Mock
    private VeiculoNotificacaoService veiculoNotificacaoService;

    @Mock
    private VeiculoManutencaoService veiculoManutencaoService;

    @InjectMocks
    private AlertaManutencaoService service;

    private Veiculo criarVeiculo(Long id, String placa, Long quilometragem, String status) {
        Veiculo v = new Veiculo();
        v.setId(id);
        v.setPlaca(placa);
        v.setQuilometragem(quilometragem);
        v.setStatus(status);
        return v;
    }

    private NotificacaoResponse criarNotificacaoResponse(Long id, String status) {
        NotificacaoResponse response = new NotificacaoResponse();
        response.setId(id);
        response.setStatus(status);
        return response;
    }

    // =====================================================================
    // verificarVeiculosElegiveis - Por Quilometragem
    // =====================================================================

    @Nested
    @DisplayName("verificarVeiculosElegiveis - Por Quilometragem")
    class VerificarPorQuilometragem {

        @Test
        @DisplayName("Deve detectar veículo com km >= limite e criar alerta ENVIADA")
        void deveDetetarVeiculoComKmAltoECriarAlertaEnviada() {
            // Arrange
            Veiculo veiculo = criarVeiculo(1L, "DEF4G56", 120000L, "ATIVO");
            when(veiculoManutencaoService.getVerificarNecessidadeLimite()).thenReturn(50000L);
            when(veiculoManutencaoService.getIntervaloMeses()).thenReturn(3);
            when(veiculoRepository.findElegiveisPorQuilometragem(50000L))
                    .thenReturn(List.of(veiculo));
            when(veiculoRepository.findElegiveisPorTempoSemManutencao(3))
                    .thenReturn(Collections.emptyList());
            when(alertaManutencaoRepository.existsByVeiculoIdAndStatusNotificacaoNotEnviada(1L))
                    .thenReturn(false);
            when(veiculoNotificacaoService.notificarManutencao("DEF4G56", 120000L))
                    .thenReturn(Optional.of(criarNotificacaoResponse(1001L, "ENVIADO")));
            when(alertaManutencaoRepository.save(any(AlertaManutencao.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            List<AlertaManutencao> alertas = service.verificarVeiculosElegiveis();

            // Assert
            assertEquals(1, alertas.size());
            AlertaManutencao alerta = alertas.get(0);
            assertEquals(1L, alerta.getVeiculoId());
            assertEquals(TipoAlerta.QUILOMETRAGEM, alerta.getTipoAlerta());
            assertEquals(120000L, alerta.getQuilometragemAtual());
            assertEquals(50000L, alerta.getLimiteQuilometragem());
            assertEquals(StatusNotificacaoAlerta.ENVIADA, alerta.getStatusNotificacao());
            assertEquals(1001L, alerta.getNotificacaoId());
            verify(veiculoNotificacaoService).notificarManutencao("DEF4G56", 120000L);
            verify(alertaManutencaoRepository).save(any(AlertaManutencao.class));
        }

        @Test
        @DisplayName("Deve ignorar veículo abaixo do limite de km")
        void deveIgnorarVeiculoAbaixoDoLimite() {
            // Arrange
            when(veiculoManutencaoService.getVerificarNecessidadeLimite()).thenReturn(50000L);
            when(veiculoManutencaoService.getIntervaloMeses()).thenReturn(3);
            when(veiculoRepository.findElegiveisPorQuilometragem(50000L))
                    .thenReturn(Collections.emptyList());
            when(veiculoRepository.findElegiveisPorTempoSemManutencao(3))
                    .thenReturn(Collections.emptyList());

            // Act
            List<AlertaManutencao> alertas = service.verificarVeiculosElegiveis();

            // Assert
            assertTrue(alertas.isEmpty());
        }
    }

    // =====================================================================
    // verificarVeiculosElegiveis - Por Tempo
    // =====================================================================

    @Nested
    @DisplayName("verificarVeiculosElegiveis - Por Tempo")
    class VerificarPorTempo {

        @Test
        @DisplayName("Deve detectar veículo sem manutenção há X meses e criar alerta ENVIADA")
        void deveDetetarVeiculoSemManutencaoECriarAlertaEnviada() {
            // Arrange
            Veiculo veiculo = criarVeiculo(2L, "VWX9Y01", 30000L, "ATIVO");
            when(veiculoManutencaoService.getVerificarNecessidadeLimite()).thenReturn(50000L);
            when(veiculoManutencaoService.getIntervaloMeses()).thenReturn(3);
            when(veiculoRepository.findElegiveisPorQuilometragem(50000L))
                    .thenReturn(Collections.emptyList());
            when(veiculoRepository.findElegiveisPorTempoSemManutencao(3))
                    .thenReturn(List.of(veiculo));
            when(alertaManutencaoRepository.existsByVeiculoIdAndStatusNotificacaoNotEnviada(2L))
                    .thenReturn(false);
            when(veiculoNotificacaoService.notificarManutencao("VWX9Y01", 30000L))
                    .thenReturn(Optional.of(criarNotificacaoResponse(1002L, "ENVIADO")));
            when(alertaManutencaoRepository.save(any(AlertaManutencao.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            List<AlertaManutencao> alertas = service.verificarVeiculosElegiveis();

            // Assert
            assertEquals(1, alertas.size());
            AlertaManutencao alerta = alertas.get(0);
            assertEquals(2L, alerta.getVeiculoId());
            assertEquals(TipoAlerta.TEMPO, alerta.getTipoAlerta());
            assertEquals(StatusNotificacaoAlerta.ENVIADA, alerta.getStatusNotificacao());
            assertEquals(1002L, alerta.getNotificacaoId());
        }
    }

    // =====================================================================
    // Deduplicação
    // =====================================================================

    @Nested
    @DisplayName("Deduplicação de veículos")
    class Deduplicacao {

        @Test
        @DisplayName("Não deve criar alerta duplicado para veículo já elegível por km e tempo")
        void naoDeveCriarAlertaDuplicadoParaMesmoVeiculo() {
            // Arrange
            Veiculo veiculo = criarVeiculo(1L, "DEF4G56", 120000L, "ATIVO");
            when(veiculoManutencaoService.getVerificarNecessidadeLimite()).thenReturn(50000L);
            when(veiculoManutencaoService.getIntervaloMeses()).thenReturn(3);
            when(veiculoRepository.findElegiveisPorQuilometragem(50000L))
                    .thenReturn(List.of(veiculo));
            when(veiculoRepository.findElegiveisPorTempoSemManutencao(3))
                    .thenReturn(List.of(veiculo)); // Mesmo veículo aparece nas duas listas
            when(alertaManutencaoRepository.existsByVeiculoIdAndStatusNotificacaoNotEnviada(1L))
                    .thenReturn(false);
            when(veiculoNotificacaoService.notificarManutencao("DEF4G56", 120000L))
                    .thenReturn(Optional.of(criarNotificacaoResponse(1001L, "ENVIADO")));
            when(alertaManutencaoRepository.save(any(AlertaManutencao.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            List<AlertaManutencao> alertas = service.verificarVeiculosElegiveis();

            // Assert
            assertEquals(1, alertas.size(), "Deve criar apenas um alerta mesmo aparecendo em ambas as listas");
            verify(veiculoNotificacaoService, times(1)).notificarManutencao(anyString(), anyLong());
        }

        @Test
        @DisplayName("Não deve criar alerta se veículo já tem alerta pendente")
        void naoDeveCriarAlertaSeVeiculoJaTemAlertaPendente() {
            // Arrange
            Veiculo veiculo = criarVeiculo(1L, "DEF4G56", 120000L, "ATIVO");
            when(veiculoManutencaoService.getVerificarNecessidadeLimite()).thenReturn(50000L);
            when(veiculoManutencaoService.getIntervaloMeses()).thenReturn(3);
            when(veiculoRepository.findElegiveisPorQuilometragem(50000L))
                    .thenReturn(List.of(veiculo));
            when(veiculoRepository.findElegiveisPorTempoSemManutencao(3))
                    .thenReturn(Collections.emptyList());
            when(alertaManutencaoRepository.existsByVeiculoIdAndStatusNotificacaoNotEnviada(1L))
                    .thenReturn(true); // Já existe alerta pendente

            // Act
            List<AlertaManutencao> alertas = service.verificarVeiculosElegiveis();

            // Assert
            assertTrue(alertas.isEmpty());
            verify(veiculoNotificacaoService, never()).notificarManutencao(anyString(), anyLong());
            verify(alertaManutencaoRepository, never()).save(any(AlertaManutencao.class));
        }
    }

    // =====================================================================
    // Falha na api-notificacoes
    // =====================================================================

    @Nested
    @DisplayName("Tratamento de falha na api-notificacoes")
    class FalhaNotificacao {

        @Test
        @DisplayName("Deve criar alerta com status PENDENTE quando api-notificacoes falha (exceção)")
        void deveCriarAlertaPendenteQuandoApiNotificacoesFalha() {
            // Arrange
            Veiculo veiculo = criarVeiculo(1L, "DEF4G56", 120000L, "ATIVO");
            when(veiculoManutencaoService.getVerificarNecessidadeLimite()).thenReturn(50000L);
            when(veiculoManutencaoService.getIntervaloMeses()).thenReturn(3);
            when(veiculoRepository.findElegiveisPorQuilometragem(50000L))
                    .thenReturn(List.of(veiculo));
            when(veiculoRepository.findElegiveisPorTempoSemManutencao(3))
                    .thenReturn(Collections.emptyList());
            when(alertaManutencaoRepository.existsByVeiculoIdAndStatusNotificacaoNotEnviada(1L))
                    .thenReturn(false);
            when(veiculoNotificacaoService.notificarManutencao("DEF4G56", 120000L))
                    .thenReturn(Optional.empty()); // Falha na notificação
            when(alertaManutencaoRepository.save(any(AlertaManutencao.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            List<AlertaManutencao> alertas = service.verificarVeiculosElegiveis();

            // Assert
            assertEquals(1, alertas.size());
            AlertaManutencao alerta = alertas.get(0);
            assertEquals(StatusNotificacaoAlerta.PENDENTE, alerta.getStatusNotificacao());
            assertNull(alerta.getNotificacaoId());
        }

        @Test
        @DisplayName("Deve criar alerta com status PENDENTE quando resposta é nula")
        void deveCriarAlertaPendenteQuandoRespostaNula() {
            // Arrange
            Veiculo veiculo = criarVeiculo(1L, "DEF4G56", 120000L, "ATIVO");
            when(veiculoManutencaoService.getVerificarNecessidadeLimite()).thenReturn(50000L);
            when(veiculoManutencaoService.getIntervaloMeses()).thenReturn(3);
            when(veiculoRepository.findElegiveisPorQuilometragem(50000L))
                    .thenReturn(List.of(veiculo));
            when(veiculoRepository.findElegiveisPorTempoSemManutencao(3))
                    .thenReturn(Collections.emptyList());
            when(alertaManutencaoRepository.existsByVeiculoIdAndStatusNotificacaoNotEnviada(1L))
                    .thenReturn(false);
            when(veiculoNotificacaoService.notificarManutencao("DEF4G56", 120000L))
                    .thenReturn(Optional.ofNullable(null)); // Resposta nula
            when(alertaManutencaoRepository.save(any(AlertaManutencao.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            List<AlertaManutencao> alertas = service.verificarVeiculosElegiveis();

            // Assert
            assertEquals(1, alertas.size());
            assertEquals(StatusNotificacaoAlerta.PENDENTE, alertas.get(0).getStatusNotificacao());
        }

        @Test
        @DisplayName("Deve criar alerta com status PENDENTE quando exceção é lançada")
        void deveCriarAlertaPendenteQuandoExcecaoLancada() {
            // Arrange
            Veiculo veiculo = criarVeiculo(1L, "DEF4G56", 120000L, "ATIVO");
            when(veiculoManutencaoService.getVerificarNecessidadeLimite()).thenReturn(50000L);
            when(veiculoManutencaoService.getIntervaloMeses()).thenReturn(3);
            when(veiculoRepository.findElegiveisPorQuilometragem(50000L))
                    .thenReturn(List.of(veiculo));
            when(veiculoRepository.findElegiveisPorTempoSemManutencao(3))
                    .thenReturn(Collections.emptyList());
            when(alertaManutencaoRepository.existsByVeiculoIdAndStatusNotificacaoNotEnviada(1L))
                    .thenReturn(false);
            when(veiculoNotificacaoService.notificarManutencao("DEF4G56", 120000L))
                    .thenThrow(new RuntimeException("Connection refused"));
            when(alertaManutencaoRepository.save(any(AlertaManutencao.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            List<AlertaManutencao> alertas = service.verificarVeiculosElegiveis();

            // Assert
            assertEquals(1, alertas.size());
            assertEquals(StatusNotificacaoAlerta.PENDENTE, alertas.get(0).getStatusNotificacao());
        }
    }

    // =====================================================================
    // Sucesso na api-notificacoes
    // =====================================================================

    @Nested
    @DisplayName("Sucesso na api-notificacoes")
    class SucessoNotificacao {

        @Test
        @DisplayName("Deve criar alerta com status ENVIADA e notificacaoId quando sucesso")
        void deveCriarAlertaEnviadaComNotificacaoIdQuandoSucesso() {
            // Arrange
            Veiculo veiculo = criarVeiculo(1L, "DEF4G56", 120000L, "ATIVO");
            when(veiculoManutencaoService.getVerificarNecessidadeLimite()).thenReturn(50000L);
            when(veiculoManutencaoService.getIntervaloMeses()).thenReturn(3);
            when(veiculoRepository.findElegiveisPorQuilometragem(50000L))
                    .thenReturn(List.of(veiculo));
            when(veiculoRepository.findElegiveisPorTempoSemManutencao(3))
                    .thenReturn(Collections.emptyList());
            when(alertaManutencaoRepository.existsByVeiculoIdAndStatusNotificacaoNotEnviada(1L))
                    .thenReturn(false);
            NotificacaoResponse response = criarNotificacaoResponse(999L, "ENVIADO");
            when(veiculoNotificacaoService.notificarManutencao("DEF4G56", 120000L))
                    .thenReturn(Optional.of(response));
            when(alertaManutencaoRepository.save(any(AlertaManutencao.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            List<AlertaManutencao> alertas = service.verificarVeiculosElegiveis();

            // Assert
            assertEquals(1, alertas.size());
            AlertaManutencao alerta = alertas.get(0);
            assertEquals(StatusNotificacaoAlerta.ENVIADA, alerta.getStatusNotificacao());
            assertEquals(999L, alerta.getNotificacaoId());
        }
    }

    // =====================================================================
    // Múltiplos veículos
    // =====================================================================

    @Nested
    @DisplayName("Múltiplos veículos")
    class MultiplosVeiculos {

        @Test
        @DisplayName("Deve criar alertas para múltiplos veículos elegíveis")
        void deveCriarAlertasParaMultiplosVeiculos() {
            // Arrange
            Veiculo v1 = criarVeiculo(1L, "DEF4G56", 120000L, "ATIVO");
            Veiculo v2 = criarVeiculo(2L, "VWX9Y01", 95000L, "ATIVO");
            Veiculo v3 = criarVeiculo(3L, "BCD2E34", 78000L, "ATIVO");
            when(veiculoManutencaoService.getVerificarNecessidadeLimite()).thenReturn(50000L);
            when(veiculoManutencaoService.getIntervaloMeses()).thenReturn(3);
            when(veiculoRepository.findElegiveisPorQuilometragem(50000L))
                    .thenReturn(List.of(v1, v2, v3));
            when(veiculoRepository.findElegiveisPorTempoSemManutencao(3))
                    .thenReturn(Collections.emptyList());
            when(alertaManutencaoRepository.existsByVeiculoIdAndStatusNotificacaoNotEnviada(anyLong()))
                    .thenReturn(false);
            when(veiculoNotificacaoService.notificarManutencao(anyString(), anyLong()))
                    .thenReturn(Optional.of(criarNotificacaoResponse(1000L, "ENVIADO")));
            when(alertaManutencaoRepository.save(any(AlertaManutencao.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            List<AlertaManutencao> alertas = service.verificarVeiculosElegiveis();

            // Assert
            assertEquals(3, alertas.size());
            verify(veiculoNotificacaoService, times(3)).notificarManutencao(anyString(), anyLong());
        }
    }
}