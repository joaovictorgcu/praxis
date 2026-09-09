# praxis

Sistema web de gestão para escritórios de advocacia — processos, prazos e honorários.
Projeto acadêmico da disciplina de Requisitos e Fundamentos de Software (CESAR School), com **DDD**, **arquitetura limpa**, **padrões de projeto GoF**, persistência relacional com ORM e cenários **BDD automatizados com Cucumber**.

**Stack:** Java 17 · Spring Boot 4.1.1 (WebMVC, Data JPA, Validation, Thymeleaf) · H2 · JUnit 5 · Cucumber 7 · Maven Wrapper.

## Como rodar

```bash
./mvnw test            # 84 testes: unidade + contrato HTTP + 32 cenários BDD (271 steps)
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

### 4. Cadastro de modelos de documento

- O escritório cadastra **corpo** e **pedidos** com marcadores `{{campo}}`, e passa a gerar peça nova **sem alterar código**.
- Modelo cadastrado **coexiste** com as peças compiladas: `GeradorPorModelo` é mais uma subclasse do Template Method, então `gerar()` segue `final` e a ordem das seções continua sendo regra do domínio, não escolha do usuário.
- Marcadores resolvidos por **Interpreter**: `cliente`, `comarca`, `processo`, `advogado` e `oab` vêm dos autos automaticamente; os demais são cobrados na geração e o cadastro informa **quais campos cada modelo espera**.
- Campo não informado vira marcador visível na peça (`(valorDivida a preencher)`) em vez de lacuna silenciosa.
- Modelo que **não se endereça ao juízo** abandona as três seções do juízo de uma vez (endereçamento, qualificação e fecho), como a `Procuracao` faz — só que decidido pelo cadastro.
- O `TipoDocumento` da peça registrada nos autos vem do **modelo**, não do pedido. `PECA_AVULSA` existe para o que não é petição, contestação nem procuração.
- Editar modelo não altera peça já gerada, porque o documento persiste o próprio conteúdo.
- Sobe com dois modelos de exemplo (`COBRANCA_ALUGUEL` e `ACORDO_EXTRAJUDICIAL`). Desligue com `praxis.modelos-iniciais=false`.

### 5. Upload e anexação de arquivos ao processo

- Arquivo recebido de fora (procuração assinada, comprovante, laudo) é juntado aos autos e guardado **no banco**, em BLOB.
- **Agregado próprio** (`ArquivoAnexo`), e não um documento gerado com bytes: a peça nasce de template e tem seções; o anexo é binário opaco, com nome original, tipo e tamanho.
- O que os dois têm em comum é só a **regra de acesso**, extraída em `ConteudoRestrito`: o mesmo Proxy do segredo de justiça protege peça gerada e anexo, sem duplicar a checagem do art. 189.
- O anexo copia segredo de justiça e OABs habilitadas do processo **no momento da juntada** — anexo juntado quando os autos eram públicos não passa a ser sigiloso depois.
- Tipos aceitos são uma lista curta (PDF, JPEG, PNG, texto) e o limite é de 10 MB: autos eletrônicos não recebem binário qualquer, e recusar na porta evita anexo que o juízo não abre.
- O nome vem do computador de quem envia, então **não é confiável**: caminho de diretório é descartado (bloqueia `../`) e a extensão do tipo aceito é garantida.
- Juntada avisa o advogado responsável, pelo mesmo Observer dos demais eventos.
- Não existe caso de uso de remoção: documento juntado aos autos não se desanexa — retirar peça depende de decisão judicial (desentranhamento), que não é operação de tela.

Funcionalidades de apoio já no repositório: cadastro de processo, registro de andamento com linha do tempo cronológica, e cálculo de honorários (fixo, por hora, quota litis com limite ético de 30%).

## Arquitetura limpa

```
presentation/   REST (/api/**) e web Thymeleaf (/painel/**) — só traduz HTTP em caso de uso
application/    port/in (casos de uso), port/out (repositórios), usecase (orquestração)
domain/         processo, prazo, documento, modelo, anexo, feriado, notificacao, honorario, compartilhado — Java puro
infrastructure/ persistence (JPA + mappers + adapters), notificacao, scheduler, config
```

Regra de dependência: **nada no `domain` importa Spring ou JPA**. As entidades JPA vivem em `infrastructure.persistence.entity` e o `PersistenciaMapper` traduz nos dois sentidos; quem instancia e liga as classes de domínio é `infrastructure.config.DominioConfig`.

## DDD nos quatro níveis

| Nível | Onde está |
|---|---|
| Preliminar | [`docs/dominio.md`](docs/dominio.md) — problema, e por que estas duas funcionalidades primeiro |
| Estratégico | 4 subdomínios / bounded contexts e suas relações — [`docs/praxis.cml`](docs/praxis.cml) (Context Mapper) |
| Tático | Agregados `Processo`, `Prazo`, `DocumentoGerado`, `Feriado`, `ModeloDocumento`, `ArquivoAnexo`; VOs `NumeroCnj`, `Advogado`, `AlertaPrazo`, `BaseCalculo`, `Abrangencia`, `Jurisdicao`, `CodigoModelo`, `TextoModelo`; núcleo compartilhado `ConteudoRestrito`; serviços `MotorDePrazos`, `CalendarioForense`; eventos em `EventoProcesso` |
| Operacional | Casos de uso em `application.usecase`, job de varredura, endpoints REST e telas |

Linguagem onipresente preservada no código: processo, andamento, intimação, citação, prazo fatal, termo inicial, dias úteis, recesso forense, cumprir, peça, endereçamento, qualificação, procuração ad judicia, segredo de justiça, OAB habilitada, quota litis, feriado, abrangência, comarca, foro.

## Padrões de projeto (os 6 da lista do enunciado, mais Composite e Interpreter)

| Padrão | Onde | Problema real que resolve |
|---|---|---|
| **Strategy** | `ContagemPrazoStrategy` → `ContagemDiasUteis` / `ContagemDiasCorridos`; `CalculoHonorarioStrategy` → fixo / hora / quota litis; `RegraRecorrencia` → `DataUnica` / `RecorrenciaAnualFixa` | A lei define regimes distintos de contagem e de cobrança; o agregado não deve saber qual está em vigor. No feriado, separa *quando incide* de *onde vale* |
| **Composite** | `RegraDiaNaoUtil` → `FimDeSemana`, `RecessoForense`, `FeriadosFixos`, `FeriadosDoForo`, combinadas por `CalendarioForense` | Cada motivo de suspensão do expediente é independente; o calendário combina todos sem saber quantos são, e a origem dos feriados (constante em teste, cadastro em banco em produção) troca sem tocar no calendário |
| **Template Method** | `GeradorDocumento` → `PeticaoInicial`, `Contestacao`, `Procuracao`, `GeradorPorModelo` | A ordem das seções da peça é regra do domínio (`gerar()` é `final`); só corpo e pedidos mudam. `Procuracao` sobrescreve os hooks porque não se endereça ao juízo, e `GeradorPorModelo` lê os mesmos dois passos de um modelo cadastrado — é o que deixa a peça ser cadastrável sem abrir mão do padrão |
| **Interpreter** | `ExpressaoTexto` → `Literal` / `ReferenciaCampo`, montadas por `TextoModelo` e avaliadas contra `ContextoTexto` | O texto do modelo é uma linguagem mínima (trecho fixo + referência a campo); cada termo sabe se interpretar, então o modelo não faz varredura de string a cada geração nem precisa saber quais campos existem |
| **Observer** | `Processo` e `MotorDePrazos` publicam `EventoProcesso`; `AdvogadoResponsavel` observa | Novo andamento e prazo em risco precisam avisar o responsável sem o agregado conhecer e-mail nem banco |
| **Decorator** | `NotificadorPainel` decorado por `NotificadorEmail` e `NotificadorAuditoria` | Canais e trilha de auditoria empilháveis sobre a notificação base, sem `if` de canal |
| **Proxy** | `ProxyDeAcesso` sobre `ConteudoRestrito`, especializado em `DocumentoProxy` e `ArquivoProxy` | Segredo de justiça conferido antes de o conteúdo sair da persistência; impossível esquecer a checagem, porque o caso de uso só tem acesso ao Proxy. A regra do art. 189 é escrita uma vez e vale para peça gerada e arquivo anexado |
| **Iterator** | `Processo implements Iterable<Andamento>` | Linha do tempo em ordem cronológica sem expor a coleção interna |

## BDD

Cenários em português em [`src/test/resources/features`](src/test/resources/features), automatizados com Cucumber + Spring (`src/test/java/school/cesar/praxis/bdd`). Os steps exercitam os casos de uso reais contra o banco, não dublês.

> **Dado** um processo com prazo fatal em 5 dias úteis, **quando** faltarem 3 dias, **então** o advogado responsável deve ser notificado.

```
32 scenarios (32 passed)
271 steps (271 passed)
Tests run: 84, Failures: 0, Errors: 0
```

Os cenários chamam os casos de uso, então não cobrem o corpo da requisição dos controllers. `ModeloHttpTest` e `AnexoHttpTest` fecham essa lacuna pelo mesmo caminho do navegador (MockMvc) — foi assim que apareceram um `codigoModelo` faltando no `record` de requisição e um `500` onde devia haver `400`, ambos invisíveis para o BDD.

## Endpoints

```
POST /api/processos                            cadastra processo + responsável
POST /api/processos/{numero}/andamentos        registra andamento (dispara Observer)
GET  /api/processos/{numero}/linha-do-tempo    timeline cronológica (Iterator)

POST /api/prazos                               abre prazo (calcula vencimento pela Strategy)
GET  /api/prazos/agenda?ate=YYYY-MM-DD         agenda ordenada por vencimento
POST /api/prazos/{id}/cumprir                  registra cumprimento
POST /api/prazos/varredura?hoje=YYYY-MM-DD     roda o motor de prazos

POST /api/documentos                           gera peça (campo codigoModelo opcional escolhe o modelo)
GET  /api/documentos?processo=                 lista peças
GET  /api/documentos/{id}?oab=                 baixa a peça (passa pelo Proxy; 403 se não habilitada)

POST   /api/modelos                            cadastra modelo de peça (corpo e pedidos com {{campo}})
GET    /api/modelos                            lista modelos e os campos que cada um espera
DELETE /api/modelos/{id}                       remove modelo

POST /api/anexos                               junta arquivo aos autos (multipart)
GET  /api/anexos?processo=                     lista anexos (sem os bytes)
GET  /api/anexos/{id}?oab=                     baixa o arquivo (passa pelo Proxy; 403 se não habilitada)

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

Peça nova sem tocar no código, cadastrando um modelo:

```bash
curl -X POST localhost:8080/api/modelos -H 'Content-Type: application/json' -d '{
  "codigo":"ACORDO","nome":"Acordo extrajudicial","tipo":"PECA_AVULSA",
  "titulo":"Instrumento particular de acordo",
  "corpo":"As partes {{cliente}} e {{outraParte}} ajustam {{valorAcordo}}.",
  "pedidos":"CLAUSULAS a) quitacao reciproca.","enderecaAoJuizo":false}'
# -> camposEsperados: ["outraParte","valorAcordo"]  (cliente vem dos autos)

curl -X POST localhost:8080/api/documentos -H 'Content-Type: application/json' -d '{
  "numeroProcesso":"0001234-56.2026.8.17.0001","tipo":"PECA_AVULSA",
  "codigoModelo":"ACORDO","oabSolicitante":"PE12345",
  "campos":{"outraParte":"Imobiliaria Beta ME","valorAcordo":"R$ 9.000,00"}}'
# -> peca com o mesmo esqueleto das compiladas, sem linguagem de juizo
```

Juntada de arquivo e o sigilo dos autos:

```bash
curl -X POST localhost:8080/api/anexos \
  -F 'numeroProcesso=0007654-32.2026.8.17.0002' \
  -F 'arquivo=@laudo.pdf;type=application/pdf' \
  -F 'descricao=Laudo pericial' -F 'oab=PE54321'
# -> {"id":1,...,"segredoJustica":true}   (herdado do processo)

curl 'localhost:8080/api/anexos/1?oab=PE54321'   # -> 200, o arquivo
curl 'localhost:8080/api/anexos/1?oab=PE99999'   # -> 403, barrado pelo Proxy
```

Falha de domínio vira status HTTP correto (`TratadorDeErrosRest`): invariante violada
pela requisição é `400`, agregado inexistente é `404` e segredo de justiça é `403` —
nunca `500`.

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
- O modelo de documento define corpo e pedidos, não a ordem das seções: o esqueleto é regra do domínio. Modelo que precise de estrutura própria exigiria nova subclasse de `GeradorDocumento`.
- Modelos não têm versão. Editar o modelo não afeta peça já gerada (o documento persiste o conteúdo), mas o histórico do próprio modelo não é guardado.
- Os campos do modelo são texto simples, sem tipo nem obrigatoriedade: campo esquecido sai como `(nome a preencher)` na peça, e não barra a geração.
- Anexos são guardados em BLOB no banco. Simplifica o backup e a transação (arquivo e metadados commitam juntos), mas não escala para volume alto — a troca por armazenamento de objetos afeta apenas `ArquivoRepositorioJpa`.
- O conteúdo do anexo não é inspecionado: confia-se no `Content-Type` declarado no upload. Um PDF renomeado passaria. Validar assinatura de arquivo (magic number) e antivírus fica para produção.
- Anexo não tem versão nem desentranhamento: a juntada é definitiva na tela.
- O arquivo `.cml` não foi validado com o plugin do Context Mapper nesta máquina (extensão não instalada).
