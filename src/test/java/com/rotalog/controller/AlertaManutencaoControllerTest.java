package com.rotalog.controller;

import com.rotalog.domain.AlertaManutencao;
import com.rotalog.domain.StatusNotificacaoAlerta;
import com.rotalog.domain.TipoAlerta;
import com.rotalog.repository.AlertaManutencaoRepository;
import com.rotalog.service.AlertaManutencaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaManutencaoControllerTest {

    @Mock
    private AlertaManutencaoService alertaManutencaoService;

    @Mock
    private AlertaManutencaoRepository alertaManutencaoRepository;

    @InjectMocks
    private AlertaManutencaoController controller;

    private AlertaManutencao criarAlerta(Long id, Long veiculoId, TipoAlerta tipo, StatusNotificacaoAlerta status) {
        AlertaManutencao alerta = new AlertaManutencao(veiculoId, tipo);
        alerta.setId(id);
        alerta.setStatusNotificacao(status);
        alerta.setDataAlerta(LocalDateTime.now());
        return alerta;
    }

    // =====================================================================
    // verificarElegiveis (GET /veiculos/alertas-manutencao)
    // =====================================================================

    @Nested
    @DisplayName("GET /veiculos/alertas-manutencao - verificarElegiveis")
    class VerificarElegiveis {

        @Test
        @DisplayName("Deve retornar lista de alertas gerados")
        void deveRetornarListaDeAlertas() {
            // Arrange
            AlertaManutencao alerta1 = criarAlerta(1L, 1L, TipoAlerta.QUILOMETRAGEM, StatusNotificacaoAlerta.ENVIADA);
            AlertaManutencao alerta2 = criarAlerta(2L, 2L, TipoAlerta.TEMPO, StatusNotificacaoAlerta.ENVIADA);
            when(alertaManutencaoService.verificarVeiculosElegiveis())
                    .thenReturn(List.of(alerta1, alerta2));

            // Act
            ResponseEntity<?> response = controller.verificarElegiveis();

            // Assert
            assertEquals(200, response.getStatusCodeValue());
            assertTrue(response.getBody() instanceof List);
            List<?> body = (List<?>) response.getBody();
            assertEquals(2, body.size());
            verify(alertaManutencaoService).verificarVeiculosElegiveis();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nenhum veículo elegível")
        void deveRetornarListaVaziaQuandoNenhumElegivel() {
            // Arrange
            when(alertaManutencaoService.verificarVeiculosElegiveis())
                    .thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<?> response = controller.verificarElegiveis();

            // Assert
            assertEquals(200, response.getStatusCodeValue());
            assertTrue(response.getBody() instanceof List);
            List<?> body = (List<?>) response.getBody();
            assertTrue(body.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar 500 quando serviço lança exceção")
        void deveRetornar500QuandoServicoLancaExcecao() {
            // Arrange
            when(alertaManutencaoService.verificarVeiculosElegiveis())
                    .thenThrow(new RuntimeException("Erro interno"));

            // Act
            ResponseEntity<?> response = controller.verificarElegiveis();

            // Assert
            assertEquals(500, response.getStatusCodeValue());
            assertTrue(response.getBody() instanceof java.util.Map);
            java.util.Map<?, ?> body = (java.util.Map<?, ?>) response.getBody();
            assertTrue(body.get("erro").toString().contains("Falha ao verificar alertas de manutenção"));
        }
    }

    // =====================================================================
    // listarAlertas (GET /veiculos/alertas-manutencao/listar)
    // =====================================================================

    @Nested
    @DisplayName("GET /veiculos/alertas-manutencao/listar - listarAlertas")
    class ListarAlertas {

        @Test
        @DisplayName("Deve retornar todos os alertas quando sem filtro")
        void deveRetornarTodosAlertasQuandoSemFiltro() {
            // Arrange
            AlertaManutencao alerta1 = criarAlerta(1L, 1L, TipoAlerta.QUILOMETRAGEM, StatusNotificacaoAlerta.ENVIADA);
            AlertaManutencao alerta2 = criarAlerta(2L, 2L, TipoAlerta.TEMPO, StatusNotificacaoAlerta.PENDENTE);
            when(alertaManutencaoRepository.findAllOrderByDataAlertaDesc())
                    .thenReturn(List.of(alerta1, alerta2));

            // Act
            ResponseEntity<List<AlertaManutencao>> response = controller.listarAlertas(null);

            // Assert
            assertEquals(200, response.getStatusCodeValue());
            assertEquals(2, response.getBody().size());
            verify(alertaManutencaoRepository).findAllOrderByDataAlertaDesc();
        }

        @Test
        @DisplayName("Deve retornar alertas filtrados por status ENVIADA")
        void deveRetornarAlertasFiltradosPorStatusEnviada() {
            // Arrange
            AlertaManutencao alerta = criarAlerta(1L, 1L, TipoAlerta.QUILOMETRAGEM, StatusNotificacaoAlerta.ENVIADA);
            when(alertaManutencaoRepository.findByStatusNotificacao(StatusNotificacaoAlerta.ENVIADA))
                    .thenReturn(List.of(alerta));

            // Act
            ResponseEntity<List<AlertaManutencao>> response = controller.listarAlertas("ENVIADA");

            // Assert
            assertEquals(200, response.getStatusCodeValue());
            assertEquals(1, response.getBody().size());
            assertEquals(StatusNotificacaoAlerta.ENVIADA, response.getBody().get(0).getStatusNotificacao());
            verify(alertaManutencaoRepository).findByStatusNotificacao(StatusNotificacaoAlerta.ENVIADA);
        }

        @Test
        @DisplayName("Deve retornar alertas filtrados por status PENDENTE")
        void deveRetornarAlertasFiltradosPorStatusPendente() {
            // Arrange
            AlertaManutencao alerta = criarAlerta(1L, 1L, TipoAlerta.QUILOMETRAGEM, StatusNotificacaoAlerta.PENDENTE);
            when(alertaManutencaoRepository.findByStatusNotificacao(StatusNotificacaoAlerta.PENDENTE))
                    .thenReturn(List.of(alerta));

            // Act
            ResponseEntity<List<AlertaManutencao>> response = controller.listarAlertas("PENDENTE");

            // Assert
            assertEquals(200, response.getStatusCodeValue());
            assertEquals(1, response.getBody().size());
            assertEquals(StatusNotificacaoAlerta.PENDENTE, response.getBody().get(0).getStatusNotificacao());
        }

        @Test
        @DisplayName("Deve retornar alertas filtrados por status FALHA")
        void deveRetornarAlertasFiltradosPorStatusFalha() {
            // Arrange
            AlertaManutencao alerta = criarAlerta(1L, 1L, TipoAlerta.QUILOMETRAGEM, StatusNotificacaoAlerta.FALHA);
            when(alertaManutencaoRepository.findByStatusNotificacao(StatusNotificacaoAlerta.FALHA))
                    .thenReturn(List.of(alerta));

            // Act
            ResponseEntity<List<AlertaManutencao>> response = controller.listarAlertas("FALHA");

            // Assert
            assertEquals(200, response.getStatusCodeValue());
            assertEquals(1, response.getBody().size());
            assertEquals(StatusNotificacaoAlerta.FALHA, response.getBody().get(0).getStatusNotificacao());
        }

        @Test
        @DisplayName("Deve ignorar case do status (lowercase)")
        void deveIgnorarCaseDoStatus() {
            // Arrange
            AlertaManutencao alerta = criarAlerta(1L, 1L, TipoAlerta.QUILOMETRAGEM, StatusNotificacaoAlerta.ENVIADA);
            when(alertaManutencaoRepository.findByStatusNotificacao(StatusNotificacaoAlerta.ENVIADA))
                    .thenReturn(List.of(alerta));

            // Act
            ResponseEntity<List<AlertaManutencao>> response = controller.listarAlertas("enviada");

            // Assert
            assertEquals(200, response.getStatusCodeValue());
            assertEquals(1, response.getBody().size());
            verify(alertaManutencaoRepository).findByStatusNotificacao(StatusNotificacaoAlerta.ENVIADA);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando status não tem resultados")
        void deveRetornarListaVaziaQuandoStatusNaoTemResultados() {
            // Arrange
            when(alertaManutencaoRepository.findByStatusNotificacao(StatusNotificacaoAlerta.ENVIADA))
                    .thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<AlertaManutencao>> response = controller.listarAlertas("ENVIADA");

            // Assert
            assertEquals(200, response.getStatusCodeValue());
            assertTrue(response.getBody().isEmpty());
        }
    }
}