# language: pt
Funcionalidade: Controle financeiro de honorarios
  Para cobrar corretamente pelo trabalho no processo, com parcelamento e controle de inadimplencia
  Como advogado responsavel pelos autos
  Quero contratar honorarios parcelados, registrar pagamentos e apurar o resultado da causa

  Contexto:
    Dado um processo para honorarios financeiros

  Cenario: parcelamento com centavos bate exatamente com o valor contratado
    Quando eu contrato honorario fixo de "1000.00" em 3 parcelas
    Entao a soma das parcelas deve ser igual a "1000.00"
    E a parcela 3 deve valer "333.34"

  Cenario: pagamento parcial deixa a parcela parcialmente paga
    Dado que contratei honorario fixo de "900.00" em 3 parcelas
    Quando eu registro um pagamento de "100.00" na parcela 1
    Entao a parcela 1 deve estar com status "PARCIALMENTE_PAGA"
    E o contrato deve estar com status "PARCIALMENTE_PAGO"

  Cenario: quitar todas as parcelas encerra o contrato como quitado
    Dado que contratei honorario fixo de "900.00" em 3 parcelas
    Quando eu pago integralmente a parcela 1
    E eu pago integralmente a parcela 2
    E eu pago integralmente a parcela 3
    Entao o contrato deve estar com status "QUITADO"

  Cenario: pagamento acima do saldo da parcela e recusado
    Dado que contratei honorario fixo de "900.00" em 3 parcelas
    Quando eu tento registrar um pagamento de "1000.00" na parcela 1
    Entao a operacao deve ser recusada por saldo insuficiente

  Cenario: parcela vencida acumula multa e juros pro rata die
    Dado que contratei honorario fixo de "1000.00" em 1 parcela com vencimento ha 10 dias
    Entao a parcela 1 deve estar com status "EM_ATRASO"
    E a multa da parcela 1 deve ser "20.00"
    E os juros da parcela 1 devem ser maiores que "0"

  Cenario: quota litis com exito calcula o valor final sobre o proveito economico de verdade
    Quando eu contrato honorario quota litis de 20% com causa estimada em "100000.00" em 2 parcelas
    Entao o contrato deve estar aguardando resultado
    Quando eu registro o resultado da causa com valor obtido "150000.00" e sucumbencia "5000.00"
    Entao o valor contratado deve ser "30000.00"
    E o contrato nao deve mais estar aguardando resultado

  Cenario: quota litis sem exito encerra o contrato sem cobranca
    Dado que contratei honorario quota litis de 20% com causa estimada em "100000.00" em 2 parcelas
    Quando eu registro o resultado da causa com valor obtido "0.00" e sucumbencia "0.00"
    Entao o valor contratado deve ser "0.00"
    E o contrato deve estar com status "QUITADO"

  Cenario: honorarios mais sucumbencia acima do valor obtido fere o art. 50 da OAB
    Dado que contratei honorario quota litis de 20% com causa estimada em "100000.00" em 2 parcelas
    Quando eu tento registrar o resultado da causa com valor obtido "10000.00" e sucumbencia "9000.00"
    Entao a operacao deve ser recusada por violar o art. 50 do Codigo de Etica da OAB
