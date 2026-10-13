package br.com.conde.texto;

import java.util.ArrayList;
import java.util.List;

/**
 * A FORCA BRUTA: experimenta TODAS as maneiras de quebrar.
 *
 * <p>Ela e o juiz deste repositorio. Entre duas palavras consecutivas ou ha quebra ou nao
 * ha, entao com n palavras existem 2^(n-1) quebras possiveis, e ela percorre todas,
 * descarta as que nao cabem e fica com a mais barata.
 *
 * <p>Nao ha nela nenhuma observacao sobre a estrutura do problema, nenhuma ordem de laco
 * que precise estar certa, nenhum caminho a reconstruir. E e justamente por isso que ela
 * serve de juiz da programacao dinamica: a dinamica depende de um argumento, e um argumento
 * pode estar errado.
 *
 * <p>O teto de vinte palavras nao e timidez: sao 524.288 quebras, e cada palavra a mais
 * dobra isso. Com trinta palavras seriam quinhentos milhoes.
 */
public final class ForcaBruta implements Quebra {

    /** Alem disto ela recusa, em vez de ficar rodando para sempre. */
    public static final int MAXIMO_DE_PALAVRAS = 20;

    @Override
    public String nome() {
        return "forca bruta";
    }

    @Override
    public int[] quebras(Paragrafo paragrafo, Custo custo, Contador contador) {
        contador.paragrafo();

        int n = paragrafo.palavras();

        if (n > MAXIMO_DE_PALAVRAS)
            throw new IllegalArgumentException(
                    "a forca bruta vai ate " + MAXIMO_DE_PALAVRAS + " palavras, vieram " + n);

        if (n == 0) return new int[0];

        long combinacoes = 1L << (n - 1);
        long menorCusto = Long.MAX_VALUE;
        int[] melhor = null;

        for (long mascara = 0; mascara < combinacoes; mascara++) {
            List<Integer> inicios = new ArrayList<>();

            inicios.add(0);

            /*
             * O BIT k DIZ SE HA QUEBRA ENTRE A PALAVRA k E A k+1. Percorrer os inteiros de
             * zero a 2^(n-1) percorre todas as combinacoes de quebras, uma vez cada.
             */
            for (int k = 0; k + 1 < n; k++)
                if ((mascara & (1L << k)) != 0) inicios.add(k + 1);

            contador.candidata(inicios.size());

            boolean serve = true;
            long total = 0;

            for (int linha = 0; linha < inicios.size() && serve; linha++) {
                int de = inicios.get(linha);
                int ate = linha + 1 < inicios.size() ? inicios.get(linha + 1) : n;

                if (!paragrafo.cabe(de, ate)) {
                    serve = false;
                    continue;
                }

                if (linha + 1 < inicios.size()) total += custo.de(paragrafo.sobraDa(de, ate));
            }

            if (!serve) continue;

            contador.compara();

            if (total >= menorCusto) continue;

            menorCusto = total;
            melhor = new int[inicios.size()];

            for (int i = 0; i < melhor.length; i++) melhor[i] = inicios.get(i);
        }

        if (melhor == null)
            throw new IllegalStateException("nenhuma quebra cabe, o que nao deveria acontecer");

        return melhor;
    }
}
