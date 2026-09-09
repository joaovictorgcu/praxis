# language: pt
Funcionalidade: Cadastro de feriados
  Para que a contagem de prazo respeite o calendario do foro
  Como responsavel pela agenda do escritorio
  Quero cadastrar feriados nacionais, estaduais e da comarca

  Contexto:
    Dado um processo "0001234-56.2026.8.17.0001" do cliente "Construtora Alfa Ltda." na comarca de "Recife"
    E o advogado responsavel "Ana Souza" com e-mail "ana@praxis.adv.br" e OAB "PE12345"

  Cenario: feriado cadastrado retira o dia do calendario
    Dado o feriado "Ponto facultativo" cadastrado em "2026-10-15" valido em todo o pais
    Entao o dia "2026-10-15" nao deve correr prazo
    E o proximo dia util depois de "2026-10-15" deve ser "2026-10-16"

  Cenario: feriado anual vale tambem nos anos seguintes
    Dado o feriado "Data magna do foro" cadastrado em "2026-10-15" repetindo todo ano
    Entao o dia "2026-10-15" nao deve correr prazo
    E o dia "2027-10-15" nao deve correr prazo

  Cenario: feriado de outra comarca nao suspende o expediente do foro
    Dado o feriado "Padroeira de Olinda" cadastrado em "2026-10-16" so na comarca de "Olinda"
    Entao o dia "2026-10-16" deve correr prazo

  Cenario: feriado da comarca do foro suspende o expediente
    Dado o feriado "Aniversario do Recife" cadastrado em "2026-10-16" so na comarca de "Recife"
    Entao o dia "2026-10-16" nao deve correr prazo

  Cenario: remover feriado devolve o dia ao calendario
    Dado o feriado "Ponto facultativo" cadastrado em "2026-10-15" valido em todo o pais
    E o dia "2026-10-15" nao deve correr prazo
    Quando eu removo o feriado cadastrado
    Entao o dia "2026-10-15" deve correr prazo

  Cenario: feriado novo nao move prazo ja lancado, mas vale para o proximo
    Dado um prazo fatal "Contestacao" de 5 dias uteis intimado em "2026-10-01"
    E o vencimento do prazo deve ser "2026-10-08"
    Quando o feriado "Feriado municipal" e cadastrado em "2026-10-06" valido em todo o pais
    Entao o vencimento do prazo ja lancado deve continuar "2026-10-08"
    E um novo prazo fatal de 5 dias uteis intimado em "2026-10-01" deve vencer em "2026-10-09"
