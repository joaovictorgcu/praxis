# PRAXIS - API REST Endpoints Summary

## 🎯 Agenda de Audiências (8 endpoints)

### Create
```
POST /api/audiencias
Headers: Content-Type: application/json
Body: {
  "numeroProcesso": "2024-001",
  "nomeParteAutora": "João Silva",
  "dataHoraInicio": "2024-10-15T10:00:00",
  "dataHoraFim": "2024-10-15T11:00:00",
  "sala": "1001",
  "observacoes": "Audiência de conciliação"
}
Response: 201 CREATED (AudienciaResponse)
Errors: 409 CONFLICT (conflito de horário), 400 BAD_REQUEST (validação)
```

### Read
```
GET /api/audiencias/{id}
Response: 200 OK (AudienciaResponse) ou 404 NOT FOUND

GET /api/audiencias/processo/{numeroProcesso}
Response: 200 OK (AudienciaResponse) ou 404 NOT FOUND
```

### List
```
GET /api/audiencias
Response: 200 OK (List<AudienciaResponse>)

GET /api/audiencias/sala/{sala}
Response: 200 OK (List<AudienciaResponse>)

GET /api/audiencias/periodo?dataInicio=2024-10-01T00:00:00&dataFim=2024-10-31T23:59:59
Response: 200 OK (List<AudienciaResponse>)
```

### Update
```
PUT /api/audiencias/{id}
Headers: Content-Type: application/json
Body: { ...CriarAudienciaRequest }
Response: 200 OK (AudienciaResponse)
Errors: 400 BAD_REQUEST, 409 CONFLICT (conflito ao editar)
```

### Delete
```
DELETE /api/audiencias/{id}
Response: 204 NO CONTENT
Errors: 404 NOT FOUND
```

### Reactivate
```
POST /api/audiencias/{id}/reativar
Response: 200 OK
Errors: 400 BAD_REQUEST, 404 NOT FOUND
```

### Special
```
POST /api/audiencias/conflitos/detectar
Headers: Content-Type: application/json
Body: { ...CriarAudienciaRequest }
Response: 200 OK (List<AudienciaResponse>) - audiências em conflito ou vazia
```

---

## 👥 Partes Contrárias (8 endpoints)

### Create
```
POST /api/partes-contrarias
Body: {
  "nome": "Empresa ABC Ltda",
  "cpfOuCnpj": "12.345.678/0001-90",
  "tipoPessoa": "JURIDICA",
  "email": "contato@empresa.com",
  "telefone": "(11) 3000-0000",
  "endereco": "Rua X, 100",
  "cidade": "São Paulo",
  "estado": "SP",
  "cep": "01000-000",
  "observacoes": "Cliente recorrente"
}
Response: 201 CREATED (ParteContrariaResponse)
Errors: 400 BAD_REQUEST (duplicado ou validação)
```

### Read
```
GET /api/partes-contrarias/{id}
GET /api/partes-contrarias/cpf-cnpj/{cpfOuCnpj}
Response: 200 OK (ParteContrariaResponse) ou 404 NOT FOUND
```

### List
```
GET /api/partes-contrarias
GET /api/partes-contrarias/tipo/{tipo}  (FISICA ou JURIDICA)
GET /api/partes-contrarias/cidade/{cidade}
Response: 200 OK (List<ParteContrariaResponse>)
```

### Update
```
PUT /api/partes-contrarias/{id}
Body: { ...CriarParteContrariaRequest }
Response: 200 OK (ParteContrariaResponse)
```

### Delete & Reactivate
```
DELETE /api/partes-contrarias/{id}
POST /api/partes-contrarias/{id}/reativar
```

---

## 👤 Clientes (8 endpoints)

### Create
```
POST /api/clientes
Body: {
  "nome": "Maria Santos",
  "cpfOuCnpj": "123.456.789-00",
  "tipoPessoa": "FISICA",
  "email": "maria@email.com",
  "telefone": "(11) 2000-0000",
  "celular": "(11) 99999-9999",
  "endereco": "Av. Y, 200",
  "cidade": "Rio de Janeiro",
  "estado": "RJ",
  "cep": "20000-000",
  "profissao": "Engenheira",
  "empresaTrabalho": "Construtora XYZ",
  "observacoes": "Cliente VIP"
}
Response: 201 CREATED (ClienteResponse)
Errors: 400 BAD_REQUEST
```

### Read
```
GET /api/clientes/{id}
GET /api/clientes/cpf-cnpj/{cpfOuCnpj}
Response: 200 OK (ClienteResponse) ou 404 NOT FOUND
```

### List
```
GET /api/clientes
GET /api/clientes/tipo/{tipo}  (FISICA ou JURIDICA)
GET /api/clientes/cidade/{cidade}
GET /api/clientes/estado/{estado}  (único para Cliente)
Response: 200 OK (List<ClienteResponse>)
```

### Update
```
PUT /api/clientes/{id}
Body: { ...CriarClienteRequest }
Response: 200 OK (ClienteResponse)
```

### Delete & Reactivate
```
DELETE /api/clientes/{id}
POST /api/clientes/{id}/reativar
```

---

## 🔧 Common Response Formats

### Success Response (Entity)
```json
{
  "id": 1,
  "nome": "...",
  "criadoEm": "2024-10-10T10:00:00",
  "atualizadoEm": "2024-10-10T10:00:00",
  "ativa": true
}
```

### Error Response
```json
{
  "codigo": "CONFLITO" | "DADOS_INVALIDOS" | "TIPO_INVALIDO",
  "mensagem": "Descrição do erro"
}
```

### HTTP Status Codes
- **200 OK**: Requisição bem-sucedida (GET, PUT, POST especiais)
- **201 CREATED**: Entidade criada (POST create)
- **204 NO CONTENT**: Operação bem-sucedida sem resposta (DELETE)
- **400 BAD_REQUEST**: Validação falhou
- **404 NOT FOUND**: Recurso não encontrado
- **409 CONFLICT**: Conflito detectado (audiências)

---

## 📝 Base URL
```
http://localhost:8080
```

## 🔐 CORS
- Configurado para aceitar requisições de qualquer origem: `@CrossOrigin(origins = "*")`

## 📊 Exemplos de Fluxo

### Criar Audiência com Detecção de Conflito

1. **Detectar conflitos potenciais**
```
POST /api/audiencias/conflitos/detectar
Body: { ...dados da nova audiência }
→ Retorna lista de audiências em conflito (se houver)
```

2. **Se sem conflito, criar**
```
POST /api/audiencias
Body: { ...dados da audiência }
→ 201 CREATED com audiência criada
```

### Gerenciar Cliente

1. **Criar cliente**
```
POST /api/clientes → 201
```

2. **Editar dados**
```
PUT /api/clientes/{id} → 200
```

3. **Listar por estado (específico para Cliente)**
```
GET /api/clientes/estado/SP → 200 com lista filtrada
```

4. **Deletar (desativar)**
```
DELETE /api/clientes/{id} → 204
```

5. **Reativar**
```
POST /api/clientes/{id}/reativar → 200
```

---

## 📌 Notas Importantes

1. **Soft Delete**: Ao deletar, a entidade é desativada (ativa=false), não removida
2. **Conflitos**: Apenas audiências na **mesma sala** com **horários sobrepostos**
3. **Unicidade**: CPF/CNPJ deve ser único entre entidades ativas
4. **Timestamps**: Automaticamente gerenciados (@CreationTimestamp, @UpdateTimestamp)
5. **Filtros**: Todos os GET naturalmente filtram apenas entidades ativas

---

**Status**: ✅ TODOS OS ENDPOINTS IMPLEMENTADOS E COMPILADOS
