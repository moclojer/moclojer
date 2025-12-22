---
description: >-
  Guia completo sobre métodos HTTP (GET, POST, PUT, DELETE, etc.) no moclojer.
  Aprenda quando usar cada método e como implementar APIs RESTful corretas.
---

# HTTP Methods (Métodos HTTP)

Métodos HTTP (também chamados de verbos HTTP) definem a ação que você quer realizar em um recurso. Escolher o método correto é fundamental para criar APIs RESTful semânticas e consistentes.

## Métodos Suportados

Moclojer suporta todos os métodos HTTP padrão:

| Método | Propósito | Idempotente¹ | Safe² |
|--------|-----------|--------------|-------|
| **GET** | Ler dados | ✅ | ✅ |
| **POST** | Criar dados | ❌ | ❌ |
| **PUT** | Atualizar/Substituir | ✅ | ❌ |
| **PATCH** | Atualizar parcialmente | ❌ | ❌ |
| **DELETE** | Remover dados | ✅ | ❌ |
| **HEAD** | Obter headers (sem body) | ✅ | ✅ |
| **OPTIONS** | Descobrir métodos permitidos | ✅ | ✅ |

¹ **Idempotente**: Múltiplas chamadas idênticas têm o mesmo efeito que uma única chamada
² **Safe**: Não modifica dados (apenas leitura)

## Sintaxe no Moclojer

```yaml
- endpoint:
    method: GET      # Especifica o método HTTP
    path: /users
    response:
      status: 200
      body: "..."
```

**Padrão:** Se você omitir `method`, moclojer usa `GET`.

```yaml
# Estes são equivalentes:
- endpoint:
    path: /users
    # method: GET é implícito

- endpoint:
    method: GET
    path: /users
```

## GET - Ler Dados

**Propósito:** Recuperar dados sem modificá-los.

**Características:**
- **Safe**: Não altera dados no servidor
- **Idempotente**: Múltiplas chamadas retornam o mesmo resultado
- **Cacheable**: Respostas podem ser cacheadas
- **Sem body**: Não deveria ter corpo na requisição

### Exemplo: Listar Recursos

```yaml
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "100"
      body: >
        [
          {"id": 1, "name": "Alice"},
          {"id": 2, "name": "Bob"},
          {"id": 3, "name": "Carol"}
        ]
```

**Uso:**
```bash
curl http://localhost:8000/users
```

### Exemplo: Obter Recurso Específico

```yaml
- endpoint:
    method: GET
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": "{{path-params.id}}",
          "name": "User {{path-params.id}}",
          "email": "user{{path-params.id}}@example.com"
        }
```

**Uso:**
```bash
curl http://localhost:8000/users/123
```

### GET com Query Parameters

```yaml
- endpoint:
    method: GET
    path: /products
    response:
      status: 200
      body: >
        {
          "filters": {
            "category": "{{query-params.category}}",
            "minPrice": "{{query-params.min_price}}"
          },
          "products": []
        }
```

**Uso:**
```bash
curl "http://localhost:8000/products?category=electronics&min_price=100"
```

### Quando Usar GET

✅ **Use GET para:**
- Listar recursos (`GET /users`)
- Obter detalhes (`GET /users/123`)
- Buscar/filtrar (`GET /search?q=term`)
- Exportar dados (`GET /reports/sales`)

❌ **Não use GET para:**
- Criar recursos (use POST)
- Atualizar recursos (use PUT/PATCH)
- Deletar recursos (use DELETE)
- Operações com efeitos colaterais

---

## POST - Criar Dados

**Propósito:** Criar novos recursos ou processar dados.

**Características:**
- **Não idempotente**: Múltiplas chamadas criam múltiplos recursos
- **Não safe**: Modifica o estado do servidor
- **Com body**: Geralmente envia dados no corpo
- **Retorna 201**: Status "Created" quando bem-sucedido

### Exemplo: Criar Recurso

```yaml
- endpoint:
    method: POST
    path: /users
    response:
      status: 201
      headers:
        Content-Type: application/json
        Location: /users/123
      body: >
        {
          "id": 123,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "createdAt": "2024-01-15T10:30:00Z"
        }
```

**Uso:**
```bash
curl -X POST http://localhost:8000/users \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com"}'
```

**Resposta:**
```json
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### POST para Ações Customizadas

```yaml
- endpoint:
    method: POST
    path: /users/:id/activate
    response:
      status: 200
      body: >
        {
          "userId": "{{path-params.id}}",
          "action": "activate",
          "status": "activated",
          "message": "User {{path-params.id}} activated successfully"
        }
```

**Uso:**
```bash
curl -X POST http://localhost:8000/users/123/activate
```

### Quando Usar POST

✅ **Use POST para:**
- Criar novos recursos (`POST /users`)
- Upload de arquivos (`POST /upload`)
- Ações customizadas (`POST /users/123/activate`)
- Processamento complexo (`POST /calculate`)
- Quando a operação não é idempotente

❌ **Não use POST para:**
- Leitura de dados (use GET)
- Atualização completa (use PUT)
- Remoção (use DELETE)

---

## PUT - Atualizar/Substituir

**Propósito:** Substituir completamente um recurso existente.

**Características:**
- **Idempotente**: Múltiplas chamadas têm o mesmo efeito
- **Substitui completamente**: Todos os campos devem ser enviados
- **Requer ID**: Geralmente usado com `/resource/:id`
- **Retorna 200**: Status "OK" quando atualizado

### Exemplo: Atualizar Recurso Completo

```yaml
- endpoint:
    method: PUT
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": "{{path-params.id}}",
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "role": "{{json-params.role}}",
          "updatedAt": "2024-01-15T10:30:00Z"
        }
```

**Uso:**
```bash
curl -X PUT http://localhost:8000/users/123 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Updated",
    "email": "john.new@example.com",
    "role": "admin"
  }'
```

### PUT vs POST

**PUT** é idempotente:
```bash
# Chamar 5 vezes resulta no mesmo estado
PUT /users/123 {"name": "John"}
PUT /users/123 {"name": "John"}
PUT /users/123 {"name": "John"}
# Resultado: user 123 com name="John"
```

**POST** não é idempotente:
```bash
# Chamar 5 vezes cria 5 recursos
POST /users {"name": "John"}  # Cria user 1
POST /users {"name": "John"}  # Cria user 2
POST /users {"name": "John"}  # Cria user 3
# Resultado: 3 users diferentes
```

### Quando Usar PUT

✅ **Use PUT para:**
- Atualizar recurso completo (`PUT /users/123`)
- Substituir configurações (`PUT /settings`)
- Operações idempotentes de atualização

❌ **Não use PUT para:**
- Criar recursos (use POST)
- Atualização parcial (use PATCH)
- Coleções (`PUT /users` não faz sentido)

---

## PATCH - Atualizar Parcialmente

**Propósito:** Atualizar apenas alguns campos de um recurso.

**Características:**
- **Parcial**: Envia apenas campos que mudaram
- **Mais eficiente**: Menos dados trafegados
- **Não necessariamente idempotente**: Depende da implementação

### Exemplo: Atualização Parcial

```yaml
- endpoint:
    method: PATCH
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": "{{path-params.id}}",
          "updated": {
            "name": "{{json-params.name}}",
            "email": "{{json-params.email}}"
          },
          "message": "User {{path-params.id}} updated successfully"
        }
```

**Uso:**
```bash
# Atualiza apenas o email (name continua o mesmo)
curl -X PATCH http://localhost:8000/users/123 \
  -H "Content-Type: application/json" \
  -d '{"email": "newemail@example.com"}'
```

### PATCH vs PUT

| Aspecto | PUT | PATCH |
|---------|-----|-------|
| **Escopo** | Substitui recurso completo | Atualiza campos específicos |
| **Campos** | Todos os campos obrigatórios | Apenas campos que mudam |
| **Idempotente** | Sim | Depende |
| **Exemplo** | `PUT /users/1` (todos os dados) | `PATCH /users/1` (só email) |

### Quando Usar PATCH

✅ **Use PATCH para:**
- Atualizar poucos campos (`PATCH /users/123`)
- Alternar flags (`PATCH /posts/1 {"published": true}`)
- Operações de edição parcial

❌ **Não use PATCH para:**
- Substituir recurso completo (use PUT)
- Criar recursos (use POST)

---

## DELETE - Remover Dados

**Propósito:** Remover um recurso.

**Características:**
- **Idempotente**: Deletar múltiplas vezes = deletar uma vez
- **Sem body na resposta**: Geralmente retorna 204 No Content
- **Irreversível**: (em APIs reais, considere soft delete)

### Exemplo: Deletar Recurso

```yaml
- endpoint:
    method: DELETE
    path: /users/:id
    response:
      status: 204    # No Content (sem body)
```

**Uso:**
```bash
curl -X DELETE http://localhost:8000/users/123
```

### DELETE com Confirmação

```yaml
- endpoint:
    method: DELETE
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": "{{path-params.id}}",
          "deleted": true,
          "message": "User {{path-params.id}} deleted successfully",
          "deletedAt": "2024-01-15T10:30:00Z"
        }
```

### DELETE para Recurso Não Encontrado

```yaml
- endpoint:
    method: DELETE
    path: /users/999
    response:
      status: 404
      body: >
        {
          "error": "User not found",
          "message": "User with ID 999 does not exist"
        }
```

### Quando Usar DELETE

✅ **Use DELETE para:**
- Remover recursos (`DELETE /users/123`)
- Limpar dados (`DELETE /cache`)
- Logout (`DELETE /sessions/current`)

❌ **Não use DELETE para:**
- Leitura (use GET)
- Atualização (use PUT/PATCH)
- Ações que não removem dados

---

## HEAD - Obter Metadata

**Propósito:** Obter headers de uma resposta sem o corpo.

**Características:**
- Idêntico ao GET, mas **sem corpo na resposta**
- Útil para checar se recurso existe
- Verificar tamanho do arquivo antes de baixar

### Exemplo

```yaml
- endpoint:
    method: HEAD
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
        Content-Length: "256"
        Last-Modified: "2024-01-15T10:30:00Z"
      # body é ignorado em HEAD
```

**Uso:**
```bash
curl -I http://localhost:8000/users/123
# Retorna apenas headers, sem body
```

### Quando Usar HEAD

✅ **Use HEAD para:**
- Verificar se recurso existe
- Checar `Last-Modified` ou `ETag`
- Ver tamanho do arquivo (`Content-Length`)

---

## OPTIONS - Descobrir Métodos Permitidos

**Propósito:** Descobrir quais métodos HTTP são suportados.

**Características:**
- Usado em **CORS preflight requests**
- Retorna métodos permitidos em `Allow` header

### Exemplo

```yaml
- endpoint:
    method: OPTIONS
    path: /users
    response:
      status: 200
      headers:
        Allow: GET, POST, OPTIONS
        Access-Control-Allow-Methods: GET, POST, OPTIONS
        Access-Control-Allow-Origin: "*"
```

**Uso:**
```bash
curl -X OPTIONS http://localhost:8000/users
```

### CORS Preflight

Browsers fazem OPTIONS automaticamente antes de requests "complexos":

```yaml
- endpoint:
    method: OPTIONS
    path: /api/:path
    response:
      status: 204
      headers:
        Access-Control-Allow-Origin: "*"
        Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
        Access-Control-Allow-Headers: Content-Type, Authorization
        Access-Control-Max-Age: "86400"
```

---

## Múltiplos Métodos, Mesmo Path

Você pode ter o mesmo path com métodos diferentes:

```yaml
# GET /users - Listar
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: '[{"id": 1}, {"id": 2}]'

# POST /users - Criar
- endpoint:
    method: POST
    path: /users
    response:
      status: 201
      body: '{"id": 3, "name": "{{json-params.name}}"}'

# DELETE /users (limpar todos - raro!)
- endpoint:
    method: DELETE
    path: /users
    response:
      status: 204
```

**Resultado:**
- `GET /users` → lista users
- `POST /users` → cria user
- `DELETE /users` → remove todos (cuidado!)

---

## API RESTful Completa

Exemplo de CRUD completo:

```yaml
# CREATE - POST /users
- endpoint:
    method: POST
    path: /users
    response:
      status: 201
      headers:
        Location: /users/123
      body: >
        {
          "id": 123,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}"
        }

# READ (lista) - GET /users
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        [
          {"id": 1, "name": "Alice"},
          {"id": 2, "name": "Bob"}
        ]

# READ (item) - GET /users/:id
- endpoint:
    method: GET
    path: /users/:id
    response:
      status: 200
      body: >
        {
          "id": "{{path-params.id}}",
          "name": "User {{path-params.id}}"
        }

# UPDATE (completo) - PUT /users/:id
- endpoint:
    method: PUT
    path: /users/:id
    response:
      status: 200
      body: >
        {
          "id": "{{path-params.id}}",
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}"
        }

# UPDATE (parcial) - PATCH /users/:id
- endpoint:
    method: PATCH
    path: /users/:id
    response:
      status: 200
      body: >
        {
          "id": "{{path-params.id}}",
          "updated": true
        }

# DELETE - DELETE /users/:id
- endpoint:
    method: DELETE
    path: /users/:id
    response:
      status: 204
```

---

## Status Codes por Método

| Método | Success | Error Comum |
|--------|---------|-------------|
| GET | 200 OK | 404 Not Found |
| POST | 201 Created | 400 Bad Request, 422 Unprocessable |
| PUT | 200 OK | 404 Not Found, 400 Bad Request |
| PATCH | 200 OK | 404 Not Found, 400 Bad Request |
| DELETE | 204 No Content | 404 Not Found |
| HEAD | 200 OK | 404 Not Found |
| OPTIONS | 200 OK | - |

---

## Boas Práticas

### ✅ Faça

1. **Use o método semântico correto**
   ```yaml
   # ✅ Correto
   GET /users        # Ler
   POST /users       # Criar
   PUT /users/:id    # Atualizar completo
   PATCH /users/:id  # Atualizar parcial
   DELETE /users/:id # Remover
   ```

2. **GET e HEAD não devem modificar dados**
   ```yaml
   # ❌ Errado - GET não deve deletar!
   GET /users/:id/delete

   # ✅ Correto
   DELETE /users/:id
   ```

3. **Use status codes apropriados**
   ```yaml
   POST: 201 Created
   PUT: 200 OK
   DELETE: 204 No Content
   ```

4. **Implemente OPTIONS para CORS**
   ```yaml
   - endpoint:
       method: OPTIONS
       path: /:path
       response:
         status: 204
         headers:
           Access-Control-Allow-Methods: "*"
   ```

### ❌ Evite

1. **Métodos na URL**
   ```yaml
   # ❌ Não faça
   GET /users/create
   GET /users/:id/update
   GET /users/:id/delete

   # ✅ Use os métodos HTTP
   POST /users
   PUT /users/:id
   DELETE /users/:id
   ```

2. **POST para tudo**
   ```yaml
   # ❌ Anti-pattern
   POST /getUsers
   POST /updateUser
   POST /deleteUser

   # ✅ RESTful
   GET /users
   PUT /users/:id
   DELETE /users/:id
   ```

---

## Próximos Passos

- **[Path Patterns](path-patterns.md)** - Padrões de rotas
- **[Response Structure](response-structure.md)** - Estrutura de respostas
- **[CRUD Operations How-to](../../how-to/patterns/crud-operations.md)** - CRUD completo

## Veja Também

- [HTTP Status Codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)
- [REST API Best Practices](https://restfulapi.net/)
- [Your First Mock](../../getting-started/your-first-mock.md)
