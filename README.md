# texto

Quebra de linha do zero em Java 21: guloso contra ótimo por programação dinâmica, com a força
bruta como juiz.

O juiz é a **força bruta**, e aqui ela é o juiz certo por um motivo específico: a programação
dinâmica depende de um **argumento** sobre a estrutura do problema, e um argumento pode estar
errado. A força bruta não depende de argumento nenhum — ela experimenta todas as 2^(n−1)
quebras e fica com a mais barata.

E ela confere o **custo**, não a quebra. Duas quebras diferentes podem ter exatamente o mesmo
custo, e aí a dinâmica escolhe a que o laço achou primeiro e a força bruta escolhe a de menor
máscara. Conferir a quebra reprovaria as duas por um motivo que não tem nada a ver com o
problema.

Este repositório é diferente dos outros: aqui os métodos **não devolvem a mesma resposta**. O
guloso devolve uma quebra pior, e isso não é um defeito dele, é o que ele é. Então há duas
medidas de naturezas diferentes: o **trabalho** gasto para achar a quebra e a **feiura** da
quebra achada.

## Quanto o guloso perde

```
$ mvn exec:java -Dexec.args="feiura"

  entrada                 largura     custo guloso      custo otimo     quanto perde  linhas guloso   linhas otimo
  texto de verdade             20            94557            76205           24.08%           6298           6298
  texto de verdade             70            23515            18825           24.91%           1671           1671
  texto de verdade            120            14719            11297           30.29%            965            965
  todas iguais                 20            59994            59994            0.00%           6667           6667
  todas iguais                120              999              999            0.00%           1000           1000
  sorteadas 1 a 20             20           596306           560914            6.31%          14348          14348
  sorteadas 1 a 20            120           146973           121145           21.32%           2019           2019
```

Eu escrevi que a resposta seria *"pouco, poucos por cento"*. São **de 24% a 30%** em texto
corrido, em todas as larguras.

E há uma linha em que ele empata exatamente: com todas as palavras do mesmo tamanho, **0,00%**
em todas as larguras. Ali não há escolha a fazer, e o guloso não erra por não olhar adiante
porque não há nada adiante que importe. A direção que eu previ, essa valeu: quanto mais os
tamanhos variam, mais ele perde.

## Como rodar

```
mvn test
mvn exec:java -Dexec.args="tudo"
```

Os comandos do medidor são `correcao`, `feiura`, `pior`, `linhas`, `custo`, `trabalho` e
`tudo`.

## O que as medidas me corrigiram

**O parágrafo que eu construí para derrubar o guloso não derrubou. Então eu mandei procurar.**

A ideia era: palavras curtas que enchem a linha exatamente, e logo depois uma palavra comprida
que não cabe mais e vai sozinha deixando um buraco. O guloso empatou com o ótimo em **todas**
as larguras, 0,00%.

O motivo é óbvio depois de medido: uma palavra **comprida** sozinha numa linha não deixa
buraco nenhum, porque ela é comprida. Eu tinha desenhado o adversário errado.

Então a tabela passou a **procurar** em vez de adivinhar: percorrer todas as combinações de
larguras até um teto, rodar os dois métodos em cada uma, e ficar com a que o guloso perde por
mais.

```
$ mvn exec:java -Dexec.args="pior"

   largura padrao achado                custo guloso    custo otimo   quanto perde sobra gulosa  sobra otima
         8 [1, 4, 1, 4, 3, 2, 6]               11996           4796        150.13%            6            2
        10 [1, 6, 1, 6, 3, 4, 6]               13192           8384         57.35%            6            4
        12 [1, 7, 2, 6, 5, 3, 9]               25795          10791        139.04%            9            3
```

**150%**, e numa estrutura que eu não teria inventado: uma palavra de 1, uma de 4, outra de 1,
outra de 4. O guloso deixa uma linha com **seis das oito** colunas vazias; o ótimo não deixa
nenhuma pior que dois.

O resultado não é um exemplo que eu imaginei e que pode estar errado: é o pior que existe
naquele espaço de parágrafos.

**O ótimo não usa mais linhas que o guloso, e eu escrevi que usaria.**

O guloso enche cada linha ao máximo, que é exatamente a estratégia que minimiza o **número** de
linhas. Então ele nunca usa mais que o ótimo; a pergunta era se o ótimo usa mais que ele.

Eu disse que sim, com um argumento que parecia sólido: a última linha é de graça, então abrir
uma linha a mais joga sobra para onde ela não custa nada.

```
$ mvn exec:java -Dexec.args="linhas"

  entrada                 largura    linhas guloso     linhas otimo       a mais
  texto de verdade             20              625              625            0
  texto de verdade             70              167              167            0
  sorteadas 1 a 20             70              360              360            0
```

Zero nas nove combinações. O argumento esquecia o outro lado: abrir uma linha a mais também
espalha sobra por **todas** as linhas anteriores, e o custo quadrático cobra isso muito mais do
que a última linha devolve.

Uma linha a mais aparece, e aparece uma vez: na tabela do custo, com a função **cúbica**, que
odeia a linha muito curta o bastante para pagar por ela. Então o argumento não estava errado de
todo — estava errado sobre o quadrado.

## A função de custo muda a resposta

```
$ mvn exec:java -Dexec.args="custo"

  custo            linhas       pela propria      pelo quadrado    maior sobra candidatas por palavra
  quadrado           1671              18825              18825              9                  13.16
  cubo               1672              87532              19036              9                  13.16
  linear             1671               4727              25447             43                  13.16
```

A coluna do meio é a prova de que as três quebras são **diferentes**: ela mede as três com a
mesma régua, a do quadrado. Se as quebras fossem iguais, os três números seriam iguais.

E a coluna da direita explica por que a função linear é a que menos serve: ela deixa uma linha
com **43 das 70 colunas vazias**, contra 9 do quadrado, usando o mesmo número de linhas. A soma
das sobras é quase a mesma em toda quebra com o mesmo número de linhas, então a linear não
distingue **onde** o branco fica.

## Três curvas numa coluna só

```
$ mvn exec:java -Dexec.args="trabalho"

    palavras metodo                 candidatas            por palavra
          16 gulosa                         15                   0.94
          16 otima                         136                   8.50
          16 forca bruta                278528               17408.00
          20 forca bruta               5505024              275251.20
       20000 gulosa                      19999                   1.00
       20000 otima                      263187                  13.16
```

O guloso examina uma candidata por palavra. A dinâmica examina **13,16**, e esse número não
cresce com o tamanho do parágrafo: ela para na primeira candidata que não cabe, e quantas
cabem depende da **largura da linha**, não do tamanho do texto. Ela é quadrática no pior caso e
linear na prática.

A força bruta sai da tabela em vinte palavras porque cada palavra a mais **dobra** o número
dela: com vinte ela já custa vinte e sete mil vezes a dinâmica.

## O que não tem aqui

Sem hifenização, sem espaços elásticos e sem penalidade por quebrar em lugares ruins. O TeX faz
as três, e elas mudariam os números sem mudar nenhuma das conclusões: o guloso continuaria sem
olhar adiante, e a dinâmica continuaria sendo a mesma recorrência com um custo de linha
diferente.

E a última linha é de graça em todas as funções. Sem essa regra o resultado ficaria absurdo: o
método tentaria encher a última linha empurrando palavras das anteriores para trás.

## Licença

MIT.
