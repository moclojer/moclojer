---
description: >-
  Learn how to work with HTTP headers in moclojer. Access and use request
  headers in dynamic responses (Authorization, User-Agent, etc).
---

# Header Parameters (HTTP Header Parameters)

HTTP headers carry metadata about the request and response. In moclojer, you can access headers sent by the client and use them in dynamic responses, as well as define custom headers in responses.

## What Are HTTP Headers?

**Headers** are key-value pairs sent in HTTP requests and responses:

```http
GET /api/users HTTP/1.1
Host: localhost:8000
Content-Type: application/json
Authorization: Bearer token123
User-Agent: curl/7.64.1
Accept: application/json
```

**Divided into:**

- **Request Headers**: Client → Server
- **Response Headers**: Server → Client

## Why Use Headers?

✅ **Authentication**: `Authorization: Bearer token`
✅ **Content Negotiation**: `Accept: application/json`
✅ **Caching**: `Cache-Control`, `ETag`
✅ **CORS**: `Access-Control-Allow-Origin`
✅ **Tracking**: `X-Request-ID`, `X-Correlation-ID`
✅ **Client Info**: `User-Agent`, `Referer`

---

## Accessing Request Headers

### Syntax: `{{header-params.HeaderName}}`

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

**Test:**

```bash
curl http://localhost:8000/api/protected \
  -H "Authorization: Bearer abc123" \
  -H "User-Agent: MyApp/1.0" \
  -H "X-Request-ID: req-456"
```

**Response:**

```json
{
  "authenticated": true,
  "token": "Bearer abc123",
  "userAgent": "MyApp/1.0",
  "requestId": "req-456"
}
```

---

## Common Headers

### 1. Authorization (Authentication)

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

**Response:**

```json
{
  "userId": 1,
  "name": "John Doe",
  "authenticatedWith": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "admin"
}
```

### 2. User-Agent (Client)

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

**Request XML (simulated):**

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

## Defining Response Headers

### Simple Headers

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

**Test:**

```bash
curl -I http://localhost:8000/api/data
```

**Response headers:**

```
HTTP/1.1 200 OK
Content-Type: application/json
X-Custom-Header: custom-value
X-Rate-Limit: 100
```

### Dynamic Headers

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

**Response headers include:**

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

**Or use global flag:**

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

## Practical Use Cases

### 1. Bearer Token Authentication

```yaml
# Protected endpoint - requires token
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

# Endpoint without token - returns 401
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

⚠️ **Note:** moclojer doesn't validate tokens. Both endpoints will respond. Use correct order or validation tools.

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

## Combining Parameters

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

HTTP headers are **case-insensitive**:

```yaml
# All work:
{{header-params.Authorization}}
{{header-params.authorization}}
{{header-params.AUTHORIZATION}}
```

**But convention:**

- Use `Title-Case` in headers: `Content-Type`, `Authorization`
- Use exactly as defined in template

---

## Standard Headers vs Custom

### Standard Headers (Avoid X- prefix)

RFC 6648 discourages `X-` in new headers:

```yaml
# ❌ Outdated
X-Request-ID: abc123

# ✅ Modern
Request-ID: abc123
```

**But `X-` is still very common in practice:**

- `X-Request-ID`
- `X-Correlation-ID`
- `X-API-Key`
- `X-Forwarded-For`

### Custom Headers (Use specific prefix)

```yaml
# ✅ Good (identifiable)
X-MyApp-Version: 1.0
X-MyApp-Client-ID: client-123

# ❌ Too generic
X-Version: 1.0
```

---

## Best Practices

### ✅ Do

1. **Use headers for metadata, not data**

   ```yaml
   # ✅ Metadata
   headers:
     Authorization: Bearer token

   # ❌ Data should go in body
   headers:
     X-User-Name: Alice
     X-User-Email: alice@example.com
   ```

2. **CORS headers when needed**

   ```yaml
   headers:
     Access-Control-Allow-Origin: "*"
     Access-Control-Allow-Methods: "GET, POST"
   ```

3. **Content-Type always explicit**

   ```yaml
   headers:
     Content-Type: application/json
   ```

4. **Request tracking headers**

   ```yaml
   headers:
     X-Request-ID: "{{header-params.X-Request-ID}}"
   ```

### ❌ Avoid

1. **Sensitive headers in logs**

   ```yaml
   # ⚠️ Tokens appear in logs
   "token": "{{header-params.Authorization}}"
   ```

2. **Giant headers**

   ```yaml
   # ❌ Headers have size limit (~8KB)
   X-Large-Data: "..." (100KB)
   ```

3. **Complex data in headers**

   ```yaml
   # ❌ Use body for this
   X-User-Data: '{"name":"Alice","email":"alice@example.com"}'
   ```

---

## Troubleshooting

### Problem: Header is not replaced

**Cause:** Incorrect name (case-sensitive in template)

```yaml
# Request: Authorization: Bearer token

# ❌ Doesn't work
{{header-params.authorization}}

# ✅ Works
{{header-params.Authorization}}
```

### Problem: Custom header doesn't appear

**Cause:** Not defined in response

```yaml
# ✅ Define explicitly
response:
  headers:
    X-Custom: "value"
```

### Problem: CORS error

**Solution:**

```bash
# Enable CORS globally
moclojer --enable-cors

# Or per endpoint
headers:
  Access-Control-Allow-Origin: "*"
```

---

## Important Headers

| Header | Usage | Example |
|--------|-----|---------|
| `Authorization` | Authentication | `Bearer token123` |
| `Content-Type` | Body type | `application/json` |
| `Accept` | Desired format | `application/json` |
| `User-Agent` | Client info | `curl/7.64.1` |
| `X-Request-ID` | Request tracking | `req-abc-123` |
| `X-API-Key` | API key auth | `sk_live_abc123` |
| `Cache-Control` | Caching policy | `max-age=3600` |
| `Location` | Redirect/Created | `/users/123` |

---

## Next Steps

- **[Path Parameters](path-parameters.md)** - URL parameters
- **[Query Parameters](query-parameters.md)** - Filters and pagination
- **[Body Parameters](body-parameters.md)** - Data in body
- **[HTTP Methods](../endpoints/http-methods.md)** - GET, POST, etc.

## See Also

- [Template Variables](../templates/template-variables.md) - Complete reference
- [Dynamic Responses](../../getting-started/dynamic-responses.md) - Practical tutorial
- [CRUD Operations](../../how-to/patterns/crud-operations.md) - Complete examples
