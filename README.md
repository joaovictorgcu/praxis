# praxis

Sistema web de gestão para escritórios de advocacia — processos, prazos e honorários.

Projeto acadêmico da disciplina de Requisitos e Fundamentos de Software (CESAR School), feito com **DDD**, **arquitetura limpa**, **padrões de projeto GoF**, persistência relacional com ORM e cenários **BDD automatizados com Cucumber**.

![Agenda do dia: contadores, prazos por vencimento e os avisos emitidos pelo motor de prazos](docs/prototipos/painel-agenda.png)

---

## Por que este sistema existe

Um escritório de advocacia perde causas por **prazo** e perde tempo por **retrabalho**.

Prazo processual não perdoa: se a contestação vence numa terça e ninguém protocola, a causa está perdida — e não adianta alegar que o advogado estava viajando ou que ninguém avisou. Contar esse prazo à mão é mais difícil do que parece: só se contam **dias úteis**, o dia da intimação não conta, feriado da comarca conta como feriado, feriado da cidade vizinha não, e entre 20 de dezembro e 20 de janeiro o prazo simplesmente não corre.

Do outro lado, toda peça que sai do escritório (petição, contestação, procuração) tem estrutura obrigatória e leva o nome e a OAB de quem assina. Redigir cada uma do zero, no editor de texto, é como um advogado júnior esquece o endereçamento — ou como um sócio descobre tarde demais que a peça saiu sem revisão.

O praxis existe para resolver essas duas coisas:

1. **Nenhum prazo processual passa em silêncio.** O advogado lança a intimação; o sistema calcula o vencimento pela regra do CPC (dias úteis, feriados do foro, recesso forense), congela essa data e avisa o responsável de forma escalonada — 5, 3, 1 e 0 dias antes, e de novo se o prazo for perdido — sem repetir o mesmo aviso duas vezes.
2. **Nenhuma peça sai do escritório sem estrutura nem revisão.** Petição, contestação e procuração nascem de um template com esqueleto fixo (ou de um modelo cadastrado pelo próprio escritório), ficam registradas nos autos, respeitam o segredo de justiça e só são protocoladas depois de passar pela revisão do chefe.

Em volta disso ficam os apoios que o dia a dia exige: cadastro de processos com linha do tempo, anexos, feriados, modelos de peça, honorários, audiências, clientes, partes contrárias — e o controle de quem pode fazer o quê (advogado × chefe).

---

## Comece por aqui

Dois comandos, sem instalar banco nem configurar nada:

```bash
./mvnw test            # 174 testes: unidade + contrato HTTP + 53 cenários BDD (481 steps)
./mvnw spring-boot:run # sobe em http://localhost:8080
```

Abra <http://localhost:8080/painel>. Ele pede login, porque todo o painel é autenticado.

A aplicação já sobe com um escritório de mentira montado: dois processos (um deles em segredo de justiça), quatro prazos em estados diferentes (inclusive um vencido e um já cumprido), uma peça esperando revisão, dois clientes, uma parte contrária, uma audiência marcada, dois contratos de honorário, um anexo nos autos, dois modelos de peça e o calendário de feriados nacionais. É o suficiente para percorrer o sistema inteiro sem cadastrar nada.

**Com quem entrar:**

| Papel | Nome | Usuário no login | Senha | OAB |
|---|---|---|---|---|
| Chefe (admin) | Administrador | `admin` | `123` | ADMIN |
| Chefe | Carla Mendes | `carla.mendes` | `praxis123` | PE00001 |
| Advogada | Ana Beatriz Souza | `ana.souza` | `praxis123` | PE12345 |
| Advogado | Bruno Carvalho | `bruno.carvalho` | `praxis123` | PE54321 |

Basta o nome curto: sem `@`, o sistema completa o domínio (`praxis.dominio-email`). Ana é responsável pelo processo público; Bruno, pelo processo em segredo de justiça; Carla e o admin são chefes e podem aprovar peças. A senha dos usuários de exemplo vem de `praxis.senha-inicial` — troque em produção.

> **Sugestão de roteiro:** entre como `admin` para ver tudo, depois como `ana.souza` para sentir o que um advogado *não* pode fazer.

Outros endereços e chaves úteis:

- Console do banco: <http://localhost:8080/h2-console> (JDBC `jdbc:h2:mem:praxis`, usuário `sa`, sem senha)
- Desligar a carga de exemplo: `praxis.dados-exemplo=false` · feriados iniciais: `praxis.feriados-iniciais=false` · modelos: `praxis.modelos-iniciais=false` · usuários: `praxis.usuarios-iniciais=false`
- O foro do escritório define quais feriados estaduais e comarcais contam: `praxis.foro.uf=PE` e `praxis.foro.comarca=Recife`

---

## Visita guiada: um dia no escritório

Esta seção percorre o sistema na ordem em que ele é usado de verdade. Todas as imagens são capturas da interface real, não desenhos — estão em [`docs/prototipos/`](docs/prototipos/) e são regeradas por script.

### 1. Entrar

![Tela de login: usuário curto ou e-mail, senha, e a explicação dos dois papéis](docs/prototipos/login.png)

A tela aceita o nome curto (`ana.souza`) ou o e-mail inteiro. Nada do painel é visível antes do login.

E quando alguém erra?

![Login recusado com a mensagem genérica "e-mail ou senha invalidos"](docs/prototipos/login-erro.png)

A mensagem é **de propósito** vaga. Se o sistema dissesse "usuário não encontrado", qualquer um poderia descobrir quem tem conta no escritório testando e-mails. Errar cinco vezes seguidas bloqueia aquele e-mail por um minuto — um freio simples contra tentativa de senha em massa.

### 2. A agenda do dia

![Agenda: contadores no topo, prazos ordenados por vencimento e a fila de avisos](docs/prototipos/painel-agenda.png)

É a primeira tela depois do login, e responde à pergunta que o advogado faz ao chegar: **o que vence hoje?**

- Os cinco contadores no topo separam o que é urgente (prazos vencidos, vencendo em até 3 dias) do que é trabalho acumulado (peças esperando revisão, rascunhos a retrabalhar).
- A agenda lista os prazos **em aberto**, do mais próximo ao mais distante, com a etiqueta `fatal` ou `comum` e o advogado responsável. Prazo cumprido some daqui.
- Embaixo, os avisos que o motor de prazos já emitiu — é a caixa de entrada do escritório.

### 3. Achar o processo

![Lista de processos com busca e formulário de cadastro](docs/prototipos/processos.png)

Busca por número CNJ, cliente ou comarca. No cadastro, o responsável é **escolhido entre os usuários do escritório** — não é um nome digitado à mão, porque é a OAB dele que vai assinar as peças e receber os avisos.

### 4. A ficha do processo: onde o trabalho acontece

![Ficha do processo: cabeçalho, registrar andamento, abrir prazo, linha do tempo, prazos, peças e anexos](docs/prototipos/processo-ficha.png)

Esta é a tela mais importante do sistema. Ela reúne, em uma página:

- **O cabeçalho** com cliente, comarca, responsável e a última intimação — e a etiqueta `publico` ou `segredo`.
- **Registrar andamento**: cada movimentação do processo (intimação, citação, audiência, despacho, sentença, juntada). Intimação e citação são as que **iniciam contagem de prazo**.
- **Abrir prazo**: você informa a data da intimação, quantos dias e o regime; o sistema calcula o vencimento. Repare no texto da tela — ele explica a regra que está sendo aplicada, com os artigos do CPC. O usuário não precisa confiar cegamente.
- **Linha do tempo** sempre em ordem cronológica, independente da ordem em que os andamentos foram digitados.
- **Prazos, peças e anexos** daquele processo, cada um com o que falta fazer.

Um detalhe que vale o olhar: o prazo "Contestacao" foi aberto com intimação em 12/09 e 15 dias úteis, e venceu em **02/10/2026** — o sistema pulou fins de semana e feriado. Ninguém contou no calendário de parede.

### 5. O motor de prazos trabalhando

O sistema não espera alguém olhar a agenda. Todo dia útil às 7h ele varre os prazos em aberto e avisa quem precisa ser avisado. Na tela, dá para simular uma data e rodar a mesma varredura na hora:

![Varredura rodada simulando uma data: prazo vencido em vermelho e os avisos ATENCAO e VENCIDO](docs/prototipos/agenda-varredura.png)

O que aconteceu aqui:

- O prazo "Embargos de declaracao" venceu em 14/09 e ninguém cumpriu: a linha fica vermelha, o contador de vencidos sobe e sai um aviso **[VENCIDO] Prazo perdido** para o responsável. Perder prazo é grave; o sistema não deixa isso passar em silêncio nem depois do fato consumado.
- O prazo "Contestacao" entrou na faixa de 5 dias e gerou um **[ATENCAO]**.
- A faixa verde diz "2 alerta(s) emitido(s)". Rodar de novo no mesmo dia emite **zero** — cada nível de alerta é avisado uma única vez por prazo. Sem isso, o advogado receberia o mesmo aviso todo dia e pararia de ler.

Os níveis são escalonados por dias úteis restantes: `ATENCAO` (≤5) → `URGENTE` (≤3) → `CRITICO` (≤1) → `VENCE_HOJE` (0) → `VENCIDO`. E, por política padrão do escritório, **só prazo fatal dispara alerta**: prazo comum aparece na agenda, mas não interrompe o dia de ninguém.

### 6. Gerar a peça

![Gerar peça: escolhido o modelo, a tela pede um campo por marcador do template](docs/prototipos/documentos-gerar.png)

O advogado escolhe o processo, o tipo de peça e (opcionalmente) um modelo cadastrado. Quando escolhe o modelo `COBRANCA_ALUGUEL`, a tela **descobre sozinha** quais campos aquele modelo precisa (`enderecoImovel`, `valorDivida`) e pede só esses. Cliente, comarca, número do processo, advogado e OAB não são pedidos: já estão nos autos.

E de onde vêm esses modelos? Do próprio escritório, sem programador:

![Cadastro de modelo: corpo e pedidos com marcadores, e os campos detectados enquanto se digita](docs/prototipos/modelos.png)

Você escreve o corpo e os pedidos usando `{{marcadores}}`. Enquanto digita, a linha de baixo mostra quais campos serão pedidos na hora de gerar — os que vêm dos autos aparecem riscados, porque o sistema já os conhece. A caixa "Endereça ao juízo" existe porque uma procuração não se dirige ao juiz: desmarcando, as seções de endereçamento, qualificação e fecho somem de uma vez.

O que o modelo **não** controla é a ordem das seções (cabeçalho → endereçamento → qualificação → corpo → pedidos → assinatura). Isso é regra do domínio, não preferência de quem cadastra.

### 7. A revisão do chefe

Peça gerada nasce como **rascunho**. Ela não vai para o fórum antes de passar por gente:

```
RASCUNHO → EM_REVISAO → APROVADO ou REJEITADO → (aprovado) PROTOCOLADO
```

![Peça em revisão: o chefe vê o texto completo, aprova com comentário ou rejeita com motivo](docs/prototipos/documento-aprovacao.png)

O chefe vê a peça inteira — com o esqueleto obrigatório preenchido e a assinatura do responsável — e decide. Aprovar aceita um comentário; **rejeitar exige um motivo**, porque devolver trabalho sem dizer o porquê não ajuda ninguém.

Depois de aprovada, a tela muda:

![Peça aprovada: botões Protocolar e Desfazer decisão, e o histórico de transições com autor e comentário](docs/prototipos/documento-aprovado.png)

Os botões de aprovar/rejeitar desaparecem e surgem **Protocolar** e **Desfazer decisão**. Embaixo fica o histórico: quem mudou o quê, quando, com qual OAB e com qual comentário. Nada se perde — inclusive um "desfazer", que volta a peça para revisão e **fica registrado** em vez de sumir.

Quem é advogado e não chefe vê, no mesmo lugar, apenas "Aguardando revisao do chefe do escritorio".

### 8. Juntar arquivos aos autos

![Juntar arquivo: tipos aceitos, limite de tamanho e a lista de arquivos já juntados](docs/prototipos/anexos.png)

Nem todo documento nasce dentro do sistema: procuração assinada, comprovante e laudo pericial chegam de fora. A tela diz claramente o que aceita (PDF, JPEG, PNG, texto, até 10 MB) — recusar na porta evita juntar aos autos um arquivo que o juízo não consegue abrir.

Não existe botão de remover. **Documento juntado aos autos não se desanexa**: retirar peça dos autos depende de decisão judicial (desentranhamento), não de um clique.

### 9. Segredo de justiça: a regra que o sistema não deixa esquecer

Processo em segredo de justiça (CPC art. 189) só pode ser lido pelos advogados habilitados nos autos. Veja a mesma ficha de processo, agora com a advogada Ana logada — que **não** está habilitada:

![Ficha de processo em segredo de justiça vista por advogada sem OAB habilitada: aviso vermelho](docs/prototipos/processo-sigiloso-sem-oab.png)

O aviso vermelho não é decoração. A peça e o anexo desse processo respondem **403** para ela — na tela e também na API, se alguém tentar montar a requisição na mão. A checagem vive num Proxy pelo qual todo conteúdo restrito obrigatoriamente passa; é impossível esquecer de conferir, porque não existe caminho que desvie dele. Para liberar, um chefe habilita a OAB dela na peça.

### 10. Feriados: o calendário que alimenta a contagem

![Cadastro de feriados: recorrência, abrangência e a consulta "corre prazo em"](docs/prototipos/feriados.png)

Aqui fica o motivo pelo qual a contagem de prazos acerta. Cada feriado tem:

- **Quando incide**: data única (um ponto facultativo de 2026) ou todo ano na mesma data (Natal, Tiradentes).
- **Onde vale**: nacional, estadual (UF) ou da comarca. Feriado de Olinda **não** suspende prazo que corre em Recife — e o sistema sabe disso porque conhece o foro do escritório.

A caixinha "Corre prazo em" responde, para qualquer data, se aquele dia conta — é a forma mais direta de conferir o efeito de um cadastro.

Duas decisões importantes: feriado cadastrado vale já na **próxima contagem**, sem reiniciar a aplicação; e **prazo já lançado não se move**, porque o vencimento foi congelado na abertura. Cadastrar um feriado hoje não pode mudar retroativamente uma data que o advogado já anotou na agenda.

Fim de semana e recesso forense (20/12 a 20/01) não aparecem nessa lista: são regra de lei, ficam no código e ninguém pode apagá-los por engano.

### 11. Pessoas: usuários, papéis e senha provisória

![Gestão de usuários pelo chefe: cadastro com papel e senha provisória](docs/prototipos/usuarios.png)

Só o chefe cadastra e remove usuários — com duas travas: ninguém remove a si mesmo, e o escritório não pode ficar sem nenhum chefe.

A senha que o chefe define é **provisória**. Quando o novo usuário entra, o painel fica assim:

![Primeiro acesso com senha provisória: só a tela Minha conta está disponível](docs/prototipos/senha-provisoria.png)

Nada além de *Minha conta* funciona até ele trocar a senha — assim o chefe nunca sabe a senha definitiva de ninguém. Repare também no menu: Diego é `ADVOGADO`, então não existe "Administracao" ali.

![Minha conta: troca de senha exigindo a senha atual](docs/prototipos/conta.png)

E se um advogado digitar o endereço da tela de chefe direto na barra do navegador?

![403: acesso negado, esta ação é reservada ao chefe do escritório](docs/prototipos/sem-permissao.png)

Esconder o link do menu é conforto, não segurança. A proteção de verdade está no servidor, e o teste cobre os dois caminhos.

### 12. Administração: o escritório inteiro em uma tela

`/painel/admin`, reservada ao chefe. Antes dela, saber o que o sistema guardava exigia passar por seis telas — e três cadastros (cliente, parte contrária e audiência) **não tinham tela nenhuma**, só API REST.

![Administração: contadores, o que exige atenção, atalhos e ficha da instância](docs/prototipos/admin-panorama.png)

No topo, os contadores de doze cadastros, cada um ancorado na sua tabela. Os que exigem ação mudam de cor: prazo vencido fica vermelho; peça aguardando revisão e usuário com senha provisória ficam âmbar. A "ficha da instância" (perfil ativo, banco, Flyway, tempo no ar) responde "qual banco esta instância está usando?" sem abrir terminal.

![Administração: usuários, processos, prazos (com os cumpridos), peças e anexos](docs/prototipos/admin-cadastros.png)

Depois vêm as tabelas, uma seção por cadastro. Os prazos trazem os **já cumpridos** (etiqueta verde), que a agenda do dia esconde, e os vencidos em aberto aparecem com a linha em vermelho.

![Administração: clientes, partes contrárias, audiências e contratos de honorário](docs/prototipos/admin-relacionados.png)

Os três cadastros sem tela própria aparecem aqui, inclusive os desativados, marcados pela etiqueta de situação.

![Administração: modelos, feriados e avisos emitidos](docs/prototipos/admin-apoio.png)

E, no fim, o que sustenta o resto: modelos de peça, o calendário forense que alimenta a contagem de prazos e a fila de avisos emitidos por esta instância.

**Três decisões desta tela que valem a discussão:**

- **A tela só lê.** Criar e remover continua em cada cadastro, que é onde a regra vive: remover usuário não pode deixar o escritório sem chefe; remover feriado muda a contagem de prazo de todo mundo. Duplicar esses formulários aqui duplicaria a regra — ou, pior, deixaria uma cópia sem ela.
- **Consulta própria, em vez de reaproveitar as do dia a dia.** As listagens existentes servem à tela do dia e por isso escondem o que já saiu de cena (a agenda só devolve prazo em aberto; cliente, parte e audiência filtram os ativos). Mudar esses métodos para trazer tudo estragaria as telas que dependem deles. Foram criadas operações novas — `PrazosUseCases.ListarTodosOsPrazos`, `listarTodosOsClientes()`, `listarTodasAsPartesContrarias()`, `listarTodasAsAudiencias()` — e as antigas ficaram como estavam.
- **`ConsultarPanorama` não fala com repositório.** O `PanoramaAppService` compõe casos de uso que já existem; nenhuma consulta nova desce à persistência por fora das portas. Assim a tela não vira um segundo caminho até o banco, com regra de leitura própria.

E o que ela não deixa vazar: `@SomenteChefe` na classe (advogado que montar a URL recebe `403`); a senha codificada não entra no panorama e portanto não chega ao HTML (`AdminHttpTest` falha se chegar); a URL do banco é mostrada sem os parâmetros, que em alguns drivers carregam credencial.

---

## As regras que o domínio faz cumprir

Cada regra abaixo tem teste de unidade ou cenário BDD. Artigos citados são do CPC/2015.

**Processo e andamentos**
- Processo tem número **CNJ** válido (`NNNNNNN-DD.AAAA.J.TR.OOOO`), cliente, comarca e **um advogado responsável** (nome, e-mail, OAB); o número é único.
- Pode nascer em **segredo de justiça** (art. 189): a OAB do responsável fica habilitada nos autos; qualquer outra precisa ser habilitada pelo chefe.
- Andamento tem data, descrição e tipo (intimação, citação, audiência, despacho, sentença, juntada, outro). A linha do tempo é sempre **cronológica**, independente da ordem de registro.
- **Intimação e citação** são os andamentos que iniciam contagem de prazo; registrar andamento avisa o responsável.

**Prazos (motor de prazos)**
- Prazo tem descrição, data da intimação, quantidade de dias (≥ 1), regime e é **fatal** ou comum; o responsável é o do processo.
- Regime **dias úteis** (art. 219, para prazos processuais) ou **dias corridos** (prazos materiais).
- **Termo inicial**: o primeiro dia útil seguinte à intimação (art. 224); o vencimento cai no último dia da contagem e, se for dia sem expediente, prorroga para o próximo útil.
- Não contam como dia útil: **fim de semana**, **feriados** que valem para o foro (nacional, da UF ou da comarca) e o **recesso forense de 20/12 a 20/01** (art. 220). Fim de semana e recesso são regra de lei, não cadastráveis.
- O vencimento é **calculado e congelado na abertura**: cadastrar ou remover feriado depois não move prazo já lançado.
- Alertas por dias contáveis restantes: `ATENCAO` (≤ 5), `URGENTE` (≤ 3), `CRITICO` (≤ 1), `VENCE_HOJE` (0) e `VENCIDO` (em aberto após o vencimento). Sempre o mais severo aplicável.
- **Idempotência por marco**: cada nível é avisado uma única vez por prazo — rodar a varredura duas vezes no dia não repete aviso.
- Política padrão do escritório: **só prazo fatal gera alerta**; prazo comum entra na agenda mas não dispara aviso (existe uma política "inclusiva" para quem quiser o contrário).
- Prazo **cumprido** sai da varredura e não pode ser cumprido de novo.
- A varredura roda todo dia útil às 7h e também sob demanda, pelo mesmo caso de uso; a notificação segue para painel, e-mail (log) e trilha de auditoria em banco.

**Peças (geração de documentos)**
- Toda peça tem o mesmo **esqueleto**: cabeçalho, endereçamento, qualificação, corpo, pedidos e assinatura com nome e OAB do responsável. A ordem é regra do domínio e não pode ser alterada por modelo.
- Tipos compilados: **petição inicial** (fatos, direito), **contestação** (preliminares, mérito) e **procuração ad judicia** (poderes especiais; não se endereça ao juízo). **Peça avulsa** é o tipo dos modelos que não são nenhum dos três.
- A peça **herda o segredo de justiça** do processo e a lista de OABs habilitadas no momento da geração; a OAB de quem gerou entra na lista.
- Leitura de peça sigilosa só por **OAB habilitada** — na tela (OAB da sessão) e na API (`403`). Peça sigilosa exige ao menos uma OAB habilitada; não se revoga a última.
- O tipo da peça registrada vem do **gerador** (modelo ou compilado), não do que o usuário pediu.
- Gerar peça avisa o advogado responsável.

**Fluxo de aprovação da peça**
- Estados: `RASCUNHO → EM_REVISAO → APROVADO | REJEITADO → (APROVADO) PROTOCOLADO`.
- Só rascunho ou rejeitada vai para revisão; só em revisão pode ser aprovada ou rejeitada; só aprovada pode ser protocolada; protocolada não muda mais.
- **Aprovar, rejeitar e desfazer decisão são do chefe**; aprovação exige OAB do aprovador; rejeição exige motivo.
- Desfazer volta a peça para `EM_REVISAO` e fica no histórico; toda transição registra de/para, OAB e comentário.
- Transição inválida é erro do cliente (`409`), nunca do servidor.

**Modelos de peça**
- Modelo tem código único (maiúsculas/underscore), nome, tipo de peça produzida, título opcional, **corpo e pedidos** com marcadores `{{campo}}`, e diz se **endereça ao juízo**.
- Marcadores reservados vêm dos autos: `cliente`, `comarca`, `processo`, `advogado`, `oab`. Qualquer outro é **pedido ao gerar** a peça; o cadastro informa quais.
- Campo não informado vira marcador visível na peça — `(valorDivida a preencher)` — nunca lacuna silenciosa.
- Modelo que não endereça ao juízo dispensa endereçamento, qualificação e fecho de uma vez.
- Editar ou remover modelo não altera peça já gerada (a peça persiste o próprio texto). Remover modelo é ação do chefe.

**Anexos**
- Aceitos: **PDF, JPEG, PNG e texto**, até **10 MB**; o tipo é conferido pelo `Content-Type` e a extensão correta é garantida no nome.
- O nome do arquivo é saneado: caminho de diretório (`../`) é descartado.
- O anexo **copia** segredo de justiça e OABs habilitadas do processo **no momento da juntada**; a leitura passa pelo mesmo Proxy das peças.
- Não existe remoção de anexo: documento juntado aos autos não se desanexa (desentranhamento é decisão judicial).
- Juntada avisa o responsável.

**Feriados e calendário**
- Feriado tem descrição, **recorrência** (data única ou anual fixa) e **abrangência** (nacional, estadual com UF, comarcal com comarca).
- Só entram na contagem os feriados que valem para o **foro do escritório** (`praxis.foro.uf` / `comarca`): feriado de Olinda não suspende prazo em Recife.
- Feriado cadastrado vale na **próxima contagem**, sem reiniciar. Remover feriado é ação do chefe.

**Honorários**
- Contrato por processo em uma de três modalidades: **fixo**, **por hora** (valor × horas) ou **quota litis** (percentual sobre o valor da causa).
- Quota litis acima de **30 %** é recusada (limite ético do Código de Ética da OAB).

**Audiências, clientes e partes contrárias**
- Audiência tem processo, parte autora, início, fim (posterior ao início) e sala; **duas audiências não se sobrepõem na mesma sala**; a edição respeita a mesma regra e conflitos podem ser consultados antes de gravar.
- Cliente (pessoa física ou jurídica) tem CPF/CNPJ **único**; a exclusão é lógica (inativa) e a reativação é possível; a edição é parcial (campo omitido mantém o valor).
- Parte contrária segue o mesmo modelo, sem unicidade de documento.

**Acesso**
- Todo o painel exige login; dois papéis: **advogado** (conduz os autos) e **chefe** (tudo do advogado + aprovar peça, habilitar OAB, remover feriado/modelo, gerir usuários).
- E-mail e OAB são **únicos**; senha mínima de 6 caracteres pela tela; usuário criado pelo chefe entra com **senha provisória** e só libera o painel depois de trocá-la.
- Ninguém remove a si mesmo, e o escritório precisa de **ao menos um chefe**.
- 5 falhas de login seguidas bloqueiam o e-mail por 1 minuto; e-mail desconhecido e senha errada recebem a mesma resposta.

---

## Como o praxis é construído

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
| Preliminar | [`docs/dominio.md`](docs/dominio.md) — o problema, e por que estas duas funcionalidades primeiro |
| Estratégico | 4 subdomínios / bounded contexts e suas relações — [`docs/praxis.cml`](docs/praxis.cml) (Context Mapper) |
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

## Testes e BDD

```
53 scenarios (53 passed)
481 steps (481 passed)
Tests run: 174, Failures: 0, Errors: 0
```

Os cenários são escritos em português, em [`src/test/resources/features`](src/test/resources/features), e automatizados com Cucumber + Spring (`src/test/java/school/cesar/praxis/bdd`). Os steps exercitam os casos de uso reais contra o banco, não dublês.

> **Dado** um processo com prazo fatal em 5 dias úteis, **quando** faltarem 3 dias, **então** o advogado responsável deve ser notificado.

Como os cenários chamam os casos de uso, eles não cobrem o corpo da requisição dos controllers. `ModeloHttpTest`, `AnexoHttpTest` e `LoginHttpTest` fecham essa lacuna pelo mesmo caminho do navegador (MockMvc) — foi assim que apareceram um `codigoModelo` faltando no `record` de requisição e um `500` onde devia haver `400`, ambos invisíveis para o BDD.

---

## A API REST

O painel é a interface para gente; a API é o contrato para scripts e testes.

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

POST /api/documentos/{id}/enviar-revisao       rascunho -> em revisão
POST /api/documentos/{id}/aprovar              {texto}   em revisão -> aprovado  (chefe)
POST /api/documentos/{id}/rejeitar             {texto}   em revisão -> rejeitado (chefe)
POST /api/documentos/{id}/desfazer             desfaz a última decisão (chefe)
POST /api/documentos/{id}/protocolar           aprovado -> protocolado
POST /api/documentos/{id}/oabs                 {oab}  habilita OAB nos autos da peça (chefe)
```

Existem ainda `/api/clientes`, `/api/partes-contrarias`, `/api/audiencias` e `/api/honorarios` — os três cadastros sem tela própria mais os contratos de honorário.

**Leitura é aberta; mutação exige a sessão do painel.** `GET`, `HEAD`, `OPTIONS` e `TRACE` em `/api/**` passam sem autenticar. Todo `POST`, `PUT`, `PATCH` e `DELETE` passa pelo `SessaoApiInterceptor`: sem sessão é `401`, sem o token CSRF (parâmetro `_csrf` ou cabeçalho `X-CSRF-Token`) é `403`, senha provisória é `403` e ação de chefe pedida por advogado é `403`. A recusa sai como `{"erro": ...}` — nunca um redirect para `/login`, que um script leria como sucesso.

A OAB de quem age **vem sempre da sessão**, nunca do corpo: gerar peça, aprovar, rejeitar e juntar anexo usam a inscrição de quem está logado. Continuam vindo da requisição as duas OABs que são de terceiro ou de leitura: a de `POST /api/documentos/{id}/oabs` (o chefe habilitando outro advogado nos autos) e a de `?oab=` nos downloads, que é o que o Proxy confere.

O `?oab=` deixa de ser campo livre para quem está logado: informar a inscrição de outro advogado é `403`, mesmo que ela esteja habilitada nos autos — senão bastaria a um advogado do escritório descobrir uma OAB habilitada para ler processo alheio. Sem sessão nada muda, e aí está o limite: **um chamador anônimo que saiba o id da peça e uma OAB habilitada ainda lê os autos sigilosos**, porque o `GET` é aberto por decisão de projeto. Fechar isso é exigir sessão nos dois downloads.

Numa sessão de terminal, o ritual é logar e reusar o cookie:

```bash
curl -c praxis.jar -d 'email=admin&senha=123' localhost:8080/login
csrf=$(curl -s -b praxis.jar -c praxis.jar localhost:8080/painel \
  | grep -o 'name="_csrf" value="[^"]*"' | head -1 | cut -d'"' -f4)

curl -b praxis.jar -H "X-CSRF-Token: $csrf" -X POST localhost:8080/api/...
```

### Telas (todas exigem sessão)

```
GET  /login                        formulário; POST /login autentica; POST /sair encerra
GET  /painel                       resumo do dia, agenda de prazos e avisos
GET  /painel/processos             lista/busca e cadastro de processos
GET  /painel/processos/{cnj}       ficha: linha do tempo, prazos, peças, anexos; POST andamentos e prazos
GET  /painel/documentos            gerar peça, fluxo de aprovação (botões por papel), abrir com a OAB da sessão
GET  /painel/anexos                juntar e baixar arquivos com a OAB da sessão
GET  /painel/modelos               cadastro de modelos (remover: chefe)
GET  /painel/feriados              cadastro de feriados (remover: chefe)
GET  /painel/usuarios              gestão de usuários (chefe)
GET  /painel/conta                 minha conta; POST /painel/conta/senha troca a senha
GET  /painel/admin                 administração (chefe): panorama dos cadastros e ficha da instância
```

### Exemplos que mostram as regras funcionando

O motor de prazos contando dias úteis e feriado:

```bash
curl -X POST localhost:8080/api/prazos -H 'Content-Type: application/json' -d '{
  "numeroProcesso":"0001234-56.2026.8.17.0001","descricao":"Contestacao",
  "intimacao":"2026-09-04","quantidadeDias":5,"fatal":true,"regime":"DIAS_UTEIS"}'
# -> vencimento 2026-09-14 (04/09 sexta; 07/09 feriado; conta 08, 09, 10, 11 e 14)

curl -X POST 'localhost:8080/api/prazos/varredura?hoje=2026-09-09'
# -> alerta URGENTE, 3 dias restantes
```

O cadastro de feriados mudando a contagem sem reiniciar a aplicação:

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
  "codigoModelo":"ACORDO",
  "campos":{"outraParte":"Imobiliaria Beta ME","valorAcordo":"R$ 9.000,00"}}'
# a OAB do solicitante vem da sessao, nao do corpo
# -> peca com o mesmo esqueleto das compiladas, sem linguagem de juizo
```

Juntada de arquivo e o sigilo dos autos:

```bash
curl -X POST localhost:8080/api/anexos \
  -F 'numeroProcesso=0007654-32.2026.8.17.0002' \
  -F 'arquivo=@laudo.pdf;type=application/pdf' \
  -F 'descricao=Laudo pericial'
# -> {"id":1,...,"segredoJustica":true}   (herdado do processo; a OAB e a da sessao)

curl 'localhost:8080/api/anexos/1?oab=PE54321'   # -> 200, o arquivo
curl 'localhost:8080/api/anexos/1?oab=PE99999'   # -> 403, barrado pelo Proxy
```

Falha de domínio vira status HTTP correto (`TratadorDeErrosRest`): invariante violada pela requisição é `400` (inclusive data ou enum mal formados), agregado inexistente é `404`, segredo de justiça é `403`, transição de estado inválida (aprovar rascunho, cumprir prazo já cumprido) e violação de unicidade no banco são `409` — nunca `500`.

---

## Publicar de graça (Render + PostgreSQL)

O repositório já traz o que a plataforma precisa: [`Dockerfile`](Dockerfile) (compila e entrega só o JRE com o jar) e [`render.yaml`](render.yaml), um blueprint que cria o banco e o serviço web juntos.

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/joaovictorgcu/praxis)

1. Clique no botão acima (ou, no painel do Render, **New → Blueprint**) e entre com a conta do GitHub.
2. Escolha o repositório `praxis` e confirme em **Apply**.
3. O Render cria o PostgreSQL `praxis-db`, injeta host, porta, base, usuário e senha no serviço web, compila a imagem e publica em `https://<nome>.onrender.com`. O primeiro build leva de 5 a 8 minutos.

No primeiro boot, o Flyway aplica `V1__esquema_inicial.sql` e a carga de exemplo monta o escritório. A instância publicada é uma **demonstração**: entra com `admin@admin` / `1405`, direto, sem troca de senha.

Para virar instalação real, troque as variáveis no painel do Render (Environment) e faça um redeploy:

```
PRAXIS_ADMIN_USUARIO=<seu e-mail>   PRAXIS_ADMIN_SENHA=<senha forte>
PRAXIS_DADOS_EXEMPLO=false          PRAXIS_EXIGIR_TROCA=true
PRAXIS_SENHA_INICIAL=<senha forte>
```

**O que o plano gratuito cobra em outra moeda:**

- O serviço dorme depois de 15 minutos sem tráfego; a primeira visita depois disso espera a JVM subir (~30 a 60 s). Como a sessão vive em memória, quem estava logado precisa entrar de novo.
- A varredura automática das 7h só roda se a instância estiver acordada. Um ping externo diário resolve, se isso importar.
- O PostgreSQL gratuito do Render **expira em 30 dias**. Depois disso, crie um banco gratuito permanente (Neon, Supabase) e troque as cinco variáveis de banco por uma só:
  `PRAXIS_DB_URL=jdbc:postgresql://<host>/<base>?sslmode=require`, mais `PRAXIS_DB_USER` e `PRAXIS_DB_PASSWORD`.
- 512 MB de RAM: a imagem já sobe com `-XX:MaxRAMPercentage=70 -XX:+UseSerialGC`.
- Anexos moram em BLOB no banco, e o plano gratuito dá pouco espaço — é demonstração, não arquivo do escritório.

`ImplantacaoHttpTest` sobe a aplicação com o perfil `prod` (Flyway criando o esquema, Hibernate só validando) e confirma o que a instância publicada promete: `admin@admin` entra, não cai na troca de senha e encontra o escritório de exemplo montado.

## Rodar em produção: PostgreSQL + Flyway

```bash
SPRING_PROFILES_ACTIVE=prod PRAXIS_DB_URL=jdbc:postgresql://localhost:5432/praxis PRAXIS_DB_USER=praxis PRAXIS_DB_PASSWORD=segredo PRAXIS_SENHA_INICIAL=troque-ja ./mvnw spring-boot:run
```

- O esquema é versionado em [`src/main/resources/db/migration`](src/main/resources/db/migration) (`V1__esquema_inicial.sql`); o Hibernate roda com `ddl-auto=validate`, então toda mudança de entidade exige uma nova `V{n}__*.sql`.
- Em dev/test o Flyway fica desligado e o H2 em memória segue com `ddl-auto=update`. `MigracaoFlywayTest` sobe a aplicação com H2 em modo PostgreSQL, aplica a migração e deixa o Hibernate validar — migração e entidades não divergem sem um teste quebrar.
- Binário e texto longo são `bytea`/`text` (colunas comuns), não large objects (`oid`): entram em backup e transação como qualquer coluna.
- No perfil prod os usuários iniciais nascem com **senha provisória** (`praxis.exigir-troca-senha-inicial=true`): o primeiro acesso cai na troca de senha; a carga de exemplo não roda; o cookie de sessão é `Secure`. O usuário `admin` só existe se `PRAXIS_ADMIN_SENHA` for definida.

---

## Mapa da documentação

- [`docs/dominio.md`](docs/dominio.md) — descrição do domínio e linguagem onipresente, DDD nos 4 níveis
- [`docs/mapa-historia-usuario.md`](docs/mapa-historia-usuario.md) — mapa da história do usuário
- [`docs/praxis.cml`](docs/praxis.cml) — modelo dos subdomínios em Context Mapper (CML)
- [`docs/prototipos/`](docs/prototipos/) — as capturas usadas neste README, e o script que as regera
- [`docs/declaracao-uso-ia.md`](docs/declaracao-uso-ia.md) — declaração de uso de IA por participante do grupo
- [`docs/Atividade-Requisitos-Praxis.pdf`](docs/Atividade-Requisitos-Praxis.pdf) — enunciado da atividade

---

## O que esta entrega ainda não faz

Limites conhecidos, ditos abertamente — todos com um ponto de troca identificado no código.

- E-mail é registrado em log e memória (o `NotificadorEmail` é o ponto de troca por `JavaMailSender`).
- Observadores são reanexados pela camada de aplicação a cada carregamento do agregado — suficiente para instância única, não para escala horizontal.
- H2 em memória: os dados se perdem no shutdown. Trocar para PostgreSQL altera apenas `application.properties` (ou usa o perfil `prod` acima).
- A autenticação é de sessão HTTP, própria (sem Spring Security), e vale só para o painel: o `GET` da API REST segue aberto, recebendo a OAB de leitura na requisição. Não há recuperação de senha por e-mail: quem esquece pede ao chefe para recadastrar.
- O freio de força bruta do login é em memória, por instância.
- `AgendaDeAudiencias` (domínio) não é usada pelo serviço de audiências, que consulta o repositório diretamente; a classe ficou como modelo de referência e as regras vigentes são as da JPQL.
- O papel é binário (advogado/chefe); não há vínculo entre usuário e processo além da OAB, então qualquer advogado logado vê a agenda inteira do escritório.
- Cliente, parte contrária e audiência continuam sem tela de cadastro: a administração mostra os três, inclusive os desativados, mas criar, editar e desativar segue só pela API REST.
- A administração carrega tudo de uma vez, sem paginação nem filtro: cabe no volume de um escritório, não em base grande. Paginar afeta só `PanoramaAppService` e o template.
- Os avisos listados na administração são a fila em memória do `NotificadorPainel` (200 últimos, por instância): somem no restart e não são histórico.
- O foro dos feriados é único e vem de propriedade (`praxis.foro.*`), não de cada processo: o `Processo` guarda a comarca, mas não a UF. Feriado por processo exigiria derivar a UF do código do tribunal no número CNJ.
- O cadastro de feriados é mantido em memória pelo adaptador (`FeriadoRepositorioJpa`), porque o calendário pergunta dia a dia ao percorrer um prazo. A escrita descarta o cache — suficiente para instância única.
- O modelo de documento define corpo e pedidos, não a ordem das seções: o esqueleto é regra do domínio. Modelo que precise de estrutura própria exigiria nova subclasse de `GeradorDocumento`.
- Modelos não têm versão. Editar o modelo não afeta peça já gerada (o documento persiste o conteúdo), mas o histórico do próprio modelo não é guardado.
- Os campos do modelo são texto simples, sem tipo nem obrigatoriedade: campo esquecido sai como `(nome a preencher)` na peça, e não barra a geração.
- Anexos são guardados em BLOB no banco. Simplifica backup e transação (arquivo e metadados commitam juntos), mas não escala para volume alto — a troca por armazenamento de objetos afeta apenas `ArquivoRepositorioJpa`.
- O conteúdo do anexo não é inspecionado: confia-se no `Content-Type` declarado no upload. Um PDF renomeado passaria. Validar assinatura de arquivo (magic number) e antivírus fica para produção.
- Anexo não tem versão nem desentranhamento: a juntada é definitiva na tela.
- O arquivo `.cml` não foi validado com o plugin do Context Mapper nesta máquina (extensão não instalada).
