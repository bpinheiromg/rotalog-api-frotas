package com.rotalog.service;

import com.rotalog.domain.Veiculo;
import com.rotalog.exception.VeiculoNaoEncontradoException;
import com.rotalog.repository.VeiculoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoSincronizacaoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private VeiculoSincronizacaoService service;

    // =====================================================================
    // sincronizar
    // =====================================================================

    @Nested
    @DisplayName("sincronizar")
    class Sincronizar {

        @Test
        @DisplayName("Deve executar sincronização sem erro")
        void deveExecutarSemErro() {
            assertDoesNotThrow(() -> service.sincronizar());
        }
    }

    // =====================================================================
    // sincronizarVeiculo
    // =====================================================================

    @Nested
    @DisplayName("sincronizarVeiculo")
    class SincronizarVeiculo {

        @Test
        @DisplayName("Deve sincronizar veículo existente")
        void deveSincronizarVeiculoExistente() {
            Veiculo veiculo = new Veiculo();
            veiculo.setId(1L);
            veiculo.setPlaca("ABC1234");
            when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));

            assertDoesNotThrow(() -> service.sincronizarVeiculo(1L));
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            VeiculoNaoEncontradoException ex = assertThrows(VeiculoNaoEncontradoException.class,
                    () -> service.sincronizarVeiculo(99L));

            assertTrue(ex.getMessage().contains("99"));
        }
    }
}
