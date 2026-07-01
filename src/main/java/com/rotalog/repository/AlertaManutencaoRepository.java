package com.rotalog.repository;

import com.rotalog.domain.AlertaManutencao;
import com.rotalog.domain.StatusNotificacaoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AlertaManutencaoRepository
 *
 * FIXME: Queries misturam JPQL e nativas seguindo padrão legado do projeto
 * FIXME: Sem paginação
 */
@Repository
public interface AlertaManutencaoRepository extends JpaRepository<AlertaManutencao, Long> {

    /**
     * Busca todos os alertas de um veículo, ordenados pelo mais recente.
     */
    List<AlertaManutencao> findByVeiculoIdOrderByIdDesc(Long veiculoId);

    /**
     * Busca alertas por status de notificação.
     */
    List<AlertaManutencao> findByStatusNotificacao(StatusNotificacaoAlerta status);

    /**
     * Verifica se existe alerta pendente ou com falha para o veículo.
     * Usado para evitar gerar alerta duplicado enquanto há um PENDENTE ativo.
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END " +
           "FROM AlertaManutencao a " +
           "WHERE a.veiculoId = :veiculoId " +
           "AND a.statusNotificacao <> com.rotalog.domain.StatusNotificacaoAlerta.ENVIADA")
    boolean existsByVeiculoIdAndStatusNotificacaoNotEnviada(@Param("veiculoId") Long veiculoId);

    // FIXME: Sem paginação - lista pode crescer indefinidamente
    @Query("SELECT a FROM AlertaManutencao a ORDER BY a.dataAlerta DESC")
    List<AlertaManutencao> findAllOrderByDataAlertaDesc();

    // FIXME: Deveria ter projeção para busca leve (DTO) em vez da entidade completa
    Optional<AlertaManutencao> findById(Long id);
}
