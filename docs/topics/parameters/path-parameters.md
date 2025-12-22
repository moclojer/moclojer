---
description: >-
  Learn how to use path parameters (URL) in moclojer to create dynamic
  endpoints that respond to different ID, slug, and other data values.
---

# Path Parameters (URL Parameters)

Path parameters allow you to create dynamic endpoints that respond to different values in the URL. For example, a single endpoint `/users/:id` can respond to both `/users/1` and `/users/999`.

## Why Use Path Parameters?

**Before (without path params):**

```yaml
- endpoint:
    path: /users/1
    response:
      body: '{"id": 1, "name": "Alice"}'

- endpoint:
    path: /users/2
    response:
      body: '{"id": 2, "name": "Bob"}'

# ... you would need 1000 endpoints for 1000 users! 😱
```

**After (with path params):**

```yaml
- endpoint:
    path: /users/:id
    response:
      body: >
        {
          "id": "{{path-params.id}}",
          "name": "User {{path-params.id}}"
        }

# A single endpoint responds to ANY ID! 🎉
```

## Basic Syntax

### Declaring a Path Parameter

Use a colon (`:`) before the parameter name:

```yaml
path: /users/:id
```

**Format:** `/path/:parameterName`

### Accessing the Value

Use templates `{{path-params.parameterName}}`:

```yaml
- endpoint:
    method: GET
    path: /users/:id
    response:
      status: 200
      body: >
        {
          "id": "{{path-params.id}}",
          "message": "You requested user {{path-params.id}}"
        }
```

**Test:**

```bash
curl http://localhost:8000/users/123
# Response: {"id": "123", "message": "You requested user 123"}

curl http://localhost:8000/users/alice
# Response: {"id": "alice", "message": "You requested user alice"}
```

## Path Parameter Types

Moclojer supports **type validation** using the `:param|type` syntax:

### String (default)

```yaml
path: /users/:username        # Accepts any string
path: /users/:username|string # Explicit (same behavior)
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

**Doesn't match:**

- `/users/abc` ❌
- `/users/1.5` ❌
- `/users/` ❌

### UUID

```yaml
path: /sessions/:sessionId|uuid
```

**Matches:**

- `/sessions/550e8400-e29b-41d4-a716-446655440000` ✅

**Doesn't match:**

- `/sessions/abc123` ❌
- `/sessions/123` ❌

### Boolean

```yaml
path: /features/:enabled|boolean
```

**Matches:**

- `/features/true` ✅
- `/features/false` ✅

**Doesn't match:**

- `/features/yes` ❌
- `/features/1` ❌

## Multiple Path Parameters

You can have multiple parameters in the same path:

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

**Test:**

```bash
curl http://localhost:8000/users/42/posts/7
```

**Response:**

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

## Practical Examples

### Example 1: Products API

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

**Usage:**

```bash
curl http://localhost:8000/products/101
# {"id": 101, "name": "Product 101", "sku": "PRD-101", ...}
```

### Example 2: Blog with Slugs

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
          "content": "This is the content of post {{path-params.slug}}",
          "publishedAt": "2024-01-15T10:00:00Z"
        }
```

**Usage:**

```bash
curl http://localhost:8000/blog/introduction-to-moclojer
curl http://localhost:8000/blog/path-parameters-guide
```

### Example 3: Complete RESTful API

```yaml
# GET /users/:id - Get user
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

# PUT /users/:id - Update user
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

# DELETE /users/:id - Delete user
- endpoint:
    method: DELETE
    path: /users/:id|int
    response:
      status: 204   # No Content
```

### Example 4: Nested Resources

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

**Usage:**

```bash
curl http://localhost:8000/organizations/1/teams/5/members/42
```

## Combining with Other Parameters

Path parameters work together with query params and body params:

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

**Usage:**

```bash
curl "http://localhost:8000/users/42/posts?limit=10&offset=0"
```

**Response:**

```json
{
  "userId": 42,
  "limit": "10",
  "offset": "0",
  "posts": [...]
}
```

## Route Precedence

When multiple endpoints can match, moclojer uses the first occurrence:

```yaml
# ⚠️ Order matters!

# 1. Specific route (should come FIRST)
- endpoint:
    path: /users/me
    response:
      body: '{"currentUser": true}'

# 2. Generic route (should come AFTER)
- endpoint:
    path: /users/:id
    response:
      body: '{"id": "{{path-params.id}}"}'
```

**How it works:**

- `GET /users/me` → matches endpoint #1 ✅
- `GET /users/123` → matches endpoint #2 ✅

**If you reverse the order:**

```yaml
# ❌ PROBLEM: generic route comes first!
- endpoint:
    path: /users/:id
    response:
      body: '{"id": "{{path-params.id}}"}'

- endpoint:
    path: /users/me
    response:
      body: '{"currentUser": true}'  # Will NEVER be used!
```

**Result:**

- `GET /users/me` → matches endpoint #1 (`:id` = "me") ❌ Wrong!

**Golden rule:** **Specific routes before dynamic routes!**

## Validation and Errors

### Incorrect Type

If you define a type and the value doesn't match:

```yaml
- endpoint:
    path: /users/:id|int
```

**Requests:**

- `/users/123` → ✅ Match
- `/users/abc` → ❌ No match (moclojer returns 404)

### Creating Specific Error Endpoints

```yaml
# Specific endpoint for ID not found
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

# Generic endpoint (should come after)
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

## Best Practices

### ✅ Do

1. **Use explicit types when possible**

   ```yaml
   path: /users/:id|int      # ✅ Validates it's a number
   ```

2. **Descriptive parameter names**

   ```yaml
   path: /posts/:postId      # ✅ Clear
   path: /posts/:id          # ⚠️ Less clear in nested resources
   ```

3. **Specific routes before dynamic ones**

   ```yaml
   - path: /users/me         # ✅ First
   - path: /users/:id        # ✅ After
   ```

4. **Use parameter value in response**

   ```yaml
   body: >
     {"id": "{{path-params.id}}"}  # ✅ Response reflects the input
   ```

### ❌ Avoid

1. **Parameters without type when should have one**

   ```yaml
   path: /users/:id          # ⚠️ Accepts "abc" as ID
   path: /users/:id|int      # ✅ Only accepts numbers
   ```

2. **Names too generic**

   ```yaml
   path: /api/:param1/:param2  # ❌ What are these?
   path: /api/:userId/:postId  # ✅ Self-documenting
   ```

3. **Too many nesting levels**

   ```yaml
   path: /a/:b/c/:d/e/:f/g/:h  # ❌ Hard to read
   path: /users/:id/posts      # ✅ Maximum 2-3 levels
   ```

## Troubleshooting

### Problem: "404 Not Found" when it should work

**Possible causes:**

1. **Incorrect parameter type**

   ```yaml
   path: /users/:id|int
   # Trying: /users/abc → 404 (correct, not an int)
   ```

2. **Wrong route order**

   ```yaml
   # If /users/:id is before /users/me
   # /users/me will match with :id="me"
   ```

3. **Different HTTP method**

   ```yaml
   method: GET
   path: /users/:id
   # POST /users/123 → 404 (wrong method)
   ```

### Problem: Template `{{path-params.id}}` is not replaced

**Cause:** Parameter name doesn't match

```yaml
# ❌ Wrong
path: /users/:userId
body: '{"id": "{{path-params.id}}"}'  # Should be userId!

# ✅ Correct
path: /users/:userId
body: '{"id": "{{path-params.userId}}"}'
```

### Problem: Parameter comes as string when wanted number

**Cause:** Template strings always return strings

```yaml
# ❌ Returns string "123"
body: >
  {
    "id": "{{path-params.id}}"
  }

# ✅ Returns number 123
body: >
  {
    "id": {{path-params.id}}
  }
```

**Note:** Without quotes = number, with quotes = string.

## Next Steps

Now that you've mastered path parameters:

1. **[Query Parameters](query-parameters.md)** - Parameters in the URL after `?`
2. **[Body Parameters](body-parameters.md)** - Data in the request body
3. **[Template Variables](../templates/template-variables.md)** - Complete template reference

## See Also

- [HTTP Methods](../endpoints/http-methods.md) - GET, POST, PUT, DELETE, etc.
- [Path Patterns](../endpoints/path-patterns.md) - Advanced route patterns
- [Dynamic Responses Tutorial](../../getting-started/dynamic-responses.md) - Practical tutorial
