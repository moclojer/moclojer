---
description: >-
  Complete guide about HTTP methods (GET, POST, PUT, DELETE, etc.) in moclojer.
  Learn when to use each method and how to implement correct RESTful APIs.
---

# HTTP Methods

HTTP methods (also called HTTP verbs) define the action you want to perform on a resource. Choosing the correct method is essential for creating semantic and consistent RESTful APIs.

## Supported Methods

Moclojer supports all standard HTTP methods:

| Method | Purpose | Idempotent¹ | Safe² |
|--------|-----------|--------------|-------|
| **GET** | Read data | ✅ | ✅ |
| **POST** | Create data | ❌ | ❌ |
| **PUT** | Update/Replace | ✅ | ❌ |
| **PATCH** | Update partially | ❌ | ❌ |
| **DELETE** | Remove data | ✅ | ❌ |
| **HEAD** | Get headers (no body) | ✅ | ✅ |
| **OPTIONS** | Discover allowed methods | ✅ | ✅ |

¹ **Idempotent**: Multiple identical calls have the same effect as a single call
² **Safe**: Doesn't modify data (read-only)

## Syntax in Moclojer

```yaml
- endpoint:
    method: GET      # Specifies the HTTP method
    path: /users
    response:
      status: 200
      body: "..."
```

**Default:** If you omit `method`, moclojer uses `GET`.

```yaml
# These are equivalent:
- endpoint:
    path: /users
    # method: GET is implicit

- endpoint:
    method: GET
    path: /users
```

## GET - Read Data

**Purpose:** Retrieve data without modifying it.

**Characteristics:**

- **Safe**: Doesn't change data on server
- **Idempotent**: Multiple calls return the same result
- **Cacheable**: Responses can be cached
- **No body**: Should not have body in request

### Example: List Resources

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

**Usage:**

```bash
curl http://localhost:8000/users
```

### Example: Get Specific Resource

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

**Usage:**

```bash
curl http://localhost:8000/users/123
```

### GET with Query Parameters

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

**Usage:**

```bash
curl "http://localhost:8000/products?category=electronics&min_price=100"
```

### When to Use GET

✅ **Use GET for:**

- List resources (`GET /users`)
- Get details (`GET /users/123`)
- Search/filter (`GET /search?q=term`)
- Export data (`GET /reports/sales`)

❌ **Don't use GET for:**

- Create resources (use POST)
- Update resources (use PUT/PATCH)
- Delete resources (use DELETE)
- Operations with side effects

---

## POST - Create Data

**Purpose:** Create new resources or process data.

**Characteristics:**

- **Not idempotent**: Multiple calls create multiple resources
- **Not safe**: Modifies server state
- **With body**: Usually sends data in body
- **Returns 201**: "Created" status when successful

### Example: Create Resource

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

**Usage:**

```bash
curl -X POST http://localhost:8000/users \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com"}'
```

**Response:**

```json
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### POST for Custom Actions

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

**Usage:**

```bash
curl -X POST http://localhost:8000/users/123/activate
```

### When to Use POST

✅ **Use POST for:**

- Create new resources (`POST /users`)
- File uploads (`POST /upload`)
- Custom actions (`POST /users/123/activate`)
- Complex processing (`POST /calculate`)
- When operation is not idempotent

❌ **Don't use POST for:**

- Reading data (use GET)
- Complete update (use PUT)
- Removal (use DELETE)

---

## PUT - Update/Replace

**Purpose:** Completely replace an existing resource.

**Characteristics:**

- **Idempotent**: Multiple calls have the same effect
- **Complete replacement**: All fields must be sent
- **Requires ID**: Usually used with `/resource/:id`
- **Returns 200**: "OK" status when updated

### Example: Update Complete Resource

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

**Usage:**

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

**PUT** is idempotent:

```bash
# Calling 5 times results in the same state
PUT /users/123 {"name": "John"}
PUT /users/123 {"name": "John"}
PUT /users/123 {"name": "John"}
# Result: user 123 with name="John"
```

**POST** is not idempotent:

```bash
# Calling 5 times creates 5 resources
POST /users {"name": "John"}  # Creates user 1
POST /users {"name": "John"}  # Creates user 2
POST /users {"name": "John"}  # Creates user 3
# Result: 3 different users
```

### When to Use PUT

✅ **Use PUT for:**

- Update complete resource (`PUT /users/123`)
- Replace settings (`PUT /settings`)
- Idempotent update operations

❌ **Don't use PUT for:**

- Create resources (use POST)
- Partial update (use PATCH)
- Collections (`PUT /users` doesn't make sense)

---

## PATCH - Update Partially

**Purpose:** Update only some fields of a resource.

**Characteristics:**

- **Partial**: Send only fields that changed
- **More efficient**: Less data transferred
- **Not necessarily idempotent**: Depends on implementation

### Example: Partial Update

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

**Usage:**

```bash
# Update only email (name stays the same)
curl -X PATCH http://localhost:8000/users/123 \
  -H "Content-Type: application/json" \
  -d '{"email": "newemail@example.com"}'
```

### PATCH vs PUT

| Aspect | PUT | PATCH |
|---------|-----|-------|
| **Scope** | Replace complete resource | Update specific fields |
| **Fields** | All fields required | Only fields that change |
| **Idempotent** | Yes | Depends |
| **Example** | `PUT /users/1` (all data) | `PATCH /users/1` (only email) |

### When to Use PATCH

✅ **Use PATCH for:**

- Update few fields (`PATCH /users/123`)
- Toggle flags (`PATCH /posts/1 {"published": true}`)
- Partial edit operations

❌ **Don't use PATCH for:**

- Replace complete resource (use PUT)
- Create resources (use POST)

---

## DELETE - Remove Data

**Purpose:** Remove a resource.

**Characteristics:**

- **Idempotent**: Deleting multiple times = deleting once
- **No body in response**: Usually returns 204 No Content
- **Irreversible**: (in real APIs, consider soft delete)

### Example: Delete Resource

```yaml
- endpoint:
    method: DELETE
    path: /users/:id
    response:
      status: 204    # No Content (no body)
```

**Usage:**

```bash
curl -X DELETE http://localhost:8000/users/123
```

### DELETE with Confirmation

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

### DELETE for Resource Not Found

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

### When to Use DELETE

✅ **Use DELETE for:**

- Remove resources (`DELETE /users/123`)
- Clear data (`DELETE /cache`)
- Logout (`DELETE /sessions/current`)

❌ **Don't use DELETE for:**

- Reading (use GET)
- Updating (use PUT/PATCH)
- Actions that don't remove data

---

## HEAD - Get Metadata

**Purpose:** Get response headers without the body.

**Characteristics:**

- Identical to GET, but **no body in response**
- Useful for checking if resource exists
- Check file size before downloading

### Example

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
      # body is ignored in HEAD
```

**Usage:**

```bash
curl -I http://localhost:8000/users/123
# Returns only headers, no body
```

### When to Use HEAD

✅ **Use HEAD for:**

- Check if resource exists
- Check `Last-Modified` or `ETag`
- See file size (`Content-Length`)

---

## OPTIONS - Discover Allowed Methods

**Purpose:** Discover which HTTP methods are supported.

**Characteristics:**

- Used in **CORS preflight requests**
- Returns allowed methods in `Allow` header

### Example

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

**Usage:**

```bash
curl -X OPTIONS http://localhost:8000/users
```

### CORS Preflight

Browsers automatically make OPTIONS before "complex" requests:

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

## Multiple Methods, Same Path

You can have the same path with different methods:

```yaml
# GET /users - List
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: '[{"id": 1}, {"id": 2}]'

# POST /users - Create
- endpoint:
    method: POST
    path: /users
    response:
      status: 201
      body: '{"id": 3, "name": "{{json-params.name}}"}'

# DELETE /users (clear all - rare!)
- endpoint:
    method: DELETE
    path: /users
    response:
      status: 204
```

**Result:**

- `GET /users` → lists users
- `POST /users` → creates user
- `DELETE /users` → removes all (careful!)

---

## Complete RESTful API

Example of complete CRUD:

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

# READ (list) - GET /users
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

# UPDATE (complete) - PUT /users/:id
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

# UPDATE (partial) - PATCH /users/:id
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

## Status Codes by Method

| Method | Success | Common Error |
|--------|---------|-------------|
| GET | 200 OK | 404 Not Found |
| POST | 201 Created | 400 Bad Request, 422 Unprocessable |
| PUT | 200 OK | 404 Not Found, 400 Bad Request |
| PATCH | 200 OK | 404 Not Found, 400 Bad Request |
| DELETE | 204 No Content | 404 Not Found |
| HEAD | 200 OK | 404 Not Found |
| OPTIONS | 200 OK | - |

---

## Best Practices

### ✅ Do

1. **Use the correct semantic method**

   ```yaml
   # ✅ Correct
   GET /users        # Read
   POST /users       # Create
   PUT /users/:id    # Update complete
   PATCH /users/:id  # Update partial
   DELETE /users/:id # Remove
   ```

2. **GET and HEAD should not modify data**

   ```yaml
   # ❌ Wrong - GET should not delete!
   GET /users/:id/delete

   # ✅ Correct
   DELETE /users/:id
   ```

3. **Use appropriate status codes**

   ```yaml
   POST: 201 Created
   PUT: 200 OK
   DELETE: 204 No Content
   ```

4. **Implement OPTIONS for CORS**

   ```yaml
   - endpoint:
       method: OPTIONS
       path: /:path
       response:
         status: 204
         headers:
           Access-Control-Allow-Methods: "*"
   ```

### ❌ Avoid

1. **Methods in URL**

   ```yaml
   # ❌ Don't do this
   GET /users/create
   GET /users/:id/update
   GET /users/:id/delete

   # ✅ Use HTTP methods
   POST /users
   PUT /users/:id
   DELETE /users/:id
   ```

2. **POST for everything**

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

## Next Steps

- **[Path Patterns](path-patterns.md)** - Route patterns
- **[Response Structure](response-structure.md)** - Response structure
- **[CRUD Operations How-to](../../how-to/patterns/crud-operations.md)** - Complete CRUD

## See Also

- [HTTP Status Codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)
- [REST API Best Practices](https://restfulapi.net/)
- [Your First Mock](../../getting-started/your-first-mock.md)
