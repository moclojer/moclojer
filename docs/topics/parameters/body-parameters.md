---
description: >-
  Learn how to work with body parameters (JSON, form data) in moclojer.
  Access data from the request body in POST, PUT, and PATCH.
---

# Body Parameters

Body parameters are data sent in the body of HTTP requests, primarily in POST, PUT, and PATCH. Moclojer allows you to access this data via templates and use it in dynamic responses.

## What Are Body Parameters?

**Body** is where you send complex data in HTTP requests:

```bash
# Example of POST with JSON in body
curl -X POST http://localhost:8000/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "age": 30
  }'
```

**Difference from other parameters:**

- **Path params**: Data in the URL (`/users/:id`)
- **Query params**: Data after `?` (`?page=1`)
- **Body params**: Data in the request body (JSON, form data)

## Why Use Body Parameters?

✅ **Complex data**: Nested objects, arrays, multiple fields
✅ **Security**: Don't appear in URL (logs, history)
✅ **Size**: No URL limit (which is ~2KB)
✅ **Structured**: JSON allows hierarchies

**When to use:**

- Create resources (POST)
- Update resources (PUT, PATCH)
- Operations with lots of data
- Sensitive data (passwords, tokens)

**When NOT to use:**

- GET requests (GET should not have body)
- Simple DELETE (use path params)
- Filters/pagination (use query params)

---

## Accessing Body Parameters

### Syntax: `{{json-params.field}}`

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

**Test:**

```bash
curl -X POST http://localhost:8000/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice",
    "email": "alice@example.com",
    "age": 25
  }'
```

**Response:**

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

### Simple JSON

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
    "title": "Buy milk",
    "description": "Go to the market",
    "priority": "high"
  }'
```

### Nested Objects

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

**Response:**

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

### Arrays in Body

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

⚠️ **Note:** Templates don't iterate over arrays automatically. The array is returned as a string.

---

## Data Types

### Strings

```yaml
body: >
  {
    "name": "{{json-params.name}}"
  }
```

**Request:** `{"name": "Alice"}`
**Response:** `{"name": "Alice"}`

### Numbers (no quotes!)

```yaml
body: >
  {
    "age": {{json-params.age}},
    "price": {{json-params.price}}
  }
```

**Request:** `{"age": 25, "price": 99.99}`
**Response:** `{"age": 25, "price": 99.99}`

⚠️ **Important:** No quotes for numbers! With quotes it becomes string.

### Booleans

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

## Combining Parameters

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

## Practical Use Cases

### 1. Create User (POST)

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

### 2. Update Profile (PATCH)

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

### 3. Login (Authentication)

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

### 4. Create Order (E-commerce)

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

### 5. Upload Metadata (without binary file)

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

## Validation and Errors

### Required Fields (Simulation)

Moclojer **does not validate** automatically. Simulate with specific endpoints:

```yaml
# 1. Endpoint for request without required field (returns 400)
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

# 2. Endpoint for success (should come AFTER)
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

⚠️ **Limitation:** Moclojer doesn't validate if field exists. Use tools like Prism for real validation.

### Invalid Types

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

### Invalid Email

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

Moclojer supports form data (less common in modern APIs):

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

⚠️ **Note:** Use JSON when possible. Form-urlencoded has limitations (no nested objects).

---

## Best Practices

### ✅ Do

1. **Use JSON for modern APIs**

   ```bash
   curl -X POST /api/users \
     -H "Content-Type: application/json" \
     -d '{"name": "Alice"}'
   ```

2. **Validate Content-Type in client**

   ```bash
   # ✅ Always specify Content-Type
   -H "Content-Type: application/json"
   ```

3. **Echo received data**

   ```yaml
   # Client sees what was sent
   response:
     body: >
       {
         "received": {
           "name": "{{json-params.name}}",
           "email": "{{json-params.email}}"
         }
       }
   ```

4. **Use numbers without quotes**

   ```yaml
   # ✅ Correct (number)
   {"age": {{json-params.age}}}

   # ❌ Wrong (string)
   {"age": "{{json-params.age}}"}
   ```

5. **Error endpoints for validation**

   ```yaml
   # Simulate validation errors
   - endpoint:
       path: /api/users
       response:
         status: 400
         body: '{"error": "Validation failed"}'
   ```

### ❌ Avoid

1. **GET with body**

   ```bash
   # ❌ GET should not have body
   curl -X GET /api/users \
     -d '{"filter": "admin"}'

   # ✅ Use query params
   curl "http://localhost:8000/api/users?filter=admin"
   ```

2. **Sensitive data in GET**

   ```bash
   # ❌ NEVER in query params
   curl "http://localhost:8000/login?password=secret"

   # ✅ Always in body (POST)
   curl -X POST /login \
     -d '{"password": "secret"}'
   ```

3. **Very large body inline**

   ```yaml
   # ❌ Hard to read
   body: >
     {"data": [...1000 items...]}

   # ✅ Use external-body
   external-body:
     provider: json
     path: large-response.json
   ```

---

## Troubleshooting

### Problem: Template is not replaced

**Cause:** Incorrect field name

```yaml
# Request: {"userName": "Alice"}

# ❌ Wrong
{"name": "{{json-params.name}}"}  # undefined

# ✅ Correct
{"name": "{{json-params.userName}}"}
```

### Problem: Number comes as string

**Cause:** Quotes around template

```yaml
# ❌ Returns "25" (string)
{"age": "{{json-params.age}}"}

# ✅ Returns 25 (number)
{"age": {{json-params.age}}}
```

### Problem: Nested object doesn't work

**Cause:** Incorrect syntax

```yaml
# ✅ Correct
{"city": "{{json-params.address.city}}"}

# ❌ Wrong
{"city": "{{json-params[address][city]}}"}
{"city": "{{json-params.address[city]}}"}
```

### Problem: "Unexpected end of JSON"

**Cause:** Empty template breaks JSON

```yaml
# If json-params.name is empty:
{"name": {{json-params.name}}}
# Results in: {"name": }  ← INVALID!

# ✅ Solution: always with quotes for strings
{"name": "{{json-params.name}}"}
# Results in: {"name": ""}  ← VALID
```

---

## Next Steps

- **[Path Parameters](path-parameters.md)** - Parameters in the URL
- **[Query Parameters](query-parameters.md)** - Filters and pagination
- **[Header Parameters](header-parameters.md)** - HTTP headers
- **[HTTP Methods](../endpoints/http-methods.md)** - POST, PUT, PATCH

## See Also

- [CRUD Operations](../../how-to/patterns/crud-operations.md) - Complete examples
- [Template Variables](../templates/template-variables.md)  - Complete reference
- [Dynamic Responses](../../getting-started/dynamic-responses.md) - Practical tutorial
