package com.rotalog.service;

import com.rotalog.config.VeiculoProperties;
import com.rotalog.domain.Veiculo;
import com.rotalog.exception.QuilometragemInvalidaException;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VeiculoQuilometragemServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private VeiculoProperties veiculoProperties;

    @Mock
    private VeiculoProperties.Quilometragem quilometragemProps;

    @InjectMocks
    private VeiculoQuilometragemService service;

    private Veiculo veiculoFixture;

    @BeforeEach
    void setUp() {
        when(veiculoProperties.getQuilometragem()).thenReturn(quilometragemProps);
        when(quilometragemProps.getLimiteManutencao()).thenReturn(50000L);

        veiculoFixture = new Veiculo();
        veiculoFixture.setId(1L);
        veiculoFixture.setPlaca("ABC1234");
        veiculoFixture.setModelo("Fusca");
        veiculoFixture.setQuilometragem(10000L);
        veiculoFixture.setDataAtualizacao(LocalDateTime.now());
    }

    // =====================================================================
    // atualizarQuilometragem
    // =====================================================================

    @Nested
    @DisplayName("atualizarQuilometragem")
    class AtualizarQuilometragem {

        @Test
        @DisplayName("Deve atualizar quilometragem com sucesso")
        void deveAtualizarComSucesso() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Veiculo resultado = service.atualizarQuilometragem(1L, 30000L);

            assertEquals(30000L, resultado.getQuilometragem());
            verify(veiculoRepository).save(any(Veiculo.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando quilometragem é negativa")
        void deveLancarExcecaoQuandoNegativa() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));

            QuilometragemInvalidaException ex = assertThrows(QuilometragemInvalidaException.class,
                    () -> service.atualizarQuilometragem(1L, -1L));

            assertTrue(ex.getMessage().contains("negativa"));
        }

        @Test
        @DisplayName("Deve atualizar mesmo com quilometragem menor que a atual")
        void deveAtualizarMesmoComQuilometragemMenor() {
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoFixture));
            when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Veiculo resultado = service.atualizarQuilometragem(1L, 5000L);

            assertEquals(5000L, resultado.getQuilometragem());
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(VeiculoNaoEncontradoException.class,
                    () -> service.atualizarQuilometragem(99L, 20000L));
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
        void deveRetornarTrueQuandoLimiteAtingido() {
            veiculoFixture.setQuilometragem(50000L);

            assertTrue(service.precisaDeManutencao(veiculoFixture));
        }

        @Test
        @DisplayName("Deve retornar true quando quilometragem > limite")
        void deveRetornarTrueQuandoAcimaDoLimite() {
            veiculoFixture.setQuilometragem(60000L);

            assertTrue(service.precisaDeManutencao(veiculoFixture));
        }

        @Test
        @DisplayName("Deve retornar false quando quilometragem < limite")
        void deveRetornarFalseQuandoAbaixoDoLimite() {
            veiculoFixture.setQuilometragem(49999L);

            assertFalse(service.precisaDeManutencao(veiculoFixture));
        }

        @Test
        @DisplayName("Deve retornar false quando quilometragem é null")
        void deveRetornarFalseQuandoNull() {
            veiculoFixture.setQuilometragem(null);

            assertFalse(service.precisaDeManutencao(veiculoFixture));
        }
    }

    // =====================================================================
    // isReducaoSuspeita
    // =====================================================================

    @Nested
    @DisplayName("isReducaoSuspeita")
    class IsReducaoSuspeita {

        @Test
        @DisplayName("Deve retornar true quando redução")
        void deveRetornarTrueQuandoReducao() {
            assertTrue(service.isReducaoSuspeita(10000L, 5000L));
        }

        @Test
        @DisplayName("Deve retornar false quando aumento")
        void deveRetornarFalseQuandoAumento() {
            assertFalse(service.isReducaoSuspeita(5000L, 10000L));
        }

        @Test
        @DisplayName("Deve retornar false quando igual")
        void deveRetornarFalseQuandoIgual() {
            assertFalse(service.isReducaoSuspeita(10000L, 10000L));
        }

        @Test
        @DisplayName("Deve retornar false quando atual é null")
        void deveRetornarFalseQuandoAtualNull() {
            assertFalse(service.isReducaoSuspeita(null, 5000L));
        }

        @Test
        @DisplayName("Deve retornar false quando nova é null")
        void deveRetornarFalseQuandoNovaNull() {
            assertFalse(service.isReducaoSuspeita(10000L, null));
        }
    }
}
