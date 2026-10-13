package br.com.conde.texto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Os numeros do README, afirmados.
 *
 * <p>Um README e texto: ele nao quebra quando deixa de ser verdade. Cada afirmacao das
 * tabelas esta repetida aqui para quebrar.
 */
class MedidasTest {

    /* As tabelas sao calculadas uma vez so: uma delas percorre meio milhao de paragrafos. */
    private static final List<Medidas.LinhaFeiura> FEIURA = Medidas.aFeiura();
    private static final List<Medidas.LinhaPior> PIOR = Medidas.oPior();
    private static final List<Medidas.LinhaLinhas> LINHAS = Medidas.asLinhas();
    private static final List<Medidas.LinhaCusto> CUSTO = Medidas.oCusto();
    private static final List<Medidas.LinhaTrabalho> TRABALHO = Medidas.oTrabalho();

    // ───────────────────── 1. a correcao ─────────────────────

    @Test
    @DisplayName("a programacao dinamica nunca diverge da forca bruta")
    void aCorrecaoNaoDiverge() {
        List<Medidas.LinhaCorrecao> linhas = Medidas.aCorrecao();

        assertThat(linhas).hasSize(9);
        assertThat(linhas).allSatisfy(l -> {
            assertThat(l.divergencias()).as(l.entrada() + " com custo " + l.custo()).isZero();
            assertThat(l.paragrafos()).isEqualTo(4);
        });
    }

    // ───────────────────── 2. a feiura ─────────────────────

    /**
     * A CORRECAO: nao sao poucos por cento.
     *
     * <p>Eu escrevi que o guloso ficaria a poucos por cento do otimo em texto corrido. Sao
     * de 24% a 30%, em todas as larguras medidas.
     */
    @Test
    @DisplayName("em texto corrido o guloso perde de 24% a 30%, e nao poucos por cento")
    void oGulosoPerdeVinteEQuatroPorCento() {
        for (int largura : new int[] {20, 40, 70, 120})
            assertThat(feiura("texto de verdade", largura).quantoPerde())
                    .as("largura " + largura)
                    .isBetween(24.0, 31.0);

        assertThat(feiura("texto de verdade", 20).quantoPerde()).isCloseTo(24.08, within(0.01));
        assertThat(feiura("texto de verdade", 120).quantoPerde()).isCloseTo(30.29, within(0.01));
    }

    /**
     * E HA UMA LINHA EM QUE ELE EMPATA EXATAMENTE, que explica todas as outras.
     *
     * <p>Com palavras todas do mesmo tamanho nao ha escolha a fazer, e o guloso nao erra por
     * nao olhar adiante porque nao ha nada adiante que importe.
     */
    @Test
    @DisplayName("com palavras todas iguais o guloso empata em todas as larguras")
    void comPalavrasIguaisEleEmpata() {
        for (int largura : new int[] {20, 40, 70, 120}) {
            assertThat(feiura("todas iguais", largura).quantoPerde())
                    .as("largura " + largura)
                    .isZero();

            assertThat(feiura("todas iguais", largura).custoGuloso())
                    .isEqualTo(feiura("todas iguais", largura).custoOtimo());
        }
    }

    @Test
    @DisplayName("quanto mais os tamanhos variam, mais o guloso perde")
    void quantoMaisVariaMaisEleErra() {
        assertThat(feiura("todas iguais", 70).quantoPerde()).isZero();

        assertThat(feiura("texto de verdade", 70).quantoPerde())
                .isGreaterThan(feiura("todas iguais", 70).quantoPerde());

        assertThat(feiura("sorteadas 1 a 20", 120).quantoPerde()).isCloseTo(21.32, within(0.01));
    }

    @Test
    @DisplayName("o guloso e o otimo usam o mesmo numero de linhas em todas as doze linhas")
    void asLinhasEmpatamNaTabelaDaFeiura() {
        assertThat(FEIURA).allSatisfy(l ->
                assertThat(l.linhasGuloso())
                        .as(l.entrada() + ", largura " + l.largura())
                        .isEqualTo(l.linhasOtimo()));
    }

    private static Medidas.LinhaFeiura feiura(String entrada, int largura) {
        return FEIURA.stream()
                .filter(l -> l.entrada().equals(entrada) && l.largura() == largura)
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 3. o caso ruim ─────────────────────

    /**
     * O PARAGRAFO QUE EU CONSTRUI NAO DERRUBOU O GULOSO. O procurado derrubou.
     *
     * <p>Com largura 8, o padrao achado por busca faz o guloso custar 150% a mais, e deixar
     * uma linha com seis de oito vazios.
     */
    @Test
    @DisplayName("o pior caso procurado faz o guloso custar 150% a mais")
    void oPiorCasoProcurado() {
        assertThat(pior(8).quantoPerde()).isCloseTo(150.13, within(0.01));
        assertThat(pior(12).quantoPerde()).isCloseTo(139.04, within(0.01));

        assertThat(PIOR).allSatisfy(l ->
                assertThat(l.quantoPerde())
                        .as("largura " + l.largura() + ": a busca sempre acha algo ruim")
                        .isGreaterThan(50.0));
    }

    /**
     * E A SOBRA DIZ O QUE ACONTECEU melhor do que o custo.
     *
     * <p>O guloso deixa uma linha com seis de oito colunas vazias; o otimo nao deixa
     * nenhuma pior que dois.
     */
    @Test
    @DisplayName("no pior caso o guloso deixa uma linha tres quartos vazia")
    void aSobraDizOQueAconteceu() {
        assertThat(pior(8).maiorSobraGulosa()).isEqualTo(6);
        assertThat(pior(8).maiorSobraOtima()).isEqualTo(2);

        assertThat(PIOR).allSatisfy(l ->
                assertThat(l.maiorSobraGulosa())
                        .as("largura " + l.largura())
                        .isGreaterThan(l.maiorSobraOtima()));
    }

    @Test
    @DisplayName("o padrao achado com largura 8 e o que a tabela imprime")
    void oPadraoAchado() {
        assertThat(pior(8).padrao()).isEqualTo("[1, 4, 1, 4, 3, 2, 6]");
        assertThat(pior(10).padrao()).isEqualTo("[1, 6, 1, 6, 3, 4, 6]");
        assertThat(pior(12).padrao()).isEqualTo("[1, 7, 2, 6, 5, 3, 9]");
    }

    private static Medidas.LinhaPior pior(int largura) {
        return PIOR.stream()
                .filter(l -> l.largura() == largura)
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 4. as linhas ─────────────────────

    /**
     * A SEGUNDA CORRECAO: o otimo NAO usa mais linhas que o guloso.
     *
     * <p>Eu escrevi que usaria, porque a ultima linha e de graca. O argumento esquecia que
     * abrir uma linha a mais espalha sobra por todas as anteriores.
     */
    @Test
    @DisplayName("o otimo usa exatamente o mesmo numero de linhas, nas nove combinacoes")
    void oOtimoNaoUsaMaisLinhas() {
        assertThat(LINHAS).hasSize(9);

        assertThat(LINHAS).allSatisfy(l ->
                assertThat(l.aMais())
                        .as(l.entrada() + ", largura " + l.largura())
                        .isZero());
    }

    /**
     * E COM O CUSTO CUBICO ELE USA, por uma linha so.
     *
     * <p>Entao o argumento nao estava errado de todo: ele estava errado sobre o quadrado,
     * que cobra o espalhamento mais do que a ultima linha devolve.
     */
    @Test
    @DisplayName("com o custo cubico o otimo usa uma linha a mais que com o quadrado")
    void comOCubicoEleUsaUmaAMais() {
        assertThat(custo("cubo").linhas()).isEqualTo(custo("quadrado").linhas() + 1);
        assertThat(custo("quadrado").linhas()).isEqualTo(1_671);
        assertThat(custo("linear").linhas()).isEqualTo(1_671);
    }

    // ───────────────────── 5. a funcao de custo ─────────────────────

    /**
     * A COLUNA DO MEIO E A PROVA DE QUE AS TRES QUEBRAS SAO DIFERENTES.
     *
     * <p>Ela mede as tres com a MESMA regua. Se as quebras fossem iguais, os tres numeros
     * seriam iguais.
     */
    @Test
    @DisplayName("as tres funcoes escolhem quebras diferentes, medidas pela mesma regua")
    void asTresFuncoesDiscordam() {
        assertThat(custo("quadrado").peloQuadrado()).isEqualTo(18_825);
        assertThat(custo("cubo").peloQuadrado()).isEqualTo(19_036);
        assertThat(custo("linear").peloQuadrado()).isEqualTo(25_447);

        assertThat(custo("quadrado").peloQuadrado())
                .as("e a do quadrado e a melhor pela regua do quadrado, por definicao")
                .isLessThan(custo("cubo").peloQuadrado())
                .isLessThan(custo("linear").peloQuadrado());

        assertThat(custo("quadrado").pelaPropria())
                .as("cada uma e a melhor pela propria regua")
                .isEqualTo(custo("quadrado").peloQuadrado());
    }

    /**
     * E A LINEAR E A QUE MENOS SERVE, com um numero que diz exatamente por que.
     *
     * <p>Ela deixa uma linha com 43 das 70 colunas vazias. Ela nao distingue onde o branco
     * fica: a soma das sobras e quase a mesma em toda quebra que use o mesmo numero de
     * linhas.
     */
    @Test
    @DisplayName("a funcao linear deixa uma linha com 43 das 70 colunas vazias")
    void aLinearNaoDistingueNada() {
        assertThat(custo("linear").maiorSobra()).isEqualTo(43);
        assertThat(custo("quadrado").maiorSobra()).isEqualTo(9);
        assertThat(custo("cubo").maiorSobra()).isEqualTo(9);

        assertThat(custo("linear").maiorSobra())
                .as("quase cinco vezes a do quadrado, com o mesmo numero de linhas")
                .isGreaterThan(custo("quadrado").maiorSobra() * 4);
    }

    private static Medidas.LinhaCusto custo(String nome) {
        return CUSTO.stream()
                .filter(l -> l.custo().equals(nome))
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 6. o trabalho ─────────────────────

    /**
     * TRES CURVAS NUMA COLUNA SO: linear, quase linear e exponencial.
     */
    @Test
    @DisplayName("o guloso examina uma candidata por palavra, e a dinamica treze")
    void asTresCurvas() {
        assertThat(trabalho(20_000, "gulosa").porPalavra()).isCloseTo(1.00, within(0.01));
        assertThat(trabalho(20_000, "otima").porPalavra()).isCloseTo(13.16, within(0.01));

        assertThat(trabalho(1_000, "otima").porPalavra())
                .as("e a dinamica nao cresce com o tamanho do paragrafo: ela para no que cabe")
                .isCloseTo(trabalho(20_000, "otima").porPalavra(), within(0.2));
    }

    @Test
    @DisplayName("a forca bruta dobra a cada palavra e sai da tabela em vinte")
    void aForcaBrutaDobra() {
        assertThat(trabalho(20, "forca bruta").candidatas()).isEqualTo(5_505_024);
        assertThat(trabalho(16, "forca bruta").candidatas()).isEqualTo(278_528);

        assertThat(trabalho(20, "forca bruta").candidatas()
                / (double) trabalho(16, "forca bruta").candidatas())
                .as("quatro palavras a mais, dezenove vezes mais trabalho")
                .isGreaterThan(19.0);

        assertThat(TRABALHO).filteredOn(l -> l.palavras() > ForcaBruta.MAXIMO_DE_PALAVRAS)
                .allSatisfy(l -> assertThat(l.metodo()).isNotEqualTo("forca bruta"));

        assertThat(trabalho(20, "forca bruta").candidatas()
                / (double) trabalho(20, "otima").candidatas())
                .as("e com vinte palavras ela ja custa vinte e sete mil vezes a dinamica")
                .isGreaterThan(27_000.0);
    }

    private static Medidas.LinhaTrabalho trabalho(int palavras, String metodo) {
        return TRABALHO.stream()
                .filter(l -> l.palavras() == palavras && l.metodo().equals(metodo))
                .findFirst()
                .orElseThrow();
    }
}
