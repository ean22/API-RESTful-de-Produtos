package br.edu.ial.produtosapi.config;

public class BaldeRequisicoes {

    private final long capacidadeMaxima;
    private final double taxaPorSegundo;
    private double tokensDisponiveis;
    private long ultimoReabastecimentoNanos;

    public BaldeRequisicoes(long requisicoesPorMinuto) {
        this.capacidadeMaxima = requisicoesPorMinuto;
        this.taxaPorSegundo = (double) requisicoesPorMinuto / 60.0;
        this.tokensDisponiveis = requisicoesPorMinuto;
        this.ultimoReabastecimentoNanos = System.nanoTime();
    }

    public synchronized boolean tentarConsumir() {
        reabastecer();
        if (tokensDisponiveis >= 1.0) {
            tokensDisponiveis -= 1.0;
            return true;
        }
        return false;
    }

    private void reabastecer() {
        long agoraNanos = System.nanoTime();
        double segundosPassados = (agoraNanos - ultimoReabastecimentoNanos) / 1_000_000_000.0;
        
        if (segundosPassados > 0) {
            double tokensAdicionar = segundosPassados * taxaPorSegundo;
            tokensDisponiveis = Math.min(capacidadeMaxima, tokensDisponiveis + tokensAdicionar);
            ultimoReabastecimentoNanos = agoraNanos;
        }
    }

    public synchronized double obterTokensDisponiveis() {
        reabastecer();
        return tokensDisponiveis;
    }
}
