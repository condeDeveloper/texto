package br.com.conde.texto;

/**
 * O CONTADOR: linhas candidatas examinadas. Nunca segundos.
 *
 * <p>A LINHA CANDIDATA e a medida certa e nao so a conveniente. Quebrar um paragrafo e
 * escolher, entre todas as maneiras de cortar, a de menor custo, e todo metodo daqui faz
 * isso examinando candidatas: "e se esta linha fosse da palavra i ate a j?".
 *
 * <p>O guloso examina uma candidata por palavra, porque nunca volta atras. A programacao
 * dinamica examina todas as que CABEM a partir de cada palavra. A forca bruta examina todas
 * as combinacoes de quebras, que e dois elevado ao numero de palavras.
 *
 * <p>Esses tres numeros ficam numa coluna so e contam a historia inteira: linear,
 * quadratico e exponencial, medidos no mesmo paragrafo.
 *
 * <h2>E ha uma segunda medida, que e a resposta</h2>
 *
 * <p>Este repositorio e dos poucos em que os metodos NAO devolvem a mesma coisa. O guloso
 * devolve uma quebra pior, e a diferenca de qualidade e o assunto. Por isso a feiura do
 * resultado e medida junto com o trabalho gasto para chegar nele.
 */
public final class Contador {

    private long candidatas;
    private long comparacoes;
    private long paragrafos;

    /** Uma linha possivel foi considerada: "e se esta linha fosse daqui ate ali?". */
    public void candidata() {
        candidatas++;
    }

    public void candidata(long quantas) {
        candidatas += quantas;
    }

    /** Dois custos foram comparados para escolher o menor. */
    public void compara() {
        comparacoes++;
    }

    public void paragrafo() {
        paragrafos++;
    }

    public long candidatas() {
        return candidatas;
    }

    public long comparacoes() {
        return comparacoes;
    }

    public long paragrafos() {
        return paragrafos;
    }

    public double candidatasPorParagrafo() {
        return paragrafos == 0 ? 0 : (double) candidatas / paragrafos;
    }

    public void zerar() {
        candidatas = 0;
        comparacoes = 0;
        paragrafos = 0;
    }
}
