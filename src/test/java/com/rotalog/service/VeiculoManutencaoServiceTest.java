package com.rotalog.service;

import com.rotalog.config.VeiculoProperties;
import com.rotalog.domain.Veiculo;
import com.rotalog.exception.VeiculoNaoEncontradoException;
import com.rotalog.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VeiculoManutencaoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private VeiculoProperties veiculoProperties;

    @Mock
    private VeiculoProperties.Manutencao manutencaoProps;

    @Mock
    private VeiculoProperties.Quilometragem quilometragemProps;

    @InjectMocks
    private VeiculoManutencaoService service;

    private Veiculo veiculoFixture;

    @BeforeEach
    void setUp() {
        when(veiculoProperties.getManutencao()).thenReturn(manutencaoProps);
        when(manutencaoProps.getCustoBase()).thenReturn(500.0);
        when(manutencaoProps.getCustoPorKm()).thenReturn(0.05);
        when(manutencaoProps.getIntervaloMeses()).thenReturn(3);
        when(veiculoProperties.getQuilometragem()).thenReturn(quilometragemProps);
        when(quilometragemProps.getIntervalo()).thenReturn(10000L);
        when(quilometragemProps.getLimiteManutencao()).thenReturn(50000L);

        veiculoFixture = new Veiculo();
        veiculoFixture.setId(1L);
        veiculoFixture.setPlaca("ABC1234");
        veiculoFixture.setModelo("Fusca");
        veiculoFixture.setQuilometragem(10000L);
    }

    // =====================================================================
    // agendarManutencao
    // =====================================================================

    @Nested
    @DisplayName("agendarManutencao")
    class AgendarManutencao {

        @Test
        @DisplayName("Deve agendar manutenção com sucesso")
        void deveAgendarComSucesso() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));

            assertDoesNotThrow(() -> service.agendarManutencao(1L, 20000L));
            verify(veiculoRepository).findById(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(VeiculoNaoEncontradoException.class,
                    () -> service.agendarManutencao(99L, 20000L));
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
        void deveCalcularCorretamente() {
            // custoBase(500) + 10000 * 0.05 = 500 + 500 = 1000
            Double resultado = service.calcularCustoManutencao("Fusca", 10000L);

            assertEquals(1000.0, resultado);
        }

        @Test
        @DisplayName("Deve retornar custo base quando quilometragem é zero")
        void deveRetornarCustoBaseQuandoZero() {
            Double resultado = service.calcularCustoManutencao("Fusca", 0L);

            assertEquals(500.0, resultado);
        }

        @Test
        @DisplayName("Deve calcular custo para quilometragem alta")
        void deveCalcularParaQuilometragemAlta() {
            // 500 + 100000 * 0.05 = 500 + 5000 = 5500
            Double resultado = service.calcularCustoManutencao("Fusca", 100000L);

            assertEquals(5500.0, resultado);
        }
    }

    // =====================================================================
    // verificarNecessidade
    // =====================================================================

    @Nested
    @DisplayName("verificarNecessidade")
    class VerificarNecessidade {

        @Test
        @DisplayName("Deve retornar true quando quilometragem >= limite")
        void deveRetornarTrueQuandoLimiteAtingido() {
            veiculoFixture.setQuilometragem(50000L);

            assertTrue(service.verificarNecessidade(veiculoFixture));
        }

        @Test
        @DisplayName("Deve retornar false quando quilometragem < limite")
        void deveRetornarFalseQuandoAbaixoDoLimite() {
            veiculoFixture.setQuilometragem(49999L);

            assertFalse(service.verificarNecessidade(veiculoFixture));
        }

        @Test
        @DisplayName("Deve retornar false quando quilometragem é null")
        void deveRetornarFalseQuandoNull() {
            veiculoFixture.setQuilometragem(null);

            assertFalse(service.verificarNecessidade(veiculoFixture));
        }
    }

    // =====================================================================
    // getIntervaloQuilometragem
    // =====================================================================

    @Nested
    @DisplayName("getIntervaloQuilometragem")
    class GetIntervaloQuilometragem {

        @Test
        @DisplayName("Deve retornar intervalo configurado")
        void deveRetornarIntervalo() {
            assertEquals(10000L, service.getIntervaloQuilometragem());
        }
    }

    // =====================================================================
    // getIntervaloMeses
    // =====================================================================

    @Nested
    @DisplayName("getIntervaloMeses")
    class GetIntervaloMeses {

        @Test
        @DisplayName("Deve retornar intervalo de meses configurado")
        void deveRetornarIntervaloMeses() {
            assertEquals(3, service.getIntervaloMeses());
        }
    }
}
