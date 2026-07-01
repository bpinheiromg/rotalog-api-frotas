package com.rotalog.repository;

import com.rotalog.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * VeiculoRepository
 * 
 * FIXME: Queries nativas misturadas com derived queries
 * FIXME: Sem paginação
 * FIXME: Sem specification pattern
 */
@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Optional<Veiculo> findByPlaca(String placa);

    List<Veiculo> findByStatus(String status);

    // FIXME: Query nativa quando poderia ser derived query
    @Query(value = "SELECT * FROM veiculos WHERE quilometragem > :km", nativeQuery = true)
    List<Veiculo> findVeiculosComQuilometragemAcimaDe(@Param("km") Long quilometragem);

    // FIXME: Sem paginação - pode retornar milhares de registros
    List<Veiculo> findByModeloContainingIgnoreCase(String modelo);

    @Query("SELECT v FROM Veiculo v WHERE v.status = 'ATIVO' AND v.quilometragem > :limite")
    List<Veiculo> findVeiculosAtivosComQuilometragemAlta(@Param("limite") Long limite);

    /**
     * Veículos elegantes por km: status ATIVO com quilometragem >= limite.
     */
    @Query("SELECT v FROM Veiculo v WHERE v.status = 'ATIVO' AND v.quilometragem >= :limite")
    List<Veiculo> findElegiveisPorQuilometragem(@Param("limite") Long limite);

    /**
     * Veículos ATIVO cuja última manutencao (quando existe) ocorreu ha mais de X meses,
     * ou que jamais possuem manutencao registrada e, portanto,
     * precisam de preventiva por tempo em operacao.
     *
     * Nota: usa data_manutencao como referencia; veiculos sem manutencao sao elegiveis.
     * FIXME: considera apenas data_manutencao; nao considera data de aquisicao do veiculo.
     */
    @Query(value = "SELECT v.* FROM veiculos v WHERE v.status = 'ATIVO' " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM manutencoes m " +
            "    WHERE m.veiculo_id = v.id " +
            "    AND m.data_manutencao > (CURRENT_TIMESTAMP - (:meses * INTERVAL '1 month')) " +
            ")", nativeQuery = true)
    List<Veiculo> findElegiveisPorTempoSemManutencao(@Param("meses") int meses);

    // TODO: Adicionar query para veículos por ano de fabricação
    // TODO: Adicionar paginação
}
