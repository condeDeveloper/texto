package br.com.conde.texto;

import java.util.ArrayList;
import java.util.List;

/**
 * A QUEBRA GULOSA: enche cada linha ate a proxima palavra nao caber, e quebra.
 *
 * <p>E o que todo editor de texto simples faz, e o que qualquer um escreve em cinco
 * minutos. Ela nunca volta atras: a decisao de quebrar a primeira linha e tomada sem saber
 * nada sobre o resto do paragrafo.
 *
 * <p>E ela NAO E uma versao de palha, mas eu escrevi que ela ficava a poucos por cento do
 * otimo e errei: em texto corrido ela custa de 24% a 30% a mais. O que ela ganha e o
 * trabalho, uma candidata por palavra contra treze da programacao dinamica.
 *
 * <p>E ela nao tem garantia. A tabela do pior caso PROCURA o paragrafo que mais a derruba,
 * em vez de constru-lo, e acha um em que ela custa 150% a mais.
 *
 * <h2>Ela nunca usa mais linhas que o otimo</h2>
 *
 * <p>Isso vale sempre, e nao e coincidencia: encher cada linha ao maximo e exatamente a
 * estrategia que minimiza o numero de linhas.
 *
 * <p>O que eu escrevi e que o otimo usaria MAIS linhas que ela, jogando sobra para a ultima
 * linha que e gratuita. Com o custo quadratico isso nao aconteceu em nenhuma das nove
 * combinacoes medidas: abrir uma linha a mais espalha sobra por TODAS as outras, e o
 * quadrado cobra isso mais do que a ultima linha devolve. Com o custo cubico acontece, e
 * por uma linha so.
 */
public final class Gulosa implements Quebra {

    @Override
    public String nome() {
        return "gulosa";
    }

    @Override
    public int[] quebras(Paragrafo paragrafo, Custo custo, Contador contador) {
        contador.paragrafo();

        List<Integer> inicios = new ArrayList<>();

        inicios.add(0);

        int inicioDaLinha = 0;

        for (int palavra = 1; palavra < paragrafo.palavras(); palavra++) {
            /*
             * UMA CANDIDATA POR PALAVRA. Ela pergunta uma coisa so: "a linha que comecou
             * ali ainda cabe se eu puser esta palavra?". A resposta nao depende do resto do
             * paragrafo, e e por isso que ela e linear e por isso que ela erra.
             */
            contador.candidata();
            contador.compara();

            if (paragrafo.cabe(inicioDaLinha, palavra + 1)) continue;

            inicios.add(palavra);
            inicioDaLinha = palavra;
        }

        int[] saida = new int[inicios.size()];

        for (int i = 0; i < saida.length; i++) saida[i] = inicios.get(i);

        return saida;
    }
}
