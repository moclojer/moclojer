---
description: >-
  Aprenda a usar parâmetros de path (URL) no moclojer para criar endpoints
  dinâmicos que respondem a diferentes valores de ID, slug e outros dados.
---

# Path Parameters (Parâmetros de URL)

Path parameters permitem que você crie endpoints dinâmicos que respondem a diferentes valores na URL. Por exemplo, um único endpoint `/users/:id` pode responder tanto a `/users/1` quanto a `/users/999`.

## Por que usar Path Parameters?

**Antes (sem path params):**

```yaml
- endpoint:
    path: /users/1
    response:
      body: '{"id": 1, "name": "Alice"}'

- endpoint:
    path: /users/2
    response:
      body: '{"id": 2, "name": "Bob"}'

# ... você precisaria de 1000 endpoints para 1000 usuários! 😱
```

**Depois (com path params):**

```yaml
- endpoint:
    path: /users/:id
    response:
      body: >
        {
          "id": "{{path-params.id}}",
          "name": "User {{path-params.id}}"
        }

# Um único endpoint responde a QUALQUER ID! 🎉
```

## Sintaxe Básica

### Declarando um Path Parameter

Use dois-pontos (`:`) antes do nome do parâmetro:

```yaml
path: /users/:id
```

**Formato:** `/caminho/:nomeDoParametro`

### Acessando o Valor

Use templates `{{path-params.nomeDoParametro}}`:

```yaml
- endpoint:
    method: GET
    path: /users/:id
    response:
      status: 200
      body: >
        {
          "id": "{{path-params.id}}",
          "message": "Você solicitou o usuário {{path-params.id}}"
        }
```

**Teste:**

```bash
curl http://localhost:8000/users/123
# Resposta: {"id": "123", "message": "Você solicitou o usuário 123"}

curl http://localhost:8000/users/alice
# Resposta: {"id": "alice", "message": "Você solicitou o usuário alice"}
```

## Tipos de Path Parameters

Moclojer suporta **validação de tipos** usando a sintaxe `:param|tipo`:

### String (padrão)

```yaml
path: /users/:username        # Aceita qualquer string
path: /users/:username|string # Explícito (mesmo comportamento)
```

**Matches:**

- `/users/alice` ✅
- `/users/bob123` ✅
- `/users/João` ✅

### Integer

```yaml
path: /users/:id|int
```

**Matches:**

- `/users/1` ✅
- `/users/999` ✅
- `/users/0` ✅

**Não matches:**

- `/users/abc` ❌
- `/users/1.5` ❌
- `/users/` ❌

### UUID

```yaml
path: /sessions/:sessionId|uuid
```

**Matches:**

- `/sessions/550e8400-e29b-41d4-a716-446655440000` ✅

**Não matches:**

- `/sessions/abc123` ❌
- `/sessions/123` ❌

### Boolean

```yaml
path: /features/:enabled|boolean
```

**Matches:**

- `/features/true` ✅
- `/features/false` ✅

**Não matches:**

- `/features/yes` ❌
- `/features/1` ❌

## Múltiplos Path Parameters

Você pode ter vários parâmetros no mesmo path:

```yaml
- endpoint:
    method: GET
    path: /users/:userId/posts/:postId
    response:
      status: 200
      body: >
        {
          "userId": "{{path-params.userId}}",
          "postId": "{{path-params.postId}}",
          "post": {
            "id": "{{path-params.postId}}",
            "author": "User {{path-params.userId}}",
            "title": "Post {{path-params.postId}} by User {{path-params.userId}}"
          }
        }
```

**Teste:**

```bash
curl http://localhost:8000/users/42/posts/7
```

**Resposta:**

```json
{
  "userId": "42",
  "postId": "7",
  "post": {
    "id": "7",
    "author": "User 42",
    "title": "Post 7 by User 42"
  }
}
```

## Exemplos Práticos

### Exemplo 1: API de Produtos

```yaml
- endpoint:
    method: GET
    path: /products/:productId|int
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.productId}},
          "name": "Product {{path-params.productId}}",
          "sku": "PRD-{{path-params.productId}}",
          "price": 29.99,
          "inStock": true
        }
```

**Uso:**

```bash
curl http://localhost:8000/products/101
# {"id": 101, "name": "Product 101", "sku": "PRD-101", ...}
```

### Exemplo 2: Blog com Slugs

```yaml
- endpoint:
    method: GET
    path: /blog/:slug|string
    response:
      status: 200
      body: >
        {
          "slug": "{{path-params.slug}}",
          "title": "{{path-params.slug}}",
          "content": "Este é o conteúdo do post {{path-params.slug}}",
          "publishedAt": "2024-01-15T10:00:00Z"
        }
```

**Uso:**

```bash
curl http://localhost:8000/blog/introducao-ao-moclojer
curl http://localhost:8000/blog/path-parameters-guia
```

### Exemplo 3: API RESTful Completa

```yaml
# GET /users/:id - Obter usuário
- endpoint:
    method: GET
    path: /users/:id|int
    response:
      status: 200
      body: >
        {
          "id": {{path-params.id}},
          "name": "User {{path-params.id}}",
          "email": "user{{path-params.id}}@example.com"
        }

# PUT /users/:id - Atualizar usuário
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
          "updated": true
        }

# DELETE /users/:id - Deletar usuário
- endpoint:
    method: DELETE
    path: /users/:id|int
    response:
      status: 204   # No Content
```

### Exemplo 4: Nested Resources

```yaml
# GET /organizations/:orgId/teams/:teamId/members/:memberId
- endpoint:
    method: GET
    path: /organizations/:orgId|int/teams/:teamId|int/members/:memberId|int
    response:
      status: 200
      body: >
        {
          "organizationId": {{path-params.orgId}},
          "teamId": {{path-params.teamId}},
          "memberId": {{path-params.memberId}},
          "member": {
            "id": {{path-params.memberId}},
            "name": "Member {{path-params.memberId}}",
            "team": "Team {{path-params.teamId}}",
            "organization": "Org {{path-params.orgId}}"
          }
        }
```

**Uso:**

```bash
curl http://localhost:8000/organizations/1/teams/5/members/42
```

## Combinando com Outros Parâmetros

Path parameters funcionam junto com query params e body params:

```yaml
- endpoint:
    method: GET
    path: /users/:userId|int/posts
    response:
      status: 200
      body: >
        {
          "userId": {{path-params.userId}},
          "limit": "{{query-params.limit}}",
          "offset": "{{query-params.offset}}",
          "posts": [
            {"id": 1, "title": "Post 1"},
            {"id": 2, "title": "Post 2"}
          ]
        }
```

**Uso:**

```bash
curl "http://localhost:8000/users/42/posts?limit=10&offset=0"
```

**Resposta:**

```json
{
  "userId": 42,
  "limit": "10",
  "offset": "0",
  "posts": [...]
}
```

## Precedência de Rotas

Quando múltiplos endpoints podem fazer match, moclojer usa a primeira ocorrência:

```yaml
# ⚠️ Ordem importa!

# 1. Rota específica (deve vir PRIMEIRO)
- endpoint:
    path: /users/me
    response:
      body: '{"currentUser": true}'

# 2. Rota genérica (deve vir DEPOIS)
- endpoint:
    path: /users/:id
    response:
      body: '{"id": "{{path-params.id}}"}'
```

**Como funciona:**

- `GET /users/me` → match com endpoint #1 ✅
- `GET /users/123` → match com endpoint #2 ✅

**Se inverter a ordem:**

```yaml
# ❌ PROBLEMA: rota genérica vem primeiro!
- endpoint:
    path: /users/:id
    response:
      body: '{"id": "{{path-params.id}}"}'

- endpoint:
    path: /users/me
    response:
      body: '{"currentUser": true}'  # NUNCA será usado!
```

**Resultado:**

- `GET /users/me` → match com endpoint #1 (`:id` = "me") ❌ Errado!

**Regra de ouro:** **Rotas específicas antes de rotas dinâmicas!**

## Validação e Erros

### Tipo Incorreto

Se você definir um tipo e o valor não corresponder:

```yaml
- endpoint:
    path: /users/:id|int
```

**Requests:**

- `/users/123` → ✅ Match
- `/users/abc` → ❌ Não match (moclojer retorna 404)

### Criando Endpoints de Erro Específicos

```yaml
# Endpoint específico para ID não encontrado
- endpoint:
    method: GET
    path: /users/999
    response:
      status: 404
      body: >
        {
          "error": "User not found",
          "message": "User with ID 999 does not exist"
        }

# Endpoint genérico (deve vir depois)
- endpoint:
    method: GET
    path: /users/:id|int
    response:
      status: 200
      body: >
        {
          "id": {{path-params.id}},
          "name": "User {{path-params.id}}"
        }
```

## Boas Práticas

### ✅ Faça

1. **Use tipos explícitos quando possível**

   ```yaml
   path: /users/:id|int      # ✅ Valida que é número
   ```

2. **Nomes descritivos para parâmetros**

   ```yaml
   path: /posts/:postId      # ✅ Claro
   path: /posts/:id          # ⚠️ Menos claro em nested resources
   ```

3. **Rotas específicas antes de dinâmicas**

   ```yaml
   - path: /users/me         # ✅ Primeiro
   - path: /users/:id        # ✅ Depois
   ```

4. **Use o valor do parâmetro na resposta**

   ```yaml
   body: >
     {"id": "{{path-params.id}}"}  # ✅ Response reflete o input
   ```

### ❌ Evite

1. **Parâmetros sem tipo quando deveria ter**

   ```yaml
   path: /users/:id          # ⚠️ Aceita "abc" como ID
   path: /users/:id|int      # ✅ Só aceita números
   ```

2. **Nomes genéricos demais**

   ```yaml
   path: /api/:param1/:param2  # ❌ O que são?
   path: /api/:userId/:postId  # ✅ Autodocumentado
   ```

3. **Muitos níveis de aninhamento**

   ```yaml
   path: /a/:b/c/:d/e/:f/g/:h  # ❌ Difícil de ler
   path: /users/:id/posts      # ✅ Máximo 2-3 níveis
   ```

## Troubleshooting

### Problema: "404 Not Found" quando deveria funcionar

**Possíveis causas:**

1. **Tipo de parâmetro incorreto**

   ```yaml
   path: /users/:id|int
   # Tentando: /users/abc → 404 (correto, não é int)
   ```

2. **Ordem de rotas errada**

   ```yaml
   # Se /users/:id está antes de /users/me
   # /users/me vai fazer match com :id="me"
   ```

3. **Método HTTP diferente**

   ```yaml
   method: GET
   path: /users/:id
   # POST /users/123 → 404 (método errado)
   ```

### Problema: Template `{{path-params.id}}` não é substituído

**Causa:** Nome do parâmetro não corresponde

```yaml
# ❌ Errado
path: /users/:userId
body: '{"id": "{{path-params.id}}"}'  # Deveria ser userId!

# ✅ Correto
path: /users/:userId
body: '{"id": "{{path-params.userId}}"}'
```

### Problema: Parâmetro vem como string quando queria número

**Causa:** Template strings sempre retornam strings

```yaml
# ❌ Retorna string "123"
body: >
  {
    "id": "{{path-params.id}}"
  }

# ✅ Retorna número 123
body: >
  {
    "id": {{path-params.id}}
  }
```

**Nota:** Sem aspas = número, com aspas = string.

## Próximos Passos

Agora que você domina path parameters:

1. **[Query Parameters](query-parameters.md)** - Parâmetros na URL após `?`
2. **[Body Parameters](body-parameters.md)** - Dados no corpo da requisição
3. **[Template Variables](../templates/template-variables.md)** - Referência completa de templates

## Veja Também

- [HTTP Methods](../endpoints/http-methods.md) - GET, POST, PUT, DELETE, etc.
- [Path Patterns](../endpoints/path-patterns.md) - Padrões avançados de rotas
- [Dynamic Responses Tutorial](../../getting-started/dynamic-responses.md) - Tutorial prático
