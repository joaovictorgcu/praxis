# Como o praxis e construido

Arquitetura limpa, DDD nos quatro niveis, padroes de projeto, front, seguranca e testes.

---

**Stack:** Java 17 · Spring Boot 4.1.1 (WebMVC, Data JPA, Validation, Thymeleaf) · H2 (dev/test) · PostgreSQL + Flyway (prod) · JUnit 5 · Cucumber 7 · Maven Wrapper.

### Arquitetura limpa

```
presentation/   REST (/api/**) e web Thymeleaf (/painel/**, login e guarda de sessão) — só traduz HTTP em caso de uso
application/    port/in (casos de uso), port/out (repositórios), usecase (orquestração)
domain/         processo, prazo, documento, modelo, anexo, feriado, notificacao, honorario, usuario, compartilhado — Java puro
infrastructure/ persistence (JPA + mappers + adapters), notificacao, scheduler, seguranca (PBKDF2), config
```

A regra de dependência é fácil de enunciar e fácil de verificar: **nada no `domain` importa Spring ou JPA**. As entidades JPA vivem em `infrastructure.persistence.entity` e o `PersistenciaMapper` traduz nos dois sentidos; quem instancia e liga as classes de domínio é `infrastructure.config.DominioConfig`.

Na prática, isso significa que a regra do art. 219 do CPC é testável sem subir Spring, sem banco e sem HTTP.

### DDD nos quatro níveis

| Nível | Onde está |
|---|---|
| Preliminar | [`docs/dominio.md`](dominio.md) — o problema, e por que estas duas funcionalidades primeiro |
| Estratégico | 4 subdomínios / bounded contexts e suas relações — [`docs/praxis.cml`](praxis.cml) (Context Mapper) |
| Tático | Agregados `Processo`, `Prazo`, `DocumentoGerado`, `Feriado`, `ModeloDocumento`, `ArquivoAnexo`, `Usuario`; VOs `NumeroCnj`, `Advogado`, `AlertaPrazo`, `BaseCalculo`, `Abrangencia`, `Jurisdicao`, `CodigoModelo`, `TextoModelo`; núcleo compartilhado `ConteudoRestrito`; serviços `MotorDePrazos`, `CalendarioForense`; eventos em `EventoProcesso` |
| Operacional | Casos de uso em `application.usecase`, job de varredura, endpoints REST e telas |

A linguagem onipresente foi preservada no código, não traduzida para jargão de programador: processo, andamento, intimação, citação, prazo fatal, termo inicial, dias úteis, recesso forense, cumprir, peça, endereçamento, qualificação, procuração ad judicia, segredo de justiça, OAB habilitada, quota litis, feriado, abrangência, comarca, foro.

### Padrões de projeto

Os 6 padrões da lista do enunciado, mais Composite e Interpreter. Cada um entrou por um problema real — nenhum foi encaixado depois para cumprir tabela.

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

### O front: sem framework, mas com o básico inegociável

Uma folha de estilo (`/css/praxis.css`), um arquivo de comportamento (`/js/praxis.js`) e Thymeleaf. Não há build de front, e as decisões abaixo existem para que isso não custe acessibilidade nem desempenho.

- **Atalho de teclado** em toda tela: o primeiro `Tab` revela "Ir para o conteudo" e pula o menu inteiro (WCAG 2.4.1). O link do menu correspondente à tela atual leva `aria-current="page"`, então o leitor de tela anuncia onde você está — antes era só uma cor diferente.
- **Tema claro e escuro** pela preferência do sistema (`prefers-color-scheme`). Toda cor virou token em `:root`; o tema escuro redefine os tokens e quase nenhuma regra precisa saber que ele existe.
- **Movimento é preferência do usuário**: com `prefers-reduced-motion`, a rolagem suave e as transições são desligadas (WCAG 2.3.3).
- **Folha de impressão**: peça, ficha e agenda vão ao papel sem menu, sem formulário e sem botão; o cabeçalho da tabela repete a cada página e links externos imprimem o endereço. Peça aprovada é levada impressa para a audiência — essa é a tela que mais vai para a impressora.
- **Cabeçalho fixo** com `scroll-margin-top` nos alvos de âncora, para o foco nunca ficar escondido atrás dele (WCAG 2.4.11); em tela estreita o cabeçalho volta a rolar, porque ali o menu ocupa três linhas. Nas tabelas longas da administração, o cabeçalho de coluna acompanha a rolagem.
- **Alvo de toque** de no mínimo 24 px nos botões de tabela (WCAG 2.5.8), e caixas de seleção maiores.
- **Feedback de envio**: ao submeter, o botão vira "Enviando..." com `aria-busy` e trava contra duplo clique; voltar pelo histórico o destrava.
- **Desempenho**: HTML, CSS, JS e JSON saem comprimidos; CSS e JS levam o hash do conteúdo na URL (`/css/praxis-<hash>.css`) e são cacheados por um ano — publicar versão nova troca a URL e invalida sozinho.

`FrontHttpTest` cobre o que é invisível a olho nu: o atalho existe em todas as telas do painel, o menu marca a página atual e os estáticos saem com cache longo.

### Segurança do painel

- Login próprio por sessão HTTP (sem Spring Security). A sessão guarda só uma projeção do usuário (nome, OAB, papel) — nunca a senha.
- Senha com **PBKDF2-HMAC-SHA256** (sal por usuário, 120 mil iterações) do próprio JDK, atrás da porta de domínio `CodificadorDeSenha`: o agregado `Usuario` não sabe o algoritmo.
- Ação de chefe é marcada com `@SomenteChefe` e barrada pelo `SessaoInterceptor` com `403`, mesmo que o POST seja montado na mão.
- **CSRF** em todo POST do painel (`_csrf` hidden ou cabeçalho `X-CSRF-Token`); sessão só por cookie (`HttpOnly`, `SameSite=Lax`, sem `;jsessionid` na URL) e recriada ao autenticar (evita fixação).
- **Cabeçalhos de segurança** em toda resposta: `Content-Security-Policy` (`script-src 'self'`, `frame-ancestors 'none'`), `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy` e `Cache-Control: no-store` nas páginas autenticadas. É por isso que as telas não têm JavaScript inline: o comportamento (confirmações, anti-duplo-clique) mora em `/js/praxis.js` e é ligado por atributos `data-*`.

---

---

## Testes e BDD

```
53 scenarios (53 passed)
481 steps (481 passed)
Tests run: 174, Failures: 0, Errors: 0
```

Os cenários são escritos em português, em [`src/test/resources/features`](../src/test/resources/features), e automatizados com Cucumber + Spring (`src/test/java/school/cesar/praxis/bdd`). Os steps exercitam os casos de uso reais contra o banco, não dublês.

> **Dado** um processo com prazo fatal em 5 dias úteis, **quando** faltarem 3 dias, **então** o advogado responsável deve ser notificado.

Como os cenários chamam os casos de uso, eles não cobrem o corpo da requisição dos controllers. `ModeloHttpTest`, `AnexoHttpTest` e `LoginHttpTest` fecham essa lacuna pelo mesmo caminho do navegador (MockMvc) — foi assim que apareceram um `codigoModelo` faltando no `record` de requisição e um `500` onde devia haver `400`, ambos invisíveis para o BDD.

---
