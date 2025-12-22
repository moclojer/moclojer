---
description: >-
  Aprenda a trabalhar com body parameters (JSON, form data) no moclojer.
  Acesse dados do corpo da requisição em POST, PUT e PATCH.
---

# Body Parameters (Parâmetros do Corpo)

Body parameters são dados enviados no corpo (body) de requisições HTTP, principalmente em POST, PUT e PATCH. Moclojer permite acessar esses dados via templates e usá-los em respostas dinâmicas.

## O Que São Body Parameters?

**Body** é onde você envia dados complexos em requisições HTTP:

```bash
# Exemplo de POST com JSON no body
curl -X POST http://localhost:8000/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "age": 30
  }'
```

**Diferença de outros parâmetros:**

- **Path params**: Dados na URL (`/users/:id`)
- **Query params**: Dados após `?` (`?page=1`)
- **Body params**: Dados no corpo da requisição (JSON, form data)

## Por Que Usar Body Parameters?

✅ **Dados complexos**: Objetos aninhados, arrays, múltiplos campos
✅ **Segurança**: Não aparecem na URL (logs, histórico)
✅ **Tamanho**: Sem limite de URL (que é ~2KB)
✅ **Estruturado**: JSON permite hierarquias

**Quando usar:**

- Criar recursos (POST)
- Atualizar recursos (PUT, PATCH)
- Operações com muitos dados
- Dados sensíveis (senhas, tokens)

**Quando NÃO usar:**

- GET requests (GET não deve ter body)
- DELETE simples (use path params)
- Filtros/paginação (use query params)

---

## Acessando Body Parameters

### Sintaxe: `{{json-params.campo}}`

```yaml
- endpoint:
    method: POST
    path: /users
    response:
      status: 201
      body: >
        {
          "id": 1,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "age": {{json-params.age}}
        }
```

**Testar:**

```bash
curl -X POST http://localhost:8000/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice",
    "email": "alice@example.com",
    "age": 25
  }'
```

**Resposta:**

```json
{
  "id": 1,
  "name": "Alice",
  "email": "alice@example.com",
  "age": 25
}
```

---

## Content-Type: application/json

### JSON Simples

```yaml
- endpoint:
    method: POST
    path: /api/tasks
    response:
      status: 201
      headers:
        Content-Type: application/json
      body: >
        {
          "id": 1,
          "title": "{{json-params.title}}",
          "description": "{{json-params.description}}",
          "priority": "{{json-params.priority}}",
          "completed": false,
          "createdAt": "2024-01-15T10:00:00Z"
        }
```

**Request:**

```bash
curl -X POST http://localhost:8000/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Comprar leite",
    "description": "Ir ao mercado",
    "priority": "high"
  }'
```

### Objetos Aninhados

```yaml
- endpoint:
    method: POST
    path: /api/users
    response:
      status: 201
      body: >
        {
          "id": 1,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "address": {
            "street": "{{json-params.address.street}}",
            "city": "{{json-params.address.city}}",
            "zipCode": "{{json-params.address.zipCode}}"
          }
        }
```

**Request:**

```bash
curl -X POST http://localhost:8000/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "address": {
      "street": "123 Main St",
      "city": "New York",
      "zipCode": "10001"
    }
  }'
```

**Resposta:**

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "address": {
    "street": "123 Main St",
    "city": "New York",
    "zipCode": "10001"
  }
}
```

### Arrays no Body

```yaml
- endpoint:
    method: POST
    path: /api/bulk-create
    response:
      status: 201
      body: >
        {
          "created": 3,
          "items": "{{json-params.items}}",
          "message": "Bulk creation successful"
        }
```

**Request:**

```bash
curl -X POST http://localhost:8000/api/bulk-create \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"name": "Item 1"},
      {"name": "Item 2"},
      {"name": "Item 3"}
    ]
  }'
```

⚠️ **Nota:** Templates não iteram sobre arrays automaticamente. O array é retornado como string.

---

## Tipos de Dados

### Strings

```yaml
body: >
  {
    "name": "{{json-params.name}}"
  }
```

**Request:** `{"name": "Alice"}`
**Response:** `{"name": "Alice"}`

### Números (sem aspas!)

```yaml
body: >
  {
    "age": {{json-params.age}},
    "price": {{json-params.price}}
  }
```

**Request:** `{"age": 25, "price": 99.99}`
**Response:** `{"age": 25, "price": 99.99}`

⚠️ **Importante:** Sem aspas para números! Com aspas vira string.

### Booleanos

```yaml
body: >
  {
    "completed": {{json-params.completed}},
    "active": {{json-params.active}}
  }
```

**Request:** `{"completed": true, "active": false}`
**Response:** `{"completed": true, "active": false}`

### Null

```yaml
body: >
  {
    "deletedAt": {{json-params.deletedAt}}
  }
```

**Request:** `{"deletedAt": null}`
**Response:** `{"deletedAt": null}`

---

## Combinando Parâmetros

### Body + Path Parameters

```yaml
- endpoint:
    method: PUT
    path: /users/:id|int
    response:
      status: 200
      body: >
        {
          "id": {{path-params.id}},
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "updatedAt": "2024-01-15T10:00:00Z"
        }
```

**Request:**

```bash
curl -X PUT http://localhost:8000/users/42 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Name",
    "email": "updated@example.com"
  }'
```

**Response:**

```json
{
  "id": 42,
  "name": "Updated Name",
  "email": "updated@example.com",
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

### Body + Query Parameters

```yaml
- endpoint:
    method: POST
    path: /api/search
    response:
      status: 200
      body: >
        {
          "query": "{{query-params.q}}",
          "filters": {
            "category": "{{json-params.category}}",
            "tags": "{{json-params.tags}}"
          },
          "results": []
        }
```

**Request:**

```bash
curl -X POST "http://localhost:8000/api/search?q=laptop" \
  -H "Content-Type: application/json" \
  -d '{
    "category": "electronics",
    "tags": ["gaming", "portable"]
  }'
```

### Body + Headers

```yaml
- endpoint:
    method: POST
    path: /api/protected
    response:
      status: 201
      body: >
        {
          "userId": "{{json-params.userId}}",
          "action": "{{json-params.action}}",
          "authenticatedAs": "{{header-params.Authorization}}",
          "userAgent": "{{header-params.User-Agent}}"
        }
```

**Request:**

```bash
curl -X POST http://localhost:8000/api/protected \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer token123" \
  -H "User-Agent: MyApp/1.0" \
  -d '{
    "userId": 42,
    "action": "create"
  }'
```

---

## Casos de Uso Práticos

### 1. Criar Usuário (POST)

```yaml
- endpoint:
    method: POST
    path: /api/users
    response:
      status: 201
      headers:
        Content-Type: application/json
        Location: /api/users/1
      body: >
        {
          "id": 1,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "role": "{{json-params.role}}",
          "createdAt": "2024-01-15T10:00:00Z",
          "emailVerified": false
        }
```

**Request:**

```bash
curl -X POST http://localhost:8000/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "role": "admin"
  }'
```

### 2. Atualizar Perfil (PATCH)

```yaml
- endpoint:
    method: PATCH
    path: /api/users/:id/profile
    response:
      status: 200
      body: >
        {
          "id": {{path-params.id}},
          "profile": {
            "bio": "{{json-params.bio}}",
            "avatar": "{{json-params.avatar}}",
            "website": "{{json-params.website}}"
          },
          "updatedAt": "2024-01-15T10:30:00Z"
        }
```

**Request:**

```bash
curl -X PATCH http://localhost:8000/api/users/1/profile \
  -H "Content-Type: application/json" \
  -d '{
    "bio": "Software Developer",
    "avatar": "https://example.com/avatar.jpg",
    "website": "https://example.com"
  }'
```

### 3. Login (Autenticação)

```yaml
- endpoint:
    method: POST
    path: /api/auth/login
    response:
      status: 200
      body: >
        {
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
          "user": {
            "id": 1,
            "email": "{{json-params.email}}",
            "name": "John Doe"
          },
          "expiresIn": 3600
        }
```

**Request:**

```bash
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "secret123"
  }'
```

### 4. Criar Pedido (E-commerce)

```yaml
- endpoint:
    method: POST
    path: /api/orders
    response:
      status: 201
      body: >
        {
          "orderId": "order-123",
          "customerId": {{json-params.customerId}},
          "items": {{json-params.items}},
          "shippingAddress": {
            "street": "{{json-params.shippingAddress.street}}",
            "city": "{{json-params.shippingAddress.city}}",
            "zipCode": "{{json-params.shippingAddress.zipCode}}"
          },
          "paymentMethod": "{{json-params.paymentMethod}}",
          "total": {{json-params.total}},
          "status": "pending",
          "createdAt": "2024-01-15T10:00:00Z"
        }
```

**Request:**

```bash
curl -X POST http://localhost:8000/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 42,
    "items": [
      {"productId": 1, "quantity": 2, "price": 29.99},
      {"productId": 5, "quantity": 1, "price": 99.99}
    ],
    "shippingAddress": {
      "street": "123 Main St",
      "city": "New York",
      "zipCode": "10001"
    },
    "paymentMethod": "credit_card",
    "total": 159.97
  }'
```

### 5. Upload Metadata (sem arquivo binário)

```yaml
- endpoint:
    method: POST
    path: /api/uploads
    response:
      status: 201
      body: >
        {
          "uploadId": "upload-456",
          "filename": "{{json-params.filename}}",
          "size": {{json-params.size}},
          "contentType": "{{json-params.contentType}}",
          "uploadedAt": "2024-01-15T10:00:00Z",
          "url": "https://cdn.example.com/{{json-params.filename}}"
        }
```

**Request:**

```bash
curl -X POST http://localhost:8000/api/uploads \
  -H "Content-Type: application/json" \
  -d '{
    "filename": "document.pdf",
    "size": 1024000,
    "contentType": "application/pdf"
  }'
```

---

## Validação e Erros

### Campos Obrigatórios (Simulação)

Moclojer **não valida** automaticamente. Simule com endpoints específicos:

```yaml
# 1. Endpoint para request sem campo obrigatório (retorna 400)
- endpoint:
    method: POST
    path: /api/users
    response:
      status: 400
      body: >
        {
          "error": "Validation failed",
          "message": "Field 'email' is required",
          "code": "VALIDATION_ERROR"
        }

# 2. Endpoint para sucesso (deve vir DEPOIS)
- endpoint:
    method: POST
    path: /api/users
    response:
      status: 201
      body: >
        {
          "id": 1,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}"
        }
```

⚠️ **Limitação:** Moclojer não valida se campo existe. Use ferramentas como Prism para validação real.

### Tipos Inválidos

```yaml
- endpoint:
    method: POST
    path: /api/products
    response:
      status: 422
      body: >
        {
          "error": "Unprocessable Entity",
          "message": "Field 'price' must be a number",
          "details": [
            {
              "field": "price",
              "message": "Expected number, got string"
            }
          ]
        }
```

### Email Inválido

```yaml
- endpoint:
    method: POST
    path: /api/users
    response:
      status: 422
      body: >
        {
          "error": "Validation failed",
          "message": "Invalid email format",
          "details": [
            {
              "field": "email",
              "message": "Email must be a valid email address"
            }
          ]
        }
```

---

## Content-Type: application/x-www-form-urlencoded

Moclojer suporta form data (menos comum em APIs modernas):

```yaml
- endpoint:
    method: POST
    path: /api/form-submit
    response:
      status: 200
      body: >
        {
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "message": "Form submitted successfully"
        }
```

**Request:**

```bash
curl -X POST http://localhost:8000/api/form-submit \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "name=John&email=john@example.com"
```

⚠️ **Nota:** Use JSON quando possível. Form-urlencoded tem limitações (sem objetos aninhados).

---

## Boas Práticas

### ✅ Faça

1. **Use JSON para APIs modernas**

   ```bash
   curl -X POST /api/users \
     -H "Content-Type: application/json" \
     -d '{"name": "Alice"}'
   ```

2. **Valide Content-Type no client**

   ```bash
   # ✅ Sempre especifique Content-Type
   -H "Content-Type: application/json"
   ```

3. **Echo de dados recebidos**

   ```yaml
   # Cliente vê o que foi enviado
   response:
     body: >
       {
         "received": {
           "name": "{{json-params.name}}",
           "email": "{{json-params.email}}"
         }
       }
   ```

4. **Use números sem aspas**

   ```yaml
   # ✅ Correto (número)
   {"age": {{json-params.age}}}

   # ❌ Errado (string)
   {"age": "{{json-params.age}}"}
   ```

5. **Endpoints de erro para validação**

   ```yaml
   # Simule erros de validação
   - endpoint:
       path: /api/users
       response:
         status: 400
         body: '{"error": "Validation failed"}'
   ```

### ❌ Evite

1. **GET com body**

   ```bash
   # ❌ GET não deve ter body
   curl -X GET /api/users \
     -d '{"filter": "admin"}'

   # ✅ Use query params
   curl "http://localhost:8000/api/users?filter=admin"
   ```

2. **Dados sensíveis em GET**

   ```bash
   # ❌ NUNCA em query params
   curl "http://localhost:8000/login?password=secret"

   # ✅ Sempre no body (POST)
   curl -X POST /login \
     -d '{"password": "secret"}'
   ```

3. **Body muito grande inline**

   ```yaml
   # ❌ Difícil de ler
   body: >
     {"data": [...1000 items...]}

   # ✅ Use external-body
   external-body:
     provider: json
     path: large-response.json
   ```

---

## Troubleshooting

### Problema: Template não é substituído

**Causa:** Nome do campo incorreto

```yaml
# Request: {"userName": "Alice"}

# ❌ Errado
{"name": "{{json-params.name}}"}  # undefined

# ✅ Correto
{"name": "{{json-params.userName}}"}
```

### Problema: Número vem como string

**Causa:** Aspas ao redor do template

```yaml
# ❌ Retorna "25" (string)
{"age": "{{json-params.age}}"}

# ✅ Retorna 25 (número)
{"age": {{json-params.age}}}
```

### Problema: Objeto aninhado não funciona

**Causa:** Sintaxe incorreta

```yaml
# ✅ Correto
{"city": "{{json-params.address.city}}"}

# ❌ Errado
{"city": "{{json-params[address][city]}}"}
{"city": "{{json-params.address[city]}}"}
```

### Problema: "Unexpected end of JSON"

**Causa:** Template vazio quebra JSON

```yaml
# Se json-params.name está vazio:
{"name": {{json-params.name}}}
# Resulta em: {"name": }  ← INVÁLIDO!

# ✅ Solução: sempre com aspas para strings
{"name": "{{json-params.name}}"}
# Resulta em: {"name": ""}  ← VÁLIDO
```

---

## Próximos Passos

- **[Path Parameters](path-parameters.md)** - Parâmetros na URL
- **[Query Parameters](query-parameters.md)** - Filtros e paginação
- **[Header Parameters](header-parameters.md)** - Headers HTTP
- **[HTTP Methods](../endpoints/http-methods.md)** - POST, PUT, PATCH

## Veja Também

- [CRUD Operations](../../how-to/patterns/crud-operations.md) - Exemplos completos
- [Template Variables](../templates/template-variables.md)  - Referência completa
- [Dynamic Responses](../../getting-started/dynamic-responses.md) - Tutorial prático
