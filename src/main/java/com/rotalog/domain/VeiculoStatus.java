package com.rotalog.domain;

/**
 * Enum para os possíveis status de um veículo.
 * Substitui as strings hardcodeadas "ATIVO", "INATIVO", "MANUTENCAO".
 */
public enum VeiculoStatus {

    ATIVO("Veículo ativo e disponível"),
    INATIVO("Veículo desativado"),
    MANUTENCAO("Veículo em manutenção");

    private final String descricao;

    VeiculoStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
