# Declaração de uso de IA — por participante

O slide 7 do enunciado dá duas opções por integrante: declaração de **não uso** de IA, ou entrega das **interações com IAs**. Este documento reúne, em um único lugar, a declaração de cada participante do grupo.

- **Projeto:** praxis — sistema web de gestão para escritórios de advocacia
- **Disciplina:** Requisitos e Fundamentos de Software — CESAR School
- **Repositório:** https://github.com/joaovictorgcu/praxis (branch `main`)
- **Detalhamento da declaração de João Victor:** [`uso-de-ia.md`](uso-de-ia.md)

## Resumo do grupo

| Participante | Parte do trabalho | Usou IA? |
|---|---|---|
| João Victor G. C. Uchoa | Motor de prazos processuais com alertas; geração de documentos por template; arquitetura limpa, ORM, camada web, login/segurança e documentação | Sim |
| Caio Sena | Contrato de honorários (fixo / hora / quota litis); habilitação e revogação de OAB em documento sigiloso | A declarar |
| Gustavo Laporte | Cadastro de feriados (abrangência e recorrência); cadastro de modelo de peça com marcador de campo; juntada de anexos aos autos | A declarar |
| Luis Eduardo Bérard | Fluxo de aprovação de documentos (State + Command); distribuição automática de processos (Chain of Responsibility) | A declarar |
| Pedro Ferraz | Agenda de audiências, cliente e parte contrária, com API REST, TDD e BDD | A declarar |

A coluna "Parte do trabalho" foi extraída do histórico de commits do repositório (`git log`). A coluna "Usou IA?" só pode ser preenchida pelo próprio participante — ver a seção individual abaixo.

---

## 1. João Victor G. C. Uchoa

- **Login no repositório:** `joaovictorgcu` (`jvgcu@cesar.school`)
- **Usou IA:** **Sim**
- **Ferramenta:** Claude (Claude Code, modelo Opus 5), em sessão de terminal sobre o repositório

### Parte do trabalho coberta

As duas funcionalidades de alta complexidade sob minha responsabilidade — **motor de prazos processuais com alertas** e **geração de documentos por template** (petição inicial, contestação, procuração) — além da estruturação em arquitetura limpa, do mapeamento objeto-relacional, da camada de apresentação web, do login por sessão com papéis `ADVOGADO` e `CHEFE`, e da documentação do projeto.

### Como a IA foi usada

| Etapa | Uso |
|---|---|
| Modelagem estratégica e tática | Discussão dos subdomínios, escolha de agregados (`Prazo` como agregado próprio referenciando `Processo` por identidade) e redação do `praxis.cml` |
| Implementação | Geração do código Java das camadas de domínio, aplicação, infraestrutura e apresentação, com revisão e ajuste manual |
| Padrões de projeto | Definição de onde cada padrão resolve problema real (Strategy na contagem legal, Template Method nas peças, Proxy no segredo de justiça, Observer/Decorator na notificação, Iterator na linha do tempo) |
| Testes | Escrita dos cenários Gherkin em português e dos step definitions Cucumber; testes de unidade do domínio |
| Documentação | Este arquivo, `uso-de-ia.md`, `dominio.md`, `mapa-historia-usuario.md` e o `README.md` |

### Verificação humana

Nada foi entregue sem execução e conferência:

- `./mvnw test` — suíte completa verde, incluindo os cenários BDD / steps do Cucumber.
- Contagem de prazo conferida manualmente contra o calendário de setembro/2026: intimação em 04/09 (sexta), feriado em 07/09 (segunda), prazo de 5 dias úteis — termo inicial 08/09 e vencimento em 14/09.
- Limite de 30% da quota litis conferido contra o art. 38 do Código de Ética da OAB.
- Regras de contagem conferidas contra os arts. 219, 220 e 224 do CPC; segredo de justiça contra o art. 189.

### Registro das interações

Prompts principais, em ordem:

1. Ler o enunciado do trabalho em grupo e implementar a minha parte: motor de prazos processuais com alertas (alta complexidade) e geração de documentos por template (alta complexidade).
2. Estruturar o projeto em arquitetura limpa, com DDD nos níveis preliminar, estratégico, tático e operacional.
3. Aplicar quatro ou mais dos padrões da lista do enunciado, cada um resolvendo um problema real do domínio.
4. Implementar a camada de persistência com mapeamento objeto-relacional e a camada de apresentação web.
5. Escrever os cenários BDD e automatizá-los com Cucumber.
6. Produzir a descrição do domínio com linguagem onipresente, o mapa da história do usuário, os protótipos de alta fidelidade e o modelo em Context Mapper (CML).
7. Adicionar login próprio por sessão (advogado/chefe), tela de processos e perfil PostgreSQL com Flyway.

Data: 15/09/2026 — João Victor G. C. Uchoa

---

## 2. Caio Sena

- **Login no repositório:** `Caiosenas2101` (`css4@cesar.school`)
- **Parte do trabalho (pelos commits):** contrato de honorários com cálculo fixo / por hora / quota litis; habilitação e revogação de OAB em documento sigiloso
- **Usou IA:** _( ) Não usei IA  ( ) Usei IA_

> Se **não usou**, basta marcar a opção acima e assinar.
> Se **usou**, preencher os campos abaixo.

- **Ferramenta(s) e versão:** _a preencher_
- **Parte do trabalho coberta pela IA:** _a preencher_
- **Como a IA foi usada** (modelagem / implementação / testes / documentação): _a preencher_
- **Verificação humana feita** (o que foi executado e conferido antes de entregar): _a preencher_
- **Registro das interações** (prompts principais ou link para o histórico): _a preencher_

Data: ___/___/______ — Caio Sena

---

## 3. Gustavo Laporte

- **Login no repositório:** `Gustavo Laporte` (`gustavo.laporte@finacap.com.br`)
- **Parte do trabalho (pelos commits):** cadastro de feriados com abrangência e recorrência; cadastro de modelo de peça com marcador de campo; juntada de arquivo aos autos reaproveitando o Proxy de sigilo
- **Usou IA:** _( ) Não usei IA  ( ) Usei IA_

> Se **não usou**, basta marcar a opção acima e assinar.
> Se **usou**, preencher os campos abaixo.

- **Ferramenta(s) e versão:** _a preencher_
- **Parte do trabalho coberta pela IA:** _a preencher_
- **Como a IA foi usada** (modelagem / implementação / testes / documentação): _a preencher_
- **Verificação humana feita** (o que foi executado e conferido antes de entregar): _a preencher_
- **Registro das interações** (prompts principais ou link para o histórico): _a preencher_

Data: ___/___/______ — Gustavo Laporte

---

## 4. Luis Eduardo Bérard

- **Login no repositório:** `Luis Eduardo Bérard` (`luisberard2004@gmail.com`)
- **Parte do trabalho (pelos commits):** fluxo de aprovação de documentos (State + Command); distribuição automática de processos (Chain of Responsibility)
- **Usou IA:** _( ) Não usei IA  ( ) Usei IA_

> Se **não usou**, basta marcar a opção acima e assinar.
> Se **usou**, preencher os campos abaixo.

- **Ferramenta(s) e versão:** _a preencher_
- **Parte do trabalho coberta pela IA:** _a preencher_
- **Como a IA foi usada** (modelagem / implementação / testes / documentação): _a preencher_
- **Verificação humana feita** (o que foi executado e conferido antes de entregar): _a preencher_
- **Registro das interações** (prompts principais ou link para o histórico): _a preencher_

Data: ___/___/______ — Luis Eduardo Bérard

---

## 5. Pedro Ferraz

- **Login no repositório:** `Pedro Ferraz` (`pvf@cesar.school`)
- **Parte do trabalho (pelos commits):** agenda de audiências, cliente e parte contrária — domínio, API REST, TDD e BDD
- **Usou IA:** _( ) Não usei IA  ( ) Usei IA_

> Se **não usou**, basta marcar a opção acima e assinar.
> Se **usou**, preencher os campos abaixo.

- **Ferramenta(s) e versão:** _a preencher_
- **Parte do trabalho coberta pela IA:** _a preencher_
- **Como a IA foi usada** (modelagem / implementação / testes / documentação): _a preencher_
- **Verificação humana feita** (o que foi executado e conferido antes de entregar): _a preencher_
- **Registro das interações** (prompts principais ou link para o histórico): _a preencher_

Data: ___/___/______ — Pedro Ferraz
