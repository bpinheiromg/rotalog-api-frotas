package com.rotalog.service;

import com.rotalog.config.VeiculoProperties;
import com.rotalog.domain.Veiculo;
import com.rotalog.exception.AnoFabricacaoInvalidoException;
import com.rotalog.exception.CampoObrigatorioException;
import com.rotalog.exception.ModeloInvalidoException;
import com.rotalog.exception.PlacaInvalidaException;
import com.rotalog.exception.QuilometragemInvalidaException;
import com.rotalog.exception.StatusInvalidoException;
import com.rotalog.exception.VeiculoDuplicadoException;
import com.rotalog.exception.VeiculoJaEmManutencaoException;
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
class VeiculoValidadorServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private VeiculoProperties veiculoProperties;

    @Mock
    private VeiculoProperties.Placa placaProps;

    @Mock
    private VeiculoProperties.AnoFabricacao anoFabricacaoProps;

    @InjectMocks
    private VeiculoValidadorService validadorService;

    @BeforeEach
    void setUp() {
        when(veiculoProperties.getPlaca()).thenReturn(placaProps);
        when(placaProps.getTamanho()).thenReturn(7);
        when(veiculoProperties.getAnoFabricacao()).thenReturn(anoFabricacaoProps);
        when(anoFabricacaoProps.getMinimo()).thenReturn(1900);
        when(anoFabricacaoProps.getMaximo()).thenReturn(2100);
    }

    // =====================================================================
    // validarRegistro
    // =====================================================================

    @Nested
    @DisplayName("validarRegistro")
    class ValidarRegistro {

        @Test
        @DisplayName("Deve validar com sucesso todos os campos")
        void deveValidarComSucesso() {
            when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> validadorService.validarRegistro("ABC1234", "Fusca", 2020));
        }

        @Test
        @DisplayName("Deve lançar exceção quando placa inválida")
        void deveLancarExcecaoQuandoPlacaInvalida() {
            assertThrows(PlacaInvalidaException.class,
                    () -> validadorService.validarRegistro("ABC", "Fusca", 2020));
        }

        @Test
        @DisplayName("Deve lançar exceção quando modelo vazio")
        void deveLancarExcecaoQuandoModeloVazio() {
            when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.empty());

            assertThrows(ModeloInvalidoException.class,
                    () -> validadorService.validarRegistro("ABC1234", "", 2020));
        }

        @Test
        @DisplayName("Deve lançar exceção quando ano fora do intervalo")
        void deveLancarExcecaoQuandoAnoForaDoIntervalo() {
            when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.empty());

            assertThrows(AnoFabricacaoInvalidoException.class,
                    () -> validadorService.validarRegistro("ABC1234", "Fusca", 1800));
        }
    }

    // =====================================================================
    // validarPlaca
    // =====================================================================

    @Nested
    @DisplayName("validarPlaca")
    class ValidarPlaca {

        @Test
        @DisplayName("Deve lançar exceção quando placa é null")
        void deveLancarExcecaoQuandoPlacaNull() {
            CampoObrigatorioException ex = assertThrows(CampoObrigatorioException.class,
                    () -> validadorService.validarPlaca(null));

            assertEquals("Placa é obrigatória", ex.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção quando placa é vazia")
        void deveLancarExcecaoQuandoPlacaVazia() {
            CampoObrigatorioException ex = assertThrows(CampoObrigatorioException.class,
                    () -> validadorService.validarPlaca(""));

            assertEquals("Placa é obrigatória", ex.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção quando placa tem tamanho incorreto")
        void deveLancarExcecaoQuandoPlacaTamanhoIncorreto() {
            PlacaInvalidaException ex = assertThrows(PlacaInvalidaException.class,
                    () -> validadorService.validarPlaca("ABC123"));

            assertTrue(ex.getMessage().contains("7"));
        }

        @Test
        @DisplayName("Deve aceitar placa válida")
        void deveAceitarPlacaValida() {
            assertDoesNotThrow(() -> validadorService.validarPlaca("ABC1234"));
        }
    }

    // =====================================================================
    // validarDuplicidadePlaca
    // =====================================================================

    @Nested
    @DisplayName("validarDuplicidadePlaca")
    class ValidarDuplicidadePlaca {

        @Test
        @DisplayName("Deve lançar exceção quando placa já existe")
        void deveLancarExcecaoQuandoPlacaJaExiste() {
            Veiculo existente = new Veiculo();
            existente.setPlaca("ABC1234");
            when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.of(existente));

            VeiculoDuplicadoException ex = assertThrows(VeiculoDuplicadoException.class,
                    () -> validadorService.validarDuplicidadePlaca("ABC1234"));

            assertTrue(ex.getMessage().contains("já existe"));
        }

        @Test
        @DisplayName("Deve aceitar placa não duplicada")
        void deveAceitarPlacaNaoDuplicada() {
            when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> validadorService.validarDuplicidadePlaca("ABC1234"));
        }
    }

    // =====================================================================
    // validarModelo
    // =====================================================================

    @Nested
    @DisplayName("validarModelo")
    class ValidarModelo {

        @Test
        @DisplayName("Deve lançar exceção quando modelo é null")
        void deveLancarExcecaoQuandoModeloNull() {
            ModeloInvalidoException ex = assertThrows(ModeloInvalidoException.class,
                    () -> validadorService.validarModelo(null));

            assertEquals("Modelo é obrigatório", ex.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção quando modelo é vazio")
        void deveLancarExcecaoQuandoModeloVazio() {
            ModeloInvalidoException ex = assertThrows(ModeloInvalidoException.class,
                    () -> validadorService.validarModelo(""));

            assertEquals("Modelo é obrigatório", ex.getMessage());
        }

        @Test
        @DisplayName("Deve aceitar modelo válido")
        void deveAceitarModeloValido() {
            assertDoesNotThrow(() -> validadorService.validarModelo("Fusca"));
        }
    }

    // =====================================================================
    // validarAnoFabricacao
    // =====================================================================

    @Nested
    @DisplayName("validarAnoFabricacao")
    class ValidarAnoFabricacao {

        @Test
        @DisplayName("Deve lançar exceção quando ano é null")
        void deveLancarExcecaoQuandoAnoNull() {
            AnoFabricacaoInvalidoException ex = assertThrows(AnoFabricacaoInvalidoException.class,
                    () -> validadorService.validarAnoFabricacao(null));

            assertTrue(ex.getMessage().contains("obrigatório"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando ano é menor que mínimo")
        void deveLancarExcecaoQuandoAnoMenorMinimo() {
            AnoFabricacaoInvalidoException ex = assertThrows(AnoFabricacaoInvalidoException.class,
                    () -> validadorService.validarAnoFabricacao(1899));

            assertTrue(ex.getMessage().contains("inválido"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando ano é maior que máximo")
        void deveLancarExcecaoQuandoAnoMaiorMaximo() {
            AnoFabricacaoInvalidoException ex = assertThrows(AnoFabricacaoInvalidoException.class,
                    () -> validadorService.validarAnoFabricacao(2101));

            assertTrue(ex.getMessage().contains("inválido"));
        }

        @Test
        @DisplayName("Deve aceitar ano válido")
        void deveAceitarAnoValido() {
            assertDoesNotThrow(() -> validadorService.validarAnoFabricacao(2020));
        }

        @Test
        @DisplayName("Deve aceitar ano no limite mínimo")
        void deveAceitarAnoNoLimiteMinimo() {
            assertDoesNotThrow(() -> validadorService.validarAnoFabricacao(1900));
        }

        @Test
        @DisplayName("Deve aceitar ano no limite máximo")
        void deveAceitarAnoNoLimiteMaximo() {
            assertDoesNotThrow(() -> validadorService.validarAnoFabricacao(2100));
        }
    }

    // =====================================================================
    // validarQuilometragem
    // =====================================================================

    @Nested
    @DisplayName("validarQuilometragem")
    class ValidarQuilometragem {

        @Test
        @DisplayName("Deve lançar exceção quando quilometragem é null")
        void deveLancarExcecaoQuandoNull() {
            QuilometragemInvalidaException ex = assertThrows(QuilometragemInvalidaException.class,
                    () -> validadorService.validarQuilometragem(null));

            assertTrue(ex.getMessage().contains("obrigatória"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando quilometragem é negativa")
        void deveLancarExcecaoQuandoNegativa() {
            QuilometragemInvalidaException ex = assertThrows(QuilometragemInvalidaException.class,
                    () -> validadorService.validarQuilometragem(-1L));

            assertTrue(ex.getMessage().contains("negativa"));
        }

        @Test
        @DisplayName("Deve aceitar quilometragem zero")
        void deveAceitarQuilometragemZero() {
            assertDoesNotThrow(() -> validadorService.validarQuilometragem(0L));
        }

        @Test
        @DisplayName("Deve aceitar quilometragem positiva")
        void deveAceitarQuilometragemPositiva() {
            assertDoesNotThrow(() -> validadorService.validarQuilometragem(10000L));
        }
    }

    // =====================================================================
    // validarReducaoQuilometragem
    // =====================================================================

    @Nested
    @DisplayName("validarReducaoQuilometragem")
    class ValidarReducaoQuilometragem {

        @Test
        @DisplayName("Deve logar aviso quando redução")
        void deveLogarAvisoQuandoReducao() {
            // Não lança exceção, apenas loga
            assertDoesNotThrow(() -> validadorService.validarReducaoQuilometragem(10000L, 5000L));
        }

        @Test
        @DisplayName("Não deve logar quando aumento")
        void naoDeveLogarQuandoAumento() {
            assertDoesNotThrow(() -> validadorService.validarReducaoQuilometragem(5000L, 10000L));
        }

        @Test
        @DisplayName("Não deve logar quando valores são iguais")
        void naoDeveLogarQuandoIguais() {
            assertDoesNotThrow(() -> validadorService.validarReducaoQuilometragem(10000L, 10000L));
        }
    }

    // =====================================================================
    // validarStatus
    // =====================================================================

    @Nested
    @DisplayName("validarStatus")
    class ValidarStatus {

        @Test
        @DisplayName("Deve aceitar ATIVO")
        void deveAceitarAtivo() {
            assertDoesNotThrow(() -> validadorService.validarStatus("ATIVO"));
        }

        @Test
        @DisplayName("Deve aceitar INATIVO")
        void deveAceitarInativo() {
            assertDoesNotThrow(() -> validadorService.validarStatus("INATIVO"));
        }

        @Test
        @DisplayName("Deve aceitar MANUTENCAO")
        void deveAceitarManutencao() {
            assertDoesNotThrow(() -> validadorService.validarStatus("MANUTENCAO"));
        }

        @Test
        @DisplayName("Deve lançar exceção para status inválido")
        void deveLancarExcecaoQuandoInvalido() {
            StatusInvalidoException ex = assertThrows(StatusInvalidoException.class,
                    () -> validadorService.validarStatus("INVALIDO"));

            assertTrue(ex.getMessage().contains("inválido"));
        }

        @Test
        @DisplayName("Deve lançar exceção para status null")
        void deveLancarExcecaoQuandoNull() {
            assertThrows(StatusInvalidoException.class,
                    () -> validadorService.validarStatus(null));
        }
    }

    // =====================================================================
    // validarDisponibilidadeParaManutencao
    // =====================================================================

    @Nested
    @DisplayName("validarDisponibilidadeParaManutencao")
    class ValidarDisponibilidadeParaManutencao {

        @Test
        @DisplayName("Deve lançar exceção quando veículo já está em manutenção")
        void deveLancarExcecaoQuandoJaEmManutencao() {
            Veiculo veiculo = new Veiculo();
            veiculo.setPlaca("ABC1234");
            veiculo.setStatus("MANUTENCAO");

            VeiculoJaEmManutencaoException ex = assertThrows(VeiculoJaEmManutencaoException.class,
                    () -> validadorService.validarDisponibilidadeParaManutencao(veiculo));

            assertTrue(ex.getMessage().contains("já está em manutenção"));
        }

        @Test
        @DisplayName("Deve aceitar veículo ativo")
        void deveAceitarVeiculoAtivo() {
            Veiculo veiculo = new Veiculo();
            veiculo.setPlaca("ABC1234");
            veiculo.setStatus("ATIVO");

            assertDoesNotThrow(() -> validadorService.validarDisponibilidadeParaManutencao(veiculo));
        }
    }
}
