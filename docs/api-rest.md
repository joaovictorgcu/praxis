# A API REST

Contrato HTTP para scripts e testes. O painel usa os mesmos casos de uso.

---

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
GET  /api/documentos/{id}                      baixa a peça (passa pelo Proxy; exige sessão, usa sempre a OAB
                                                dela; 401 sem sessão, 403 se a OAB não estiver habilitada)

POST   /api/modelos                            cadastra modelo de peça (corpo e pedidos com {{campo}})
GET    /api/modelos                            lista modelos e os campos que cada um espera
DELETE /api/modelos/{id}                       remove modelo

POST /api/anexos                               junta arquivo aos autos (multipart)
GET  /api/anexos?processo=                     lista anexos (sem os bytes)
GET  /api/anexos/{id}                          baixa o arquivo (passa pelo Proxy; exige sessão, usa sempre a OAB
                                                dela; 401 sem sessão, 403 se a OAB não estiver habilitada)

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

POST /api/honorarios                           contrata honorario (fixo/por hora/quota litis), parcelado em 1-24 vezes
GET  /api/honorarios?processo=                 lista contratos (resumo; filtra por processo se informado)
GET  /api/honorarios/{id}                      detalhe do contrato: parcelas, encargos de atraso e historico
POST /api/honorarios/{id}/parcelas/{numero}/pagamentos
                                                {valor,dataPagamento}  registra pagamento (parcial ou total) da parcela
POST /api/honorarios/{id}/resultado            {valorObtido,honorariosSucumbencia}  so quota litis: fecha o valor
                                                final sobre o proveito economico real e gera as parcelas

POST /api/acessos                              {numeroProcesso,oab,expiraEm}  concede acesso ao processo inteiro
                                                (expiraEm nulo = permanente) (chefe)
GET  /api/acessos?processo=                    lista acessos do processo - ativos, expirados e revogados (chefe)
POST /api/acessos/{id}/revogar                 {motivo}  revoga; recusado se for o ultimo acesso ativo do
                                                processo sigiloso (chefe)

POST /api/solicitacoes                         {numeroProcesso,justificativa}  solicita acesso a processo
                                                sigiloso (justificativa com 20+ caracteres); a OAB e a da sessão
POST /api/solicitacoes/{id}/cancelar           cancela a própria solicitação pendente
POST /api/solicitacoes/{id}/aprovar            {expiraEm}  aprova e concede o acesso (expiraEm nulo = permanente)
                                                (chefe)
POST /api/solicitacoes/{id}/negar              {motivo}  nega a solicitação (chefe)
GET  /api/solicitacoes                         chefe ve a fila inteira; advogado, só as próprias

GET  /api/auditoria?processo=&oab=&resultado=&de=&ate=
                                                consulta a auditoria de acesso - toda leitura de peça/anexo
                                                (permitida ou negada) e toda concessão, revogação, aprovação e
                                                negação (chefe)
```

Existem ainda `/api/clientes`, `/api/partes-contrarias` e `/api/audiencias`. Clientes, partes contrárias e audiências também podem ser gerenciados manualmente pelo chefe nas telas do painel descritas abaixo.

**Leitura é aberta; mutação exige a sessão do painel; download de peça/anexo exige sessão mesmo sendo GET.** `GET`, `HEAD`, `OPTIONS` e `TRACE` em `/api/**` passam sem autenticar, com uma exceção: `GET /api/documentos/{id}` e `GET /api/anexos/{id}` (a leitura protegida pelo Proxy) exigem sessão e respondem `401` sem ela — ver abaixo o porquê. Todo `POST`, `PUT`, `PATCH` e `DELETE` passa pelo `SessaoApiInterceptor`: sem sessão é `401`, sem o token CSRF (parâmetro `_csrf` ou cabeçalho `X-CSRF-Token`) é `403`, senha provisória é `403` e ação de chefe pedida por advogado é `403`. A recusa sai como `{"erro": ...}` — nunca um redirect para `/login`, que um script leria como sucesso.

A OAB de quem age **vem sempre da sessão**, nunca do corpo nem de parâmetro de URL: gerar peça, aprovar, rejeitar, juntar anexo e baixar peça/anexo usam a inscrição de quem está logado. A única OAB que ainda é dado da requisição é a de terceiro em `POST /api/documentos/{id}/oabs` (o chefe habilitando outro advogado nos autos) e nos comandos de concessão/aprovação de acesso (`oab` de quem recebe o acesso) - nunca a de quem está fazendo a chamada.

Os downloads de peça e anexo **deixaram de aceitar `?oab=`**: antes, um chamador anônimo que soubesse o id e uma OAB habilitada lia autos sigilosos sem nunca ter feito login - o `GET` era aberto por decisão de projeto e o parâmetro era campo livre. Agora os dois exigem sessão (`401` sem ela) e usam sempre a OAB de quem está logado, nunca uma informada na requisição - um `?oab=` enviado é simplesmente ignorado.

A decisão "pode ler?" passa pela `PoliticaDeLeituraDosAutos`, usada pelo Proxy: processo público, sempre sim; em segredo de justiça, a OAB precisa estar habilitada nesta peça/anexo (como antes) **ou** ter acesso ativo e não expirado ao processo inteiro, concedido direto (`POST /api/acessos`) ou por solicitação aprovada. Toda tentativa de leitura - permitida ou negada - entra na auditoria (`GET /api/auditoria`), junto com concessões, revogações, aprovações e negações.

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
GET  /painel/honorarios            lista de contratos com totais (a receber/recebido/em atraso) e filtros
GET  /painel/honorarios/{id}       detalhe: parcelas, pagamento, resultado da causa (quota litis) e historico
GET  /painel/processos/{cnj}/acessos
                                   acessos ao processo: ativos, expirados, revogados; conceder e revogar (chefe)
GET  /painel/solicitacoes          chefe: fila de pendentes (aprovar/negar) e historico; advogado: proprias
                                   solicitacoes, com cancelar nas pendentes
GET  /painel/auditoria             tentativas de leitura e acoes de gestao de acesso, com filtros (chefe)
GET  /painel/modelos               cadastro de modelos (remover: chefe)
GET  /painel/feriados              cadastro de feriados (remover: chefe)
GET  /painel/usuarios              gestão de usuários (chefe)
GET  /painel/conta                 minha conta; POST /painel/conta/senha troca a senha
GET  /painel/admin                 administração (chefe): panorama dos cadastros e ficha da instância
GET  /painel/cadastros/clientes    cadastro de clientes (chefe): criar, editar, desativar e reativar
GET  /painel/cadastros/partes-contrarias
                                   cadastro de partes contrárias (chefe): criar, editar, desativar e reativar
GET  /painel/cadastros/audiencias  agenda de audiências (chefe): agendar, editar, cancelar e reativar
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

curl -b praxis.jar 'localhost:8080/api/anexos/1'  # -> 200, o arquivo (com a sessao de antes, acima)
curl 'localhost:8080/api/anexos/1'                # -> 401, sem sessao - o ?oab= livre nao existe mais
```

Quota litis em duas fases: o valor nasce estimado (sobre o valor da causa informado
na contratação) e só vira final - com as parcelas geradas - quando o resultado da
causa é registrado, sobre o proveito econômico de verdade:

```bash
curl -X POST localhost:8080/api/honorarios -H 'Content-Type: application/json' -d '{
  "numeroProcesso":"0001234-56.2026.8.17.0001","modalidade":"QUOTA_LITIS",
  "celebradoEm":"2026-09-10","valorCausa":100000.00,"percentualExito":20,
  "quantidadeParcelas":2,"primeiroVencimento":"2026-10-10"}'
# -> valorContratado: 20000.00 (estimado), aguardandoResultado: true, sem parcelas ainda

curl -X POST localhost:8080/api/honorarios/1/resultado -H 'Content-Type: application/json' -d '{
  "valorObtido":150000.00,"honorariosSucumbencia":5000.00}'
# -> valorContratado recalculado para 30000.00 (20% de 150000, nao mais da estimativa)
#    aguardandoResultado: false, 2 parcelas geradas (15000.00 cada)

curl -X POST localhost:8080/api/honorarios/2/resultado -H 'Content-Type: application/json' -d '{
  "valorObtido":10000.00,"honorariosSucumbencia":9000.00}'
# -> 400 {"erro":"honorarios contratuais (2000.00) mais honorarios de sucumbencia (9000.00)
#    superam o valor obtido pelo cliente (10000.00) - art. 50 do Codigo de Etica da OAB"}
#    contrato nao muda de estado: continua aguardando resultado
```

Pagamento de parcela com saldo insuficiente e encargo de atraso:

```bash
curl -X POST localhost:8080/api/honorarios/1/parcelas/1/pagamentos -H 'Content-Type: application/json' -d '{
  "valor":99999.00,"dataPagamento":"2026-10-10"}'
# -> 400 {"erro":"pagamento de 99999.00 maior que o saldo da parcela 1 (15000.00)"}

curl localhost:8080/api/honorarios/1
# parcela vencida: valorAtualizado = saldo + multa (2% uma vez) + juros (1% ao mes, pro rata die)
```

Solicitar acesso a um processo sigiloso, aprovar e ler - a OAB que solicita e a da
sessão; quem aprova e nega é sempre o chefe:

```bash
curl -b ana.jar -X POST localhost:8080/api/solicitacoes -H 'Content-Type: application/json' \
  -H "X-CSRF-Token: $csrf" -d '{
  "numeroProcesso":"0007654-32.2026.8.17.0002",
  "justificativa":"preciso acompanhar a audiencia de conciliacao deste processo"}'
# -> status PENDENTE

curl -b chefe.jar -X POST localhost:8080/api/solicitacoes/1/aprovar -H 'Content-Type: application/json' \
  -H "X-CSRF-Token: $csrf" -d '{"expiraEm":"2026-12-31"}'
# -> status APROVADA; cria o acesso ao processo ate 2026-12-31

curl -b ana.jar localhost:8080/api/anexos/5
# -> 200: a OAB da Ana nao esta na lista deste anexo, mas tem acesso ativo ao processo
```

Um processo sigiloso nunca fica sem nenhum acesso ativo:

```bash
curl -b chefe.jar -X POST localhost:8080/api/acessos/1/revogar -H 'Content-Type: application/json' \
  -H "X-CSRF-Token: $csrf" -d '{"motivo":"saiu do caso"}'
# -> 409 {"erro":"processo sigiloso não pode ficar sem nenhum acesso ativo; conceda outro antes de revogar este"}
#    (so acontece quando este e o ultimo acesso ativo do processo)
```

Falha de dominio vira status HTTP correto (`TratadorDeErrosRest`): invariante
violada pela requisicao e `400` (inclusive data ou enum mal formados e
`HorarioInvalidoException`), agregado inexistente e `404` (`NoSuchElementException`
e o `notFound()` do GET por id de audiencia/cliente/parte), segredo de justica
e `403`, transicao de estado invalida e conflito de horario de audiencia
(`ConflitoDEAudienciaException`) e `409` — nunca `500`. O corpo e sempre
`{"erro": "..."}`. Os controllers de `/api/audiencias`, `/api/clientes` e
`/api/partes-contrarias` nao capturam exception. DELETE de audiencia inexistente
segue o tratador (`400` por `IllegalArgumentException`); so o GET `/{id}`
continua `404` vazio.

---
