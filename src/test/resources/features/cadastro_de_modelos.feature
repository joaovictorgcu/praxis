# language: pt
Funcionalidade: Cadastro de modelos de documento
  Para nao depender de programador a cada peca nova do escritorio
  Como advogado responsavel pelos autos
  Quero cadastrar modelos de peca e gerar documento a partir deles

  Contexto:
    Dado um processo "0001234-56.2026.8.17.0001" do cliente "Construtora Alfa Ltda." na comarca de "Recife"
    E o advogado responsavel "Ana Souza" com e-mail "ana@praxis.adv.br" e OAB "PE12345"

  Cenario: peca gerada por modelo mantem o esqueleto da peca compilada
    Dado o modelo cadastrado:
      | codigo  | COBRANCA_TESTE                                  |
      | nome    | Cobranca de aluguel                             |
      | tipo    | PETICAO_INICIAL                                 |
      | corpo   | DOS FATOS O locatario deve {{valorDivida}}.     |
      | pedidos | DOS PEDIDOS a) o pagamento de {{valorDivida}}.  |
      | juizo   | sim                                             |
    Quando eu gero a peca pelo modelo "COBRANCA_TESTE" com os campos:
      | valorDivida | R$ 12.500,00 |
    Entao a peca gerada deve conter "EXCELENTISSIMO SENHOR DOUTOR JUIZ DE DIREITO DA COMARCA DE RECIFE"
    E a peca gerada deve conter "Termos em que pede deferimento."
    E a peca gerada deve conter "OAB PE12345"
    E a peca gerada deve conter "O locatario deve R$ 12.500,00."
    E a peca deve ficar registrada nos autos do processo

  Cenario: dados dos autos entram na peca sem o usuario digitar
    Dado o modelo cadastrado:
      | codigo  | AUTOS_TESTE                                  |
      | nome    | Modelo com dados dos autos                   |
      | tipo    | PETICAO_INICIAL                              |
      | corpo   | Cliente {{cliente}} na comarca {{comarca}}.  |
      | pedidos | Processo {{processo}} por {{advogado}}.      |
      | juizo   | sim                                          |
    Quando eu gero a peca pelo modelo "AUTOS_TESTE" sem informar campos
    Entao a peca gerada deve conter "Cliente Construtora Alfa Ltda. na comarca Recife."
    E a peca gerada deve conter "Processo 0001234-56.2026.8.17.0001 por Ana Souza."

  Cenario: campo do modelo nao informado vira marcador visivel na peca
    Dado o modelo cadastrado:
      | codigo  | LACUNA_TESTE                          |
      | nome    | Modelo com lacuna                     |
      | tipo    | PETICAO_INICIAL                       |
      | corpo   | Imovel situado em {{enderecoImovel}}. |
      | pedidos | DOS PEDIDOS a) a procedencia.         |
      | juizo   | sim                                   |
    Quando eu gero a peca pelo modelo "LACUNA_TESTE" sem informar campos
    Entao a peca gerada deve conter "(enderecoImovel a preencher)"

  Cenario: modelo que nao vai a juizo troca o enderecamento
    Dado o modelo cadastrado:
      | codigo  | ACORDO_TESTE                       |
      | nome    | Acordo extrajudicial               |
      | tipo    | PECA_AVULSA                        |
      | titulo  | Instrumento particular de acordo   |
      | corpo   | As partes ajustam a composicao.    |
      | pedidos | CLAUSULAS a) quitacao reciproca.   |
      | juizo   | nao                                |
    Quando eu gero a peca pelo modelo "ACORDO_TESTE" sem informar campos
    Entao a peca gerada deve conter "INSTRUMENTO PARTICULAR DE ACORDO"
    E a peca gerada deve conter "firma o presente instrumento."
    E a peca gerada nao deve conter "EXCELENTISSIMO"
    E a peca gerada nao deve conter "Vossa Excelencia"
    E a peca gerada nao deve conter "pede deferimento"
    E o tipo da peca gerada deve ser "PECA_AVULSA"

  Cenario: geracao sem modelo continua usando a peca compilada
    Quando eu gero a peca "PETICAO_INICIAL" com os campos:
      | fatos | inadimplemento contratual |
    Entao a peca gerada deve conter "DO VALOR DA CAUSA"
    E a peca gerada deve conter "inadimplemento contratual"

  Cenario: peca avulsa sem modelo e recusada, porque nao tem gerador compilado
    Quando eu tento gerar a peca "PECA_AVULSA" sem modelo
    Entao a geracao deve ser recusada por falta de gerador

  Cenario: codigo de modelo repetido e recusado
    Dado o modelo cadastrado:
      | codigo  | DUPLICADO_TESTE               |
      | nome    | Primeiro modelo               |
      | tipo    | PETICAO_INICIAL               |
      | corpo   | Corpo do primeiro modelo.     |
      | pedidos | DOS PEDIDOS a) a procedencia. |
      | juizo   | sim                           |
    Quando eu tento cadastrar outro modelo com o codigo "DUPLICADO_TESTE"
    Entao o cadastro do modelo deve ser recusado

  Cenario: remover modelo impede novas geracoes por ele
    Dado o modelo cadastrado:
      | codigo  | REMOVIVEL_TESTE               |
      | nome    | Modelo removivel              |
      | tipo    | PETICAO_INICIAL               |
      | corpo   | Corpo do modelo removivel.    |
      | pedidos | DOS PEDIDOS a) a procedencia. |
      | juizo   | sim                           |
    Quando eu removo o modelo cadastrado
    E eu tento gerar a peca pelo modelo "REMOVIVEL_TESTE"
    Entao a geracao deve ser recusada por modelo inexistente
