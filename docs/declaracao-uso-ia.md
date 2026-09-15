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


## Caio Sena

`Caiosenas2101` — [css4@cesar.school](mailto:css4@cesar.school)

---

## Gustavo Laporte

`Gustavo Laporte` — [gustavo.laporte@finacap.com.br](mailto:gustavo.laporte@finacap.com.br)

---

## Luis Eduardo Bérard

`Luis Eduardo Bérard` — [luisberard2004@gmail.com](mailto:luisberard2004@gmail.com)

Declaração de Uso de Inteligência Artificial — Praxis, Luis Eduardo Bérard

Durante o desenvolvimento do projeto Praxis, utilizei ferramentas de Inteligência Artificial como apoio ao longo do desenvolvimento das funcionalidades pelas quais fiquei responsável: Distribuição Automática de Processos e Fluxo de Aprovação de Documentos.

A IA foi utilizada principalmente para tirar dúvidas, discutir ideias, verificar se as funcionalidades faziam sentido para o sistema e ajudar a identificar possíveis problemas durante a implementação. Alguns dos prompts utilizados foram:

> "Estou pensando em fazer uma funcionalidade de distribuição automática de processos no Praxis. Você acha que é uma história boa e interessante para o projeto?"

> "A ideia é distribuir automaticamente os processos entre os advogados. Você acha que essa funcionalidade tem uma complexidade boa para um projeto desse nível?"

> "Também pensei em fazer um fluxo de aprovação de documentos, onde o documento pode ser enviado para aprovação, aprovado ou rejeitado. Você acha essa história interessante?"

> "Se você fosse avaliar esse projeto como professor, consideraria distribuição automática de processos e fluxo de aprovação de documentos como funcionalidades relevantes e de alta complexidade?"

> "Quero desenvolver essas duas histórias no Praxis. Você acha que elas demonstram uma boa quantidade de lógica e regras de negócio?"

A IA considerou as duas ideias relevantes para o contexto do Praxis, principalmente por envolverem regras de negócio e não serem apenas funcionalidades simples de cadastro.

Durante a implementação, também utilizei a IA para tirar dúvidas e revisar a lógica que estava desenvolvendo. Alguns exemplos de prompts foram:

> "Estou implementando a distribuição automática de processos. Pode me ajudar a pensar em uma lógica para distribuir os processos de forma equilibrada entre os advogados?"

> "Estou com um problema nessa parte do código da distribuição de processos. Pode me ajudar a entender onde está o erro?"

> "A lógica que fiz para distribuir os processos está correta? Quero que você verifique sem mudar o código inteiro."

> "Estou fazendo um fluxo de aprovação de documentos. Como posso organizar os estados de um documento, como aguardando aprovação, aprovado e rejeitado?"

> "Analise essa parte do meu código e veja se a lógica do fluxo de aprovação está funcionando corretamente."

> "Um documento que já foi aprovado não deveria poder voltar para aguardando aprovação. Minha implementação está respeitando essa regra?"

Também utilizei a IA para entender melhor conceitos de Java e Spring Boot que apareciam durante o desenvolvimento e para discutir possíveis formas de implementar determinadas regras sem precisar alterar desnecessariamente a estrutura que já existia no projeto.

A utilização da IA serviu, portanto, como uma ferramenta de apoio e consulta durante o desenvolvimento, principalmente para esclarecer dúvidas, validar ideias, analisar erros e discutir soluções. A implementação das funcionalidades, as adaptações necessárias ao projeto, as decisões tomadas durante o desenvolvimento e os testes foram realizados por mim.

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