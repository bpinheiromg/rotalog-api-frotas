package com.rotalog.domain;

/**
 * Enum para os tipos de alerta de manutenção preventiva.
 * Substitui as strings hardcodeadas ao longo do fluxo de verificação.
 */
public enum TipoAlerta {

    QUILOMETRAGEM("Alerta: veículo atingiu limite de quilometragem"),
    TEMPO("Alerta: veículo atingiu tempo máximo sem manutenção");

    private final String descricao;

    TipoAlerta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
