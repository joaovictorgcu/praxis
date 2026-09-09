# praxis

Sistema web de gestão para escritórios de advocacia — processos, prazos e honorários.
Projeto acadêmico da disciplina de Requisitos e Fundamentos de Software (CESAR School), com **DDD**, **arquitetura limpa**, **padrões de projeto GoF**, persistência relacional com ORM e cenários **BDD automatizados com Cucumber**.

**Stack:** Java 17 · Spring Boot 4.1.1 (WebMVC, Data JPA, Validation, Thymeleaf) · H2 · JUnit 5 · Cucumber 7 · Maven Wrapper.

## Como rodar

```bash
./mvnw test            # 29 testes: unidade + 12 cenários BDD (80 steps)
./mvnw spring-boot:run # sobe em http://localhost:8080
```

- Interface web: <http://localhost:8080/painel>
- Console do banco: <http://localhost:8080/h2-console> (`jdbc:h2:mem:praxis`, usuário `sa`, sem senha)
- A aplicação sobe com carga de exemplo (dois processos, um deles em segredo de justiça, e três prazos em estados diferentes). Desligue com `praxis.dados-exemplo=false`.
- O calendário nasce com os feriados nacionais, mais um estadual e um comarcal de exemplo. Desligue com `praxis.feriados-iniciais=false`.
- O foro do escritório define quais feriados estaduais e comarcais contam: `praxis.foro.uf=PE` e `praxis.foro.comarca=Recife`.

## Funcionalidades implementadas nesta entrega

### 1. Motor de prazos processuais com alertas (complexidade alta)

- Contagem legal em **dias úteis** (CPC art. 219) ou **dias corridos**, com termo inicial no primeiro dia útil seguinte à intimação (art. 224), feriados e **recesso forense** de 20/12 a 20/01 (art. 220).
- Vencimento **congelado** na abertura do prazo: alterar o calendário depois não move prazo já lançado.
- Alerta **escalonado** por dias contáveis restantes: `ATENCAO` (≤5), `URGENTE` (≤3), `CRITICO` (≤1), `VENCE_HOJE` (0 ou vencido) — sempre o nível mais severo aplicável.
- **Idempotência por marco**: cada nível é notificado uma única vez por prazo, então rodar a varredura duas vezes no mesmo dia não gera aviso repetido.
- Varredura automática todo dia útil às 7h (`@Scheduled`) e endpoint manual que chama **o mesmo caso de uso**.
- Notificação entregue por cadeia de canais (painel, e-mail, trilha de auditoria em banco).
- Cumprimento de prazo retira o item da varredura.

### 2. Geração de documentos por template (complexidade alta)

- Petição inicial, contestação e procuração a partir de template com esqueleto fixo.
- Peça gerada é persistida, listada por processo e baixável em texto.
- Documento herda o **segredo de justiça** do processo e a lista de OABs habilitadas nos autos; leitura por OAB não habilitada é bloqueada (CPC art. 189) — verificado também via HTTP (`403`).
- Geração avisa o advogado responsável.

### 3. Cadastro de feriados

- Feriado com **data única** (ponto facultativo de um ano só) ou **recorrência anual fixa** (Natal, Tiradentes), escolhido por Strategy.
- **Abrangência** nacional, estadual (UF) ou da comarca: feriado de Olinda não suspende prazo que corre em Recife.
- O calendário forense passou a ser montado sobre o cadastro, e não sobre lista fixa no código: **feriado cadastrado vale na contagem seguinte, sem reiniciar** a aplicação.
- Prazo **já lançado não se move**, porque o vencimento fica congelado na abertura — verificado em cenário BDD.
- Fim de semana e recesso forense (art. 220) continuam no código, como regras de lei que o usuário não pode apagar por engano.
- Carga de referência com os feriados nacionais na subida, mais um exemplo estadual (PE) e um comarcal (Recife).

Funcionalidades de apoio já no repositório: cadastro de processo, registro de andamento com linha do tempo cronológica, e cálculo de honorários (fixo, por hora, quota litis com limite ético de 30%).

## Arquitetura limpa

```
presentation/   REST (/api/**) e web Thymeleaf (/painel/**) — só traduz HTTP em caso de uso
application/    port/in (casos de uso), port/out (repositórios), usecase (orquestração)
domain/         processo, prazo, documento, notificacao, honorario, compartilhado — Java puro
infrastructure/ persistence (JPA + mappers + adapters), notificacao, scheduler, config
```

Regra de dependência: **nada no `domain` importa Spring ou JPA**. As entidades JPA vivem em `infrastructure.persistence.entity` e o `PersistenciaMapper` traduz nos dois sentidos; quem instancia e liga as classes de domínio é `infrastructure.config.DominioConfig`.

## DDD nos quatro níveis

| Nível | Onde está |
|---|---|
| Preliminar | [`docs/dominio.md`](docs/dominio.md) — problema, e por que estas duas funcionalidades primeiro |
| Estratégico | 4 subdomínios / bounded contexts e suas relações — [`docs/praxis.cml`](docs/praxis.cml) (Context Mapper) |
| Tático | Agregados `Processo`, `Prazo`, `DocumentoGerado`, `Feriado`; VOs `NumeroCnj`, `Advogado`, `AlertaPrazo`, `BaseCalculo`, `Abrangencia`, `Jurisdicao`; serviços `MotorDePrazos`, `CalendarioForense`; eventos em `EventoProcesso` |
| Operacional | Casos de uso em `application.usecase`, job de varredura, endpoints REST e telas |

Linguagem onipresente preservada no código: processo, andamento, intimação, citação, prazo fatal, termo inicial, dias úteis, recesso forense, cumprir, peça, endereçamento, qualificação, procuração ad judicia, segredo de justiça, OAB habilitada, quota litis, feriado, abrangência, comarca, foro.

## Padrões de projeto (os 6 da lista do enunciado, mais Composite)

| Padrão | Onde | Problema real que resolve |
|---|---|---|
| **Strategy** | `ContagemPrazoStrategy` → `ContagemDiasUteis` / `ContagemDiasCorridos`; `CalculoHonorarioStrategy` → fixo / hora / quota litis; `RegraRecorrencia` → `DataUnica` / `RecorrenciaAnualFixa` | A lei define regimes distintos de contagem e de cobrança; o agregado não deve saber qual está em vigor. No feriado, separa *quando incide* de *onde vale* |
| **Composite** | `RegraDiaNaoUtil` → `FimDeSemana`, `RecessoForense`, `FeriadosFixos`, `FeriadosDoForo`, combinadas por `CalendarioForense` | Cada motivo de suspensão do expediente é independente; o calendário combina todos sem saber quantos são, e a origem dos feriados (constante em teste, cadastro em banco em produção) troca sem tocar no calendário |
| **Template Method** | `GeradorDocumento` → `PeticaoInicial`, `Contestacao`, `Procuracao` | A ordem das seções da peça é regra do domínio (`gerar()` é `final`); só corpo e pedidos mudam. `Procuracao` sobrescreve os hooks porque não se endereça ao juízo |
| **Observer** | `Processo` e `MotorDePrazos` publicam `EventoProcesso`; `AdvogadoResponsavel` observa | Novo andamento e prazo em risco precisam avisar o responsável sem o agregado conhecer e-mail nem banco |
| **Decorator** | `NotificadorPainel` decorado por `NotificadorEmail` e `NotificadorAuditoria` | Canais e trilha de auditoria empilháveis sobre a notificação base, sem `if` de canal |
| **Proxy** | `DocumentoProxy` | Segredo de justiça conferido antes de o conteúdo sair da persistência; impossível esquecer a checagem, porque o caso de uso só tem acesso ao Proxy |
| **Iterator** | `Processo implements Iterable<Andamento>` | Linha do tempo em ordem cronológica sem expor a coleção interna |

## BDD

Cenários em português em [`src/test/resources/features`](src/test/resources/features), automatizados com Cucumber + Spring (`src/test/java/school/cesar/praxis/bdd`). Os steps exercitam os casos de uso reais contra o banco, não dublês.

> **Dado** um processo com prazo fatal em 5 dias úteis, **quando** faltarem 3 dias, **então** o advogado responsável deve ser notificado.

```
18 scenarios (18 passed)
135 steps (135 passed)
Tests run: 46, Failures: 0, Errors: 0
```

## Endpoints

```
POST /api/processos                            cadastra processo + responsável
POST /api/processos/{numero}/andamentos        registra andamento (dispara Observer)
GET  /api/processos/{numero}/linha-do-tempo    timeline cronológica (Iterator)

POST /api/prazos                               abre prazo (calcula vencimento pela Strategy)
GET  /api/prazos/agenda?ate=YYYY-MM-DD         agenda ordenada por vencimento
POST /api/prazos/{id}/cumprir                  registra cumprimento
POST /api/prazos/varredura?hoje=YYYY-MM-DD     roda o motor de prazos

POST /api/documentos                           gera peça por template
GET  /api/documentos?processo=                 lista peças
GET  /api/documentos/{id}?oab=                 baixa a peça (passa pelo Proxy; 403 se não habilitada)

POST   /api/feriados                           cadastra feriado (data única ou anual; nacional/estadual/comarcal)
GET    /api/feriados                           lista o calendário cadastrado
DELETE /api/feriados/{id}                      remove feriado
GET    /api/feriados/dia-util?data=YYYY-MM-DD  corre prazo neste dia? (efeito do cadastro no motor)
```

Exemplo:

```bash
curl -X POST localhost:8080/api/prazos -H 'Content-Type: application/json' -d '{
  "numeroProcesso":"0001234-56.2026.8.17.0001","descricao":"Contestacao",
  "intimacao":"2026-09-04","quantidadeDias":5,"fatal":true,"regime":"DIAS_UTEIS"}'
# -> vencimento 2026-09-14 (04/09 sexta; 07/09 feriado; conta 08, 09, 10, 11 e 14)

curl -X POST 'localhost:8080/api/prazos/varredura?hoje=2026-09-09'
# -> alerta URGENTE, 3 dias restantes
```

O cadastro de feriados muda a contagem sem reiniciar a aplicação:

```bash
curl 'localhost:8080/api/feriados/dia-util?data=2026-10-15'
# -> {"diaUtil":true,...}

curl -X POST localhost:8080/api/feriados -H 'Content-Type: application/json' -d '{
  "descricao":"Aniversario do Recife","data":"2026-10-15",
  "repeteTodoAno":true,"nivel":"COMARCAL","abrangencia":"Recife"}'

curl 'localhost:8080/api/feriados/dia-util?data=2026-10-15'
# -> {"diaUtil":false,"proximoDiaUtil":"2026-10-16"}  e o mesmo em 2027, porque e anual
```

## Documentação

- [`docs/dominio.md`](docs/dominio.md) — descrição do domínio e linguagem onipresente, DDD nos 4 níveis
- [`docs/mapa-historia-usuario.md`](docs/mapa-historia-usuario.md) — mapa da história do usuário
- [`docs/praxis.cml`](docs/praxis.cml) — modelo dos subdomínios em Context Mapper (CML)
- [`docs/prototipos/`](docs/prototipos/) — protótipos de alta fidelidade (capturas da interface real)
- [`docs/uso-de-ia.md`](docs/uso-de-ia.md) — declaração de uso de IA e registro das interações

## Limites desta entrega

- E-mail é registrado em log e memória (o `NotificadorEmail` é o ponto de troca por `JavaMailSender`).
- Observadores são reanexados pela camada de aplicação a cada carregamento do agregado — suficiente para instância única, não para escala horizontal.
- H2 em memória: dados se perdem no shutdown. Trocar para PostgreSQL altera apenas `application.properties`.
- Sem autenticação: a OAB do solicitante é informada na requisição, não extraída de sessão.
- O foro dos feriados é único e vem de propriedade (`praxis.foro.*`), não de cada processo: o `Processo` guarda a comarca, mas não a UF. Feriado por processo exigiria derivar a UF do código do tribunal no número CNJ.
- O cadastro de feriados é mantido em memória pelo adaptador (`FeriadoRepositorioJpa`), porque o calendário pergunta dia a dia ao percorrer um prazo. A escrita descarta o cache — suficiente para instância única, não para escala horizontal.
- O arquivo `.cml` não foi validado com o plugin do Context Mapper nesta máquina (extensão não instalada).
