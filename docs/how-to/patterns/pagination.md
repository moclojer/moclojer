---
description: >-
  Aprenda a implementar paginação em APIs mock com moclojer. Offset/Limit,
  Cursor-based, Page-based e Link headers (RFC 5988).
---

# Pagination (Paginação)

Paginação é essencial em APIs que retornam listas grandes de dados. Este guia mostra como implementar diferentes estratégias de paginação com moclojer.

## Por Que Paginar?

✅ **Performance**: Não carregar 10.000 itens de uma vez
✅ **UX**: Melhor experiência do usuário
✅ **Bandwidth**: Menos dados trafegados
✅ **Custo**: Menos processamento no servidor

**Sem paginação:**

```json
GET /users → [10.000 usuários] 😱
```

**Com paginação:**

```json
GET /users?page=1&limit=20 → [20 usuários] ✅
```

---

## Estratégias de Paginação

### 1. Offset/Limit (Mais Comum)

**Conceito:** Pule X itens, retorne Y itens.

**Parâmetros:**

- `limit` (ou `per_page`): Quantidade por página
- `offset` (ou `skip`): Quantos pular

**Matemática:**

```
offset = (page - 1) * limit
```

**Exemplo:**

```yaml
- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "1000"
      body: >
        {
          "data": [
            {"id": 1, "name": "User 1"},
            {"id": 2, "name": "User 2"},
            {"id": 3, "name": "User 3"}
          ],
          "pagination": {
            "offset": "{{query-params.offset}}",
            "limit": "{{query-params.limit}}",
            "total": 1000
          }
        }
```

**Uso:**

```bash
# Primeira página (0-19)
curl "http://localhost:8000/api/users?offset=0&limit=20"

# Segunda página (20-39)
curl "http://localhost:8000/api/users?offset=20&limit=20"

# Terceira página (40-59)
curl "http://localhost:8000/api/users?offset=40&limit=20"
```

---

### 2. Page/Limit (Mais Intuitivo)

**Conceito:** Número da página + itens por página.

**Parâmetros:**

- `page`: Número da página (começa em 1)
- `limit` (ou `per_page`): Itens por página

**Exemplo:**

```yaml
- endpoint:
    method: GET
    path: /api/products
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "500"
        X-Total-Pages: "50"
      body: >
        {
          "data": [
            {"id": 1, "name": "Product 1", "price": 29.99},
            {"id": 2, "name": "Product 2", "price": 39.99}
          ],
          "meta": {
            "currentPage": "{{query-params.page}}",
            "perPage": "{{query-params.limit}}",
            "totalItems": 500,
            "totalPages": 50,
            "hasNextPage": true,
            "hasPreviousPage": false
          }
        }
```

**Uso:**

```bash
# Primeira página
curl "http://localhost:8000/api/products?page=1&limit=10"

# Segunda página
curl "http://localhost:8000/api/products?page=2&limit=10"

# Página 5 com 25 itens
curl "http://localhost:8000/api/products?page=5&limit=25"
```

---

### 3. Cursor-Based (Para Feeds Dinâmicos)

**Conceito:** Usa um cursor (ID, timestamp) para marcar posição.

**Vantagens:**

- Consistente mesmo com novos itens
- Perfeito para infinite scroll
- Não permite pular páginas (mais seguro)

**Exemplo:**

```yaml
- endpoint:
    method: GET
    path: /api/posts
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "data": [
            {"id": 101, "title": "Post 101", "createdAt": "2024-01-15T10:00:00Z"},
            {"id": 102, "title": "Post 102", "createdAt": "2024-01-15T11:00:00Z"},
            {"id": 103, "title": "Post 103", "createdAt": "2024-01-15T12:00:00Z"}
          ],
          "pagination": {
            "nextCursor": "{{query-params.cursor}}",
            "hasMore": true
          },
          "links": {
            "next": "/api/posts?cursor={{query-params.cursor}}&limit={{query-params.limit}}"
          }
        }
```

**Uso:**

```bash
# Primeira requisição (sem cursor)
curl "http://localhost:8000/api/posts?limit=10"

# Próxima página (usa cursor retornado)
curl "http://localhost:8000/api/posts?cursor=103&limit=10"

# Mais uma página
curl "http://localhost:8000/api/posts?cursor=203&limit=10"
```

---

## Metadados de Paginação

### Estrutura Completa

```yaml
- endpoint:
    method: GET
    path: /api/items
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "1000"
      body: >
        {
          "data": [
            {"id": 1, "name": "Item 1"}
          ],
          "meta": {
            "pagination": {
              "page": "{{query-params.page}}",
              "perPage": "{{query-params.limit}}",
              "totalPages": 100,
              "totalItems": 1000,
              "hasNextPage": true,
              "hasPreviousPage": false
            }
          },
          "links": {
            "self": "/api/items?page={{query-params.page}}&limit={{query-params.limit}}",
            "first": "/api/items?page=1&limit={{query-params.limit}}",
            "prev": null,
            "next": "/api/items?page=2&limit={{query-params.limit}}",
            "last": "/api/items?page=100&limit={{query-params.limit}}"
          }
        }
```

### Headers de Paginação

```yaml
- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "1000"
        X-Page: "{{query-params.page}}"
        X-Per-Page: "{{query-params.limit}}"
        X-Total-Pages: "100"
        Link: '</api/users?page=2&limit=10>; rel="next", </api/users?page=100&limit=10>; rel="last"'
      body: >
        {
          "data": []
        }
```

---

## Exemplos Práticos

### 1. API de E-commerce (Products)

```yaml
- endpoint:
    method: GET
    path: /api/products
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "2500"
      body: >
        {
          "products": [
            {
              "id": 1,
              "name": "Laptop",
              "price": 999.99,
              "category": "{{query-params.category}}"
            },
            {
              "id": 2,
              "name": "Mouse",
              "price": 29.99,
              "category": "{{query-params.category}}"
            }
          ],
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}",
            "total": 2500,
            "totalPages": 250,
            "hasMore": true
          },
          "filters": {
            "category": "{{query-params.category}}",
            "minPrice": "{{query-params.min_price}}",
            "maxPrice": "{{query-params.max_price}}"
          }
        }
```

**Uso:**

```bash
# Primeira página de eletrônicos
curl "http://localhost:8000/api/products?category=electronics&page=1&limit=20"

# Segunda página com filtro de preço
curl "http://localhost:8000/api/products?category=electronics&min_price=100&max_price=500&page=2&limit=20"
```

### 2. API de Blog (Posts com Cursor)

```yaml
- endpoint:
    method: GET
    path: /api/posts
    response:
      status: 200
      body: >
        {
          "posts": [
            {
              "id": 101,
              "title": "Getting Started with moclojer",
              "slug": "getting-started-moclojer",
              "author": "Alice",
              "publishedAt": "2024-01-15T10:00:00Z"
            },
            {
              "id": 102,
              "title": "API Mocking Best Practices",
              "slug": "api-mocking-best-practices",
              "author": "Bob",
              "publishedAt": "2024-01-14T15:30:00Z"
            }
          ],
          "pagination": {
            "cursor": "{{query-params.cursor}}",
            "limit": "{{query-params.limit}}",
            "hasMore": true,
            "next": "/api/posts?cursor=102&limit={{query-params.limit}}"
          }
        }
```

**Uso:**

```bash
# Primeira carga
curl "http://localhost:8000/api/posts?limit=20"

# Scroll infinito (próxima página)
curl "http://localhost:8000/api/posts?cursor=102&limit=20"
```

### 3. API de Comentários (Nested Pagination)

```yaml
- endpoint:
    method: GET
    path: /api/posts/:postId/comments
    response:
      status: 200
      body: >
        {
          "postId": "{{path-params.postId}}",
          "comments": [
            {
              "id": 1,
              "author": "Alice",
              "text": "Great post!",
              "createdAt": "2024-01-15T10:00:00Z"
            },
            {
              "id": 2,
              "author": "Bob",
              "text": "Thanks for sharing!",
              "createdAt": "2024-01-15T11:00:00Z"
            }
          ],
          "pagination": {
            "page": "{{query-params.page}}",
            "perPage": "{{query-params.limit}}",
            "total": 156,
            "totalPages": 16
          }
        }
```

**Uso:**

```bash
curl "http://localhost:8000/api/posts/42/comments?page=1&limit=10"
```

### 4. API com Valores Padrão

```yaml
# Endpoint SEM parâmetros (usa defaults)
- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      body: >
        {
          "data": [
            {"id": 1, "name": "Alice"},
            {"id": 2, "name": "Bob"}
          ],
          "meta": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}",
            "defaults": {
              "page": "1",
              "limit": "10",
              "note": "Use defaults if page/limit is empty"
            }
          }
        }
```

**Uso:**

```bash
# Sem parâmetros (cliente usa defaults da response)
curl "http://localhost:8000/api/users"

# Com parâmetros
curl "http://localhost:8000/api/users?page=2&limit=50"
```

---

## Link Headers (RFC 5988)

Padrão para navegação de paginação via headers:

```yaml
- endpoint:
    method: GET
    path: /api/items
    response:
      status: 200
      headers:
        Content-Type: application/json
        Link: '</api/items?page=1>; rel="first", </api/items?page=5>; rel="prev", </api/items?page=7>; rel="next", </api/items?page=100>; rel="last"'
      body: >
        {
          "data": [
            {"id": 1, "name": "Item 1"}
          ]
        }
```

**Formato:**

```
Link: <URL>; rel="relation"
```

**Relações:**

- `first`: Primeira página
- `prev`: Página anterior
- `next`: Próxima página
- `last`: Última página
- `self`: Página atual

**Exemplo do GitHub API:**

```
Link: <https://api.github.com/repos?page=2>; rel="next",
      <https://api.github.com/repos?page=50>; rel="last"
```

---

## Ordenação + Paginação

```yaml
- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      body: >
        {
          "data": [
            {"id": 1, "name": "Alice", "createdAt": "2024-01-01T00:00:00Z"},
            {"id": 2, "name": "Bob", "createdAt": "2024-01-02T00:00:00Z"}
          ],
          "sort": {
            "field": "{{query-params.sort}}",
            "order": "{{query-params.order}}"
          },
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}"
          }
        }
```

**Uso:**

```bash
# Ordenar por nome (A-Z), página 1
curl "http://localhost:8000/api/users?sort=name&order=asc&page=1&limit=20"

# Ordenar por data (mais recente), página 2
curl "http://localhost:8000/api/users?sort=createdAt&order=desc&page=2&limit=20"
```

---

## Filtros + Paginação

```yaml
- endpoint:
    method: GET
    path: /api/products
    response:
      status: 200
      body: >
        {
          "data": [],
          "filters": {
            "category": "{{query-params.category}}",
            "brand": "{{query-params.brand}}",
            "inStock": "{{query-params.in_stock}}"
          },
          "sort": {
            "field": "{{query-params.sort}}",
            "order": "{{query-params.order}}"
          },
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}",
            "total": 150
          }
        }
```

**Uso:**

```bash
curl "http://localhost:8000/api/products?category=electronics&brand=sony&in_stock=true&sort=price&order=asc&page=1&limit=20"
```

---

## Busca + Paginação

```yaml
- endpoint:
    method: GET
    path: /api/search
    response:
      status: 200
      body: >
        {
          "query": "{{query-params.q}}",
          "results": [
            {
              "id": 1,
              "title": "Result containing {{query-params.q}}",
              "relevance": 0.95
            }
          ],
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}",
            "totalResults": 456
          }
        }
```

**Uso:**

```bash
curl "http://localhost:8000/api/search?q=moclojer&page=1&limit=10"
```

---

## Boas Práticas

### ✅ Faça

1. **Sempre retorne metadados**

   ```json
   {
     "data": [...],
     "meta": {
       "total": 1000,
       "page": 1,
       "perPage": 20
     }
   }
   ```

2. **Use headers para totais**

   ```yaml
   headers:
     X-Total-Count: "1000"
     X-Total-Pages: "50"
   ```

3. **Forneça links de navegação**

   ```json
   {
     "links": {
       "next": "/api/items?page=2",
       "prev": "/api/items?page=1",
       "last": "/api/items?page=100"
     }
   }
   ```

4. **Limite máximo de itens**

   ```json
   {
     "meta": {
       "maxLimit": 100,
       "requestedLimit": "{{query-params.limit}}"
     }
   }
   ```

5. **Consistência nos nomes**

   ```bash
   # ✅ Escolha um padrão e siga
   ?page=1&limit=20
   ?page=1&per_page=20

   # ❌ Não misture
   ?page=1&limit=20
   ?offset=0&per_page=20
   ```

### ❌ Evite

1. **Paginação sem total**

   ```json
   // ❌ Difícil saber quantas páginas existem
   {"data": [...]}

   // ✅ Inclua totais
   {"data": [...], "total": 1000}
   ```

2. **Limites muito altos**

   ```bash
   # ❌ Pode sobrecarregar
   ?limit=10000

   # ✅ Defina máximo razoável
   ?limit=100  # max
   ```

3. **Offset sem limit**

   ```bash
   # ❌ Ambíguo
   ?offset=100

   # ✅ Sempre juntos
   ?offset=100&limit=20
   ```

4. **Links quebrados**

   ```json
   // ❌ Link inválido
   "next": "/api/items?page=undefined"

   // ✅ Validar antes de retornar
   "next": "/api/items?page=2&limit=20"
   ```

---

## Troubleshooting

### Problema: Cliente não sabe total de páginas

**Solução:** Retorne `totalPages` ou `X-Total-Pages` header

```yaml
headers:
  X-Total-Pages: "50"
body: >
  {"meta": {"totalPages": 50}}
```

### Problema: Links de navegação quebrados

**Solução:** Use template vars corretamente

```yaml
# ✅ Correto
"next": "/api/items?page=2&limit={{query-params.limit}}"

# ❌ Errado (hardcoded)
"next": "/api/items?page=2&limit=10"
```

### Problema: Paginação + Filtros perdidos

**Solução:** Preserve todos query params nos links

```yaml
"next": "/api/products?category={{query-params.category}}&page=2&limit={{query-params.limit}}"
```

---

## Comparação de Estratégias

| Estratégia | Prós | Contras | Quando Usar |
|------------|------|---------|-------------|
| **Offset/Limit** | Simples, permite pular páginas | Inconsistente com mudanças | APIs tradicionais |
| **Page/Limit** | Intuitivo, fácil entender | Mesmos problemas de offset | UIs com paginação |
| **Cursor** | Consistente, perfeito p/ feeds | Não permite pular, mais complexo | Infinite scroll, feeds |

---

## Próximos Passos

- **[Query Parameters](../../topics/parameters/query-parameters.md)** - Filtros e busca
- **[CRUD Operations](crud-operations.md)** - Operações completas
- **[Authentication Mock](authentication-mock.md)** - Simular autenticação

## Veja Também

- [GitHub API Pagination](https://docs.github.com/en/rest/guides/using-pagination-in-the-rest-api)
- [RFC 5988 (Link Headers)](https://tools.ietf.org/html/rfc5988)
- [Best Practices for REST API Pagination](https://www.moesif.com/blog/technical/api-design/REST-API-Design-Filtering-Sorting-and-Pagination/)
