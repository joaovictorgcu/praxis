# Domínio — gestão para escritórios de advocacia

## 1. Nível preliminar: o problema

Escritório de advocacia perde dinheiro e reputação por dois motivos operacionais recorrentes:

1. **Prazo perdido.** A intimação chega, alguém precisa contar o prazo (em dias úteis, com feriado e recesso forense), lembrar o responsável e registrar o cumprimento. Errar a contagem ou esquecer de avisar significa preclusão — e responsabilidade civil do escritório perante o cliente.
2. **Peça redigida do zero.** Petição inicial, contestação e procuração têm estrutura fixa e obrigatória; o que muda é o miolo. Copiar peça antiga e editar à mão gera erro de endereçamento, cliente trocado e cláusula esquecida.

O praxis resolve esses dois pontos primeiro: **motor de prazos com alertas** e **geração de documentos por template**. Cadastro de processo, andamentos e honorários existem para dar suporte a eles.

## 2. Linguagem onipresente

Termos usados pelo advogado, e usados no código com o mesmo significado — sem tradução para "genérico de software":

| Termo do domínio | Significado | Onde aparece no código |
|---|---|---|
| **Processo** | Ação judicial identificada por número CNJ | `domain.processo.Processo` (raiz de agregado) |
| **Número CNJ** | Identificador único no padrão `NNNNNNN-DD.AAAA.J.TR.OOOO` | `domain.processo.NumeroCnj` (Value Object autovalidado) |
| **Andamento** | Evento na tramitação (intimação, citação, audiência, despacho, sentença, juntada) | `domain.processo.Andamento`, `TipoAndamento` |
| **Intimação** | Ato que dá ciência à parte e **inicia contagem de prazo** | `Andamento.iniciaContagemDePrazo()` |
| **Prazo** | Período legal para praticar ato processual | `domain.prazo.Prazo` (raiz de agregado) |
| **Prazo fatal** | Prazo cuja perda extingue o direito (preclusão) | `Prazo.isFatal()` |
| **Termo inicial** | Primeiro dia da contagem — o dia útil seguinte à intimação (CPC art. 224) | `ContagemDiasUteis.calcularVencimento` |
| **Dias úteis / dias corridos** | Os dois regimes legais de contagem (CPC art. 219 × leis especiais) | `RegimeContagem`, `ContagemPrazoStrategy` |
| **Recesso forense** | Suspensão de prazos entre 20/12 e 20/01 (CPC art. 220) | `CalendarioForense.isDiaUtil` |
| **Calendário forense** | Dias sem expediente: fim de semana, feriado, recesso | `domain.prazo.CalendarioForense` |
| **Cumprir prazo** | Protocolar a peça e encerrar o prazo | `Prazo.cumprir(LocalDate)` |
| **Peça** | Documento processual (petição inicial, contestação, procuração) | `domain.documento.GeradorDocumento`, `TipoDocumento` |
| **Endereçamento** | Cabeçalho da peça dirigido ao juízo competente | `GeradorDocumento.enderecamento` |
| **Qualificação** | Identificação da parte na peça | `GeradorDocumento.qualificacao` |
| **Procuração ad judicia** | Instrumento de mandato para atuar no processo | `domain.documento.Procuracao` |
| **Segredo de justiça** | Restrição de acesso aos autos (CPC art. 189) | `DocumentoGerado.podeSerLidoPor`, `DocumentoProxy` |
| **OAB habilitada nos autos** | Advogado autorizado a ver processo sigiloso | `DocumentoGerado.getOabsHabilitadas()` |
| **Advogado responsável** | Quem responde pelos autos e recebe os alertas | `domain.processo.Advogado`, `AdvogadoResponsavel` |
| **Honorário** | Remuneração contratada (fixo, por hora, quota litis) | `domain.honorario.CalculoHonorarioStrategy` |
| **Quota litis** | Percentual sobre o proveito econômico, limitado a 30% (Código de Ética da OAB, art. 38) | `HonorarioQuotaLitis` |

## 3. Nível estratégico: subdomínios e bounded contexts

| Subdomínio | Tipo | Bounded context | Por quê |
|---|---|---|---|
| Gestão de Processos | **Core** | `GestaoDeProcessos` | É o cadastro do qual todo o resto depende, e onde vive a linha do tempo processual |
| Prazos & Agenda | **Core** (funcionalidade crítica) | `PrazosEAgenda` | A regra de contagem legal e a política de alerta são o diferencial do produto |
| Documentos | Apoio | `Documentos` | Reduz trabalho repetitivo; regra própria (segredo de justiça) |
| Honorários | Apoio | `Honorarios` | Modalidades de cobrança com limite ético |

Relações no mapa de contextos (arquivo `docs/praxis.cml`):

- `PrazosEAgenda` → `GestaoDeProcessos`: **Customer/Supplier**. O prazo referencia o processo por identidade (`NumeroCnj`) e copia o advogado responsável no momento da abertura — não navega para o agregado alheio.
- `Documentos` → `GestaoDeProcessos`: **Customer/Supplier**. Ao gerar a peça, o documento herda cliente, comarca, subscritor e o flag de segredo de justiça.
- `Honorarios` → `GestaoDeProcessos`: **Shared Kernel** do identificador de processo.

## 4. Nível tático: agregados, entidades e objetos de valor

**Agregados (raízes):**

- `Processo` — contém `Andamento`. É `Iterable<Andamento>`: a linha do tempo sai em ordem cronológica sem expor a coleção.
- `Prazo` — agregado próprio, referencia `Processo` por `NumeroCnj`. Agregado pequeno, transação curta: a varredura diária mexe em prazos, não em processos.
- `DocumentoGerado` — carrega o próprio controle de acesso (`podeSerLidoPor`).

**Objetos de valor:** `NumeroCnj`, `Advogado`, `Notificacao`, `AlertaPrazo`, `BaseCalculo`, `DadosDocumento`.

**Serviços de domínio:** `MotorDePrazos` (avalia prazos, escalona alerta, publica evento), `CalendarioForense` (dias sem expediente).

**Eventos de domínio:** `AndamentoRegistrado`, `PrazoEmRisco`, `PrazoVencido`, `DocumentoGerado` (`EventoProcesso`, sealed interface).

**Invariantes protegidas no construtor:** número CNJ válido; prazo com pelo menos 1 dia, intimação e responsável obrigatórios; documento nunca vazio; quota litis ≤ 30%; prazo não pode ser cumprido duas vezes.

## 5. Nível operacional: como as duas funcionalidades rodam

### Motor de prazos com alertas

1. Chega a intimação → `RegistrarAndamento` (Observer avisa o responsável).
2. `AbrirPrazo` calcula o vencimento pela `ContagemPrazoStrategy` do regime escolhido. O vencimento é **congelado**: mudança de calendário não move prazo já lançado.
3. Todo dia útil às 7h, `VarreduraDePrazosJob` chama `VarrerPrazos` (o mesmo caso de uso do endpoint manual).
4. `MotorDePrazos` calcula os dias contáveis restantes e a `PoliticaDeAlerta` deriva o nível: `ATENCAO` (≤5), `URGENTE` (≤3), `CRITICO` (≤1), `VENCE_HOJE` (0 ou vencido). Sempre o nível **mais severo** aplicável.
5. O nível é registrado no agregado e persistido: **cada marco é notificado uma única vez**, então rodar a varredura duas vezes no mesmo dia não gera aviso repetido.
6. O evento vai ao Observer, que monta a `Notificacao` e entrega pela cadeia de Decorators: painel → e-mail → trilha de auditoria em banco.
7. `CumprirPrazo` tira o prazo da varredura.

### Geração de documentos por template

1. `GerarDocumento` carrega o processo e escolhe o `GeradorDocumento` pelo tipo.
2. O `gerar()` é `final` (Template Method): a ordem cabeçalho → endereçamento → qualificação → corpo → pedidos → assinatura é regra do domínio. `Procuracao` sobrescreve os hooks porque procuração não se endereça ao juízo.
3. O `DocumentoGerado` nasce com o segredo de justiça do processo e a lista de OABs habilitadas.
4. A leitura nunca é direta: `BaixarDocumento` embrulha o repositório no `DocumentoProxy` com a OAB do solicitante. Sem OAB, ou OAB não habilitada em processo sigiloso, o acesso é negado antes de o conteúdo sair da camada de persistência.
5. O responsável é avisado da geração pelo mesmo Observer.
