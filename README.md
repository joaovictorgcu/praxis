# praxis

Sistema de gestão para escritórios de advocacia — processos, prazos e honorários.
MVP acadêmico em **arquitetura multi-camadas** com **DDD** e padrões de projeto (GoF).

> Stack: Java 17, Spring Boot 4.1.1 (WebMVC + Data JPA + Validation), H2 em memória, JUnit 5, Maven Wrapper.

## Como rodar

```bash
./mvnw test          # 10 testes
./mvnw spring-boot:run
```

API em `http://localhost:8080`, console H2 em `/h2-console`.

## Camadas

| Camada | Pacote | Responsabilidade |
|---|---|---|
| Apresentação | `infrastructure.web` | `ProcessoController` — traduz HTTP em casos de uso |
| Aplicação | `application` | `ProcessoService`, `HonorarioService`, `DocumentoService`, `PraxisConfig` — orquestra, não decide regra |
| Domínio | `domain.*` | agregado `Processo`, entidades `Andamento`/`Prazo`, VOs `NumeroCnj`/`BaseCalculo`, serviço `CalendarioForense` |
| Infraestrutura | `infrastructure.persistence`, `infrastructure.notificacao` | JPA/H2, painel de notificações |

A dependência aponta sempre para dentro: o domínio não conhece Spring nem JPA além das anotações de mapeamento, e as estratégias/notificadores são injetados de fora (`PraxisConfig`).

## Bounded contexts

- **Gestão de Processos** (core) — `domain.processo`
- **Prazos & Agenda** — `domain.prazo` (contagem em dias úteis com feriados e recesso do art. 220 CPC)
- **Honorários** — `domain.honorario`
- **Documentos** — `domain.documento`

Linguagem onipresente preservada no código: processo, andamento, intimação, citação, prazo fatal, cumprido, honorário, quota litis, segredo de justiça, comarca, OAB habilitada.

## Padrões de projeto

| Padrão | Onde | Por que ali |
|---|---|---|
| **Observer** | `Processo.assinar/publicar`, `ObservadorProcesso`, `AdvogadoResponsavel` | novo andamento ou prazo em risco notifica os responsáveis, sem o agregado conhecer o canal |
| **Strategy** | `ContagemPrazoStrategy` (dias úteis × corridos) e `CalculoHonorarioStrategy` (fixo, por hora, quota litis) | a lei define regimes distintos; o agregado não deve saber qual está em vigor |
| **Template Method** | `GeradorDocumento` → `PeticaoInicial`, `Contestacao`, `Procuracao` | toda peça tem o mesmo esqueleto (cabeçalho, endereçamento, qualificação, corpo, pedidos, fechamento); só o corpo e os pedidos mudam. `Procuracao` sobrescreve o hook de endereçamento |
| **Proxy** | `DocumentoProxy` | documento em segredo de justiça (art. 189 CPC) valida OAB habilitada nos autos **antes** de carregar o objeto real |
| **Decorator** | `NotificadorPainel` decorado por `NotificadorEmail` e `NotificadorAuditoria` | canais e trilha de auditoria empilháveis sobre a notificação base |
| **Iterator** | `Processo implements Iterable<Andamento>` | linha do tempo em ordem cronológica sem expor a coleção interna |

## Regra de negócio central (BDD)

> **Dado** um processo com prazo fatal em 5 dias úteis, **quando** faltarem 3 dias, **então** o advogado responsável deve ser notificado.

Implementada em `Prazo.emRisco` + `Processo.verificarPrazos` e coberta por
`PrazoFatalNotificacaoTest.notificaResponsavelQuandoFaltamTresDiasUteis`:
intimação em 08/09/2026 → vencimento em 15/09/2026 (09, 10, 11, 14, 15); em 08/09 nenhum alerta, em 10/09 um alerta com e-mail e registro de auditoria.

## Endpoints

```
POST /api/processos                          cadastra processo + responsável
POST /api/processos/{numero}/andamentos      registra andamento (dispara Observer)
POST /api/processos/{numero}/prazos          abre prazo (calcula vencimento pela Strategy)
GET  /api/processos/{numero}/linha-do-tempo  timeline cronológica (Iterator)
POST /api/processos/varrer-prazos?hoje=YYYY-MM-DD   job de alerta de prazos fatais
```

Exemplo:

```bash
curl -X POST localhost:8080/api/processos -H 'Content-Type: application/json' -d '{
  "numeroCnj":"0001234-56.2026.8.17.0001","cliente":"Cliente Alfa","segredoJustica":false,
  "responsavelNome":"Dra. Ana","responsavelEmail":"ana@praxis.adv.br","responsavelOab":"PE12345"}'

curl -X POST 'localhost:8080/api/processos/0001234-56.2026.8.17.0001/prazos' \
  -H 'Content-Type: application/json' \
  -d '{"descricao":"Contestacao","intimacao":"2026-09-08","dias":5,"fatal":true}'

curl -X POST 'localhost:8080/api/processos/varrer-prazos?hoje=2026-09-10'
```

## Limites do MVP

- Notificação é in-process (painel/lista em memória); e-mail real e agendamento (`@Scheduled`) ficam para a Parte 2.
- Observadores são reanexados pela camada de aplicação a cada carregamento (campo `@Transient`), o que basta para o MVP mas não para múltiplas instâncias.
- H2 em memória: os dados se perdem no shutdown.
- Sem autenticação; a OAB do solicitante é passada explicitamente ao `DocumentoProxy`.
