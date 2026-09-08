# language: pt
Funcionalidade: Geracao de documentos por template
  Para nao redigir peca repetitiva na mao
  Como advogado responsavel pelos autos
  Quero gerar peticao inicial, contestacao e procuracao a partir de template

  Contexto:
    Dado um processo "0001234-56.2026.8.17.0001" do cliente "Construtora Alfa Ltda." na comarca de "Recife"
    E o advogado responsavel "Ana Souza" com e-mail "ana@praxis.adv.br" e OAB "PE12345"

  Cenario: gerar peticao inicial com fatos e fundamentos informados
    Quando eu gero a peca "PETICAO_INICIAL" com os campos:
      | fatos       | inadimplemento contratual  |
      | fundamentos | art. 475 do Codigo Civil   |
    Entao a peca gerada deve conter "DOS FATOS"
    E a peca gerada deve conter "inadimplemento contratual"
    E a peca gerada deve conter "EXCELENTISSIMO SENHOR DOUTOR JUIZ DE DIREITO DA COMARCA DE RECIFE"
    E a peca gerada deve conter "OAB PE12345"
    E a peca deve ficar registrada nos autos do processo

  Cenario: procuracao nao se enderaca ao juizo
    Quando eu gero a peca "PROCURACAO" com os campos:
      | poderesEspeciais | receber citacao e dar quitacao |
    Entao a peca gerada deve conter "PROCURACAO AD JUDICIA ET EXTRA"
    E a peca gerada deve conter "receber citacao e dar quitacao"
    E a peca gerada nao deve conter "EXCELENTISSIMO"

  Cenario: contestacao traz preliminares e merito
    Quando eu gero a peca "CONTESTACAO" com os campos:
      | preliminares | ilegitimidade passiva  |
      | merito       | ausencia de dano       |
    Entao a peca gerada deve conter "DAS PRELIMINARES"
    E a peca gerada deve conter "ilegitimidade passiva"

  Cenario: geracao de peca avisa o advogado responsavel
    Quando eu gero a peca "PETICAO_INICIAL" com os campos:
      | fatos | inadimplemento contratual |
    Entao o advogado "ana@praxis.adv.br" deve ser notificado sobre documento gerado

  Cenario: documento em segredo de justica so e lido por quem esta habilitado
    Dado um processo em segredo de justica "0007654-32.2026.8.17.0002" do cliente "M. R. S." na comarca de "Olinda"
    E o advogado responsavel "Bruno Carvalho" com e-mail "bruno@praxis.adv.br" e OAB "PE54321"
    Quando eu gero a peca "PETICAO_INICIAL" com os campos:
      | fatos | materia de familia |
    Entao a leitura da peca pela OAB "PE54321" deve ser permitida
    E a leitura da peca pela OAB "PE99999" deve ser negada
