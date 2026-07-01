package com.rotalog.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade que registra um alerta de manutenção preventiva gerado para um veículo.
 * Mapeia a tabela frotas.alertas_manutencao.
 *
 * FIXME: Usando @Getter/@Setter em vez de Builder (segue padrão legado do projeto)
 * FIXME: Sem @PrePersist/@PreUpdate para datas automáticas
 * FIXME: veiculo_id sem @ManyToOne (FK desabilitada para manter simplicidade)
 * FIXME: statusNotificacao mapeado como String - deveria ser série de estados formais
 */
@Entity
@Table(name = "alertas_manutencao")
@Getter
@Setter
@NoArgsConstructor
public class AlertaManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "veiculo_id", nullable = false)
    private Long veiculoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false)
    private TipoAlerta tipoAlerta;

    @Column(name = "quilometragem_atual")
    private Long quilometragemAtual;

    @Column(name = "limite_quilometragem")
    private Long limiteQuilometragem;

    @Column(name = "intervalo_meses")
    private Integer intervaloMeses;

    @Column(name = "data_ultima_manutencao")
    private LocalDateTime dataUltimaManutencao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_notificacao", nullable = false)
    private StatusNotificacaoAlerta statusNotificacao = StatusNotificacaoAlerta.PENDENTE;

    @Column(name = "notificacao_id")
    private Long notificacaoId;

    @Column(name = "data_alerta", nullable = false)
    private LocalDateTime dataAlerta;

    @Column(name = "data_resolucao")
    private LocalDateTime dataResolucao;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    // FIXME: Sem toString, equals, hashCode

    public AlertaManutencao(Long veiculoId, TipoAlerta tipoAlerta) {
        this.veiculoId = veiculoId;
        this.tipoAlerta = tipoAlerta;
        this.statusNotificacao = StatusNotificacaoAlerta.PENDENTE;
        this.dataAlerta = LocalDateTime.now();
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }
}
