package br.com.conde.texto;

import java.util.Locale;

/**
 * O MEDIDOR: imprime as tabelas que o README mostra e que os testes conferem.
 *
 * <p>Toda tabela daqui tem um teste do lado que afirma os mesmos numeros. Texto nao quebra
 * quando deixa de ser verdade; teste quebra.
 */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";

        if (comando.equals("correcao") || comando.equals("tudo")) correcao();
        if (comando.equals("feiura") || comando.equals("tudo")) feiura();
        if (comando.equals("pior") || comando.equals("tudo")) pior();
        if (comando.equals("linhas") || comando.equals("tudo")) linhas();
        if (comando.equals("custo") || comando.equals("tudo")) custo();
        if (comando.equals("trabalho") || comando.equals("tudo")) trabalho();
    }

    private static void correcao() {
        System.out.println();
        System.out.println("A PROGRAMACAO DINAMICA BATE COM A FORCA BRUTA.");
        System.out.println();

        var linhas = Medidas.aCorrecao();
        long erradas = linhas.stream().filter(l -> l.divergencias() > 0).count();

        System.out.printf("  %-20s %-12s %14s %14s%n",
                "entrada", "custo", "paragrafos", "divergencias");

        for (var l : linhas)
            System.out.printf("  %-20s %-12s %14d %14d%n",
                    l.entrada(), l.custo(), l.paragrafos(), l.divergencias());

        System.out.println();
        System.out.printf("  %d combinacoes, %d com divergencia.%n", linhas.size(), erradas);
        System.out.println();
    }

    private static void feiura() {
        System.out.println();
        System.out.println("QUANTO O GULOSO PERDE, com " + Medidas.PALAVRAS + " palavras.");
        System.out.println();
        System.out.printf("  %-20s %10s %16s %16s %16s %14s %14s%n",
                "entrada", "largura", "custo guloso", "custo otimo", "quanto perde",
                "linhas guloso", "linhas otimo");

        for (var l : Medidas.aFeiura())
            System.out.printf(Locale.ROOT, "  %-20s %10d %16d %16d %15.2f%% %14d %14d%n",
                    l.entrada(), l.largura(), l.custoGuloso(), l.custoOtimo(), l.quantoPerde(),
                    l.linhasGuloso(), l.linhasOtimo());

        System.out.println();
    }

    private static void pior() {
        System.out.println();
        System.out.println("O CASO RUIM DO GULOSO, procurado em vez de construido.");
        System.out.println();
        System.out.printf("  %8s %-26s %14s %14s %14s %12s %12s%n",
                "largura", "padrao achado", "custo guloso", "custo otimo", "quanto perde",
                "sobra gulosa", "sobra otima");

        for (var l : Medidas.oPior())
            System.out.printf(Locale.ROOT, "  %8d %-26s %14d %14d %13.2f%% %12d %12d%n",
                    l.largura(), l.padrao(), l.custoGuloso(), l.custoOtimo(), l.quantoPerde(),
                    l.maiorSobraGulosa(), l.maiorSobraOtima());

        System.out.println();
    }

    private static void linhas() {
        System.out.println();
        System.out.println("O OTIMO USA MAIS LINHAS QUE O GULOSO? NAO USA.");
        System.out.println();
        System.out.printf("  %-20s %10s %16s %16s %12s%n",
                "entrada", "largura", "linhas guloso", "linhas otimo", "a mais");

        for (var l : Medidas.asLinhas())
            System.out.printf("  %-20s %10d %16d %16d %12d%n",
                    l.entrada(), l.largura(), l.linhasGuloso(), l.linhasOtimo(), l.aMais());

        System.out.println();
    }

    private static void custo() {
        System.out.println();
        System.out.println("A FUNCAO DE CUSTO MUDA A RESPOSTA, largura de " + Medidas.LARGURA + ".");
        System.out.println();
        System.out.printf("  %-12s %10s %18s %18s %14s %22s%n",
                "custo", "linhas", "pela propria", "pelo quadrado", "maior sobra",
                "candidatas por palavra");

        for (var l : Medidas.oCusto())
            System.out.printf(Locale.ROOT, "  %-12s %10d %18d %18d %14d %22.2f%n",
                    l.custo(), l.linhas(), l.pelaPropria(), l.peloQuadrado(), l.maiorSobra(),
                    l.candidatasPorPalavra());

        System.out.println();
    }

    private static void trabalho() {
        System.out.println();
        System.out.println("O TRABALHO DE CADA METODO, em candidatas examinadas.");
        System.out.println();
        System.out.printf("  %10s %-14s %18s %22s%n",
                "palavras", "metodo", "candidatas", "por palavra");

        for (var l : Medidas.oTrabalho())
            System.out.printf(Locale.ROOT, "  %10d %-14s %18d %22.2f%n",
                    l.palavras(), l.metodo(), l.candidatas(), l.porPalavra());

        System.out.println();
    }
}
