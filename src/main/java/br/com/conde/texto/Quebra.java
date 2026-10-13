package br.com.conde.texto;

/**
 * QUEBRAR UM PARAGRAFO EM LINHAS.
 *
 * <p>A resposta e a lista das palavras em que cada linha COMECA, sempre comecando no zero.
 * Com cinco palavras, {@code [0, 2, 4]} quer dizer tres linhas: as palavras 0 e 1, depois 2
 * e 3, depois 4.
 *
 * <h2>Este repositorio e diferente dos outros</h2>
 *
 * <p>Nos outros, todos os metodos devolvem a MESMA resposta e so o custo muda. Aqui nao: o
 * guloso devolve uma quebra pior, e isso nao e um defeito dele, e o que ele e.
 *
 * <p>Entao ha duas medidas, e elas sao de naturezas diferentes. O TRABALHO gasto para
 * achar a quebra, que e o assunto de sempre, e a FEIURA da quebra achada, que e a resposta.
 * Um metodo pode ganhar numa e perder na outra, e o guloso ganha a primeira por tres ordens
 * de grandeza.
 */
public interface Quebra {

    int[] quebras(Paragrafo paragrafo, Custo custo, Contador contador);

    String nome();

    /** Quantas linhas uma quebra tem. */
    static int linhas(int[] quebras) {
        return quebras.length;
    }
}
