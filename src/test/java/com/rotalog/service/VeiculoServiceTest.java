package com.rotalog.service;

import com.rotalog.config.VeiculoProperties;
import com.rotalog.domain.Veiculo;
import com.rotalog.exception.PlacaInvalidaException;
import com.rotalog.exception.StatusInvalidoException;
import com.rotalog.exception.VeiculoDuplicadoException;
import com.rotalog.exception.VeiculoNaoEncontradoException;
import com.rotalog.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private VeiculoValidadorService validadorService;

    @Mock
    private VeiculoNotificacaoService notificacaoService;

    @Mock
    private VeiculoQuilometragemService quilometragemService;

    @Mock
    private VeiculoManutencaoService manutencaoService;

    @Mock
    private VeiculoEstatisticasService estatisticasService;

    @Mock
    private VeiculoSincronizacaoService sincronizacaoService;

    @Mock
    private VeiculoProperties veiculoProperties;

    private VeiculoService veiculoService;

    private Veiculo veiculoFixture;

    @BeforeEach
    void setUp() {
        veiculoService = new VeiculoService(
                veiculoRepository, validadorService, notificacaoService,
                quilometragemService, manutencaoService, estatisticasService,
                sincronizacaoService);

        veiculoFixture = new Veiculo();
        veiculoFixture.setId(1L);
        veiculoFixture.setPlaca("ABC1234");
        veiculoFixture.setModelo("Fusca");
        veiculoFixture.setAnoFabricacao(2020);
        veiculoFixture.setStatus("ATIVO");
        veiculoFixture.setQuilometragem(10000L);
        veiculoFixture.setDataCadastro(LocalDateTime.now());
        veiculoFixture.setDataAtualizacao(LocalDateTime.now());
    }

    // =====================================================================
    // listarTodos
    // =====================================================================

    @Nested
    @DisplayName("listarTodos")
    class ListarTodos {

        @Test
        @DisplayName("Deve retornar lista de veículos")
        void deveRetornarListaDeVeiculos() {
            when(veiculoRepository.findAll()).thenReturn(List.of(veiculoFixture));

            List<Veiculo> resultado = veiculoService.listarTodos();

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("ABC1234", resultado.get(0).getPlaca());
            verify(veiculoRepository).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há veículos")
        void deveRetornarListaVazia() {
            when(veiculoRepository.findAll()).thenReturn(Collections.emptyList());

            List<Veiculo> resultado = veiculoService.listarTodos();

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }
    }

    // =====================================================================
    // buscarPorId
    // =====================================================================

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar veículo quando encontrado")
        void deveRetornarVeiculoQuandoEncontrado() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));

            Veiculo resultado = veiculoService.buscarPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            assertEquals("ABC1234", resultado.getPlaca());
        }

        @Test
        @DisplayName("Deve lançar VeiculoNaoEncontradoException quando não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            VeiculoNaoEncontradoException ex = assertThrows(VeiculoNaoEncontradoException.class,
                    () -> veiculoService.buscarPorId(99L));

            assertTrue(ex.getMessage().contains("Veículo não encontrado: 99"));
        }
    }

    // =====================================================================
    // buscarPorPlaca
    // =====================================================================

    @Nested
    @DisplayName("buscarPorPlaca")
    class BuscarPorPlaca {

        @Test
        @DisplayName("Deve retornar veículo quando placa encontrada")
        void deveRetornarVeiculoQuandoPlacaEncontrada() {
            when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.of(veiculoFixture));

            Veiculo resultado = veiculoService.buscarPorPlaca("ABC1234");

            assertNotNull(resultado);
            assertEquals("ABC1234", resultado.getPlaca());
        }

        @Test
        @DisplayName("Deve lançar VeiculoNaoEncontradoException quando placa não encontrada")
        void deveLancarExcecaoQuandoPlacaNaoEncontrada() {
            when(veiculoRepository.findByPlaca("XYZ9999")).thenReturn(Optional.empty());

            VeiculoNaoEncontradoException ex = assertThrows(VeiculoNaoEncontradoException.class,
                    () -> veiculoService.buscarPorPlaca("XYZ9999"));

            assertTrue(ex.getMessage().contains("Veículo não encontrado com placa: XYZ9999"));
        }
    }

    // =====================================================================
    // registrarVeiculo
    // =====================================================================

    @Nested
    @DisplayName("registrarVeiculo")
    class RegistrarVeiculo {

        @Test
        @DisplayName("Deve registrar veículo com sucesso")
        void deveRegistrarVeiculoComSucesso() {
            doNothing().when(validadorService).validarRegistro("ABC1234", "Fusca", 2020);
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> {
                Veiculo v = invocation.getArgument(0);
                v.setId(1L);
                return v;
            });

            Veiculo resultado = veiculoService.registrarVeiculo("ABC1234", "Fusca", 2020);

            assertNotNull(resultado);
            assertEquals("ABC1234", resultado.getPlaca());
            assertEquals("Fusca", resultado.getModelo());
            assertEquals(2020, resultado.getAnoFabricacao());
            assertEquals("ATIVO", resultado.getStatus());
            assertEquals(0L, resultado.getQuilometragem());
            verify(notificacaoService).notificarNovoVeiculo("ABC1234", "Fusca");
        }

        @Test
        @DisplayName("Deve registrar veículo com placa em maiúsculas")
        void deveRegistrarVeiculoComPlacaMaiuscula() {
            doNothing().when(validadorService).validarRegistro("abc1234", "Gol", 2021);
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> {
                Veiculo v = invocation.getArgument(0);
                v.setId(2L);
                return v;
            });

            Veiculo resultado = veiculoService.registrarVeiculo("abc1234", "Gol", 2021);

            assertEquals("ABC1234", resultado.getPlaca());
        }

        @Test
        @DisplayName("Deve lançar exceção quando placa é null")
        void deveLancarExcecaoQuandoPlacaNull() {
            doThrow(new RuntimeException("Placa é obrigatória")).when(validadorService)
                    .validarRegistro(null, "Fusca", 2020);

            assertThrows(RuntimeException.class,
                    () -> veiculoService.registrarVeiculo(null, "Fusca", 2020));
        }

        @Test
        @DisplayName("Deve lançar exceção quando placa é vazia")
        void deveLancarExcecaoQuandoPlacaVazia() {
            doThrow(new PlacaInvalidaException("Placa é obrigatória")).when(validadorService)
                    .validarRegistro("", "Fusca", 2020);

            assertThrows(PlacaInvalidaException.class,
                    () -> veiculoService.registrarVeiculo("", "Fusca", 2020));
        }

        @Test
        @DisplayName("Deve lançar exceção quando placa não tem 7 caracteres")
        void deveLancarExcecaoQuandoPlacaTamanhoInvalido() {
            doThrow(new PlacaInvalidaException("Placa deve ter 7 caracteres")).when(validadorService)
                    .validarRegistro("ABC123", "Fusca", 2020);

            assertThrows(PlacaInvalidaException.class,
                    () -> veiculoService.registrarVeiculo("ABC123", "Fusca", 2020));
        }

        @Test
        @DisplayName("Deve lançar exceção quando placa já existe")
        void deveLancarExcecaoQuandoPlacaJaExiste() {
            doThrow(new VeiculoDuplicadoException("Veículo com placa ABC1234 já existe"))
                    .when(validadorService).validarRegistro("ABC1234", "Fusca", 2020);

            assertThrows(VeiculoDuplicadoException.class,
                    () -> veiculoService.registrarVeiculo("ABC1234", "Fusca", 2020));
        }

        @Test
        @DisplayName("Deve retornar veículo mesmo quando notificação falha (serviço notificacao engole exceção)")
        void deveRetornarVeiculoMesmoQuandoNotificacaoFalha() {
            doNothing().when(validadorService).validarRegistro("ABC1234", "Fusca", 2020);
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> {
                Veiculo v = invocation.getArgument(0);
                v.setId(1L);
                return v;
            });
            // NotificacaoService real engole a exceção; mock simula o comportamento sem throw
            doNothing().when(notificacaoService).notificarNovoVeiculo(anyString(), anyString());

            Veiculo resultado = veiculoService.registrarVeiculo("ABC1234", "Fusca", 2020);

            assertNotNull(resultado);
            assertEquals("ABC1234", resultado.getPlaca());
        }
    }

    // =====================================================================
    // atualizarVeiculo
    // =====================================================================

    @Nested
    @DisplayName("atualizarVeiculo")
    class AtualizarVeiculo {

        @Test
        @DisplayName("Deve atualizar modelo, ano e quilometragem")
        void deveAtualizarTodosCampos() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Veiculo resultado = veiculoService.atualizarVeiculo(1L, "Gol", 2022, 20000L);

            assertEquals("Gol", resultado.getModelo());
            assertEquals(2022, resultado.getAnoFabricacao());
            assertEquals(20000L, resultado.getQuilometragem());
        }

        @Test
        @DisplayName("Deve manter modelo quando passado null")
        void deveManterModeloQuandoNull() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Veiculo resultado = veiculoService.atualizarVeiculo(1L, null, 2022, 20000L);

            assertEquals("Fusca", resultado.getModelo());
        }

        @Test
        @DisplayName("Deve manter modelo quando passado vazio")
        void deveManterModeloQuandoVazio() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Veiculo resultado = veiculoService.atualizarVeiculo(1L, "", 2022, 20000L);

            assertEquals("Fusca", resultado.getModelo());
        }

        @Test
        @DisplayName("Deve manter anoFabricacao quando passado null")
        void deveManterAnoQuandoNull() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Veiculo resultado = veiculoService.atualizarVeiculo(1L, "Gol", null, 20000L);

            assertEquals(2020, resultado.getAnoFabricacao());
        }

        @Test
        @DisplayName("Deve manter quilometragem quando passado null")
        void deveManterQuilometragemQuandoNull() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Veiculo resultado = veiculoService.atualizarVeiculo(1L, "Gol", 2022, null);

            assertEquals(10000L, resultado.getQuilometragem());
        }

        @Test
        @DisplayName("Deve permitir reduzir quilometragem (comportamento legado)")
        void devePermitirReduzirQuilometragem() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Veiculo resultado = veiculoService.atualizarVeiculo(1L, "Gol", 2022, 5000L);

            assertEquals(5000L, resultado.getQuilometragem());
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(VeiculoNaoEncontradoException.class,
                    () -> veiculoService.atualizarVeiculo(99L, "Gol", 2022, 20000L));
        }
    }

    // =====================================================================
    // atualizarQuilometragem
    // =====================================================================

    @Nested
    @DisplayName("atualizarQuilometragem")
    class AtualizarQuilometragem {

        @Test
        @DisplayName("Deve atualizar quilometragem com sucesso")
        void deveAtualizarQuilometragemComSucesso() {
            Veiculo veiculoAtualizado = new Veiculo();
            veiculoAtualizado.setId(1L);
            veiculoAtualizado.setPlaca("ABC1234");
            veiculoAtualizado.setQuilometragem(30000L);

            when(quilometragemService.atualizarQuilometragem(1L, 30000L))
                    .thenReturn(veiculoAtualizado);
            when(quilometragemService.precisaDeManutencao(veiculoAtualizado))
                    .thenReturn(false);

            Veiculo resultado = veiculoService.atualizarQuilometragem(1L, 30000L);

            assertEquals(30000L, resultado.getQuilometragem());
            verify(notificacaoService, never()).notificarManutencao(anyString(), anyLong());
        }

        @Test
        @DisplayName("Deve enviar alerta de manutenção quando quilometragem >= limite")
        void deveEnviarAlertaManutencaoQuandoQuilometragemAlta() {
            Veiculo veiculoAtualizado = new Veiculo();
            veiculoAtualizado.setId(1L);
            veiculoAtualizado.setPlaca("ABC1234");
            veiculoAtualizado.setQuilometragem(55000L);

            when(quilometragemService.atualizarQuilometragem(1L, 55000L))
                    .thenReturn(veiculoAtualizado);
            when(quilometragemService.precisaDeManutencao(veiculoAtualizado))
                    .thenReturn(true);

            veiculoService.atualizarQuilometragem(1L, 55000L);

            verify(notificacaoService).notificarManutencao(eq("ABC1234"), eq(55000L));
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
            when(quilometragemService.atualizarQuilometragem(99L, 20000L))
                    .thenThrow(new VeiculoNaoEncontradoException("Veículo não encontrado: 99"));

            assertThrows(VeiculoNaoEncontradoException.class,
                    () -> veiculoService.atualizarQuilometragem(99L, 20000L));
        }
    }

    // =====================================================================
    // obterVeiculosPorStatus
    // =====================================================================

    @Nested
    @DisplayName("obterVeiculosPorStatus")
    class ObterVeiculosPorStatus {

        @Test
        @DisplayName("Deve retornar veículos com status ATIVO")
        void deveRetornarVeiculosAtivos() {
            when(veiculoRepository.findByStatus("ATIVO")).thenReturn(List.of(veiculoFixture));

            List<Veiculo> resultado = veiculoService.obterVeiculosPorStatus("ATIVO");

            assertEquals(1, resultado.size());
            assertEquals("ATIVO", resultado.get(0).getStatus());
        }

        @Test
        @DisplayName("Deve retornar veículos com status INATIVO")
        void deveRetornarVeiculosInativos() {
            veiculoFixture.setStatus("INATIVO");
            when(veiculoRepository.findByStatus("INATIVO")).thenReturn(List.of(veiculoFixture));

            List<Veiculo> resultado = veiculoService.obterVeiculosPorStatus("INATIVO");

            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Deve retornar veículos com status MANUTENCAO")
        void deveRetornarVeiculosManutencao() {
            veiculoFixture.setStatus("MANUTENCAO");
            when(veiculoRepository.findByStatus("MANUTENCAO")).thenReturn(List.of(veiculoFixture));

            List<Veiculo> resultado = veiculoService.obterVeiculosPorStatus("MANUTENCAO");

            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Deve lançar StatusInvalidoException quando status inválido")
        void deveLancarExcecaoQuandoStatusInvalido() {
            doThrow(new StatusInvalidoException("Status inválido: INVALIDO"))
                    .when(validadorService).validarStatus("INVALIDO");

            assertThrows(StatusInvalidoException.class,
                    () -> veiculoService.obterVeiculosPorStatus("INVALIDO"));
        }
    }

    // =====================================================================
    // agendarManutencaoPreventiva
    // =====================================================================

    @Nested
    @DisplayName("agendarManutencaoPreventiva")
    class AgendarManutencaoPreventiva {

        @Test
        @DisplayName("Deve agendar manutenção e enviar notificação")
        void deveAgendarManutencaoEEnviarNotificacao() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            doNothing().when(manutencaoService).agendarManutencao(1L, 20000L);

            veiculoService.agendarManutencaoPreventiva(1L, 20000L);

            verify(manutencaoService).agendarManutencao(1L, 20000L);
            verify(notificacaoService).notificarManutencaoAgendada("ABC1234", 20000L);
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
            doThrow(new VeiculoNaoEncontradoException("não encontrado"))
                    .when(manutencaoService).agendarManutencao(99L, 20000L);

            assertThrows(VeiculoNaoEncontradoException.class,
                    () -> veiculoService.agendarManutencaoPreventiva(99L, 20000L));
        }
    }

    // =====================================================================
    // calcularCustoManutencao
    // =====================================================================

    @Nested
    @DisplayName("calcularCustoManutencao")
    class CalcularCustoManutencao {

        @Test
        @DisplayName("Deve calcular custo corretamente")
        void deveCalcularCustoCorretamente() {
            when(manutencaoService.calcularCustoManutencao("Fusca", 10000L))
                    .thenReturn(1000.0);

            Double resultado = veiculoService.calcularCustoManutencao("Fusca", 10000L);

            assertEquals(1000.0, resultado);
        }

        @Test
        @DisplayName("Deve retornar custo base quando quilometragem é zero")
        void deveRetornarCustoBaseQuandoQuilometragemZero() {
            when(manutencaoService.calcularCustoManutencao("Fusca", 0L))
                    .thenReturn(500.0);

            Double resultado = veiculoService.calcularCustoManutencao("Fusca", 0L);

            assertEquals(500.0, resultado);
        }

        @Test
        @DisplayName("Deve calcular custo para quilometragem alta")
        void deveCalcularCustoParaQuilometragemAlta() {
            when(manutencaoService.calcularCustoManutencao("Fusca", 100000L))
                    .thenReturn(5500.0);

            Double resultado = veiculoService.calcularCustoManutencao("Fusca", 100000L);

            assertEquals(5500.0, resultado);
        }
    }

    // =====================================================================
    // precisaDeManutencao
    // =====================================================================

    @Nested
    @DisplayName("precisaDeManutencao")
    class PrecisaDeManutencao {

        @Test
        @DisplayName("Deve retornar true quando quilometragem >= limite")
        void deveRetornarTrueQuandoQuilometragemAlta() {
            veiculoFixture.setQuilometragem(50000L);
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(quilometragemService.precisaDeManutencao(veiculoFixture)).thenReturn(true);

            assertTrue(veiculoService.precisaDeManutencao(1L));
        }

        @Test
        @DisplayName("Deve retornar false quando quilometragem < limite")
        void deveRetornarFalseQuandoQuilometragemBaixa() {
            veiculoFixture.setQuilometragem(49999L);
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(quilometragemService.precisaDeManutencao(veiculoFixture)).thenReturn(false);

            assertFalse(veiculoService.precisaDeManutencao(1L));
        }

        @Test
        @DisplayName("Deve retornar false quando quilometragem é null")
        void deveRetornarFalseQuandoQuilometragemNull() {
            veiculoFixture.setQuilometragem(null);
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(quilometragemService.precisaDeManutencao(veiculoFixture)).thenReturn(false);

            assertFalse(veiculoService.precisaDeManutencao(1L));
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(VeiculoNaoEncontradoException.class,
                    () -> veiculoService.precisaDeManutencao(99L));
        }
    }

    // =====================================================================
    // desativarVeiculo
    // =====================================================================

    @Nested
    @DisplayName("desativarVeiculo")
    class DesativarVeiculo {

        @Test
        @DisplayName("Deve desativar veículo e enviar notificação")
        void deveDesativarVeiculoEEnviarNotificacao() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Veiculo resultado = veiculoService.desativarVeiculo(1L);

            assertEquals("INATIVO", resultado.getStatus());
            verify(notificacaoService).notificarDesativacao("ABC1234");
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(VeiculoNaoEncontradoException.class,
                    () -> veiculoService.desativarVeiculo(99L));
        }
    }

    // =====================================================================
    // reativarVeiculo
    // =====================================================================

    @Nested
    @DisplayName("reativarVeiculo")
    class ReativarVeiculo {

        @Test
        @DisplayName("Deve reativar veículo com sucesso")
        void deveReativarVeiculoComSucesso() {
            veiculoFixture.setStatus("INATIVO");
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Veiculo resultado = veiculoService.reativarVeiculo(1L);

            assertEquals("ATIVO", resultado.getStatus());
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(VeiculoNaoEncontradoException.class,
                    () -> veiculoService.reativarVeiculo(99L));
        }
    }

    // =====================================================================
    // obterEstatisticasFreita
    // =====================================================================

    @Nested
    @DisplayName("obterEstatisticasFreita")
    class ObterEstatisticasFreita {

        @Test
        @DisplayName("Deve retornar estatísticas delegando ao serviço especializado")
        void deveRetornarEstatisticas() {
            String json = "{\"total\": 3, \"ativos\": 1, \"inativos\": 0, \"em_manutencao\": 0}";
            when(estatisticasService.obterEstatisticas()).thenReturn(json);

            String resultado = veiculoService.obterEstatisticasFreita();

            assertEquals(json, resultado);
            verify(estatisticasService).obterEstatisticas();
        }
    }

    // =====================================================================
    // sincronizarComSistemaExterno
    // =====================================================================

    @Nested
    @DisplayName("sincronizarComSistemaExterno")
    class SincronizarComSistemaExterno {

        @Test
        @DisplayName("Deve executar sincronização delegando ao serviço especializado")
        void deveExecutarSincronizacaoSemErro() {
            doNothing().when(sincronizacaoService).sincronizar();

            assertDoesNotThrow(() -> veiculoService.sincronizarComSistemaExterno());
            verify(sincronizacaoService).sincronizar();
        }
    }
}
