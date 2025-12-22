---
description: >-
  Aprenda a usar query parameters (parâmetros de consulta) no moclojer para
  criar endpoints que respondem a filtros, paginação, busca e ordenação.
---

# Query Parameters (Parâmetros de Consulta)

Query parameters são valores passados na URL após o caractere `?`, usados para filtrar, paginar, ordenar ou buscar dados. Eles são essenciais para criar APIs RESTful flexíveis e dinâmicas.

## O Que São Query Parameters?

**Anatomia de uma URL com query params:**
```
https://api.example.com/users?role=admin&status=active&limit=10
                          └─┬─┘ └──┬───┘ └────┬────┘ └──┬──┘
                            │      │          │         │
                            │      │          │         └─ limit=10
                            │      │          └─────────── status=active
                            │      └──────────────────── role=admin
                            └───────────────────────── Separador ?
```

**Características:**
- Começam com `?` após o path
- Pares `chave=valor` separados por `&`
- Opcionais (diferente de path params)
- Ideais para filtros, paginação, busca

## Sintaxe Básica

### Acessando Query Parameters

Use templates `{{query-params.nomeDaChave}}`:

```yaml
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        {
          "filters": {
            "role": "{{query-params.role}}",
            "status": "{{query-params.status}}"
          },
          "results": []
        }
```

**Teste:**
```bash
curl "http://localhost:8000/users?role=admin&status=active"
```

**Resposta:**
```json
{
  "filters": {
    "role": "admin",
    "status": "active"
  },
  "results": []
}
```

### Query Parameters Opcionais

Todos os query params são opcionais por padrão:

```yaml
- endpoint:
    method: GET
    path: /products
    response:
      status: 200
      body: >
        {
          "category": "{{query-params.category}}",
          "minPrice": "{{query-params.min_price}}",
          "maxPrice": "{{query-params.max_price}}"
        }
```

**Funciona com qualquer combinação:**
```bash
# Todos os parâmetros
curl "http://localhost:8000/products?category=electronics&min_price=100&max_price=500"

# Apenas alguns
curl "http://localhost:8000/products?category=electronics"

# Nenhum parâmetro
curl "http://localhost:8000/products"
# {"category": "", "minPrice": "", "maxPrice": ""}
```

## Casos de Uso Comuns

### 1. Paginação

```yaml
- endpoint:
    method: GET
    path: /posts
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "1000"
      body: >
        {
          "page": "{{query-params.page}}",
          "limit": "{{query-params.limit}}",
          "total": 1000,
          "posts": [
            {"id": 1, "title": "Post 1"},
            {"id": 2, "title": "Post 2"},
            {"id": 3, "title": "Post 3"}
          ],
          "pagination": {
            "currentPage": "{{query-params.page}}",
            "perPage": "{{query-params.limit}}",
            "totalPages": 100
          }
        }
```

**Uso:**
```bash
curl "http://localhost:8000/posts?page=1&limit=10"
curl "http://localhost:8000/posts?page=2&limit=20"
```

### 2. Filtros

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
            "brand": "{{query-params.brand}}",
            "inStock": "{{query-params.in_stock}}"
          },
          "products": [
            {
              "id": 1,
              "name": "Product matching filters",
              "category": "{{query-params.category}}"
            }
          ]
        }
```

**Uso:**
```bash
curl "http://localhost:8000/products?category=electronics&brand=sony&in_stock=true"
```

### 3. Busca (Search)

```yaml
- endpoint:
    method: GET
    path: /search
    response:
      status: 200
      body: >
        {
          "query": "{{query-params.q}}",
          "type": "{{query-params.type}}",
          "results": [
            {
              "id": 1,
              "title": "Result containing: {{query-params.q}}",
              "type": "{{query-params.type}}"
            }
          ]
        }
```

**Uso:**
```bash
curl "http://localhost:8000/search?q=moclojer&type=documentation"
```

### 4. Ordenação (Sorting)

```yaml
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        {
          "sort": {
            "field": "{{query-params.sort}}",
            "order": "{{query-params.order}}"
          },
          "users": [
            {"id": 1, "name": "Alice", "createdAt": "2024-01-01"},
            {"id": 2, "name": "Bob", "createdAt": "2024-01-02"}
          ],
          "meta": {
            "sortedBy": "{{query-params.sort}}",
            "sortOrder": "{{query-params.order}}"
          }
        }
```

**Uso:**
```bash
curl "http://localhost:8000/users?sort=name&order=asc"
curl "http://localhost:8000/users?sort=createdAt&order=desc"
```

### 5. Seleção de Campos (Field Selection)

```yaml
- endpoint:
    method: GET
    path: /users/:id
    response:
      status: 200
      body: >
        {
          "id": "{{path-params.id}}",
          "requestedFields": "{{query-params.fields}}",
          "data": {
            "id": "{{path-params.id}}",
            "name": "User {{path-params.id}}",
            "email": "user@example.com",
            "note": "Fields requested: {{query-params.fields}}"
          }
        }
```

**Uso:**
```bash
# Apenas alguns campos
curl "http://localhost:8000/users/1?fields=id,name,email"

# Todos os campos
curl "http://localhost:8000/users/1"
```

## Combinando Múltiplos Query Parameters

```yaml
- endpoint:
    method: GET
    path: /api/products
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "filters": {
            "category": "{{query-params.category}}",
            "minPrice": "{{query-params.min_price}}",
            "maxPrice": "{{query-params.max_price}}",
            "inStock": "{{query-params.in_stock}}"
          },
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}"
          },
          "sort": {
            "field": "{{query-params.sort}}",
            "order": "{{query-params.order}}"
          },
          "search": "{{query-params.q}}",
          "products": []
        }
```

**Exemplo de uso completo:**
```bash
curl "http://localhost:8000/api/products?category=electronics&min_price=100&max_price=1000&in_stock=true&page=1&limit=20&sort=price&order=asc&q=laptop"
```

## Query Parameters com Path Parameters

Query params funcionam perfeitamente com path params:

```yaml
- endpoint:
    method: GET
    path: /users/:userId/posts
    response:
      status: 200
      body: >
        {
          "userId": "{{path-params.userId}}",
          "filters": {
            "status": "{{query-params.status}}",
            "category": "{{query-params.category}}"
          },
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}"
          },
          "posts": [
            {
              "id": 1,
              "authorId": "{{path-params.userId}}",
              "status": "{{query-params.status}}",
              "title": "Post by User {{path-params.userId}}"
            }
          ]
        }
```

**Uso:**
```bash
curl "http://localhost:8000/users/42/posts?status=published&page=1&limit=10"
```

## Valores Padrão e Valores Ausentes

### Query Param Não Fornecido

Quando um query param não é passado, o template retorna string vazia:

```yaml
- endpoint:
    path: /users
    response:
      body: >
        {
          "limit": "{{query-params.limit}}",
          "page": "{{query-params.page}}"
        }
```

**Teste sem parâmetros:**
```bash
curl "http://localhost:8000/users"
# {"limit": "", "page": ""}
```

### Simulando Valores Padrão

Use múltiplos endpoints com precedência:

```yaml
# 1. Endpoint SEM query params (usa valores padrão)
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        {
          "page": "1",
          "limit": "10",
          "note": "Using default values"
        }

# 2. Endpoint COM query params (valores customizados)
# NOTA: Moclojer não diferencia presença/ausência de query params
# Ambos os endpoints acima vão responder. Use lógica no client.
```

**Alternativa melhor:**
```yaml
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        {
          "page": "{{query-params.page}}",
          "limit": "{{query-params.limit}}",
          "defaults": {
            "page": "1",
            "limit": "10"
          },
          "note": "Client should use defaults.page if page is empty"
        }
```

## Arrays em Query Parameters

URLs podem ter múltiplos valores para a mesma chave:

```
/users?id=1&id=2&id=3
/tags?tag=javascript&tag=clojure&tag=api
```

**No moclojer:**
```yaml
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        {
          "requestedIds": "{{query-params.id}}",
          "note": "Multiple IDs: 1, 2, 3"
        }
```

⚠️ **Limitação:** Moclojer atualmente retorna apenas o **último valor** quando há duplicatas.

**Workaround:** Use delimitadores
```bash
# Ao invés de: ?id=1&id=2&id=3
curl "http://localhost:8000/users?ids=1,2,3"
```

```yaml
response:
  body: >
    {
      "ids": "{{query-params.ids}}",
      "note": "Client splits by comma: [1, 2, 3]"
    }
```

## Caracteres Especiais

Query parameters devem ser URL-encoded:

| Caractere | Encoding | Exemplo |
|-----------|----------|---------|
| Espaço | `%20` ou `+` | `q=hello%20world` |
| `&` | `%26` | `company=A%26B` |
| `=` | `%3D` | `equation=x%3D5` |
| `#` | `%23` | `tag=%23moclojer` |
| `?` | `%3F` | `query=what%3F` |

**Exemplo:**
```bash
# Busca: "moclojer & API testing"
curl "http://localhost:8000/search?q=moclojer%20%26%20API%20testing"
```

Ferramentas como `curl` e browsers fazem encoding automaticamente.

## Exemplos Práticos Completos

### Exemplo 1: API de E-commerce

```yaml
- endpoint:
    method: GET
    path: /api/products
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "250"
      body: >
        {
          "meta": {
            "total": 250,
            "page": "{{query-params.page}}",
            "perPage": "{{query-params.limit}}",
            "totalPages": 25
          },
          "filters": {
            "category": "{{query-params.category}}",
            "brand": "{{query-params.brand}}",
            "minPrice": "{{query-params.min_price}}",
            "maxPrice": "{{query-params.max_price}}",
            "onSale": "{{query-params.on_sale}}",
            "inStock": "{{query-params.in_stock}}"
          },
          "sort": {
            "by": "{{query-params.sort_by}}",
            "order": "{{query-params.order}}"
          },
          "search": "{{query-params.q}}",
          "products": [
            {
              "id": 1,
              "name": "Product matching filters",
              "price": 299.99,
              "category": "{{query-params.category}}"
            }
          ]
        }
```

**Uso:**
```bash
curl "http://localhost:8000/api/products?category=laptops&brand=dell&min_price=500&max_price=1500&on_sale=true&in_stock=true&sort_by=price&order=asc&page=1&limit=20&q=gaming"
```

### Exemplo 2: API de Blog

```yaml
- endpoint:
    method: GET
    path: /api/posts
    response:
      status: 200
      body: >
        {
          "filters": {
            "author": "{{query-params.author}}",
            "category": "{{query-params.category}}",
            "tag": "{{query-params.tag}}",
            "published": "{{query-params.published}}"
          },
          "search": "{{query-params.q}}",
          "dateRange": {
            "from": "{{query-params.from}}",
            "to": "{{query-params.to}}"
          },
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}"
          },
          "posts": [
            {
              "id": 1,
              "title": "Post about {{query-params.q}}",
              "author": "{{query-params.author}}",
              "publishedAt": "2024-01-15"
            }
          ]
        }
```

**Uso:**
```bash
curl "http://localhost:8000/api/posts?author=john&category=tech&tag=api&published=true&q=moclojer&from=2024-01-01&to=2024-12-31&page=1&limit=10"
```

### Exemplo 3: API Analytics

```yaml
- endpoint:
    method: GET
    path: /api/analytics
    response:
      status: 200
      body: >
        {
          "timeRange": {
            "start": "{{query-params.start_date}}",
            "end": "{{query-params.end_date}}"
          },
          "metrics": "{{query-params.metrics}}",
          "dimensions": "{{query-params.dimensions}}",
          "filters": "{{query-params.filters}}",
          "granularity": "{{query-params.granularity}}",
          "data": [
            {
              "date": "{{query-params.start_date}}",
              "metrics": "{{query-params.metrics}}",
              "value": 1250
            }
          ]
        }
```

**Uso:**
```bash
curl "http://localhost:8000/api/analytics?start_date=2024-01-01&end_date=2024-01-31&metrics=pageviews,sessions&dimensions=country,device&granularity=day"
```

## Boas Práticas

### ✅ Faça

1. **Use nomes descritivos e consistentes**
   ```bash
   # ✅ Bom
   ?page=1&limit=10&sort_by=name

   # ❌ Evite
   ?p=1&l=10&s=name
   ```

2. **Use snake_case ou camelCase consistentemente**
   ```bash
   # ✅ snake_case
   ?min_price=100&max_price=500

   # ✅ camelCase
   ?minPrice=100&maxPrice=500

   # ❌ Misturado
   ?min_price=100&maxPrice=500
   ```

3. **Valores booleanos como strings**
   ```yaml
   # Template sempre retorna string
   "inStock": "{{query-params.in_stock}}"

   # Cliente interpreta "true"/"false"
   ```

4. **Documente parâmetros opcionais**
   ```yaml
   # No response, inclua documentação
   response:
     body: >
       {
         "_docs": {
           "params": {
             "page": "Page number (default: 1)",
             "limit": "Items per page (default: 10)",
             "sort": "Sort field (default: createdAt)"
           }
         }
       }
   ```

### ❌ Evite

1. **Parâmetros obrigatórios como query params**
   ```yaml
   # ❌ ID deveria ser path param
   path: /users
   # ?id=123

   # ✅ Use path param para IDs
   path: /users/:id
   ```

2. **Dados sensíveis em query params**
   ```bash
   # ❌ NUNCA faça isso!
   ?password=secret123
   ?api_key=sk_live_abc123

   # ✅ Use headers ou body
   Authorization: Bearer token
   ```

3. **Query params muito complexos**
   ```bash
   # ❌ Muito complexo para query param
   ?filter={"and":[{"field":"age","op":">","value":18}]}

   # ✅ Use POST com body para filtros complexos
   ```

## Troubleshooting

### Problema: Query param retorna vazio quando deveria ter valor

**Causa 1:** Nome do parâmetro não corresponde
```yaml
# ❌ Errado
URL: ?limit=10
Template: {{query-params.lim}}  # Typo!

# ✅ Correto
Template: {{query-params.limit}}
```

**Causa 2:** Espaços no nome
```yaml
# ❌ Não funciona
{{query-params.min price}}

# ✅ Use underscore ou camelCase
{{query-params.min_price}}
{{query-params.minPrice}}
```

### Problema: Caractere especial quebra a URL

**Solução:** Use URL encoding
```bash
# ❌ Quebra
curl "http://localhost:8000/search?q=A&B"

# ✅ Encode
curl "http://localhost:8000/search?q=A%26B"
```

### Problema: Número vem como string

**Causa:** Templates sempre retornam strings
```yaml
# ❌ Retorna "10" (string)
{"limit": "{{query-params.limit}}"}

# ✅ Retorna 10 (número) - sem aspas!
{"limit": {{query-params.limit}}}
```

⚠️ **Cuidado:** Sem aspas, se parâmetro estiver vazio, o JSON fica inválido!

**Solução segura:**
```yaml
# Cliente faz conversão
{"limit": "{{query-params.limit}}"}
# Cliente: parseInt(data.limit) || 10
```

## Próximos Passos

Agora que você domina query parameters:

1. **[Path Parameters](path-parameters.md)** - Parâmetros na URL
2. **[Body Parameters](body-parameters.md)** - Dados no corpo da requisição
3. **[Template Variables](../templates/template-variables.md)** - Referência completa

## Veja Também

- [Pagination How-to](../../how-to/patterns/pagination.md) - Implementar paginação
- [Dynamic Responses Tutorial](../../getting-started/dynamic-responses.md) - Tutorial prático
- [Request Matching](../request-matching.md) - Como moclojer escolhe endpoints
