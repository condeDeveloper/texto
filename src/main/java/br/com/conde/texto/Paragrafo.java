package br.com.conde.texto;

/**
 * UM PARAGRAFO: as larguras das palavras e a largura da linha.
 *
 * <p>Larguras e nao palavras. O problema de quebrar linha nao olha o que esta escrito, so
 * quanto cada pedaco ocupa, e guardar as palavras aqui faria as medidas parecerem depender
 * do idioma quando elas dependem so da distribuicao dos tamanhos.
 *
 * <p>Entre duas palavras da mesma linha ha UM espaco, de largura um. Entao a linha que vai
 * da palavra i ate a j-1 ocupa a soma das larguras mais j-i-1.
 */
public final class Paragrafo {

    private final int[] larguras;
    private final int[] somaAcumulada;
    private final int largura;

    public Paragrafo(int[] larguras, int largura) {
        if (largura < 1) throw new IllegalArgumentException("largura de pelo menos um");

        for (int l : larguras) {
            if (l < 1) throw new IllegalArgumentException("palavra de largura " + l);

            /*
             * UMA PALAVRA MAIOR QUE A LINHA NAO TEM QUEBRA POSSIVEL, e isso precisa ser uma
             * recusa e nao um resultado estranho. Um metodo que aceitasse devolveria uma
             * linha mais larga que o permitido, e todas as medidas de sobra ficariam
             * negativas sem ninguem notar.
             */
            if (l > largura)
                throw new IllegalArgumentException(
                        "a palavra de largura " + l + " nao cabe numa linha de " + largura);
        }

        this.larguras = larguras.clone();
        this.largura = largura;
        this.somaAcumulada = new int[larguras.length + 1];

        for (int i = 0; i < larguras.length; i++)
            somaAcumulada[i + 1] = somaAcumulada[i] + larguras[i];
    }

    public int palavras() {
        return larguras.length;
    }

    public int largura() {
        return largura;
    }

    public int larguraDa(int palavra) {
        return larguras[palavra];
    }

    /** Quanto a linha que vai da palavra de ate a palavra ate-1 ocupa, com os espacos. */
    public int larguraDaLinha(int de, int ate) {
        return somaAcumulada[ate] - somaAcumulada[de] + (ate - de - 1);
    }

    public boolean cabe(int de, int ate) {
        return larguraDaLinha(de, ate) <= largura;
    }

    /** Quanto sobra na linha. Negativo quer dizer que ela nao cabe. */
    public int sobraDa(int de, int ate) {
        return largura - larguraDaLinha(de, ate);
    }

    /**
     * O custo de uma quebra inteira, dada como as posicoes em que cada linha comeca.
     *
     * <p>Esta funcao e o arbitro de todas as outras: ela nao sabe quem produziu a quebra, so
     * soma. E e por isso que ela recusa uma quebra invalida em vez de dar um numero ruim: uma
     * quebra que nao cabe nao e uma quebra cara, e nao e uma quebra.
     */
    public long custoDe(int[] inicios, Custo custo) {
        if (inicios.length == 0 || inicios[0] != 0)
            throw new IllegalArgumentException("a primeira linha comeca na palavra zero");

        long total = 0;

        for (int linha = 0; linha < inicios.length; linha++) {
            int de = inicios[linha];
            int ate = linha + 1 < inicios.length ? inicios[linha + 1] : larguras.length;

            if (ate <= de) throw new IllegalArgumentException("linha vazia ou fora de ordem");

            if (!cabe(de, ate))
                throw new IllegalArgumentException(
                        "a linha " + linha + " ocupa " + larguraDaLinha(de, ate)
                                + " numa largura de " + largura);

            /* A ULTIMA LINHA E DE GRACA, por mais curta que seja. */
            if (linha + 1 == inicios.length) continue;

            total += custo.de(sobraDa(de, ate));
        }

        return total;
    }
}
