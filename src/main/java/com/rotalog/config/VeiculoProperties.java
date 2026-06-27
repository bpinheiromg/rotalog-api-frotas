package com.rotalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propriedades de configuração do domínio de veículos.
 * Centraliza valores que antes estavam hardcodeados no VeiculoService.
 */
@Configuration
@ConfigurationProperties(prefix = "veiculo")
public class VeiculoProperties {

    private Quilometragem quilometragem = new Quilometragem();
    private Manutencao manutencao = new Manutencao();
    private Placa placa = new Placa();
    private AnoFabricacao anoFabricacao = new AnoFabricacao();

    public Quilometragem getQuilometragem() {
        return quilometragem;
    }

    public Manutencao getManutencao() {
        return manutencao;
    }

    public Placa getPlaca() {
        return placa;
    }

    public AnoFabricacao getAnoFabricacao() {
        return anoFabricacao;
    }

    public static class Quilometragem {
        private long limiteManutencao = 50000L;
        private long intervalo = 10000L;

        public long getLimiteManutencao() {
            return limiteManutencao;
        }

        public void setLimiteManutencao(long limiteManutencao) {
            this.limiteManutencao = limiteManutencao;
        }

        public long getIntervalo() {
            return intervalo;
        }

        public void setIntervalo(long intervalo) {
            this.intervalo = intervalo;
        }
    }

    public static class Manutencao {
        private int intervaloMeses = 3;
        private double custoBase = 500.0;
        private double custoPorKm = 0.05;

        public int getIntervaloMeses() {
            return intervaloMeses;
        }

        public void setIntervaloMeses(int intervaloMeses) {
            this.intervaloMeses = intervaloMeses;
        }

        public double getCustoBase() {
            return custoBase;
        }

        public void setCustoBase(double custoBase) {
            this.custoBase = custoBase;
        }

        public double getCustoPorKm() {
            return custoPorKm;
        }

        public void setCustoPorKm(double custoPorKm) {
            this.custoPorKm = custoPorKm;
        }
    }

    public static class Placa {
        private int tamanho = 7;

        public int getTamanho() {
            return tamanho;
        }

        public void setTamanho(int tamanho) {
            this.tamanho = tamanho;
        }
    }

    public static class AnoFabricacao {
        private int minimo = 1900;
        private int maximo = 2100;

        public int getMinimo() {
            return minimo;
        }

        public void setMinimo(int minimo) {
            this.minimo = minimo;
        }

        public int getMaximo() {
            return maximo;
        }

        public void setMaximo(int maximo) {
            this.maximo = maximo;
        }
    }
}
