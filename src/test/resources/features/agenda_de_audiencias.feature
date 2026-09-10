# language: pt
Funcionalidade: Agenda de Audiências
  Como um usuário do sistema jurídico
  Quero gerenciar a agenda de audiências
  Para evitar conflitos de horário e organizar as audiências corretamente

  Cenário: Criar uma audiência válida
    Dado que o sistema está limpo
    Quando eu crio uma audiência com os seguintes dados:
      | campo           | valor                           |
      | numeroProcesso  | Proc. 001/2024                  |
      | nomeParteAutora | João Silva                      |
      | dataHoraInicio  | 2024-09-15T10:00:00             |
      | dataHoraFim     | 2024-09-15T11:00:00             |
      | sala            | Sala 1                          |
      | observacoes     | Audiência inicial               |
    Então a audiência deve ser criada com sucesso
    E a audiência deve estar ativa

  Cenário: Impedir criação de audiência com conflito de horário
    Dado que existe uma audiência cadastrada:
      | campo           | valor                           |
      | numeroProcesso  | Proc. 001/2024                  |
      | nomeParteAutora | João Silva                      |
      | dataHoraInicio  | 2024-09-15T10:00:00             |
      | dataHoraFim     | 2024-09-15T11:00:00             |
      | sala            | Sala 1                          |
    Quando eu tento criar uma audiência em conflito:
      | campo           | valor                           |
      | numeroProcesso  | Proc. 002/2024                  |
      | nomeParteAutora | Maria Santos                    |
      | dataHoraInicio  | 2024-09-15T10:30:00             |
      | dataHoraFim     | 2024-09-15T11:30:00             |
      | sala            | Sala 1                          |
    Então devo receber um erro de conflito de horário
    E a segunda audiência não deve ser criada

  Cenário: Permitir audiências em horários diferentes
    Dado que existe uma audiência cadastrada:
      | campo           | valor                           |
      | numeroProcesso  | Proc. 001/2024                  |
      | nomeParteAutora | João Silva                      |
      | dataHoraInicio  | 2024-09-15T10:00:00             |
      | dataHoraFim     | 2024-09-15T11:00:00             |
      | sala            | Sala 1                          |
    Quando eu crio uma audiência sem conflito:
      | campo           | valor                           |
      | numeroProcesso  | Proc. 002/2024                  |
      | nomeParteAutora | Maria Santos                    |
      | dataHoraInicio  | 2024-09-15T14:00:00             |
      | dataHoraFim     | 2024-09-15T15:00:00             |
      | sala            | Sala 1                          |
    Então a audiência deve ser criada com sucesso
    E o sistema deve conter 2 audiências

  Cenário: Permitir audiências no mesmo horário em salas diferentes
    Dado que existe uma audiência cadastrada:
      | campo           | valor                           |
      | numeroProcesso  | Proc. 001/2024                  |
      | nomeParteAutora | João Silva                      |
      | dataHoraInicio  | 2024-09-15T10:00:00             |
      | dataHoraFim     | 2024-09-15T11:00:00             |
      | sala            | Sala 1                          |
    Quando eu crio uma audiência na mesma hora em sala diferente:
      | campo           | valor                           |
      | numeroProcesso  | Proc. 002/2024                  |
      | nomeParteAutora | Maria Santos                    |
      | dataHoraInicio  | 2024-09-15T10:00:00             |
      | dataHoraFim     | 2024-09-15T11:00:00             |
      | sala            | Sala 2                          |
    Então a audiência deve ser criada com sucesso
    E o sistema deve conter 2 audiências

  Cenário: Consultar audiência por ID
    Dado que existe uma audiência cadastrada:
      | campo           | valor                           |
      | numeroProcesso  | Proc. 001/2024                  |
      | nomeParteAutora | João Silva                      |
      | dataHoraInicio  | 2024-09-15T10:00:00             |
      | dataHoraFim     | 2024-09-15T11:00:00             |
      | sala            | Sala 1                          |
    Quando eu consulto a audiência por ID
    Então devo obter os dados da audiência
    E o número do processo deve ser "Proc. 001/2024"

  Cenário: Editar uma audiência válida
    Dado que existe uma audiência cadastrada:
      | campo           | valor                           |
      | numeroProcesso  | Proc. 001/2024                  |
      | nomeParteAutora | João Silva                      |
      | dataHoraInicio  | 2024-09-15T10:00:00             |
      | dataHoraFim     | 2024-09-15T11:00:00             |
      | sala            | Sala 1                          |
    Quando eu edito a audiência com os seguintes dados:
      | campo           | valor                           |
      | nomeParteAutora | João Silva Santos               |
      | dataHoraInicio  | 2024-09-15T15:00:00             |
      | dataHoraFim     | 2024-09-15T16:00:00             |
      | sala            | Sala 3                          |
    Então a audiência deve ser atualizada com sucesso
    E o nome da parte autora deve ser "João Silva Santos"

  Cenário: Impedir edição que gere conflito
    Dado que existem as seguintes audiências cadastradas:
      | numeroProcesso | nomeParteAutora | dataHoraInicio      | dataHoraFim         | sala    |
      | Proc. 001/2024 | João Silva      | 2024-09-15T10:00:00 | 2024-09-15T11:00:00 | Sala 1  |
      | Proc. 002/2024 | Maria Santos    | 2024-09-15T14:00:00 | 2024-09-15T15:00:00 | Sala 1  |
    Quando eu tento editar a primeira audiência para o horário da segunda:
      | dataHoraInicio | 2024-09-15T14:00:00 |
      | dataHoraFim    | 2024-09-15T15:00:00 |
    Então devo receber um erro de conflito de horário
    E a audiência não deve ser alterada

  Cenário: Deletar uma audiência
    Dado que existe uma audiência cadastrada:
      | campo           | valor                           |
      | numeroProcesso  | Proc. 001/2024                  |
      | nomeParteAutora | João Silva                      |
      | dataHoraInicio  | 2024-09-15T10:00:00             |
      | dataHoraFim     | 2024-09-15T11:00:00             |
      | sala            | Sala 1                          |
    Quando eu deleto a audiência
    Então a audiência não deve estar ativa
    E o sistema não deve listar essa audiência

  Cenário: Detectar conflitos antes de criar
    Dado que existe uma audiência cadastrada:
      | campo           | valor                           |
      | numeroProcesso  | Proc. 001/2024                  |
      | nomeParteAutora | João Silva                      |
      | dataHoraInicio  | 2024-09-15T10:00:00             |
      | dataHoraFim     | 2024-09-15T11:00:00             |
      | sala            | Sala 1                          |
    Quando eu detecto conflitos para a seguinte audiência:
      | campo           | valor                           |
      | numeroProcesso  | Proc. 002/2024                  |
      | nomeParteAutora | Maria Santos                    |
      | dataHoraInicio  | 2024-09-15T10:30:00             |
      | dataHoraFim     | 2024-09-15T11:30:00             |
      | sala            | Sala 1                          |
    Então devo obter uma lista com 1 conflito
    E o conflito deve ser a audiência "Proc. 001/2024"
