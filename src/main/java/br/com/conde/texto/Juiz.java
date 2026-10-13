package br.com.conde.texto;

/**
 * O JUIZ: a forca bruta.
 *
 * <p>Ela e o juiz certo e nao so o conveniente. A programacao dinamica depende de um
 * ARGUMENTO sobre a estrutura do problema, e um argumento pode estar errado. A forca bruta
 * nao depende de argumento nenhum: ela experimenta todas as quebras e fica com a melhor.
 *
 * <h2>O que ele confere</h2>
 *
 * <p>O CUSTO, e nao a quebra. Duas quebras diferentes podem ter exatamente o mesmo custo, e
 * nesse caso a dinamica e a forca bruta podem escolher qualquer uma sem nenhuma estar
 * errada: a dinamica escolhe a que o laco achou primeiro, a forca bruta escolhe a de menor
 * mascara.
 *
 * <p>Conferir a quebra reprovaria as duas por um motivo que nao tem nada a ver com o
 * problema. O que precisa ser igual e o numero, e o numero e unico.
 *
 * <p>E a quebra ainda e conferida de outro jeito: pela VALIDADE. Toda linha cabe, nenhuma
 * palavra some e nenhuma aparece duas vezes.
 */
public final class Juiz {

    private Juiz() {
    }

    public record Veredito(long custoCerto, long custoDele, int linhasCertas, int linhasDele,
                           String falha) {

        public boolean passou() {
            return falha == null;
        }
    }

    public static Veredito julgar(Quebra metodo, Paragrafo paragrafo, Custo custo) {
        int[] certo = new ForcaBruta().quebras(paragrafo, custo, new Contador());
        int[] dele = metodo.quebras(paragrafo, custo, new Contador());

        String invalida = porQueENaoServe(dele, paragrafo);

        if (invalida != null)
            return new Veredito(0, 0, certo.length, dele.length, invalida);

        long custoCerto = paragrafo.custoDe(certo, custo);
        long custoDele = paragrafo.custoDe(dele, custo);

        String falha = custoDele == custoCerto ? null
                : "custo " + custoDele + " contra " + custoCerto;

        return new Veredito(custoCerto, custoDele, certo.length, dele.length, falha);
    }

    /**
     * Por que esta quebra nao e uma quebra valida, ou nulo se ela e.
     *
     * <p>Esta conferencia e separada do custo de proposito. Uma quebra invalida nao e uma
     * quebra cara: ela nao e uma resposta. Misturar as duas coisas faria um metodo que
     * estoura a largura parecer so um pouco pior.
     */
    public static String porQueENaoServe(int[] inicios, Paragrafo paragrafo) {
        if (paragrafo.palavras() == 0) return inicios.length == 0 ? null : "paragrafo vazio";

        if (inicios.length == 0) return "nenhuma linha num paragrafo com palavras";
        if (inicios[0] != 0) return "a primeira linha nao comeca na palavra zero";

        for (int linha = 0; linha < inicios.length; linha++) {
            int de = inicios[linha];
            int ate = linha + 1 < inicios.length ? inicios[linha + 1] : paragrafo.palavras();

            if (ate <= de) return "a linha " + linha + " esta vazia ou fora de ordem";

            if (!paragrafo.cabe(de, ate))
                return "a linha " + linha + " ocupa " + paragrafo.larguraDaLinha(de, ate)
                        + " numa largura de " + paragrafo.largura();
        }

        return null;
    }

    /**
     * Quanto o metodo perde para o otimo, em por cento.
     *
     * <p>Com o otimo em zero, qualquer coisa a mais seria uma divisao por zero, e por isso a
     * funcao devolve zero quando os dois empatam em zero. Isso acontece de verdade: com
     * palavras todas iguais e largura redonda, nao sobra espaco em linha nenhuma.
     */
    public static double quantoPerde(long custoDele, long custoOtimo) {
        if (custoOtimo == 0) return custoDele == 0 ? 0 : Double.POSITIVE_INFINITY;

        return 100.0 * (custoDele - custoOtimo) / custoOtimo;
    }
}
