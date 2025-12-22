---
description: >-
  Aprenda a trabalhar com HTTP headers no moclojer. Acesse e use headers de
  requisição em respostas dinâmicas (Authorization, User-Agent, etc).
---

# Header Parameters (Parâmetros de Headers HTTP)

Headers HTTP carregam metadata sobre a requisição e resposta. No moclojer, você pode acessar headers enviados pelo cliente e usá-los em respostas dinâmicas, além de definir headers customizados nas respostas.

## O Que São HTTP Headers?

**Headers** são pares chave-valor enviados em requisições e respostas HTTP:

```http
GET /api/users HTTP/1.1
Host: localhost:8000
Content-Type: application/json
Authorization: Bearer token123
User-Agent: curl/7.64.1
Accept: application/json
```

**Divididos em:**

- **Request Headers**: Cliente → Servidor
- **Response Headers**: Servidor → Cliente

## Por Que Usar Headers?

✅ **Autenticação**: `Authorization: Bearer token`
✅ **Content Negotiation**: `Accept: application/json`
✅ **Caching**: `Cache-Control`, `ETag`
✅ **CORS**: `Access-Control-Allow-Origin`
✅ **Tracking**: `X-Request-ID`, `X-Correlation-ID`
✅ **Client Info**: `User-Agent`, `Referer`

---

## Acessando Request Headers

### Sintaxe: `{{header-params.NomeDoHeader}}`

```yaml
- endpoint:
    method: GET
    path: /api/protected
    response:
      status: 200
      body: >
        {
          "authenticated": true,
          "token": "{{header-params.Authorization}}",
          "userAgent": "{{header-params.User-Agent}}",
          "requestId": "{{header-params.X-Request-ID}}"
        }
```

**Testar:**

```bash
curl http://localhost:8000/api/protected \
  -H "Authorization: Bearer abc123" \
  -H "User-Agent: MyApp/1.0" \
  -H "X-Request-ID: req-456"
```

**Resposta:**

```json
{
  "authenticated": true,
  "token": "Bearer abc123",
  "userAgent": "MyApp/1.0",
  "requestId": "req-456"
}
```

---

## Headers Comuns

### 1. Authorization (Autenticação)

```yaml
- endpoint:
    method: GET
    path: /api/me
    response:
      status: 200
      body: >
        {
          "userId": 1,
          "name": "John Doe",
          "authenticatedWith": "{{header-params.Authorization}}",
          "role": "admin"
        }
```

**Request:**

```bash
curl http://localhost:8000/api/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Resposta:**

```json
{
  "userId": 1,
  "name": "John Doe",
  "authenticatedWith": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "admin"
}
```

### 2. User-Agent (Cliente)

```yaml
- endpoint:
    method: GET
    path: /api/analytics/track
    response:
      status: 200
      body: >
        {
          "tracked": true,
          "client": "{{header-params.User-Agent}}",
          "timestamp": "2024-01-15T10:00:00Z"
        }
```

**Request:**

```bash
curl http://localhost:8000/api/analytics/track \
  -H "User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)"
```

### 3. Content-Type

```yaml
- endpoint:
    method: POST
    path: /api/data
    response:
      status: 200
      body: >
        {
          "receivedContentType": "{{header-params.Content-Type}}",
          "processed": true
        }
```

**Request:**

```bash
curl -X POST http://localhost:8000/api/data \
  -H "Content-Type: application/json" \
  -d '{"data": "test"}'
```

### 4. Accept (Content Negotiation)

```yaml
- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      headers:
        Content-Type: "{{header-params.Accept}}"
      body: >
        {
          "users": [],
          "format": "{{header-params.Accept}}"
        }
```

**Request JSON:**

```bash
curl http://localhost:8000/api/users \
  -H "Accept: application/json"
```

**Request XML (simulado):**

```bash
curl http://localhost:8000/api/users \
  -H "Accept: application/xml"
```

### 5. Custom Headers (X-*)

```yaml
- endpoint:
    method: GET
    path: /api/service
    response:
      status: 200
      body: >
        {
          "requestId": "{{header-params.X-Request-ID}}",
          "correlationId": "{{header-params.X-Correlation-ID}}",
          "tenantId": "{{header-params.X-Tenant-ID}}",
          "apiVersion": "{{header-params.X-API-Version}}"
        }
```

**Request:**

```bash
curl http://localhost:8000/api/service \
  -H "X-Request-ID: req-123" \
  -H "X-Correlation-ID: corr-456" \
  -H "X-Tenant-ID: tenant-789" \
  -H "X-API-Version: v2"
```

---

## Definindo Response Headers

### Headers Simples

```yaml
- endpoint:
    method: GET
    path: /api/data
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Custom-Header: custom-value
        X-Rate-Limit: "100"
      body: >
        {"data": []}
```

**Teste:**

```bash
curl -I http://localhost:8000/api/data
```

**Headers da resposta:**

```
HTTP/1.1 200 OK
Content-Type: application/json
X-Custom-Header: custom-value
X-Rate-Limit: 100
```

### Headers Dinâmicos

```yaml
- endpoint:
    method: GET
    path: /api/echo
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Echo-User-Agent: "{{header-params.User-Agent}}"
        X-Echo-Auth: "{{header-params.Authorization}}"
      body: >
        {
          "message": "Headers echoed"
        }
```

**Request:**

```bash
curl http://localhost:8000/api/echo \
  -H "User-Agent: MyClient/1.0" \
  -H "Authorization: Bearer token123"
```

**Response headers incluem:**

```
X-Echo-User-Agent: MyClient/1.0
X-Echo-Auth: Bearer token123
```

### CORS Headers

```yaml
- endpoint:
    method: GET
    path: /api/public
    response:
      status: 200
      headers:
        Content-Type: application/json
        Access-Control-Allow-Origin: "*"
        Access-Control-Allow-Methods: "GET, POST, PUT, DELETE, OPTIONS"
        Access-Control-Allow-Headers: "Content-Type, Authorization"
      body: >
        {"message": "CORS enabled"}
```

**Ou use flag global:**

```bash
moclojer --config mocks.yml --enable-cors
```

### Cache Headers

```yaml
- endpoint:
    method: GET
    path: /api/static-data
    response:
      status: 200
      headers:
        Content-Type: application/json
        Cache-Control: "public, max-age=3600"
        ETag: '"abc123"'
        Last-Modified: "Mon, 15 Jan 2024 10:00:00 GMT"
      body: >
        {"data": "cacheable"}
```

---

## Casos de Uso Práticos

### 1. Autenticação Bearer Token

```yaml
# Endpoint protegido - requer token
- endpoint:
    method: GET
    path: /api/protected
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "message": "Access granted",
          "user": {
            "id": 1,
            "tokenHash": "{{header-params.Authorization}}"
          }
        }

# Endpoint sem token - retorna 401
- endpoint:
    method: GET
    path: /api/protected
    response:
      status: 401
      headers:
        Content-Type: application/json
        WWW-Authenticate: 'Bearer realm="API"'
      body: >
        {
          "error": "Unauthorized",
          "message": "Bearer token is required"
        }
```

⚠️ **Nota:** moclojer não valida tokens. Ambos endpoints respondem. Use ordem correta ou ferramentas de validação.

### 2. API Versioning via Header

```yaml
- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-API-Version: "{{header-params.X-API-Version}}"
      body: >
        {
          "version": "{{header-params.X-API-Version}}",
          "users": [
            {"id": 1, "name": "Alice"}
          ]
        }
```

**Request v1:**

```bash
curl http://localhost:8000/api/users \
  -H "X-API-Version: v1"
```

**Request v2:**

```bash
curl http://localhost:8000/api/users \
  -H "X-API-Version: v2"
```

### 3. Request Tracking

```yaml
- endpoint:
    method: POST
    path: /api/events
    response:
      status: 202
      headers:
        Content-Type: application/json
        X-Request-ID: "{{header-params.X-Request-ID}}"
        X-Correlation-ID: "{{header-params.X-Correlation-ID}}"
      body: >
        {
          "eventId": "evt-123",
          "requestId": "{{header-params.X-Request-ID}}",
          "correlationId": "{{header-params.X-Correlation-ID}}",
          "status": "accepted"
        }
```

**Request:**

```bash
curl -X POST http://localhost:8000/api/events \
  -H "X-Request-ID: req-abc-123" \
  -H "X-Correlation-ID: corr-xyz-456" \
  -d '{"event": "user.signup"}'
```

### 4. Multi-Tenant API

```yaml
- endpoint:
    method: GET
    path: /api/data
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Tenant-ID: "{{header-params.X-Tenant-ID}}"
      body: >
        {
          "tenantId": "{{header-params.X-Tenant-ID}}",
          "data": [
            {"id": 1, "value": "Data for tenant {{header-params.X-Tenant-ID}}"}
          ]
        }
```

**Request:**

```bash
curl http://localhost:8000/api/data \
  -H "X-Tenant-ID: tenant-acme-corp"
```

### 5. Rate Limiting Headers

```yaml
- endpoint:
    method: GET
    path: /api/search
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-RateLimit-Limit: "100"
        X-RateLimit-Remaining: "95"
        X-RateLimit-Reset: "1610712000"
      body: >
        {
          "results": []
        }
```

### 6. Content Negotiation

```yaml
# JSON Response
- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      headers:
        Content-Type: "{{header-params.Accept}}"
      body: >
        {
          "format": "{{header-params.Accept}}",
          "users": [{"id": 1, "name": "Alice"}]
        }
```

**Request:**

```bash
curl http://localhost:8000/api/users \
  -H "Accept: application/json"

curl http://localhost:8000/api/users \
  -H "Accept: application/xml"
```

---

## Combinando Parâmetros

### Headers + Path + Query + Body

```yaml
- endpoint:
    method: POST
    path: /api/tenants/:tenantId/users
    response:
      status: 201
      headers:
        Content-Type: application/json
        X-Tenant-ID: "{{header-params.X-Tenant-ID}}"
        X-Request-ID: "{{header-params.X-Request-ID}}"
        Location: "/api/tenants/{{path-params.tenantId}}/users/1"
      body: >
        {
          "id": 1,
          "tenantId": "{{path-params.tenantId}}",
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "role": "{{query-params.role}}",
          "requestId": "{{header-params.X-Request-ID}}",
          "createdAt": "2024-01-15T10:00:00Z"
        }
```

**Request:**

```bash
curl -X POST "http://localhost:8000/api/tenants/acme/users?role=admin" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: acme" \
  -H "X-Request-ID: req-123" \
  -d '{
    "name": "Alice",
    "email": "alice@acme.com"
  }'
```

---

## Headers Case-Insensitive

HTTP headers são **case-insensitive**:

```yaml
# Todos funcionam:
{{header-params.Authorization}}
{{header-params.authorization}}
{{header-params.AUTHORIZATION}}
```

**Mas convenção:**

- Use `Title-Case` nos headers: `Content-Type`, `Authorization`
- Use exatamente como definido no template

---

## Headers Standard vs Custom

### Standard Headers (Evite prefixo X-)

RFC 6648 desencoraja `X-` em novos headers:

```yaml
# ❌ Desatualizado
X-Request-ID: abc123

# ✅ Moderno
Request-ID: abc123
```

**Mas `X-` ainda é muito comum na prática:**

- `X-Request-ID`
- `X-Correlation-ID`
- `X-API-Key`
- `X-Forwarded-For`

### Custom Headers (Use prefixo específico)

```yaml
# ✅ Bom (identificável)
X-MyApp-Version: 1.0
X-MyApp-Client-ID: client-123

# ❌ Genérico demais
X-Version: 1.0
```

---

## Boas Práticas

### ✅ Faça

1. **Use headers para metadata, não dados**

   ```yaml
   # ✅ Metadata
   headers:
     Authorization: Bearer token

   # ❌ Dados devem ir no body
   headers:
     X-User-Name: Alice
     X-User-Email: alice@example.com
   ```

2. **CORS headers quando necessário**

   ```yaml
   headers:
     Access-Control-Allow-Origin: "*"
     Access-Control-Allow-Methods: "GET, POST"
   ```

3. **Content-Type sempre explícito**

   ```yaml
   headers:
     Content-Type: application/json
   ```

4. **Request tracking headers**

   ```yaml
   headers:
     X-Request-ID: "{{header-params.X-Request-ID}}"
   ```

### ❌ Evite

1. **Headers sensíveis em logs**

   ```yaml
   # ⚠️ Tokens aparecem em logs
   "token": "{{header-params.Authorization}}"
   ```

2. **Headers gigantes**

   ```yaml
   # ❌ Headers têm limite de tamanho (~8KB)
   X-Large-Data: "..." (100KB)
   ```

3. **Dados complexos em headers**

   ```yaml
   # ❌ Use body para isso
   X-User-Data: '{"name":"Alice","email":"alice@example.com"}'
   ```

---

## Troubleshooting

### Problema: Header não é substituído

**Causa:** Nome incorreto (case-sensitive no template)

```yaml
# Request: Authorization: Bearer token

# ❌ Não funciona
{{header-params.authorization}}

# ✅ Funciona
{{header-params.Authorization}}
```

### Problema: Header customizado não aparece

**Causa:** Não definiu no response

```yaml
# ✅ Defina explicitamente
response:
  headers:
    X-Custom: "value"
```

### Problema: CORS error

**Solução:**

```bash
# Habilitar CORS globalmente
moclojer --enable-cors

# Ou por endpoint
headers:
  Access-Control-Allow-Origin: "*"
```

---

## Headers Importantes

| Header | Uso | Exemplo |
|--------|-----|---------|
| `Authorization` | Autenticação | `Bearer token123` |
| `Content-Type` | Tipo do body | `application/json` |
| `Accept` | Formato desejado | `application/json` |
| `User-Agent` | Cliente info | `curl/7.64.1` |
| `X-Request-ID` | Request tracking | `req-abc-123` |
| `X-API-Key` | API key auth | `sk_live_abc123` |
| `Cache-Control` | Caching policy | `max-age=3600` |
| `Location` | Redirect/Created | `/users/123` |

---

## Próximos Passos

- **[Path Parameters](path-parameters.md)** - Parâmetros de URL
- **[Query Parameters](query-parameters.md)** - Filtros e paginação
- **[Body Parameters](body-parameters.md)** - Dados no corpo
- **[HTTP Methods](../endpoints/http-methods.md)** - GET, POST, etc.

## Veja Também

- [Template Variables](../templates/template-variables.md) - Referência completa
- [Dynamic Responses](../../getting-started/dynamic-responses.md) - Tutorial prático
- [CRUD Operations](../../how-to/patterns/crud-operations.md) - Exemplos completos
