package com.rotalog.service;

import com.rotalog.domain.Veiculo;
import com.rotalog.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoEstatisticasServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private VeiculoEstatisticasService service;

    private Veiculo veiculoFixture;

    @BeforeEach
    void setUp() {
        veiculoFixture = new Veiculo();
        veiculoFixture.setId(1L);
        veiculoFixture.setPlaca("ABC1234");
        veiculoFixture.setStatus("ATIVO");
    }

    // =====================================================================
    // obterEstatisticas
    // =====================================================================

    @Nested
    @DisplayName("obterEstatisticas")
    class ObterEstatisticas {

        @Test
        @DisplayName("Deve retornar estatísticas com dados")
        void deveRetornarEstatisticasComDados() {
            when(veiculoRepository.count()).thenReturn(3L);
            when(veiculoRepository.findByStatus("ATIVO")).thenReturn(List.of(veiculoFixture));
            when(veiculoRepository.findByStatus("INATIVO")).thenReturn(Collections.emptyList());
            when(veiculoRepository.findByStatus("MANUTENCAO")).thenReturn(Collections.emptyList());

            String resultado = service.obterEstatisticas();

            assertTrue(resultado.contains("\"total\": 3"));
            assertTrue(resultado.contains("\"ativos\": 1"));
            assertTrue(resultado.contains("\"inativos\": 0"));
            assertTrue(resultado.contains("\"em_manutencao\": 0"));
        }

        @Test
        @DisplayName("Deve retornar estatísticas zeradas quando sem veículos")
        void deveRetornarEstatisticasZeradas() {
            when(veiculoRepository.count()).thenReturn(0L);
            when(veiculoRepository.findByStatus("ATIVO")).thenReturn(Collections.emptyList());
            when(veiculoRepository.findByStatus("INATIVO")).thenReturn(Collections.emptyList());
            when(veiculoRepository.findByStatus("MANUTENCAO")).thenReturn(Collections.emptyList());

            String resultado = service.obterEstatisticas();

            assertTrue(resultado.contains("\"total\": 0"));
            assertTrue(resultado.contains("\"ativos\": 0"));
        }

        @Test
        @DisplayName("Deve retornar estatísticas com todos os status preenchidos")
        void deveRetornarEstatisticasComTodosStatus() {
            Veiculo inativo = new Veiculo();
            inativo.setStatus("INATIVO");
            Veiculo manutencao = new Veiculo();
            manutencao.setStatus("MANUTENCAO");

            when(veiculoRepository.count()).thenReturn(5L);
            when(veiculoRepository.findByStatus("ATIVO")).thenReturn(List.of(veiculoFixture));
            when(veiculoRepository.findByStatus("INATIVO")).thenReturn(List.of(inativo));
            when(veiculoRepository.findByStatus("MANUTENCAO")).thenReturn(List.of(manutencao, manutencao));

            String resultado = service.obterEstatisticas();

            assertTrue(resultado.contains("\"total\": 5"));
            assertTrue(resultado.contains("\"ativos\": 1"));
            assertTrue(resultado.contains("\"inativos\": 1"));
            assertTrue(resultado.contains("\"em_manutencao\": 2"));
        }
    }

    // =====================================================================
    // obterEstatisticasFreita (compatibilidade)
    // =====================================================================

    @Nested
    @DisplayName("obterEstatisticasFreita")
    class ObterEstatisticasFreita {

        @Test
        @DisplayName("Deve delegar para obterEstatisticas")
        void deveDelegarParaObterEstatisticas() {
            when(veiculoRepository.count()).thenReturn(0L);
            when(veiculoRepository.findByStatus("ATIVO")).thenReturn(Collections.emptyList());
            when(veiculoRepository.findByStatus("INATIVO")).thenReturn(Collections.emptyList());
            when(veiculoRepository.findByStatus("MANUTENCAO")).thenReturn(Collections.emptyList());

            String resultado = service.obterEstatisticasFreita();

            assertNotNull(resultado);
            assertTrue(resultado.contains("\"total\": 0"));
        }
    }

    // =====================================================================
    // obterPorStatus
    // =====================================================================

    @Nested
    @DisplayName("obterPorStatus")
    class ObterPorStatus {

        @Test
        @DisplayName("Deve retornar lista de veículos por status")
        void deveRetornarVeiculosPorStatus() {
            when(veiculoRepository.findByStatus("ATIVO")).thenReturn(List.of(veiculoFixture));

            List<Veiculo> resultado = service.obterPorStatus("ATIVO");

            assertEquals(1, resultado.size());
            assertEquals("ATIVO", resultado.get(0).getStatus());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando sem veículos")
        void deveRetornarListaVazia() {
            when(veiculoRepository.findByStatus("INATIVO")).thenReturn(Collections.emptyList());

            List<Veiculo> resultado = service.obterPorStatus("INATIVO");

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }
    }
}
