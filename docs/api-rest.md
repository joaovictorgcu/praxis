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
