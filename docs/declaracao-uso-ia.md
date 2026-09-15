# Declaração de uso de IA

Cada participante deve preencher individualmente a sua respectiva seção, informando de forma transparente como utilizou ferramentas de Inteligência Artificial durante o desenvolvimento do projeto.

* **Projeto:** Praxis — Sistema Web de Gestão para Escritórios de Advocacia
* **Disciplina:** Requisitos e Fundamentos de Software — CESAR School
* **Repositório:** https://github.com/joaovictorgcu/praxis

---

## João Victor G. C. Uchoa

`joaovictorgcu` — [jvgcu@cesar.school](mailto:jvgcu@cesar.school)

Usei o Claude (Claude Code, modelo Opus 5) em sessão de terminal sobre o repositório, nas duas funcionalidades de alta complexidade sob minha responsabilidade — **motor de prazos processuais com alertas** e **geração de documentos por template** (petição inicial, contestação, procuração) — além da estruturação em arquitetura limpa, do mapeamento objeto-relacional, da camada de apresentação web e destes documentos.

Onde a IA entrou:

- **Modelagem estratégica e tática:** discussão dos subdomínios, escolha de agregados (`Prazo` como agregado próprio referenciando `Processo` por identidade) e redação do `praxis.cml`.
- **Implementação:** geração do código Java das camadas de domínio, aplicação, infraestrutura e apresentação, com revisão e ajuste manual.
- **Padrões de projeto:** definição de onde cada padrão resolve problema real — Strategy na contagem legal, Template Method nas peças, Proxy no segredo de justiça, Observer/Decorator na notificação, Iterator na linha do tempo.
- **Testes:** cenários Gherkin em português e step definitions Cucumber; testes de unidade do domínio.
- **Documentação:** `dominio.md`, `mapa-historia-usuario.md` e o `README.md`.

Prompts principais, em ordem: ler o enunciado e implementar a minha parte (motor de prazos e geração de documentos por template); estruturar o projeto em arquitetura limpa com DDD nos níveis preliminar, estratégico, tático e operacional; aplicar quatro ou mais dos padrões da lista do enunciado, cada um resolvendo um problema real do domínio; implementar a persistência com mapeamento objeto-relacional e a camada de apresentação web; escrever os cenários BDD e automatizá-los com Cucumber; produzir a descrição do domínio com linguagem onipresente, o mapa da história do usuário, os protótipos de alta fidelidade e o modelo em Context Mapper (CML).

Nada foi entregue sem execução e conferência:

- `./mvnw test` — **29 testes**, sendo **12 cenários BDD / 80 steps** do Cucumber, todos verdes.
- Contagem de prazo conferida manualmente contra o calendário de setembro/2026: intimação em 04/09 (sexta), feriado em 07/09 (segunda), prazo de 5 dias úteis, termo inicial 08/09 e vencimento em 14/09.
- Limite de 30% da quota litis conferido contra o art. 38 do Código de Ética da OAB.
- Regras de contagem conferidas contra os arts. 219, 220 e 224 do CPC; segredo de justiça contra o art. 189.

- João Victor G. C. Uchoa

---

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