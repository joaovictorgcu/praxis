# language: pt
Funcionalidade: Controle de acesso aos autos sigilosos
  Para proteger o segredo de justica sem travar quem precisa mesmo do acesso
  Como advogado do escritorio
  Quero solicitar, conceder, revogar e auditar o acesso a processos sigilosos

  Contexto:
    Dado um processo em segredo de justica para controle de acesso

  Cenario: solicitar acesso, ser aprovado e ler os autos
    Quando a OAB "PE54321" solicita acesso ao processo com justificativa "preciso acompanhar a audiencia de conciliacao"
    Entao a solicitacao deve estar com status "PENDENTE"
    E o chefe deve ser notificado da nova solicitacao
    Quando o chefe aprova a solicitacao
    Entao a solicitacao deve estar com status "APROVADA"
    E o solicitante deve ser notificado da aprovacao
    E o acesso da OAB "PE54321" ao anexo deve ser permitido

  Cenario: solicitar acesso e ser negado continua barrado
    Quando a OAB "PE54321" solicita acesso ao processo com justificativa "preciso acompanhar a audiencia de conciliacao"
    E o chefe nega a solicitacao com motivo "sem relacao com o processo"
    Entao a solicitacao deve estar com status "NEGADA"
    E o solicitante deve ser notificado da negativa
    E o acesso da OAB "PE54321" ao anexo deve ser negado

  Cenario: acesso temporario expirado nao da mais leitura
    Dado que a OAB "PE99999" tem acesso ao processo que expirou ha 1 dia
    Entao o acesso da OAB "PE99999" ao anexo deve ser negado

  Cenario: acesso temporario ainda valido mostra os dias restantes e permite a leitura
    Dado que a OAB "PE99999" tem acesso ao processo que expira em 5 dias
    Entao faltam 5 dias para o acesso da OAB "PE99999" expirar
    E o acesso da OAB "PE99999" ao anexo deve ser permitido

  Cenario: nao revoga o ultimo acesso ativo do processo
    Quando eu tento revogar o acesso do responsavel pelo processo
    Entao a operacao deve ser recusada por exigir ao menos um acesso ativo

  Cenario: tentativa de leitura negada aparece na auditoria
    Quando eu tento ler o anexo do processo com a OAB "PE99999"
    Entao a tentativa deve constar na auditoria como negada

  Cenario: nao ha duas solicitacoes pendentes da mesma OAB para o mesmo processo
    Dado que a OAB "PE99999" solicitou acesso ao processo com justificativa "preciso acompanhar a audiencia de conciliacao"
    Quando a OAB "PE99999" tenta solicitar acesso de novo ao processo com justificativa "ainda preciso acompanhar a audiencia"
    Entao a operacao deve ser recusada por solicitacao pendente duplicada

  Cenario: download sem login e recusado
    Entao o download do anexo sem login deve ser recusado
