# language: pt
Funcionalidade: Ciclo de vida do prazo
  Para que o vencimento acompanhe o que acontece nos autos
  Como advogado responsavel pelo processo
  Quero suspender, retomar, dilatar, cancelar e reabrir prazos, com cada fato gravado na trilha do prazo

  Contexto:
    Dado um processo para o ciclo de vida do prazo
    E que hoje e "2026-09-16"

  Cenario: suspensao e retomada restituem exatamente o saldo que faltava (CPC art. 221)
    Dado um prazo fatal de 5 dias uteis intimado em "2026-09-04" no ciclo de vida
    Quando o prazo e suspenso desde "2026-09-10" por "Convencao das partes (art. 313, II)"
    Entao a situacao do prazo deve ser "SUSPENSO"
    E o saldo congelado deve ser de 3 dias
    Quando o prazo e retomado em "2026-09-16"
    Entao o vencimento vigente deve ser "2026-09-18"
    E o vencimento original deve continuar "2026-09-14"
    E a trilha do prazo deve ter os eventos "ABERTURA, SUSPENSAO, RETOMADA"

  Cenario: varredura ignora prazo suspenso e grava a execucao mesmo sem alerta
    Dado um prazo fatal de 5 dias uteis intimado em "2026-09-04" no ciclo de vida
    E o prazo e suspenso desde "2026-09-10" por "Convencao das partes"
    Quando a varredura manual roda em "2026-09-11" pedida por "Rafael Torres"
    Entao nenhum alerta deve ser emitido no ciclo de vida
    E a ultima varredura registrada deve ter avaliado 0 prazos e emitido 0 alertas
    E a ultima varredura registrada deve ter sido pedida por "Rafael Torres"

  Cenario: alerta emitido vai para a trilha e a dilacao reinicia a regua
    Dado que hoje e "2026-09-09"
    E um prazo fatal de 5 dias uteis intimado em "2026-09-04" no ciclo de vida
    E a varredura manual roda em "2026-09-09" pedida por "Ana Souza"
    Quando o prazo e dilatado em 5 dias por "Decisao de 09/09/2026 (art. 139, VI)"
    Entao o vencimento vigente deve ser "2026-09-21"
    E o prazo nao deve ter alertas emitidos
    E a trilha do prazo deve ter os eventos "ABERTURA, ALERTA, DILACAO"

  Cenario: dilacao depois de encerrado o prazo regular e recusada (CPC art. 139, paragrafo unico)
    Dado que hoje e "2026-09-18"
    E um prazo fatal de 5 dias uteis intimado em "2026-09-04" no ciclo de vida
    Quando eu tento dilatar o prazo em 3 dias por "Pedido tardio"
    Entao a operacao no prazo deve ser recusada com a mensagem contendo "139"
    E a trilha do prazo deve ter os eventos "ABERTURA"

  Cenario: prazo em dobro para a Fazenda Publica (CPC art. 183)
    Dado um prazo fatal de 5 dias uteis intimado em "2026-09-04" com prerrogativa "FAZENDA_PUBLICA"
    Entao o vencimento vigente deve ser "2026-09-21"

  Cenario: so o responsavel ou o chefe cancelam e reabrem o prazo
    Dado um prazo fatal de 5 dias uteis intimado em "2026-09-04" no ciclo de vida
    Quando o advogado de OAB "PE99999" tenta cancelar o prazo por "Engano"
    Entao a operacao no prazo deve ser recusada com a mensagem contendo "responsável"
    Quando o responsavel cancela o prazo por "Lancado em duplicidade"
    Entao a situacao do prazo deve ser "CANCELADO"
    Quando o chefe reabre o prazo por "Cancelado por engano"
    Entao a situacao do prazo deve ser "EM_ABERTO"
    E a trilha do prazo deve ter os eventos "ABERTURA, CANCELAMENTO, REABERTURA"
