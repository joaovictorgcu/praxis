# language: pt
Funcionalidade: Fluxo de aprovacao de documentos
  Para garantir que nenhuma peca seja protocolada sem revisao
  Como escritorio de advocacia
  Quero que todo documento passe por rascunho, revisao e aprovacao antes de virar peca final

  Contexto:
    Dado um processo "0001234-56.2026.8.17.0001" do cliente "Construtora Alfa Ltda." na comarca de "Recife"
    E o advogado responsavel "Ana Souza" com e-mail "ana@praxis.adv.br" e OAB "PE12345"
    E eu gero a peca "PETICAO_INICIAL" com os campos:
      | fatos | inadimplemento contratual |

  Cenario: documento gerado comeca como rascunho
    Entao o status do documento deve ser "RASCUNHO"

  Cenario: rascunho enviado para revisao muda de estado
    Quando eu envio o documento para revisao
    Entao o status do documento deve ser "EM_REVISAO"

  Cenario: documento em revisao pode ser aprovado
    Quando eu envio o documento para revisao
    E eu aprovo o documento com OAB "PE12345" e comentario "de acordo"
    Entao o status do documento deve ser "APROVADO"
    E o historico do documento deve conter uma transicao de "EM_REVISAO" para "APROVADO"

  Cenario: documento em revisao pode ser rejeitado
    Quando eu envio o documento para revisao
    E eu rejeito o documento com OAB "PE12345" e motivo "faltam fundamentos"
    Entao o status do documento deve ser "REJEITADO"

  Cenario: documento aprovado pode ser protocolado
    Quando eu envio o documento para revisao
    E eu aprovo o documento com OAB "PE12345" e comentario "de acordo"
    E eu protocolo o documento
    Entao o status do documento deve ser "PROTOCOLADO"

  Cenario: nao e possivel aprovar um documento ainda em rascunho
    Entao tentar aprovar o documento deve falhar

  Cenario: decisao de aprovacao pode ser desfeita
    Quando eu envio o documento para revisao
    E eu aprovo o documento com OAB "PE12345" e comentario "de acordo"
    E eu desfaco a ultima decisao do documento
    Entao o status do documento deve ser "EM_REVISAO"