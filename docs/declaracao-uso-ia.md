# Declaração de uso de IA

Cada participante deve preencher individualmente a sua respectiva seção, informando de forma transparente como utilizou ferramentas de Inteligência Artificial durante o desenvolvimento do projeto.

* **Projeto:** Praxis — Sistema Web de Gestão para Escritórios de Advocacia
* **Disciplina:** Requisitos e Fundamentos de Software — CESAR School
* **Repositório:** https://github.com/joaovictorgcu/praxis

---
# PROMPTS 

## João Victor Uchôa
`joaovictorgcu` — [jvgcu@cesar.school](mailto:jvgcu@cesar.school)

1. to estudando o praxis e queria entender melhor como a arquitetura dele ta organizada. me explica a função de cada camada, dominio, aplicação, infraestrutura e apresentação, usando exemplos do proprio projeto. tambem queria saber o que pode dar errado quando uma regra de negocio acaba ficando na camada errada.

2. pegando o caso de abrir um prazo no praxis, me mostra o caminho de uma requisição até chegar no banco. quero entender o que acontece no controller, na aplicação, no dominio e na persistencia e quem cuida de cada parte.

3. em um dos commits o jpa saiu do dominio do praxis. me explica pq isso foi feito e qual problema existia em deixar @Entity e outras coisas de persistencia dentro do dominio. tambem quero entender o que muda nos testes com essa separação.

4. me explica as portas de entrada e de saida usadas no praxis de um jeito simples, usando as interfaces do projeto. quero entender quem depende de quem e pq um caso de uso não deve falar direto com o banco. depois faz 5 perguntas pra eu ver se entendi mesmo.

5. olha NumeroCnj e Advogado no praxis e me explica pq eles existem como classes proprias em vez de usar String em tudo. depois me passa 5 exemplos pra eu tentar decidir quais fariam sentido como value object.

6. quero revisar os padrões que realmente aparecem no praxis: Strategy, Observer, Template Method, Proxy, Decorator e Iterator. me explica onde cada um aparece, qual problema resolveu e o que poderia ser feito no lugar. tambem fala o que eu perderia usando a solução mais simples.

7. me explica como o praxis calcula um prazo. considera data da intimação, começo da contagem, dias uteis, dias corridos, fim de semana, feriado, recesso, vencimento e prorrogação. depois me passa alguns exemplos pra eu tentar calcular sozinho.

8. no motor de prazos tem mais de uma forma de contar os dias. me explica pq isso foi separado em estrategias diferentes. depois me passa 5 situações e deixa eu escolher qual seria usada.

9. no praxis o vencimento é calculado quando o prazo é criado e depois não muda mais. pq fizeram assim? o que poderia acontecer se recalculasse todo dia? depois cria uns exemplos com feriado sendo adicionado ou removido pra eu dizer o que deveria acontecer.

10. me explica como a PoliticaDeAlerta chega em ATENCAO, URGENTE, CRITICO, VENCE_HOJE e VENCIDO. depois cria 10 exercicios pra eu descobrir qual alerta deveria aparecer em cada caso.

11. me explica a idempotencia no motor de alertas do praxis. quero entender pq rodar a mesma varredura duas vezes não pode gerar o mesmo aviso de novo e como o sistema evita isso. depois me passa 5 situações pra eu analisar.

12. o MotorDePrazos publica coisas como PrazoEmRisco e PrazoVencido. me explica pq fizeram dessa forma e pq o motor não precisa saber quem vai tratar esses eventos depois.

13. o praxis faz uma varredura diaria dos prazos, mas tambem da pra executar manualmente. me explica como isso foi organizado e pq os dois caminhos usam a mesma regra em vez de duplicar a implementação.

14. analisa MotorDePrazosTest, PrazoFatalNotificacaoTest e os cenarios BDD do motor. quero saber qual regra de negocio cada teste ta protegendo e o que poderia quebrar se ele não existisse.

15. faz uma simulação de defesa sobre o motor de prazos. vai fazendo uma pergunta por vez e espera eu responder. começa mais simples e depois vai complicando.

16. me explica como funciona a geração de documentos no praxis, começando pelo modelo e pelos campos e chegando no DocumentoGerado, armazenamento e leitura.

17. me explica pq GeradorDocumento foi organizado desse jeito e o que muda entre PeticaoInicial, Contestacao e Procuracao. quero entender o que fica igual e o que muda.

18. compara as responsabilidades de GeradorDocumento, PeticaoInicial, Contestacao e Procuracao. depois me passa algumas situações e eu tento dizer qual classe deveria cuidar de cada uma.

19. nos modelos do praxis tem coisas como {{campo}}. me explica como isso funciona, desde descobrir os campos até substituir eles na hora de gerar o documento. tambem quero ver o que acontece quando algum campo não é preenchido.

20. os campos dos modelos começaram a gerar os campos de preenchimento automaticamente na tela. me explica como backend, html e javascript trabalham juntos nisso e pq é melhor do que deixar varios campos fixos.

21. no praxis, mudar ou excluir um modelo não pode mudar um documento que ja foi gerado. me explica pq isso é importante e depois cria 4 situações pra eu dizer o que deveria acontecer.

22. faz uma simulação de prova sobre a parte de documentos do praxis. pergunta uma coisa por vez e espera minha resposta. pode passar por modelos, campos, geração, persistencia, sigilo e organização das classes.

23. me explica o DocumentoProxy do praxis de um jeito simples. quero entender qual é o documento real, o que o proxy faz, onde ele verifica o acesso e pq não seria legal repetir essa regra em varios controllers.

24. no praxis, um documento sigiloso pode retornar 403 quando a OAB não tem acesso. me explica a diferença entre autenticação, autorização, identificação da OAB e segredo de justiça usando o projeto como exemplo.

25. imagina que alguem crie um endpoint novo e esqueça de verificar o segredo de justiça. me explica como o DocumentoProxy ajuda nisso. depois me mostra outras formas de proteger e me deixa comparar qual seria melhor.

26. faz uma simulação de entrevista sobre a parte de segurança dos documentos do praxis. pergunta uma coisa por vez e espera eu responder.

27. me explica como funciona o login do praxis sem resumir tudo em spring security. quero entender o papel da sessão, usuario, senha, papel, OAB, cookie e logout.

28. as senhas do praxis usam PBKDF2-HMAC-SHA256. me explica pq não pode guardar a senha direto no banco e me explica tambem hash, salt, força bruta e como a senha é conferida.

29. no praxis existem os papeis ADVOGADO e CHEFE. me explica a diferença entre autenticar e autorizar usando exemplos do projeto, principalmente @SomenteChefe e o 403.

30. me explica pq o praxis tem proteção contra CSRF nos POSTs do painel. primeiro explica o ataque de um jeito simples e depois relaciona isso com sessão e cookie.

31. pq o praxis usa a sessão HTTP pra saber qual usuario está logado em vez de confiar na OAB enviada pela interface? quero entender o que poderia dar errado se confiasse direto no que vem do cliente.

32. no praxis, cinco tentativas erradas fazem o email ficar bloqueado por um minuto. me explica qual problema isso tenta evitar, o que resolve e quais problemas ainda podem existir. depois pede pra eu pensar numa melhoria.

33. o praxis permite usar um login curto como admin e completar o dominio automaticamente. me explica como isso funciona e pq essa escolha pode ser boa.

34. faz uma banca sobre segurança no praxis. pergunta uma coisa por vez sobre senha, sessão, cookie, CSRF, autorização, brute force e senha provisoria. depois de cada resposta fala só o que eu acertei e o que faltou.

35. me explica o fluxo da tela de processos do praxis, começando pela lista e busca e passando por cadastro, ficha, andamento, prazo, documento e anexo. quero entender qual caso de uso entra em cada parte.

36. no praxis, Processo implementa Iterable<Andamento> pra trabalhar com a linha do tempo. me explica pq fizeram isso e compara com simplesmente expor a lista ou ordenar tudo no controller.

37. me explica pq o painel usa POST-Redirect-GET e qual problema isso evita. depois me passa alguns exemplos de formulario e me pede pra prever o que vai acontecer.

38. me passa uma lista de responsabilidades da tela de processos e pede pra eu dizer o que deveria ficar no controller, caso de uso, dominio, infraestrutura ou tela.

39. faz uma simulação de apresentação da tela de processos pra um professor. pergunta mais sobre as decisões tecnicas e de arquitetura do que sobre o funcionamento basico.

40. me explica pq o praxis tem classes de persistencia diferentes das classes do dominio e como uma vira a outra.

41. mostra o caminho de um objeto do dominio até o banco e depois o caminho contrario. usa Processo como exemplo pra ficar mais facil.

42. me explica pra que serve o Flyway no praxis, pq existem migrations, qual a diferença pra ddl-auto e pq o projeto usa PostgreSQL.

43. me explica a preocupação com bytea, varchar grande e large objects no PostgreSQL e qual problema estavam tentando evitar.

44. faz 8 perguntas sobre persistencia no praxis, misturando JPA, mapeamento, repository, PostgreSQL, Flyway, transação, H2 e PostgreSQL. não mostra as respostas antes de eu tentar.

45. me explica pq o praxis tem consultas especificas pro painel administrativo em vez de usar exatamente as mesmas do dia a dia.

46. foram criadas operações como listarTodosOsClientes e listarTodasAsAudiencias. pq criaram outras consultas em vez de simplesmente mudar as que ja existiam?

47. me explica como funciona o /painel/admin e pq ConsultarPanorama não deveria acessar os repositories diretamente.

48. me explica como menu, controller, autorização, sessão e 403 ajudam a proteger o painel. e pq só esconder o link não resolve.

49. me explica pq a senha codificada não aparece no html e pq a url do banco precisa ser tratada antes de aparecer na tela.

50. analisa o AdminHttpTest e me explica o que cada teste está protegendo. depois faz uma pergunta de banca sobre essa parte.

51. me explica a diferença entre teste unitario, teste de integração, teste HTTP e BDD usando os testes do proprio praxis.

52. no praxis os cenarios BDD usam casos de uso reais. quais as vantagens e desvantagens de fazer desse jeito?

53. pega o teste do alerta escalonado e me explica qual regra de negocio ele protege e como esse teste verifica ela.

54. me explica pq os testes devem proteger o comportamento do sistema e não ficar presos aos detalhes do codigo. usa exemplos do praxis.

55. relaciona as principais regras do motor de prazos com os testes que existem pra proteger cada uma.

56. faz 10 perguntas sobre os testes do praxis, uma por vez. pergunta coisas tipo: o que quebraria se esse teste fosse removido? qual regra ele protege? é unitario ou integração? o que ele realmente garante?

57. faz uma simulação de banca sobre testes. começa facil e vai aumentando a dificuldade conforme eu respondo.

58. me explica como visão de dominio, mapa de historia, CML, arquitetura e implementação se relacionam dentro do praxis.

59. escolhe as duas funcionalidades mais complexas do praxis e mostra como elas aparecem nos requisitos, dominio, casos de uso, tela e testes.

60. escolhe uma regra de negocio e vai me guiando pelo caminho requisito → dominio → caso de uso → tela ou API → teste. faz uma etapa por vez e espera minha resposta.

61. quero criar uma tela de admin no praxis onde eu possa administrar tudo e saber todos os dados. analisa a arquitetura atual do projeto e me diz qual seria a melhor forma de fazer isso sem quebrar as regras que já existem.

62. quero uma tela de admin somente de leitura no praxis. me mostra como organizar isso respeitando a arquitetura atual, sem colocar regra de negocio no controller e sem acessar os repositories diretamente pela tela.

63. olhando os casos de uso que o praxis já tem, como eu poderia montar um `ConsultarPanorama` para juntar os dados necessários para a tela de admin? quero entender onde essa responsabilidade deveria ficar.

64. preciso mostrar todos os prazos no admin, inclusive os que já foram cumpridos. analisa as listagens atuais de prazo e me diz como criar uma consulta específica sem alterar o comportamento das telas que já existem.

65. quero mostrar todos os clientes, partes contrárias e audiências na tela de admin. me explica quais consultas novas eu precisaria criar e por que seria melhor não mudar as consultas usadas pelas outras telas.

66. analisa como proteger a rota `/painel/admin` para que somente o chefe consiga acessar. quero entender como usar a autorização existente no projeto e o que deve acontecer quando um advogado tentar entrar diretamente pela URL.

67. quero adicionar um link para a tela de admin no menu do praxis. me mostra como fazer isso de forma que o link apareça somente para quem tem permissão, sem usar isso como única forma de proteção.

68. preciso criar testes HTTP para a tela de administração. me sugere os testes mais importantes para garantir autorização, renderização da tela e proteção de informações sensíveis, usando os padrões de teste que já existem no projeto.

69. no `AdminHttpTest`, quero verificar que a senha codificada nunca aparece no html da tela. me explica como testar isso e quais dados seriam perigosos de expor no panorama administrativo.

70. quero verificar no teste do admin que um prazo já cumprido aparece no panorama, mesmo que as listagens normais escondam esse tipo de prazo. como eu deveria implementar esse teste e qual regra ele está protegendo?

71. quero implementar a tela de admin sem transformar o `ConsultarPanorama` em um acesso direto aos repositories. analisa a arquitetura do praxis e me mostra o caminho correto entre controller, caso de uso, application service e repositories.

72. antes de eu aceitar a implementação da tela de admin, revisa a solução pensando em arquitetura, segurança e impacto nas funcionalidades existentes. aponta o que pode quebrar e o que precisa ser testado.

73. implemente a tela `/painel/admin` no praxis seguindo a arquitetura existente. use os casos de uso já disponíveis quando fizer sentido, crie apenas as consultas que realmente forem necessárias e mantenha a tela somente de leitura.

74. agora que a tela de admin foi implementada, analisa o código gerado e explica cada classe criada ou alterada, a responsabilidade de cada uma e por que essa organização faz sentido dentro da arquitetura do projeto.

75. cria uma bateria de testes para a tela de administração e explica o que cada teste protege. quero garantir pelo menos acesso negado para advogado, acesso permitido para chefe, ausência de senha codificada no html e presença de prazo cumprido.

76. revisa a implementação da tela de admin e verifica se existe algum segundo caminho até o banco, alguma duplicação de regra ou alguma alteração indevida nas consultas usadas pelas telas normais.

77. analisa as limitações da tela de admin depois da implementação. ela não tem paginação e algumas entidades não possuem tela própria de cadastro. me explica como registrar essas limitações sem tratar isso como bug.

78. revisei a implementação e os testes passaram. me ajuda a interpretar o resultado de `./mvnw -o test` e montar uma lista do que ainda preciso validar manualmente na aplicação antes de considerar a funcionalidade pronta.

79. quero fazer uma revisão final da tela de admin como se você fosse um professor avaliando o projeto. analisa arquitetura, segurança, testes, decisões tomadas e possíveis problemas da solução e me faça perguntas para eu justificar cada escolha.

80. to estudando a segurança da API REST do praxis e quero entender primeiro o problema antes de mexer no codigo. me explica por que os GETs podem continuar abertos mas as operações que alteram dados precisam de sessão, CSRF e autorização.

81. me explica como funcionaria um SessaoApiInterceptor no praxis. quero entender onde ele entra no fluxo de uma requisição, o que ele deve verificar e por que faz sentido registrar ele somente em /api/**.

82. no SessaoApiInterceptor, alguns metodos como GET, HEAD, OPTIONS e TRACE ficam liberados. me explica por que esses metodos não devem passar pelas mesmas verificações das mutações e quais riscos existem nos outros metodos.

83. me explica a diferença entre retornar 401 e 403 na API REST do praxis. usa os casos de sessão ausente, CSRF inválido, senha provisória e falta de permissão de chefe para eu entender quando cada um deve acontecer.

84. a API do praxis hoje pode redirecionar para login em alguns fluxos. me explica por que isso é ruim para uma API REST e como seria melhor retornar um JSON como {"erro": "..."}.

85. quero entender como reaproveitar a validação de CSRF que já existe no CsrfInterceptor dentro do SessaoApiInterceptor. me explica como fazer isso sem duplicar a regra de validação.

86. no praxis existe SessaoInterceptor.exigeChefe. me explica por que tornar esse método package-visible pode ser útil nessa mudança e o que isso tem a ver com reutilização da regra de autorização.

87. existe um Javadoc dizendo que a API REST é aberta. me explica por que esse comentário precisa ser atualizado depois de proteger as mutações e como uma documentação errada pode atrapalhar quem mantém o projeto.

88. quero revisar quais endpoints REST realmente deveriam exigir @SomenteChefe. considera aprovação, rejeição e desfazer documento, habilitar e revogar OAB, remover feriado e remover modelo. me explica o motivo de cada operação exigir esse nível de autorização.

89. no painel do praxis também existe criação de feriado e modelo. me explica por que não basta proteger os endpoints REST e por que essas operações do painel também precisam de @SomenteChefe.

90. quero entender o problema de receber a OAB pelo corpo ou por parâmetro nas operações de documento e anexo. me explica por que a OAB deveria vir da sessão e o que poderia acontecer se eu confiar na OAB enviada pelo cliente.

91. alguns GETs de download do praxis ainda usam OabDoCorpo.oab e ?oab=. me explica por que pode fazer sentido manter isso nesses downloads mesmo depois de mudar as operações de mutação para usar a OAB da sessão.

92. me explica como sessão, OAB, autenticação, autorização e segredo de justiça se relacionam numa operação de documento do praxis. usa um exemplo em que um advogado tenta acessar um documento sigiloso pertencente a outra OAB.

93. quero entender como testar a segurança da API REST sem testar só o código interno. me explica como os testes HTTP existentes do praxis podem verificar sessão, CSRF, autorização e o conteúdo JSON das respostas.

94. cria alguns cenários de teste para a API REST do praxis, mas não mostra as respostas. quero tentar descobrir qual deveria ser o status HTTP em cada situação: sem login, advogado tentando ação de chefe, CSRF inválido, senha provisória e acesso a documento sigiloso de outra OAB.

95. me explica por que um POST sem CSRF deve retornar 403 mesmo quando o usuario está logado. relaciona isso com sessão baseada em cookie e explica qual ataque essa proteção tenta evitar.

96. no caso de upload multipart, o token CSRF pode não chegar pelo campo normal do formulario. me explica por que isso pode acontecer e como enviar o token pelo header sem criar uma exceção insegura na proteção.

97. quero analisar a diferença entre proteger uma rota pelo controller e proteger a API inteira com um interceptor. me explica as vantagens e desvantagens de cada abordagem usando o SessaoApiInterceptor do praxis como exemplo.

98. me explica o que poderia acontecer se eu colocasse @SomenteChefe em alguns endpoints mas esquecesse outros endpoints que fazem a mesma alteração. quero entender por que a proteção precisa estar em todos os caminhos que permitem a mutação.

99. quero revisar o fluxo completo de uma mutação REST no praxis. começa na requisição HTTP e passa pelo interceptor, sessão, CSRF, senha provisória, @SomenteChefe, controller e caso de uso. explica uma etapa por vez e espera eu confirmar que entendi antes de continuar.

100. faz uma simulação de banca sobre a segurança da API REST do praxis. pergunta uma coisa por vez, começando por 401 e 403 e depois passando por sessão, CSRF, OAB, @SomenteChefe, interceptor e documentos sigilosos. depois de cada resposta fala somente o que eu acertei e o que faltou.

101. analisa os testes HTTP que eu deveria criar para fechar a segurança das mutações da API. quero entender o que cada teste protege e qual comportamento poderia voltar a ficar vulnerável se ele fosse removido.

102. quero entender por que os GETs devem continuar funcionando mesmo sem sessão, enquanto POST, PUT, PATCH e DELETE precisam ser protegidos. cria exemplos do proprio contexto do praxis e depois me passa situações para eu decidir se devem ser liberadas ou bloqueadas.

103. me explica como eu verificaria se a API realmente nunca redireciona para login. quero entender como isso poderia ser validado em um teste HTTP e o que eu deveria verificar na resposta.

104. me explica como revisar uma implementação do SessaoApiInterceptor sem simplesmente olhar se os testes passaram. cria um checklist de coisas que eu deveria procurar em arquitetura, segurança, autorização, CSRF, sessão e compatibilidade com os GETs existentes.

105. quero fazer uma revisão final da segurança das mutações REST do praxis. considera interceptor, 401, 403, JSON de erro, CSRF, senha provisória, @SomenteChefe, OAB da sessão, documentos sigilosos, multipart e testes HTTP. me faça perguntas de banca uma por vez para eu justificar cada decisão.



## Caio Sena

`Caiosenas2101` — [css4@cesar.school](mailto:css4@cesar.school)

Durante o desenvolvimento do projeto Praxis, utilizei ferramentas de Inteligência Artificial como apoio na análise do projeto e no desenvolvimento das funcionalidades pelas quais fiquei responsável, principalmente o Módulo de Honorários e o Controle de Sigilo e Permissões de Documentos.

A IA foi utilizada inicialmente para compreender melhor a arquitetura já existente no Praxis e identificar como novas funcionalidades poderiam ser incorporadas sem fugir da organização adotada pelo projeto. Também utilizei a ferramenta para discutir decisões de implementação, entender melhor algumas classes e padrões já existentes e revisar possíveis impactos das alterações no restante do sistema.

Alguns dos prompts utilizados nesse processo foram:

"Analise a arquitetura atual do Praxis e me explique como domínio, aplicação, infraestrutura e apresentação estão organizados. Quero implementar novas funcionalidades sem fugir do padrão que já existe no projeto."

"Quero implementar um módulo de honorários no Praxis com contratos e cálculo de honorários fixos, por hora e por quota litis. Analise o que já existe no projeto e me ajude a entender o que ainda precisa ser implementado."

"O projeto já possui classes para calcular diferentes modalidades de honorários. Como posso aproveitar essa estrutura para criar o contrato de honorários sem duplicar regras que já existem?"

"Analise a implementação do módulo de honorários e verifique se as responsabilidades estão nas camadas corretas. Quero evitar colocar regra de negócio na camada de aplicação ou no controller."

"Por que o cálculo das diferentes modalidades de honorários faz sentido como Strategy? Explique como HonorarioFixo, HonorarioPorHora e HonorarioQuotaLitis se relacionam com esse padrão."

"No contrato de honorários, quero que o valor seja calculado no momento da contratação e permaneça armazenado mesmo que alguma informação utilizada no cálculo seja alterada depois. Essa regra faz sentido? Como ela pode ser protegida por testes?"

Durante a implementação, também utilizei a IA para revisar regras de negócio e pensar nos testes necessários para garantir o comportamento esperado. Alguns exemplos foram:

"Quais testes são importantes para garantir que as três modalidades de honorários estejam calculando corretamente e que as regras de negócio sejam respeitadas?"

"Analise os testes do módulo de honorários e veja se eles realmente protegem as regras de negócio, principalmente o limite da quota litis e o valor do contrato depois de criado."

Na funcionalidade relacionada ao controle de acesso aos documentos, utilizei a IA primeiro para analisar o mecanismo de sigilo que já existia no projeto e identificar o que poderia ser acrescentado sem duplicar a proteção implementada anteriormente.

"Analise como o Praxis atualmente controla o acesso a documentos sigilosos e me explique o papel do DocumentoProxy e das OABs autorizadas."

"O projeto já impede o acesso de uma OAB não autorizada a um documento sigiloso. Quero permitir que as OABs autorizadas sejam gerenciadas depois da criação do documento. Qual seria uma forma de implementar isso aproveitando a arquitetura existente?"

"Quero permitir habilitar e revogar o acesso de uma OAB a um documento sigiloso. Em qual camada essa regra deve ficar e como evitar colocar essa responsabilidade diretamente no controller?"

"Um documento sigiloso não deve ficar sem nenhuma OAB autorizada. Como posso garantir que a última OAB não seja removida e como testar essa regra?"

"Revise a implementação do gerenciamento de permissões de documentos e verifique se ela interfere no mecanismo de sigilo que já existia no Praxis."

Além da implementação dessas funcionalidades, utilizei a IA para revisar a integração entre domínio, casos de uso, persistência e endpoints REST, além de esclarecer conceitos relacionados à arquitetura utilizada pelo projeto, como Strategy, Proxy, portas de entrada e saída, casos de uso, persistência JPA e separação de responsabilidades entre as camadas.

Também recorri à IA durante a revisão final para analisar possíveis duplicações de regras, verificar a organização das classes, discutir a cobertura dos testes e identificar pontos que poderiam afetar funcionalidades já existentes.

A utilização da Inteligência Artificial serviu como ferramenta de apoio durante diferentes etapas do desenvolvimento, principalmente na compreensão da arquitetura existente, discussão de alternativas de implementação, análise de regras de negócio, identificação de possíveis problemas e revisão dos testes e do código desenvolvido. As decisões sobre o escopo das funcionalidades, a integração das alterações ao projeto, a validação do comportamento esperado e a revisão final das entregas foram acompanhadas e avaliadas por mim durante o desenvolvimento.

- Caio Santos

---

## Gustavo Laporte

`Gustavo Laporte` — [gustavo.laporte@finacap.com.br](mailto:gustavo.laporte@finacap.com.br)

---

## Luis Eduardo Bérard

`Luis Eduardo Bérard` — [luisberard2004@gmail.com](mailto:luisberard2004@gmail.com)

Declaração de Uso de Inteligência Artificial — Praxis, Luis Eduardo Bérard

1. to pensando em fazer uma funcionalidade de distribuição automática de processos no praxis, você acha que é uma história boa e interessante pro projeto?

2. a ideia é distribuir automaticamente os processos entre os advogados, você acha que essa funcionalidade tem uma complexidade boa pra um projeto desse nível?

3. também pensei em fazer um fluxo de aprovação de documentos, onde o documento pode ser enviado pra aprovação, aprovado ou rejeitado, você acha essa história interessante?

4. se você fosse avaliar esse projeto como professor, consideraria distribuição automática de processos e fluxo de aprovação de documentos como funcionalidades relevantes e de alta complexidade?

5. quero desenvolver essas duas histórias no praxis, você acha que elas demonstram uma boa quantidade de lógica e regras de negócio?

6. to implementando a distribuição automática de processos, pode me ajudar a pensar numa lógica pra distribuir os processos de forma equilibrada entre os advogados?

7. estou com um problema nessa parte do código da distribuição de processos, pode me ajudar a entender onde está o erro?

8. a lógica que fiz pra distribuir os processos tá correta? quero que você verifique sem mudar o código inteiro.

9. to fazendo um fluxo de aprovação de documentos, como posso organizar os estados de um documento, tipo aguardando aprovação, aprovado e rejeitado?

10. analisa essa parte do meu código e vê se a lógica do fluxo de aprovação tá funcionando corretamente.

11. um documento que já foi aprovado não deveria poder voltar pra aguardando aprovação, minha implementação tá respeitando essa regra?

12. to com dúvida em um conceito de spring boot que apareceu enquanto eu implementava a distribuição de processos, pode me explicar?

13. quero discutir uma forma de implementar essa regra de aprovação sem precisar alterar desnecessariamente a estrutura que já existe no projeto.

14. depois que um processo é distribuído, faz sentido ele poder ser redistribuído manualmente? o que você acha da regra que pensei pra isso?

15. revisa a lógica de balanceamento que fiz pra distribuição de processos e me diz se tem algum caso que eu não tratei.

16. no fluxo de aprovação, quero rejeitar um documento com um motivo obrigatório, minha validação pra isso tá fazendo sentido?

17. tem algum problema em eu deixar o estado do documento como um enum simples em vez de criar uma classe própria pra isso?

18. me ajuda a entender esse erro de compilação relacionado ao meu serviço de aprovação de documentos.

19. revisa se essas duas funcionalidades que implementei fazem sentido dentro da arquitetura que o restante do praxis já segue.

20. faz uma simulação de banca comigo sobre distribuição de processos e fluxo de aprovação de documentos, pergunta uma coisa por vez e espera minha resposta.

21. no meu fluxo de aprovação, um chefe pode aprovar um documento que outro advogado enviou? me ajuda a pensar se essa regra faz sentido pro contexto do praxis.

22. quero adicionar um histórico de quem aprovou ou rejeitou cada documento, como você organizaria isso sem duplicar informação que já existe no sistema?

23. tô tentando decidir se a distribuição automática deveria considerar a quantidade de processos que cada advogado já tem ou só distribuir em ordem, o que acha melhor pra esse projeto?

24. revisa esse teste que fiz pra garantir que um documento rejeitado não pode ser considerado aprovado depois, ele tá cobrindo o cenário certo?

25. me ajuda a entender a diferença entre colocar essa validação de estado do documento no serviço ou dentro da própria classe do documento.

26. depois de rejeitar um documento, deveria ser possível reenviar ele pra aprovação de novo? gostaria de pensar contigo se essa regra faz sentido.

27. tenho um caso onde a distribuição automática pode falhar se não tiver nenhum advogado disponível, como eu deveria tratar isso na minha implementação?

28. revisa se meu código de distribuição de processos tá seguindo o mesmo padrão que o resto do projeto usa pra separar regra de negócio do controller.

29. quero garantir que só quem tem permissão de chefe possa forçar uma redistribuição manual de um processo, como você sugere eu verificar isso?

30. me explica se faz sentido eu criar um evento quando um documento é aprovado, pra caso outra parte do sistema precise reagir a isso no futuro.

31. analisa se os nomes que escolhi pros estados do fluxo de aprovação fazem sentido e se comunicam bem a regra de negócio.

32. faz mais uma simulação de banca comigo, mas agora focando só no fluxo de aprovação de documentos, pergunta uma coisa por vez.

- Luis Eduardo Bérard

---

## Pedro Ferraz

`Pedro Ferraz` — [pvf@cesar.school](mailto:pvf@cesar.school)

Declaração de Uso de Inteligência Artificial — Praxis, Pedro Valença Ferraz

Durante o desenvolvimento do projeto Praxis, utilizei ferramentas de Inteligência Artificial como apoio ao longo do desenvolvimento das funcionalidades pelas quais fiquei responsável: Agenda de Audiências com Detecção de Conflitos, Cadastro de Clientes e Cadastro de Partes Contrárias.

A IA foi utilizada principalmente para tirar dúvidas, discutir ideias, validar a estrutura das funcionalidades e regras de negócio, além de auxiliar na compreensão dos requisitos do projeto e na organização da implementação. Alguns dos prompts utilizados foram:

"Estou responsável por desenvolver uma agenda de audiências com detecção de conflitos, cadastro de clientes e cadastro de partes contrárias no Praxis. Você acha que essas funcionalidades fazem sentido para o sistema?"

"Como posso estruturar uma funcionalidade de agenda de audiências com detecção de conflitos para que ela seja considerada completa?"

"Quais regras de negócio posso considerar para uma agenda de audiências com detecção de conflitos?"

"Como posso identificar conflitos entre duas audiências? Quero considerar data, horário e sala."

"Cadastro de clientes e cadastro de partes contrárias podem ser considerados funcionalidades completas? O que preciso implementar além do cadastro?"

"Estou fazendo TDD e BDD no projeto. Pode me explicar como transformar as regras de negócio das minhas funcionalidades em cenários BDD?"

"Como posso escrever cenários Given, When e Then para o conflito de horário de uma audiência?"

A IA também foi utilizada para analisar os requisitos da atividade e verificar se as funcionalidades estavam completas do ponto de vista do usuário. Durante esse processo, utilizei a IA para estruturar as operações de cadastro, consulta, alteração e desativação, além de identificar regras como a obrigatoriedade de dados, validação de informações e a necessidade de impedir conflitos entre audiências.

Durante a implementação, também utilizei a IA para tirar dúvidas relacionadas à organização do código e aos conceitos de TDD, BDD e Cucumber, principalmente para entender como os testes poderiam ser relacionados às regras de negócio. Alguns exemplos de prompts foram:

"Como posso aplicar TDD na funcionalidade de agenda de audiências?"

"Quero criar primeiro os testes para a detecção de conflitos e depois implementar a regra. Como posso organizar isso?"

"Como transformar essa regra de negócio em um cenário de teste BDD com Cucumber?"

"Analise essa regra de conflito de audiência e me diga quais casos de teste eu deveria considerar."

"Uma audiência que já foi cancelada deve continuar sendo considerada para detectar conflito?"

Também utilizei a IA para auxiliar na documentação das funcionalidades, principalmente no preenchimento dos campos de título, descrição, entidades envolvidas, regras de negócio, consultas ao banco de dados e classificação da complexidade. A IA também foi utilizada para revisar a documentação e verificar se as regras estavam coerentes com o funcionamento proposto para o sistema.

A utilização da IA serviu, portanto, como uma ferramenta de apoio e consulta durante o desenvolvimento, principalmente para esclarecer dúvidas, discutir soluções, estruturar regras de negócio, compreender TDD e BDD e auxiliar na documentação das funcionalidades. A implementação das funcionalidades, as adaptações necessárias ao projeto, as decisões tomadas durante o desenvolvimento e os testes foram realizados por mim.