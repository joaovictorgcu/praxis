# language: pt
Funcionalidade: Modulo de honorarios
  Para cobrar corretamente pelo trabalho no processo
  Como advogado responsavel pelos autos
  Quero celebrar um contrato de honorarios na modalidade combinada com o cliente

  Contexto:
    Dado um processo "0001234-56.2026.8.17.0001" do cliente "Construtora Alfa Ltda." na comarca de "Recife"
    E o advogado responsavel "Ana Souza" com e-mail "ana@praxis.adv.br" e OAB "PE12345"

  Cenario: honorario fixo cobra o valor combinado
    Quando eu contrato honorario fixo de "5000.00"
    Entao o valor contratado deve ser "5000.00"
    E a modalidade do contrato deve ser "FIXO"

  Cenario: honorario por hora multiplica o valor da hora pelas horas trabalhadas
    Quando eu contrato honorario por hora de "150.00" com 10 horas trabalhadas
    Entao o valor contratado deve ser "1500.00"
    E a modalidade do contrato deve ser "POR_HORA"

  Cenario: honorario quota litis cobra o percentual do proveito economico
    Quando eu contrato honorario quota litis de 20% sobre causa de "100000.00"
    Entao o valor contratado deve ser "20000.00"
    E a modalidade do contrato deve ser "QUOTA_LITIS"

  Cenario: honorario quota litis acima do limite etico e recusado
    Quando eu tento contratar honorario quota litis de 35% sobre causa de "100000.00"
    Entao a contratacao deve ser recusada por limite etico

  Cenario: contrato ja celebrado nao muda de valor se a base de calculo original for reconsultada
    Dado que eu contratei honorario fixo de "5000.00"
    Entao o contrato deve constar na lista de honorarios do processo

  Cenario: contratar honorario para processo inexistente e recusado
    Quando eu tento contratar honorario fixo de "3000.00" no processo "9999999-99.2026.8.17.0001"
    Entao a contratacao deve ser recusada por processo inexistente
