package br.com.conde.texto;

/**
 * QUANTO CUSTA UMA LINHA que sobrou com tanto espaco em branco.
 *
 * <p>Esta e a decisao de projeto que o repositorio inteiro gira em torno, e e uma escolha e
 * nao uma verdade. "Qual quebra de linha e a melhor" nao tem resposta ate alguem dizer o
 * que melhor quer dizer, e cada funcao daqui diz uma coisa diferente.
 *
 * <p>A tabela do custo mostra que elas DISCORDAM: o mesmo paragrafo quebrado de jeitos
 * diferentes conforme a funcao, e cada um otimo para a sua.
 *
 * <h2>A ultima linha nao custa nada</h2>
 *
 * <p>Todas elas tratam a ultima linha do paragrafo como gratuita, por mais curta que seja.
 * E a regra de tipografia de verdade, e sem ela o resultado ficaria absurdo: o metodo
 * tentaria encher a ultima linha empurrando palavras das anteriores para tras.
 */
public enum Custo {

    /**
     * O quadrado da sobra.
     *
     * <p>E a escolha mais comum e o motivo e que ela pune o EXTREMO mais do que a soma. Duas
     * linhas sobrando cinco cada custam 50; uma sobrando zero e outra sobrando dez custam
     * 100. Entao ela espalha o branco em vez de concentrar.
     */
    QUADRADO {
        @Override
        public long de(long sobra) {
            return sobra * sobra;
        }
    },

    /**
     * O cubo da sobra, que e o que o TeX usa.
     *
     * <p>Mesma ideia do quadrado, so que mais agressiva: ela odeia a linha muito curta ainda
     * mais, e por isso aceita piorar varias linhas um pouco para consertar uma linha
     * horrivel.
     */
    CUBO {
        @Override
        public long de(long sobra) {
            return sobra * sobra * sobra;
        }
    },

    /**
     * A propria sobra.
     *
     * <p>Ela parece razoavel e e a que menos serve, e a tabela do custo mostra porque: a
     * soma das sobras de um paragrafo e quase a mesma em TODA quebra que use o mesmo numero
     * de linhas. Ela nao distingue quase nada, e empata com a gulosa na maioria dos casos.
     */
    LINEAR {
        @Override
        public long de(long sobra) {
            return sobra;
        }
    };

    public abstract long de(long sobra);

    public String nome() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
