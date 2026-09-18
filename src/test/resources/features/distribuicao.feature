# language: pt
Funcionalidade: Distribuição Automática de Processos

  Cenário: Distribuir processo priorizando a especialidade do advogado
    Dado que existem os seguintes advogados cadastrados:
      | nome    | especialidade | disponivel | processosAtivos |
      | Carlos  | Trabalhista   | true       | 5               |
      | Mariana | Cível         | true       | 1               |
    Quando o sistema solicita a distribuição de um processo da área "Trabalhista"
    Então o processo deve ser atribuído ao advogado "Carlos"

  Cenário: Distribuir processo por disponibilidade quando nenhum especialista estiver disponível
    Dado que existem os seguintes advogados cadastrados:
      | nome    | especialidade | disponivel | processosAtivos |
      | Carlos  | Trabalhista   | false      | 2               |
      | Mariana | Cível         | true       | 4               |
    Quando o sistema solicita a distribuição de um processo da área "Trabalhista"
    Então o processo deve ser atribuído ao advogado "Mariana"

  Cenário: Distribuir processo usando a regra padrão como fallback
    Dado que existem os seguintes advogados cadastrados:
      | nome    | especialidade | disponivel | processosAtivos |
      | João    | Cível         | false      | 10              |
      | Ana     | Penal         | false      | 2               |
    Quando o sistema solicita a distribuição de um processo da área "Tributário"
    Então o processo deve ser atribuído ao advogado "Ana"