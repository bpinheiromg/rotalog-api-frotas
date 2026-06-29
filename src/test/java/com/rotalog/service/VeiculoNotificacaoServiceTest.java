package com.rotalog.service;

import com.rotalog.exception.NotificacaoFalhaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoNotificacaoServiceTest {

    @Mock
    private NotificacaoClient notificacaoClient;

    @InjectMocks
    private VeiculoNotificacaoService service;

    // =====================================================================
    // notificarNovoVeiculo
    // =====================================================================

    @Nested
    @DisplayName("notificarNovoVeiculo")
    class NotificarNovoVeiculo {

        @Test
        @DisplayName("Deve enviar notificação com sucesso")
        void deveEnviarComSucesso() {
            ReflectionTestUtils.setField(service, "emailGestor", "gestor@rotalog.com");

            service.notificarNovoVeiculo("ABC1234", "Fusca");

            verify(notificacaoClient).enviarNotificacao(
                    eq("NOVO_VEICULO"), eq("gestor@rotalog.com"), anyString());
        }

        @Test
        @DisplayName("Deve engolir exceção quando notificação falha")
        void deveEngolirExcecaoQuandoFalha() {
            ReflectionTestUtils.setField(service, "emailGestor", "gestor@rotalog.com");
            doThrow(new RuntimeException("Falha")).when(notificacaoClient)
                    .enviarNotificacao(anyString(), anyString(), anyString());

            assertDoesNotThrow(() -> service.notificarNovoVeiculo("ABC1234", "Fusca"));
        }
    }

    // =====================================================================
    // notificarManutencao
    // =====================================================================

    @Nested
    @DisplayName("notificarManutencao")
    class NotificarManutencao {

        @Test
        @DisplayName("Deve enviar alerta de manutenção")
        void deveEnviarAlerta() {
            ReflectionTestUtils.setField(service, "emailGestor", "gestor@rotalog.com");

            service.notificarManutencao("ABC1234", 55000L);

            ArgumentCaptor<String> mensagemCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificacaoClient).enviarNotificacao(
                    eq("ALERTA_MANUTENCAO"), eq("gestor@rotalog.com"), mensagemCaptor.capture());

            assertTrue(mensagemCaptor.getValue().contains("55000"));
            assertTrue(mensagemCaptor.getValue().contains("ABC1234"));
        }

        @Test
        @DisplayName("Deve engolir exceção quando notificação falha")
        void deveEngolirExcecaoQuandoFalha() {
            ReflectionTestUtils.setField(service, "emailGestor", "gestor@rotalog.com");
            doThrow(new RuntimeException("Falha")).when(notificacaoClient)
                    .enviarNotificacao(anyString(), anyString(), anyString());

            assertDoesNotThrow(() -> service.notificarManutencao("ABC1234", 55000L));
        }
    }

    // =====================================================================
    // notificarManutencaoAgendada
    // =====================================================================

    @Nested
    @DisplayName("notificarManutencaoAgendada")
    class NotificarManutencaoAgendada {

        @Test
        @DisplayName("Deve enviar notificação de agendamento")
        void deveEnviarNotificacaoAgendamento() {
            ReflectionTestUtils.setField(service, "emailGestor", "gestor@rotalog.com");

            service.notificarManutencaoAgendada("ABC1234", 20000L);

            ArgumentCaptor<String> mensagemCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificacaoClient).enviarNotificacao(
                    eq("MANUTENCAO_AGENDADA"), eq("gestor@rotalog.com"), mensagemCaptor.capture());

            assertTrue(mensagemCaptor.getValue().contains("ABC1234"));
            assertTrue(mensagemCaptor.getValue().contains("20000"));
        }

        @Test
        @DisplayName("Deve engolir exceção quando notificação falha")
        void deveEngolirExcecaoQuandoFalha() {
            ReflectionTestUtils.setField(service, "emailGestor", "gestor@rotalog.com");
            doThrow(new RuntimeException("Falha")).when(notificacaoClient)
                    .enviarNotificacao(anyString(), anyString(), anyString());

            assertDoesNotThrow(() -> service.notificarManutencaoAgendada("ABC1234", 20000L));
        }
    }

    // =====================================================================
    // notificarDesativacao
    // =====================================================================

    @Nested
    @DisplayName("notificarDesativacao")
    class NotificarDesativacao {

        @Test
        @DisplayName("Deve enviar notificação de desativação")
        void deveEnviarNotificacaoDesativacao() {
            ReflectionTestUtils.setField(service, "emailGestor", "gestor@rotalog.com");

            service.notificarDesativacao("ABC1234");

            ArgumentCaptor<String> mensagemCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificacaoClient).enviarNotificacao(
                    eq("VEICULO_DESATIVADO"), eq("gestor@rotalog.com"), mensagemCaptor.capture());

            assertTrue(mensagemCaptor.getValue().contains("ABC1234"));
            assertTrue(mensagemCaptor.getValue().contains("desativado"));
        }

        @Test
        @DisplayName("Deve engolir exceção quando notificação falha")
        void deveEngolirExcecaoQuandoFalha() {
            ReflectionTestUtils.setField(service, "emailGestor", "gestor@rotalog.com");
            doThrow(new RuntimeException("Falha")).when(notificacaoClient)
                    .enviarNotificacao(anyString(), anyString(), anyString());

            assertDoesNotThrow(() -> service.notificarDesativacao("ABC1234"));
        }
    }
}
