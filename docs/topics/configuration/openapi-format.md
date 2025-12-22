---
description: >-
  Learn how to use OpenAPI (Swagger) specifications with moclojer. Import your
  OpenAPI 3.0/3.1 specs and generate mocks automatically.
---

# OpenAPI Format (Swagger)

Moclojer supports **OpenAPI 3.0 and 3.1** (formerly known as Swagger), allowing you to use your existing API specifications to generate mocks automatically. No need to rewrite anything in moclojer YAML!

## What Is OpenAPI?

**OpenAPI Specification** is a standard for describing RESTful APIs in a machine-readable format. It's widely used for:

- API documentation
- Automatic SDK generation
- Contract validation
- **Mock servers** (like moclojer!)

## Why Use OpenAPI with Moclojer?

✅ **Reusability**: Use your existing OpenAPI specs
✅ **Standardization**: OpenAPI is an industry standard
✅ **Zero configuration**: Moclojer converts automatically
✅ **Validation**: OpenAPI specs include validation schemas
✅ **Living documentation**: Your spec is documentation + mock

## OpenAPI Support in Moclojer

### Supported Versions

- ✅ OpenAPI 3.0.x
- ✅ OpenAPI 3.1.x
- ⚠️ Swagger 2.0 (partial support - recommend converting to 3.x)

### Accepted Formats

- ✅ JSON (`.json`)
- ✅ YAML (`.yml`, `.yaml`)

## Quick Start

### 1. Create or Get an OpenAPI Spec

Minimal example (`openapi.yml`):

```yaml
openapi: 3.0.0
info:
  title: Users API
  version: 1.0.0
paths:
  /users:
    get:
      summary: List all users
      responses:
        '200':
          description: Success
          content:
            application/json:
              schema:
                type: array
                items:
                  type: object
                  properties:
                    id:
                      type: integer
                    name:
                      type: string
              example:
                - id: 1
                  name: Alice
                - id: 2
                  name: Bob
```

### 2. Start Moclojer with OpenAPI

```bash
# Using local file
moclojer --config openapi.yml

# Or specify port
moclojer --config openapi.yml --port 3000

# Enable CORS
moclojer --config openapi.yml --enable-cors
```

### 3. Test

```bash
curl http://localhost:8000/users
```

**Response:**

```json
[
  {"id": 1, "name": "Alice"},
  {"id": 2, "name": "Bob"}
]
```

🎉 **Done!** Moclojer automatically converted your OpenAPI spec into functional endpoints.

---

## How Moclojer Converts OpenAPI

### Automatic Conversion

Moclojer automatically detects it's an OpenAPI spec and converts:

```yaml
# OpenAPI spec
paths:
  /users/{id}:          # → path: /users/:id
    get:                # → method: GET
      responses:
        '200':          # → status: 200
          content:
            application/json:
              example:  # → body
                id: 1
                name: John
```

**Results in (internal moclojer equivalent):**

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
          "id": 1,
          "name": "John"
        }
```

### Path Parameters

OpenAPI uses `{param}`, moclojer converts to `:param`:

```yaml
# OpenAPI
paths:
  /users/{userId}/posts/{postId}:
    get:
      parameters:
        - name: userId
          in: path
          schema:
            type: integer
        - name: postId
          in: path
          schema:
            type: integer
```

**Converted to:** `/users/:userId/posts/:postId`

**With types:**

- `type: integer` → `:userId|int`
- `type: string` → `:userId|string`

### Query Parameters

```yaml
# OpenAPI
paths:
  /products:
    get:
      parameters:
        - name: category
          in: query
          schema:
            type: string
        - name: limit
          in: query
          schema:
            type: integer
```

**Moclojer understands and accepts:**

```bash
curl "http://localhost:8000/products?category=electronics&limit=10"
```

### Request Body

```yaml
# OpenAPI
paths:
  /users:
    post:
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                name:
                  type: string
                email:
                  type: string
            example:
              name: John Doe
              email: john@example.com
```

**Moclojer uses the `example` for the response.**

---

## Complete OpenAPI Examples

### Example 1: Simple User API

```yaml
openapi: 3.0.0
info:
  title: Users API
  description: API for managing users
  version: 1.0.0

servers:
  - url: http://localhost:8000
    description: Local mock server

paths:
  /users:
    get:
      summary: List all users
      operationId: listUsers
      tags:
        - users
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            default: 1
        - name: limit
          in: query
          schema:
            type: integer
            default: 10
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                type: object
                properties:
                  data:
                    type: array
                    items:
                      $ref: '#/components/schemas/User'
                  meta:
                    type: object
                    properties:
                      total:
                        type: integer
                      page:
                        type: integer
              example:
                data:
                  - id: 1
                    name: Alice Johnson
                    email: alice@example.com
                  - id: 2
                    name: Bob Smith
                    email: bob@example.com
                meta:
                  total: 100
                  page: 1

    post:
      summary: Create a new user
      operationId: createUser
      tags:
        - users
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserInput'
            example:
              name: Carol Davis
              email: carol@example.com
      responses:
        '201':
          description: User created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
              example:
                id: 3
                name: Carol Davis
                email: carol@example.com
                createdAt: "2024-01-15T10:00:00Z"

  /users/{id}:
    get:
      summary: Get user by ID
      operationId: getUserById
      tags:
        - users
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: User found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
              example:
                id: 1
                name: Alice Johnson
                email: alice@example.com
                createdAt: "2024-01-01T00:00:00Z"
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
              example:
                error: "Not Found"
                message: "User with ID 999 not found"

    put:
      summary: Update user
      operationId: updateUser
      tags:
        - users
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserInput'
      responses:
        '200':
          description: User updated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'

    delete:
      summary: Delete user
      operationId: deleteUser
      tags:
        - users
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '204':
          description: User deleted

components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
          format: int64
        name:
          type: string
        email:
          type: string
          format: email
        createdAt:
          type: string
          format: date-time

    UserInput:
      type: object
      required:
        - name
        - email
      properties:
        name:
          type: string
          minLength: 1
        email:
          type: string
          format: email

    Error:
      type: object
      properties:
        error:
          type: string
        message:
          type: string
```

**Test:**

```bash
# List users
curl http://localhost:8000/users

# With pagination
curl "http://localhost:8000/users?page=1&limit=5"

# Get specific user
curl http://localhost:8000/users/1

# Create user
curl -X POST http://localhost:8000/users \
  -H "Content-Type: application/json" \
  -d '{"name": "New User", "email": "new@example.com"}'

# Update user
curl -X PUT http://localhost:8000/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Updated Name", "email": "updated@example.com"}'

# Delete user
curl -X DELETE http://localhost:8000/users/1
```

### Example 2: E-commerce API

```yaml
openapi: 3.0.0
info:
  title: E-commerce API
  version: 1.0.0

paths:
  /products:
    get:
      summary: List products
      parameters:
        - name: category
          in: query
          schema:
            type: string
        - name: minPrice
          in: query
          schema:
            type: number
        - name: maxPrice
          in: query
          schema:
            type: number
      responses:
        '200':
          description: Products list
          content:
            application/json:
              example:
                products:
                  - id: 1
                    name: "Laptop"
                    price: 999.99
                    category: "electronics"
                  - id: 2
                    name: "Mouse"
                    price: 29.99
                    category: "electronics"

  /products/{productId}:
    get:
      summary: Get product details
      parameters:
        - name: productId
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: Product details
          content:
            application/json:
              example:
                id: 1
                name: "Laptop"
                description: "High-performance laptop"
                price: 999.99
                category: "electronics"
                stock: 50

  /cart:
    post:
      summary: Add to cart
      requestBody:
        content:
          application/json:
            example:
              productId: 1
              quantity: 2
      responses:
        '200':
          description: Added to cart
          content:
            application/json:
              example:
                cartId: "cart-123"
                items:
                  - productId: 1
                    quantity: 2
                    subtotal: 1999.98
                total: 1999.98

  /orders:
    post:
      summary: Create order
      requestBody:
        content:
          application/json:
            example:
              cartId: "cart-123"
              shippingAddress:
                street: "123 Main St"
                city: "New York"
                zipCode: "10001"
      responses:
        '201':
          description: Order created
          content:
            application/json:
              example:
                orderId: "order-456"
                status: "pending"
                total: 1999.98
                createdAt: "2024-01-15T10:00:00Z"
```

---

## Multiple Responses by Status Code

OpenAPI allows defining multiple responses:

```yaml
paths:
  /users/{id}:
    get:
      responses:
        '200':
          description: Success
          content:
            application/json:
              example:
                id: 1
                name: "John"
        '404':
          description: Not found
          content:
            application/json:
              example:
                error: "User not found"
        '500':
          description: Server error
          content:
            application/json:
              example:
                error: "Internal server error"
```

**Moclojer uses the first `example` found (usually 200).**

To simulate errors, create specific endpoints:

```yaml
# In moclojer, add separate endpoints for errors
- endpoint:
    path: /users/999
    response:
      status: 404
      body: '{"error": "User not found"}'
```

---

## Headers and Content-Type

OpenAPI defines headers automatically:

```yaml
responses:
  '200':
    description: Success
    headers:
      X-RateLimit-Limit:
        schema:
          type: integer
        example: 100
      X-RateLimit-Remaining:
        schema:
          type: integer
        example: 99
    content:
      application/json:
        example:
          data: []
```

**Moclojer automatically adds:**

- `Content-Type` based on `content`
- Custom headers defined in `headers`

---

## Schemas and $ref

OpenAPI uses `$ref` to reuse schemas:

```yaml
components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
        name:
          type: string

paths:
  /users/{id}:
    get:
      responses:
        '200':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
              example:
                id: 1
                name: "Alice"
```

**Moclojer resolves `$ref` and uses the provided `example`.**

---

## Security Schemes (Authentication)

OpenAPI defines security schemes:

```yaml
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

security:
  - bearerAuth: []

paths:
  /protected:
    get:
      summary: Protected endpoint
      security:
        - bearerAuth: []
      responses:
        '200':
          description: Success
          content:
            application/json:
              example:
                message: "You are authenticated!"
        '401':
          description: Unauthorized
          content:
            application/json:
              example:
                error: "Unauthorized"
```

**Note:** Moclojer **does not validate authentication** automatically. To simulate:

```yaml
# Add endpoint for invalid token
- endpoint:
    path: /protected
    response:
      status: 401
      body: '{"error": "Unauthorized"}'

# And endpoint for success (should come after)
- endpoint:
    path: /protected
    response:
      status: 200
      body: '{"message": "You are authenticated!"}'
```

---

## Useful Tools

### OpenAPI Editors

1. **Swagger Editor** (online)
   - <https://editor.swagger.io/>
   - Validates spec in real-time

2. **VS Code Extension**
   - "OpenAPI (Swagger) Editor" by 42Crunch
   - Autocompletion and validation

3. **Stoplight Studio**
   - <https://stoplight.io/studio>
   - Visual editor

### Spec Validation

```bash
# Swagger CLI (Node.js)
npm install -g @apidevtools/swagger-cli
swagger-cli validate openapi.yml

# Spectral (advanced linting)
npm install -g @stoplight/spectral-cli
spectral lint openapi.yml
```

### Spec Generation

```bash
# From existing code
# Spring Boot (Java)
# FastAPI (Python) generates automatically
# Express (Node.js) with swagger-jsdoc
```

---

## Limitations and Workarounds

### 1. Request Validation

**Limitation:** Moclojer does not validate requests against the schema.

```yaml
# Schema defines 'name' as required
requestBody:
  schema:
    required: ['name']
```

**Moclojer accepts any request**, even without `name`.

**Workaround:** Use tools like Prism for real validation.

### 2. Multiple Responses by Status

**Limitation:** Moclojer uses only one `example` per endpoint.

**Workaround:** Create separate endpoints for each status code.

### 3. Callbacks and Links

**Limitation:** OpenAPI 3.x supports complex callbacks and links, moclojer ignores them.

### 4. oneOf, anyOf, allOf

**Limitation:** Complex schemas are not processed.

**Workaround:** Use explicit `example`.

---

## OpenAPI vs Native Moclojer YAML

| Aspect | OpenAPI | Moclojer YAML |
|---------|---------|---------------|
| **Standard** | Industry (portable) | Moclojer-specific |
| **Verbosity** | More verbose | More concise |
| **Tools** | Many (editors, validators) | Few |
| **Documentation** | Spec = documentation | Mock only |
| **Dynamic** | Static examples | Dynamic templates |
| **Validation** | Schema validation (with tools) | None |
| **Learning curve** | Higher (complex spec) | Lower (simple YAML) |

**When to use OpenAPI:**

- Already have existing OpenAPI specs
- Want to generate SDKs/documentation
- Need standardization across teams
- API goes beyond mocks (production)

**When to use native YAML:**

- Want dynamic responses with templates
- Need quick and simple mocks
- Don't need portability
- Want minimalist configuration

---

## Combining OpenAPI + Moclojer YAML

You can use **both** in the same project:

```bash
# File structure
project/
├── openapi.yml        # OpenAPI spec (main endpoints)
├── mocks-extras.yml   # Custom mocks with templates
└── mocks-errors.yml   # Error simulations
```

**Start with multiple files:**

```bash
# Moclojer doesn't support multiple configs directly
# Workaround: combine in one file or use proxy
```

**Alternative:** Convert OpenAPI to moclojer YAML:

```bash
# Conversion tools (create a script)
# openapi.yml → moclojer.yml
```

---

## Best Practices

### ✅ Do

1. **Use `examples` in all endpoints**

   ```yaml
   responses:
     '200':
       content:
         application/json:
           example:  # ← ALWAYS include!
             id: 1
             name: "John"
   ```

2. **Define path parameter types**

   ```yaml
   parameters:
     - name: id
       in: path
       schema:
         type: integer  # → Moclojer uses :id|int
   ```

3. **Organize with tags**

   ```yaml
   paths:
     /users:
       get:
         tags: [users]
     /products:
       get:
         tags: [products]
   ```

4. **Use `$ref` to reuse**

   ```yaml
   components:
     schemas:
       Error:
         type: object
   paths:
     /users:
       get:
         responses:
           '404':
             content:
               application/json:
                 schema:
                   $ref: '#/components/schemas/Error'
   ```

### ❌ Avoid

1. **Specs without `examples`**

   ```yaml
   # ❌ Moclojer doesn't know what to return
   responses:
     '200':
       content:
         application/json:
           schema:
             type: object
   ```

2. **Complex paths without types**

   ```yaml
   # ⚠️ Without type, accepts any string
   /users/{id}:  # Define type: integer!
   ```

---

## Troubleshooting

### "OpenAPI spec not detected"

**Cause:** Missing required `openapi` field.

**Solution:**

```yaml
# ✅ Add at the top
openapi: 3.0.0
info:
  title: My API
  version: 1.0.0
```

### "No examples found"

**Cause:** Spec without `example` or `examples`.

**Solution:** Add explicit examples:

```yaml
responses:
  '200':
    content:
      application/json:
        example:  # ← Add this!
          data: []
```

### "Path parameters not working"

**Cause:** Incorrect syntax.

**Solution:**

```yaml
# ✅ OpenAPI uses {param}
/users/{id}:

# ❌ Don't use :param in OpenAPI
/users/:id:
```

---

## Next Steps

- **[Postman Format](postman-format.md)** - Use Postman Collections
- **[YAML Format](yaml-format.md)** - Native moclojer syntax
- **[Path Parameters](../parameters/path-parameters.md)** - Dynamic parameters
- **[HTTP Methods](../endpoints/http-methods.md)** - GET, POST, PUT, DELETE

## See Also

- [OpenAPI 3.1 Specification](https://spec.openapis.org/oas/v3.1.0)
- [Swagger Editor](https://editor.swagger.io/)
- [OpenAPI Examples](https://github.com/OAI/OpenAPI-Specification/tree/main/examples)
- [Configuration Spec](../../reference/configuration-spec.md)
