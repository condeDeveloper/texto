package br.com.conde.texto;

import java.util.ArrayList;
import java.util.List;

/**
 * As MEDIDAS: a feiura da quebra e as candidatas examinadas. Nunca segundos.
 *
 * <p>Sao duas medidas de naturezas diferentes, e esse e o assunto. O TRABALHO gasto para
 * achar a quebra e a FEIURA da quebra achada. O guloso ganha a primeira por tres ordens de
 * grandeza e perde a segunda por alguns por cento.
 *
 * <p>Toda tabela daqui tem um teste do lado que afirma os mesmos numeros.
 */
public final class Medidas {

    private Medidas() {
    }

    public static final int PALAVRAS = 20_000;

    public static final int LARGURA = 70;

    public static final long SEMENTE = 20_251_012L;

    // ───────────────────── 1. a correcao ─────────────────────

    public record LinhaCorrecao(String entrada, String custo, int paragrafos, int divergencias) {
    }

    /**
     * A PROGRAMACAO DINAMICA BATE COM A FORCA BRUTA, em todo paragrafo pequeno.
     *
     * <p>Pequeno porque a forca bruta e exponencial: com vinte palavras sao meio milhao de
     * quebras. Mas o argumento que a dinamica usa nao depende do tamanho, entao conferi-lo
     * em paragrafos pequenos confere o argumento.
     */
    public static List<LinhaCorrecao> aCorrecao() {
        List<LinhaCorrecao> linhas = new ArrayList<>();

        for (var entrada : paragrafosPequenos().entrySet())
            for (Custo custo : Custo.values()) {
                int divergencias = 0;

                for (Paragrafo paragrafo : entrada.getValue()) {
                    Juiz.Veredito veredito = Juiz.julgar(new Exata(), paragrafo, custo);

                    if (!veredito.passou()) divergencias++;
                }

                linhas.add(new LinhaCorrecao(entrada.getKey(), custo.nome(),
                        entrada.getValue().size(), divergencias));
            }

        return linhas;
    }

    private static java.util.Map<String, List<Paragrafo>> paragrafosPequenos() {
        var entradas = new java.util.LinkedHashMap<String, List<Paragrafo>>();

        for (var tipo : Paragrafos.todos(16, SEMENTE).entrySet()) {
            List<Paragrafo> paragrafos = new ArrayList<>();

            for (int largura : new int[] {10, 20, 30, 45}) {
                int[] larguras = tipo.getValue().clone();

                for (int i = 0; i < larguras.length; i++)
                    larguras[i] = Math.min(larguras[i], largura);

                paragrafos.add(new Paragrafo(larguras, largura));
            }

            entradas.put(tipo.getKey(), paragrafos);
        }

        return entradas;
    }

    // ───────────────────── 2. a feiura ─────────────────────

    public record LinhaFeiura(String entrada, int largura, long custoGuloso, long custoOtimo,
                              double quantoPerde, int linhasGuloso, int linhasOtimo) {
    }

    /**
     * QUANTO O GULOSO PERDE EM TEXTO DE VERDADE, por largura de linha.
     *
     * <p>Eu escrevi que a resposta seria "pouco, poucos por cento". Sao de 24% a 30% em
     * texto corrido, em todas as larguras, e de 6% a 21% com larguras sorteadas.
     *
     * <p>E ha uma linha em que ele empata exatamente: com todas as palavras do mesmo
     * tamanho, 0,00% em todas as larguras. Ali nao ha escolha a fazer, e o guloso nao erra
     * por nao olhar adiante porque nao ha nada adiante que importe.
     *
     * <p>A direcao que eu previ, essa valeu: quanto mais os tamanhos variam, mais ele perde.
     */
    public static List<LinhaFeiura> aFeiura() {
        List<LinhaFeiura> linhas = new ArrayList<>();

        for (var entrada : Paragrafos.todos(PALAVRAS, SEMENTE).entrySet())
            for (int largura : new int[] {20, 40, 70, 120}) {
                int[] larguras = entrada.getValue().clone();

                for (int i = 0; i < larguras.length; i++)
                    larguras[i] = Math.min(larguras[i], largura);

                Paragrafo paragrafo = new Paragrafo(larguras, largura);

                int[] guloso = new Gulosa().quebras(paragrafo, Custo.QUADRADO, new Contador());
                int[] otimo = new Exata().quebras(paragrafo, Custo.QUADRADO, new Contador());

                long custoGuloso = paragrafo.custoDe(guloso, Custo.QUADRADO);
                long custoOtimo = paragrafo.custoDe(otimo, Custo.QUADRADO);

                linhas.add(new LinhaFeiura(entrada.getKey(), largura, custoGuloso, custoOtimo,
                        Juiz.quantoPerde(custoGuloso, custoOtimo), guloso.length, otimo.length));
            }

        return linhas;
    }

    // ───────────────────── 3. o caso ruim ─────────────────────

    public record LinhaPior(int largura, String padrao, long custoGuloso, long custoOtimo,
                            double quantoPerde, long maiorSobraGulosa, long maiorSobraOtima) {
    }

    /**
     * O CASO RUIM DO GULOSO, ACHADO POR BUSCA e nao construido a mao.
     *
     * <p>Eu construi um paragrafo para derrubar o guloso e ele nao derrubou: empate em todas
     * as larguras. Entao a tabela passou a PROCURAR: ela percorre todas as combinacoes de
     * larguras ate um teto e fica com a que o guloso perde por mais.
     *
     * <p>O resultado nao e um exemplo que eu imaginei e que pode estar errado: e o pior que
     * existe naquele espaco de paragrafos.
     *
     * <p>E o padrao achado e repetido para o efeito aparecer num texto de tamanho de
     * verdade, porque num paragrafo de sete palavras a ultima linha gratuita esconde quase
     * tudo.
     */
    public static List<LinhaPior> oPior() {
        List<LinhaPior> linhas = new ArrayList<>();

        for (int largura : new int[] {8, 10, 12}) {
            int[] padrao = Paragrafos.piorContraOGuloso(7, largura - 2, largura, Custo.QUADRADO);

            Paragrafo paragrafo = new Paragrafo(Paragrafos.repetido(padrao, 300), largura);

            int[] guloso = new Gulosa().quebras(paragrafo, Custo.QUADRADO, new Contador());
            int[] otimo = new Exata().quebras(paragrafo, Custo.QUADRADO, new Contador());

            long custoGuloso = paragrafo.custoDe(guloso, Custo.QUADRADO);
            long custoOtimo = paragrafo.custoDe(otimo, Custo.QUADRADO);

            linhas.add(new LinhaPior(largura, java.util.Arrays.toString(padrao),
                    custoGuloso, custoOtimo, Juiz.quantoPerde(custoGuloso, custoOtimo),
                    maiorSobra(paragrafo, guloso), maiorSobra(paragrafo, otimo)));
        }

        return linhas;
    }

    private static long maiorSobra(Paragrafo paragrafo, int[] inicios) {
        long maior = 0;

        for (int linha = 0; linha + 1 < inicios.length; linha++)
            maior = Math.max(maior,
                    paragrafo.sobraDa(inicios[linha], inicios[linha + 1]));

        return maior;
    }

    // ───────────────────── 4. as linhas ─────────────────────

    public record LinhaLinhas(String entrada, int largura, int linhasGuloso, int linhasOtimo,
                              int aMais) {
    }

    /**
     * O OTIMO NAO USA MAIS LINHAS QUE O GULOSO, e eu escrevi que usaria.
     *
     * <p>O guloso enche cada linha ao maximo, que e exatamente a estrategia que minimiza o
     * NUMERO de linhas. Entao ele nunca usa mais que o otimo; a pergunta era se o otimo usa
     * mais que ele.
     *
     * <p>Eu disse que sim, com um argumento que parecia solido: a ultima linha e de graca,
     * entao abrir uma linha a mais joga sobra para onde ela nao custa nada.
     *
     * <p>A coluna "a mais" e ZERO nas nove combinacoes. O argumento esquecia o outro lado:
     * abrir uma linha a mais tambem espalha sobra por TODAS as linhas anteriores, e o custo
     * quadratico cobra isso muito mais do que a ultima linha devolve. Uma linha a mais
     * aparece so com o custo cubico, na tabela do custo, e aparece uma vez.
     */
    public static List<LinhaLinhas> asLinhas() {
        List<LinhaLinhas> linhas = new ArrayList<>();

        for (var entrada : Paragrafos.todos(2_000, SEMENTE).entrySet())
            for (int largura : new int[] {20, 40, 70}) {
                int[] larguras = entrada.getValue().clone();

                for (int i = 0; i < larguras.length; i++)
                    larguras[i] = Math.min(larguras[i], largura);

                Paragrafo paragrafo = new Paragrafo(larguras, largura);

                int gulosas = new Gulosa().quebras(paragrafo, Custo.QUADRADO,
                        new Contador()).length;
                int otimas = new Exata().quebras(paragrafo, Custo.QUADRADO,
                        new Contador()).length;

                linhas.add(new LinhaLinhas(entrada.getKey(), largura, gulosas, otimas,
                        otimas - gulosas));
            }

        return linhas;
    }

    // ───────────────────── 5. a funcao de custo ─────────────────────

    public record LinhaCusto(String custo, int linhas, long pelaPropria, long peloQuadrado,
                             long maiorSobra, double candidatasPorPalavra) {
    }

    /**
     * A FUNCAO DE CUSTO MUDA A RESPOSTA, e a coluna do meio e a prova.
     *
     * <p>Cada linha otimiza a propria funcao, e a coluna "pela propria" e o custo dela
     * medido nela mesma: e o menor possivel, por definicao.
     *
     * <p>A coluna "pelo quadrado" mede as TRES quebras com a MESMA regua, a do quadrado.
     * Se as tres quebras fossem iguais, essa coluna teria tres numeros iguais. Ela nao tem:
     * as funcoes escolhem quebras diferentes, e cada uma e pior que as outras na regua
     * alheia.
     */
    public static List<LinhaCusto> oCusto() {
        List<LinhaCusto> linhas = new ArrayList<>();

        int[] larguras = Paragrafos.comoTextoDeVerdade(PALAVRAS, SEMENTE);
        Paragrafo paragrafo = new Paragrafo(larguras, LARGURA);

        for (Custo custo : Custo.values()) {
            Contador contador = new Contador();
            int[] quebra = new Exata().quebras(paragrafo, custo, contador);

            linhas.add(new LinhaCusto(custo.nome(), quebra.length,
                    paragrafo.custoDe(quebra, custo),
                    paragrafo.custoDe(quebra, Custo.QUADRADO),
                    maiorSobra(paragrafo, quebra),
                    (double) contador.candidatas() / PALAVRAS));
        }

        return linhas;
    }

    // ───────────────────── 6. o trabalho ─────────────────────

    public record LinhaTrabalho(int palavras, String metodo, long candidatas,
                                double porPalavra) {
    }

    /**
     * O TRABALHO DE CADA METODO, incluindo a forca bruta enquanto ela ainda cabe.
     *
     * <p>Tres curvas numa coluna so: o guloso examina uma candidata por palavra, a dinamica
     * examina as que cabem a partir de cada palavra, e a forca bruta examina todas as
     * combinacoes.
     *
     * <p>A forca bruta sai da tabela em vinte palavras porque cada palavra a mais dobra o
     * numero dela. E a dinamica nao cresce com o quadrado na pratica: ela para na primeira
     * candidata que nao cabe, e quantas cabem depende da largura da linha e nao do tamanho
     * do paragrafo.
     */
    public static List<LinhaTrabalho> oTrabalho() {
        List<LinhaTrabalho> linhas = new ArrayList<>();

        for (int palavras : new int[] {12, 16, 20, 1_000, 20_000}) {
            int[] larguras = Paragrafos.comoTextoDeVerdade(palavras, SEMENTE);
            Paragrafo paragrafo = new Paragrafo(larguras, LARGURA);

            for (var metodo : Paragrafos.osMetodos().entrySet()) {
                Contador contador = new Contador();

                metodo.getValue().quebras(paragrafo, Custo.QUADRADO, contador);

                linhas.add(new LinhaTrabalho(palavras, metodo.getKey(), contador.candidatas(),
                        (double) contador.candidatas() / palavras));
            }

            if (palavras > ForcaBruta.MAXIMO_DE_PALAVRAS) continue;

            Contador contador = new Contador();

            new ForcaBruta().quebras(paragrafo, Custo.QUADRADO, contador);

            linhas.add(new LinhaTrabalho(palavras, "forca bruta", contador.candidatas(),
                    (double) contador.candidatas() / palavras));
        }

        return linhas;
    }
}
