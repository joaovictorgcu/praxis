# language: pt
Funcionalidade: Versoes do texto da peca
  Para corrigir a peca sem perder o que ja foi escrito
  Como advogado que redige e chefe que revisa
  Quero editar, regenerar, comparar e restaurar versoes, com o estado da peca decidindo o que cabe

  Contexto:
    Dado um processo para versoes de peca
    E o modelo de embargos de declaracao para versoes de peca

  Cenario: a geracao grava a versao 1 com o modelo e os campos usados
    Quando eu gero a peca de embargos com a omissao "o pedido de tutela"
    Entao a peca deve estar na versao 1
    E a versao 1 deve ter origem "GERACAO"
    E a peca deve guardar o modelo "EMBARGOS_VERSOES" e o campo "omissao" com "o pedido de tutela"

  Cenario: edicao, regeneracao e restauracao criam versoes novas sem apagar as antigas
    Dado a peca de embargos gerada com a omissao "o pedido de tutela"
    Quando eu edito a peca acrescentando a linha "Requer efeito modificativo."
    E eu regenero a peca com a omissao "o pedido de restabelecimento da linha"
    E eu restauro a versao 2
    Entao a peca deve estar na versao 4
    E as origens das versoes devem ser "GERACAO, EDICAO, REGENERACAO, RESTAURACAO"
    E o texto atual deve conter "Requer efeito modificativo."

  Cenario: comparacao entre versoes aponta linhas incluidas e removidas
    Dado a peca de embargos gerada com a omissao "o pedido de tutela"
    Quando eu edito a peca acrescentando a linha "Requer efeito modificativo."
    E eu comparo a versao 1 com a versao 2
    Entao a comparacao deve ter 1 linha incluida e 0 removida

  Cenario: peca em revisao nao aceita mudanca de texto
    Dado a peca de embargos gerada com a omissao "o pedido de tutela"
    E a peca de embargos foi enviada para revisao
    Quando eu tento editar a peca
    Entao a mudanca de texto deve ser recusada
    E a peca deve estar na versao 1

  Cenario: rascunho pode ser excluido e a exclusao fica na auditoria
    Dado a peca de embargos gerada com a omissao "o pedido de tutela"
    Quando o autor exclui o rascunho
    Entao a peca nao deve mais existir
    E a auditoria deve registrar a exclusao da peca

  Cenario: peca que ja foi para revisao nao pode ser excluida
    Dado a peca de embargos gerada com a omissao "o pedido de tutela"
    E a peca de embargos foi enviada para revisao
    Quando o autor tenta excluir a peca
    Entao a exclusao deve ser recusada
