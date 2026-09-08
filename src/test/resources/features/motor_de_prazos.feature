# language: pt
Funcionalidade: Motor de prazos processuais com alertas
  Para nao perder prazo fatal
  Como advogado responsavel pelos autos
  Quero ser avisado com antecedencia sobre os prazos que estao vencendo

  Contexto:
    Dado um processo "0001234-56.2026.8.17.0001" do cliente "Construtora Alfa Ltda." na comarca de "Recife"
    E o advogado responsavel "Ana Souza" com e-mail "ana@praxis.adv.br" e OAB "PE12345"

  Cenario: prazo fatal a tres dias uteis do vencimento notifica o responsavel
    Dado um prazo fatal "Contestacao" de 5 dias uteis intimado em "2026-09-04"
    Quando a varredura de prazos roda em "2026-09-09"
    Entao o advogado "ana@praxis.adv.br" deve ser notificado com nivel "URGENTE"
    E a notificacao deve informar 3 dias restantes

  Cenario: contagem em dias uteis ignora fim de semana e feriado
    Dado um prazo fatal "Contestacao" de 5 dias uteis intimado em "2026-09-04"
    Entao o vencimento do prazo deve ser "2026-09-14"

  Cenario: o mesmo nivel de alerta nao e reenviado no mesmo dia
    Dado um prazo fatal "Contestacao" de 5 dias uteis intimado em "2026-09-04"
    Quando a varredura de prazos roda em "2026-09-09"
    E a varredura de prazos roda em "2026-09-09"
    Entao o advogado deve receber exatamente 1 notificacao

  Cenario: alerta escalona conforme o prazo se aproxima
    Dado um prazo fatal "Contestacao" de 5 dias uteis intimado em "2026-09-04"
    Quando a varredura de prazos roda em "2026-09-09"
    E a varredura de prazos roda em "2026-09-11"
    Entao os niveis de alerta emitidos devem ser "URGENTE, CRITICO"

  Cenario: prazo cumprido deixa de ser alertado
    Dado um prazo fatal "Contestacao" de 5 dias uteis intimado em "2026-09-04"
    Quando o prazo e cumprido
    E a varredura de prazos roda em "2026-09-11"
    Entao nenhuma notificacao deve ser enviada

  Cenario: prazo comum nao gera alerta na politica padrao do escritorio
    Dado um prazo comum "Manifestacao sobre laudo" de 5 dias uteis intimado em "2026-09-04"
    Quando a varredura de prazos roda em "2026-09-11"
    Entao nenhuma notificacao deve ser enviada

  Cenario: prazo vencido em aberto avisa a perda
    Dado um prazo fatal "Contestacao" de 5 dias uteis intimado em "2026-09-04"
    Quando a varredura de prazos roda em "2026-09-21"
    Entao o advogado "ana@praxis.adv.br" deve ser notificado sobre prazo vencido
