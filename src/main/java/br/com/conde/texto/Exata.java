package br.com.conde.texto;

import java.util.ArrayList;
import java.util.List;

/**
 * A QUEBRA OTIMA POR PROGRAMACAO DINAMICA.
 *
 * <p>A ideia e uma observacao sobre a estrutura do problema: se a melhor quebra do
 * paragrafo inteiro poe a primeira linha indo da palavra 0 ate a j, entao o resto dela e a
 * melhor quebra do paragrafo que comeca na palavra j. Nao pode ser outra coisa: se houvesse
 * uma quebra melhor para o resto, trocar daria uma quebra melhor para o todo.
 *
 * <p>Entao da para resolver de tras para frente. {@code melhor[i]} e o custo da melhor
 * quebra das palavras de i em diante, e ele se calcula olhando so os {@code melhor[j]} ja
 * resolvidos.
 *
 * <h2>Por que nao ha recursao aqui</h2>
 *
 * <p>A formulacao e naturalmente recursiva e o laco e de tras para frente de proposito.
 * Resolvendo de n ate 0, todo {@code melhor[j]} que o passo i precisa ja esta pronto, e nao
 * ha pilha de chamadas nem memoria de resultados: a ordem do laco E a memoria.
 */
public final class Exata implements Quebra {

    @Override
    public String nome() {
        return "otima";
    }

    @Override
    public int[] quebras(Paragrafo paragrafo, Custo custo, Contador contador) {
        contador.paragrafo();

        int n = paragrafo.palavras();
        long[] melhor = new long[n + 1];
        int[] proximaQuebra = new int[n + 1];

        melhor[n] = 0;

        for (int i = n - 1; i >= 0; i--) {
            melhor[i] = Long.MAX_VALUE;

            for (int j = i + 1; j <= n; j++) {
                contador.candidata();

                /*
                 * O LACO PARA NA PRIMEIRA QUE NAO CABE, e isso e o que torna o metodo
                 * quadratico no PIOR caso e quase linear no comum: numa linha de oitenta
                 * colunas com palavras de cinco letras, so umas quinze candidatas cabem a
                 * partir de cada palavra, e nao n.
                 */
                if (!paragrafo.cabe(i, j)) break;

                long daLinha = j == n ? 0 : custo.de(paragrafo.sobraDa(i, j));

                if (melhor[j] == Long.MAX_VALUE) continue;

                long total = daLinha + melhor[j];

                contador.compara();

                if (total >= melhor[i]) continue;

                melhor[i] = total;
                proximaQuebra[i] = j;
            }
        }

        List<Integer> inicios = new ArrayList<>();

        for (int i = 0; i < n; i = proximaQuebra[i]) inicios.add(i);

        int[] saida = new int[inicios.size()];

        for (int i = 0; i < saida.length; i++) saida[i] = inicios.get(i);

        return saida;
    }
}
