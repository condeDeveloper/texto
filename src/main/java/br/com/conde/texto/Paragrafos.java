package br.com.conde.texto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OS PARAGRAFOS DE ENTRADA. A distribuicao das larguras decide quanto o guloso perde.
 *
 * <p>Com palavras todas do mesmo tamanho, o guloso e otimo: nao ha escolha a fazer. Quanto
 * mais os tamanhos variam, mais a decisao de onde quebrar a primeira linha importa para o
 * resto, e mais o guloso erra por nao olhar adiante.
 */
public final class Paragrafos {

    private Paragrafos() {
    }

    /**
     * O SORTEIO DA CASA: um gerador congruente linear escrito aqui, com constantes fixas.
     *
     * <p>Nao e {@link java.util.Random} de proposito. Uma medida que depende de uma
     * biblioteca nao e reproduzivel: a implementacao pode mudar entre versoes da maquina
     * virtual e as tabelas do README deixariam de bater sem que nada aqui tivesse mudado.
     */
    public static final class Sorteio {

        private long estado;

        public Sorteio(long semente) {
            this.estado = semente;
        }

        /**
         * O proximo inteiro.
         *
         * <p>O DESLOCAMENTO DE 32 CARREGA O METODO. Os bits BAIXOS de um gerador congruente
         * linear de modulo potencia de dois sao pessimos: o ultimo bit alterna zero e um
         * para sempre, o penultimo tem periodo quatro.
         *
         * <p>Aqui isso seria especialmente enganoso. As larguras sao restos por um numero
         * pequeno, ou seja, bits baixos: com bits ruins elas sairiam num ciclo curto, o
         * paragrafo viraria uma repeticao e o guloso pareceria otimo por um motivo que nao
         * e dele. Ha um teste so para isto.
         */
        public int proximo() {
            estado = estado * 6364136223846793005L + 1442695040888963407L;
            return (int) (estado >>> 32);
        }

        public int ate(int limite) {
            return Math.floorMod(proximo(), limite);
        }
    }

    /**
     * Palavras com tamanhos parecidos com os de texto corrido.
     *
     * <p>A distribuicao e tirada do portugues escrito: muitas palavras de duas a seis
     * letras, poucas acima de dez. Ela nao precisa ser exata; o que importa e ela ter a
     * forma certa, com a maioria curta e uma cauda.
     */
    public static int[] comoTextoDeVerdade(int quantas, long semente) {
        int[] pesos = {0, 60, 160, 180, 150, 120, 100, 80, 60, 40, 25, 15, 8, 4, 2, 1};
        int total = 0;

        for (int peso : pesos) total += peso;

        Sorteio sorteio = new Sorteio(semente);
        int[] larguras = new int[quantas];

        for (int i = 0; i < quantas; i++) {
            int sorteado = sorteio.ate(total);
            int acumulado = 0;
            int escolhida = 1;

            for (int tamanho = 1; tamanho < pesos.length; tamanho++) {
                acumulado += pesos[tamanho];

                if (sorteado < acumulado) {
                    escolhida = tamanho;
                    break;
                }
            }

            larguras[i] = escolhida;
        }

        return larguras;
    }

    /** Todas as palavras do mesmo tamanho. O guloso e otimo aqui, e a tabela mostra. */
    public static int[] todasIguais(int quantas, int tamanho) {
        int[] larguras = new int[quantas];

        java.util.Arrays.fill(larguras, tamanho);

        return larguras;
    }

    /** Larguras sorteadas por igual numa faixa, que varia muito mais que texto de verdade. */
    public static int[] sorteadas(int quantas, int menor, int maior, long semente) {
        Sorteio sorteio = new Sorteio(semente);
        int[] larguras = new int[quantas];

        for (int i = 0; i < quantas; i++) larguras[i] = menor + sorteio.ate(maior - menor + 1);

        return larguras;
    }

    /**
     * O PARAGRAFO QUE MAIS DERRUBA O GULOSO, ACHADO POR BUSCA.
     *
     * <p>Eu tentei construir esse paragrafo a mao primeiro. A ideia era: palavras curtas que
     * enchem a linha exatamente, e logo depois uma palavra comprida que nao cabe mais e vai
     * sozinha deixando um buraco. Ela nao funcionou, e por um motivo obvio depois de medido:
     * uma palavra COMPRIDA sozinha numa linha nao deixa buraco nenhum, porque ela e comprida.
     * O guloso empatou com o otimo em todas as larguras.
     *
     * <p>Entao em vez de adivinhar, aqui o paragrafo e PROCURADO. A funcao percorre todas as
     * combinacoes de larguras ate o teto dado, roda os dois metodos em cada uma e devolve a
     * que o guloso perde por mais.
     *
     * <p>Isso custa o teto elevado ao numero de palavras, e e por isso que o numero de
     * palavras e pequeno. Mas o resultado e de outra natureza: nao e um exemplo que eu
     * imaginei e que pode estar errado, e o PIOR que existe naquele espaco.
     */
    public static int[] piorContraOGuloso(int palavras, int maiorPalavra, int largura,
                                          Custo custo) {

        int[] atual = new int[palavras];
        int[] pior = null;
        double maiorPerda = -1;

        java.util.Arrays.fill(atual, 1);

        while (true) {
            Paragrafo paragrafo = new Paragrafo(atual, largura);

            long guloso = paragrafo.custoDe(
                    new Gulosa().quebras(paragrafo, custo, new Contador()), custo);

            long otimo = paragrafo.custoDe(
                    new Exata().quebras(paragrafo, custo, new Contador()), custo);

            double perda = otimo == 0 ? (guloso == 0 ? 0 : Double.MAX_VALUE)
                    : (double) guloso / otimo;

            if (perda > maiorPerda) {
                maiorPerda = perda;
                pior = atual.clone();
            }

            int posicao = palavras - 1;

            while (posicao >= 0 && atual[posicao] == maiorPalavra) {
                atual[posicao] = 1;
                posicao--;
            }

            if (posicao < 0) break;

            atual[posicao]++;
        }

        return pior;
    }

    /** O mesmo paragrafo repetido, para o efeito aparecer num texto de tamanho de verdade. */
    public static int[] repetido(int[] padrao, int vezes) {
        int[] larguras = new int[padrao.length * vezes];

        for (int v = 0; v < vezes; v++)
            System.arraycopy(padrao, 0, larguras, v * padrao.length, padrao.length);

        return larguras;
    }

    public static Map<String, int[]> todos(int palavras, long semente) {
        Map<String, int[]> entradas = new LinkedHashMap<>();

        entradas.put("texto de verdade", comoTextoDeVerdade(palavras, semente));
        entradas.put("todas iguais", todasIguais(palavras, 5));
        entradas.put("sorteadas 1 a 20", sorteadas(palavras, 1, 20, semente + 1));

        return entradas;
    }

    /** Os metodos que as tabelas percorrem, cada um com o nome que a tabela imprime. */
    public static Map<String, Quebra> osMetodos() {
        Map<String, Quebra> metodos = new LinkedHashMap<>();

        metodos.put("gulosa", new Gulosa());
        metodos.put("otima", new Exata());

        return metodos;
    }
}
