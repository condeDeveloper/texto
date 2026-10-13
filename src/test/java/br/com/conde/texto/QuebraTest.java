package br.com.conde.texto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuebraTest {

    // ───────────────────── o juiz contra a programacao dinamica ─────────────────────

    /**
     * A DINAMICA DEPENDE DE UM ARGUMENTO, e um argumento pode estar errado.
     *
     * <p>A forca bruta nao depende de argumento nenhum: ela experimenta todas as quebras. E
     * por isso que ela e o juiz, e por isso que ela confere a dinamica e nao o contrario.
     */
    @Test
    @DisplayName("a dinamica acha o mesmo custo que a forca bruta, em tudo que cabe testar")
    void aDinamicaBateComAForcaBruta() {
        List<String> falhas = new ArrayList<>();
        int conferidos = 0;

        for (int palavras : new int[] {1, 2, 5, 12, 16})
            for (int largura : new int[] {5, 9, 14, 25})
                for (var tipo : Paragrafos.todos(palavras, 29).entrySet())
                    for (Custo custo : Custo.values()) {
                        int[] larguras = tipo.getValue().clone();

                        for (int i = 0; i < larguras.length; i++)
                            larguras[i] = Math.min(larguras[i], largura);

                        Paragrafo paragrafo = new Paragrafo(larguras, largura);
                        Juiz.Veredito veredito = Juiz.julgar(new Exata(), paragrafo, custo);

                        conferidos++;

                        if (!veredito.passou())
                            falhas.add(tipo.getKey() + " com " + palavras + " palavras, largura "
                                    + largura + ", custo " + custo.nome() + ": "
                                    + veredito.falha());
                    }

        assertThat(conferidos).isGreaterThan(150);
        assertThat(falhas).isEmpty();
    }

    /**
     * O JUIZ CONFERE O CUSTO, e nao a quebra, e isso precisa de um teste proprio.
     *
     * <p>Duas quebras diferentes podem ter exatamente o mesmo custo. Conferir a quebra
     * reprovaria os dois metodos por um motivo que nao tem nada a ver com o problema.
     */
    @Test
    @DisplayName("duas quebras validas do mesmo paragrafo tem custos diferentes, e o juiz compara isso")
    void oJuizComparaOCusto() {
        Paragrafo paragrafo = new Paragrafo(new int[] {3, 3, 3, 3}, 7);

        int[] duasLinhas = {0, 2};
        int[] tresLinhas = {0, 1, 3};

        assertThat(Juiz.porQueENaoServe(duasLinhas, paragrafo))
                .as("as duas sao quebras validas")
                .isNull();

        assertThat(Juiz.porQueENaoServe(tresLinhas, paragrafo)).isNull();

        assertThat(paragrafo.custoDe(duasLinhas, Custo.QUADRADO))
                .as("duas linhas cheias de 3 e 3, a segunda e a ultima e nao custa")
                .isZero();

        assertThat(paragrafo.custoDe(tresLinhas, Custo.QUADRADO))
                .as("tres linhas: a primeira sobra 4 e custa 16, a do meio enche, a ultima e livre")
                .isEqualTo(16);

        assertThat(new Exata().quebras(paragrafo, Custo.QUADRADO, new Contador()))
                .as("e o otimo e a de custo zero")
                .containsExactly(0, 2);
    }

    @Test
    @DisplayName("toda quebra devolvida e valida: cabe, nao perde palavra e nao repete")
    void todaQuebraEValida() {
        for (var tipo : Paragrafos.todos(400, 31).entrySet())
            for (int largura : new int[] {8, 25, 60})
                for (Custo custo : Custo.values())
                    for (var metodo : Paragrafos.osMetodos().entrySet()) {
                        int[] larguras = tipo.getValue().clone();

                        for (int i = 0; i < larguras.length; i++)
                            larguras[i] = Math.min(larguras[i], largura);

                        Paragrafo paragrafo = new Paragrafo(larguras, largura);
                        int[] quebra = metodo.getValue().quebras(paragrafo, custo,
                                new Contador());

                        assertThat(Juiz.porQueENaoServe(quebra, paragrafo))
                                .as(metodo.getKey() + " em " + tipo.getKey() + ", largura "
                                        + largura)
                                .isNull();
                    }
    }

    // ───────────────────── as regras do problema ─────────────────────

    /**
     * A ULTIMA LINHA E DE GRACA, e essa regra muda a resposta inteira.
     *
     * <p>Sem ela, o metodo tentaria encher a ultima linha empurrando palavras das anteriores
     * para tras, e o resultado ficaria absurdo.
     */
    @Test
    @DisplayName("a ultima linha nao custa nada, por mais curta que seja")
    void aUltimaLinhaEDeGraca() {
        Paragrafo paragrafo = new Paragrafo(new int[] {5, 5, 1}, 11);

        assertThat(paragrafo.custoDe(new int[] {0, 2}, Custo.QUADRADO))
                .as("a primeira linha enche exatamente, a ultima tem uma palavra e nao custa")
                .isZero();

        /*
         * E UM PARAGRAFO QUE CABE INTEIRO NUMA LINHA custa zero, por mais que sobre. Aqui
         * sobram seis de onze, que numa linha do meio custariam trinta e seis.
         */
        assertThat(new Paragrafo(new int[] {5}, 11).custoDe(new int[] {0}, Custo.QUADRADO))
                .isZero();
    }

    @Test
    @DisplayName("uma linha que nao cabe e recusada, em vez de contar como cara")
    void aLinhaQueNaoCabeNaoEUmaQuebra() {
        Paragrafo paragrafo = new Paragrafo(new int[] {5, 5}, 8);

        assertThatThrownBy(() -> paragrafo.custoDe(new int[] {0}, Custo.QUADRADO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ocupa 11");

        assertThat(Juiz.porQueENaoServe(new int[] {0}, paragrafo)).contains("ocupa 11");
        assertThat(Juiz.porQueENaoServe(new int[] {0, 1}, paragrafo)).isNull();
    }

    @Test
    @DisplayName("uma palavra maior que a linha e recusada na construcao")
    void palavraMaiorQueALinha() {
        assertThatThrownBy(() -> new Paragrafo(new int[] {3, 11, 3}, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao cabe numa linha");

        assertThatThrownBy(() -> new Paragrafo(new int[] {0}, 10))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Paragrafo(new int[] {1}, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a largura de uma linha soma as palavras mais um espaco entre cada duas")
    void aLarguraDaLinha() {
        Paragrafo paragrafo = new Paragrafo(new int[] {3, 4, 2, 5}, 20);

        assertThat(paragrafo.larguraDaLinha(0, 1)).as("uma palavra, sem espaco").isEqualTo(3);
        assertThat(paragrafo.larguraDaLinha(0, 2)).as("duas, com um espaco").isEqualTo(8);
        assertThat(paragrafo.larguraDaLinha(0, 4)).as("quatro, com tres espacos").isEqualTo(17);
        assertThat(paragrafo.larguraDaLinha(1, 3)).isEqualTo(7);

        assertThat(paragrafo.sobraDa(0, 4)).isEqualTo(3);
        assertThat(paragrafo.cabe(0, 4)).isTrue();
    }

    @Test
    @DisplayName("um paragrafo de uma palavra tem uma linha so, de graca")
    void paragrafoDeUmaPalavra() {
        Paragrafo paragrafo = new Paragrafo(new int[] {4}, 10);

        for (var metodo : Paragrafos.osMetodos().entrySet()) {
            int[] quebra = metodo.getValue().quebras(paragrafo, Custo.QUADRADO, new Contador());

            assertThat(quebra).as(metodo.getKey()).containsExactly(0);
            assertThat(paragrafo.custoDe(quebra, Custo.QUADRADO)).isZero();
        }
    }

    // ───────────────────── o que cada metodo promete sobre si ─────────────────────

    /**
     * O GULOSO NUNCA USA MAIS LINHAS QUE O OTIMO, e isso vale sempre.
     *
     * <p>Encher cada linha ao maximo e exatamente a estrategia que minimiza o numero de
     * linhas. Nao e uma observacao sobre estes dados; e sobre o metodo.
     */
    @Test
    @DisplayName("o guloso nunca usa mais linhas que o otimo, em nenhuma entrada")
    void oGulosoMinimizaAsLinhas() {
        for (var tipo : Paragrafos.todos(600, 37).entrySet())
            for (int largura : new int[] {8, 15, 30, 60})
                for (Custo custo : Custo.values()) {
                    int[] larguras = tipo.getValue().clone();

                    for (int i = 0; i < larguras.length; i++)
                        larguras[i] = Math.min(larguras[i], largura);

                    Paragrafo paragrafo = new Paragrafo(larguras, largura);

                    int gulosas = new Gulosa().quebras(paragrafo, custo, new Contador()).length;
                    int otimas = new Exata().quebras(paragrafo, custo, new Contador()).length;

                    assertThat(gulosas)
                            .as(tipo.getKey() + ", largura " + largura + ", " + custo.nome())
                            .isLessThanOrEqualTo(otimas);
                }
    }

    @Test
    @DisplayName("o otimo nunca custa mais que o guloso, que e o que otimo quer dizer")
    void oOtimoNuncaCustaMais() {
        for (var tipo : Paragrafos.todos(600, 41).entrySet())
            for (int largura : new int[] {8, 15, 30, 60})
                for (Custo custo : Custo.values()) {
                    int[] larguras = tipo.getValue().clone();

                    for (int i = 0; i < larguras.length; i++)
                        larguras[i] = Math.min(larguras[i], largura);

                    Paragrafo paragrafo = new Paragrafo(larguras, largura);

                    long guloso = paragrafo.custoDe(
                            new Gulosa().quebras(paragrafo, custo, new Contador()), custo);

                    long otimo = paragrafo.custoDe(
                            new Exata().quebras(paragrafo, custo, new Contador()), custo);

                    assertThat(otimo)
                            .as(tipo.getKey() + ", largura " + largura + ", " + custo.nome())
                            .isLessThanOrEqualTo(guloso);
                }
    }

    /**
     * COM PALAVRAS TODAS IGUAIS O GULOSO E OTIMO, e isso explica o resto.
     *
     * <p>Ele erra por nao olhar adiante; quando nao ha nada adiante que importe, ele nao
     * erra.
     */
    @Test
    @DisplayName("com palavras todas do mesmo tamanho, o guloso empata com o otimo")
    void comPalavrasIguaisOGulosoEOtimo() {
        for (int tamanho : new int[] {1, 3, 5, 9})
            for (int largura : new int[] {10, 23, 40, 71}) {
                if (tamanho > largura) continue;

                Paragrafo paragrafo = new Paragrafo(
                        Paragrafos.todasIguais(3_000, tamanho), largura);

                long guloso = paragrafo.custoDe(
                        new Gulosa().quebras(paragrafo, Custo.QUADRADO, new Contador()),
                        Custo.QUADRADO);

                long otimo = paragrafo.custoDe(
                        new Exata().quebras(paragrafo, Custo.QUADRADO, new Contador()),
                        Custo.QUADRADO);

                assertThat(guloso).as("palavras de " + tamanho + " numa linha de " + largura)
                        .isEqualTo(otimo);
            }
    }

    @Test
    @DisplayName("a forca bruta recusa paragrafo grande em vez de rodar para sempre")
    void aForcaBrutaTemTeto() {
        Paragrafo grande = new Paragrafo(Paragrafos.todasIguais(21, 3), 10);

        assertThatThrownBy(() -> new ForcaBruta().quebras(grande, Custo.QUADRADO,
                new Contador()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vai ate 20 palavras");

        assertThat(ForcaBruta.MAXIMO_DE_PALAVRAS).isEqualTo(20);
    }

    /**
     * A BUSCA PELO PIOR CASO ACHA UM PARAGRAFO QUE A MINHA CONSTRUCAO NAO ACHOU.
     *
     * <p>Eu construi um paragrafo para derrubar o guloso e ele empatou. A busca percorre
     * todas as combinacoes e devolve a pior que existe naquele espaco.
     */
    @Test
    @DisplayName("a busca acha um paragrafo em que o guloso custa mais que o dobro do otimo")
    void aBuscaAchaOPiorCaso() {
        int[] padrao = Paragrafos.piorContraOGuloso(6, 6, 8, Custo.QUADRADO);

        Paragrafo repetido = new Paragrafo(Paragrafos.repetido(padrao, 200), 8);

        long guloso = repetido.custoDe(
                new Gulosa().quebras(repetido, Custo.QUADRADO, new Contador()), Custo.QUADRADO);

        long otimo = repetido.custoDe(
                new Exata().quebras(repetido, Custo.QUADRADO, new Contador()), Custo.QUADRADO);

        assertThat(guloso).isGreaterThan(otimo * 2);

        assertThat(padrao).hasSize(6);
        for (int largura : padrao) assertThat(largura).isBetween(1, 6);
    }

    // ───────────────────── o sorteio da casa ─────────────────────

    /**
     * O TESTE DO DESLOCAMENTO DE 32.
     *
     * <p>As larguras sao restos por um numero pequeno, ou seja, bits baixos. Com bits ruins
     * elas sairiam num ciclo curto, o paragrafo viraria uma repeticao e o guloso pareceria
     * otimo por um motivo que nao e dele.
     */
    @Test
    @DisplayName("o deslocamento de 32 e o que impede as larguras de cairem num ciclo curto")
    void oDeslocamentoDeTrintaEDois() {
        int[] larguras = Paragrafos.sorteadas(60_000, 1, 6, 1);
        int[] quantas = new int[7];

        for (int largura : larguras) quantas[largura]++;

        for (int largura = 1; largura <= 6; largura++)
            assertThat(quantas[largura]).as("largura " + largura).isBetween(9_600, 10_400);

        long estado = 1;
        int alternancias = 0;
        int anterior = -1;

        for (int i = 0; i < 1_000; i++) {
            estado = estado * 6364136223846793005L + 1442695040888963407L;

            int baixo = (int) (estado & 1);

            if (anterior >= 0 && baixo != anterior) alternancias++;

            anterior = baixo;
        }

        assertThat(alternancias)
                .as("o bit baixo do estado cru alterna em TODAS: isso e um ciclo de dois")
                .isEqualTo(999);
    }

    @Test
    @DisplayName("o texto de verdade tem a forma certa: muita palavra curta e uma cauda")
    void oTextoDeVerdadeTemAFormaCerta() {
        int[] larguras = Paragrafos.comoTextoDeVerdade(100_000, 1);
        int[] quantas = new int[20];
        double soma = 0;

        for (int largura : larguras) {
            quantas[largura]++;
            soma += largura;
        }

        assertThat(soma / larguras.length)
                .as("a media fica perto de cinco letras, como em portugues escrito")
                .isBetween(4.5, 5.5);

        assertThat(quantas[3]).as("o pico esta em tres letras")
                .isGreaterThan(quantas[1])
                .isGreaterThan(quantas[8]);

        assertThat(quantas[15]).as("e ha cauda ate quinze").isGreaterThan(0);
        assertThat(quantas[1]).isGreaterThan(0);
    }
}
