---
description: >-
  Learn YAML syntax for configuring your mocks in moclojer. Complete guide
  with practical examples and best practices.
---

# YAML Configuration Format

YAML is the most common configuration format in moclojer. It's simple to read and write, requires no programming knowledge, and is perfect for defining mock APIs declaratively.

## Why YAML?

**Advantages:**

- ✅ **Readable**: Looks like English, easy to understand
- ✅ **Simple**: Less verbose than JSON or XML
- ✅ **Structured**: Maintains clear hierarchy
- ✅ **Comments**: Can document inline

**When to use YAML:**

- You're starting with moclojer
- Need simple and clear configuration
- Want to collaborate with non-programmers
- Prefer readable configuration files

## Basic Structure

Every moclojer YAML file is a **list of endpoints**:

```yaml
- endpoint:
    method: GET
    path: /hello
    response:
      status: 200
      body: "Hello, World!"

- endpoint:
    method: POST
    path: /users
    response:
      status: 201
      body: '{"id": 1, "name": "Alice"}'
```

**Anatomy:**

- Each endpoint starts with `- endpoint:`
- Indentation with **2 spaces** (not tabs!)
- Required keys: `path`, `response`
- Optional but recommended key: `method`

## Essential YAML Syntax

### 1. Indentation

Indentation defines hierarchy:

```yaml
- endpoint:           # Level 0
    method: GET       # Level 1 (2 spaces)
    path: /users      # Level 1
    response:         # Level 1
      status: 200     # Level 2 (4 spaces)
      body: "data"    # Level 2
```

⚠️ **IMPORTANT:**

- Always use **2 spaces** per level
- Never mix spaces and tabs
- Tools: configure your editor for "soft tabs"

### 2. Strings

Three ways to write strings:

```yaml
# 1. Without quotes (for simple text)
path: /users

# 2. With double quotes (when containing special characters)
body: "Hello, \"World\"!"

# 3. Multi-line with > (removes line breaks)
body: >
  This very long text
  will be converted to a
  single line.

# 4. Multi-line with | (preserves line breaks)
body: |
  Line 1
  Line 2
  Line 3
```

**For JSON in body, use `>`:**

```yaml
body: >
  {
    "name": "Alice",
    "email": "alice@example.com"
  }
```

### 3. Numbers and Booleans

```yaml
# Numbers (without quotes)
status: 200
max-requests: 100

# Booleans
enabled: true
disabled: false
```

### 4. Lists

```yaml
# Inline list
tags: [moclojer, api, testing]

# Multi-line list (recommended)
tags:
  - moclojer
  - api
  - testing
```

### 5. Objects (Maps)

```yaml
# Inline
headers: {Content-Type: application/json, X-Custom: value}

# Multi-line (recommended)
headers:
  Content-Type: application/json
  X-Custom: value
```

### 6. Comments

```yaml
# This is a comment
- endpoint:  # Inline comment
    method: GET
    path: /users
    # TODO: add pagination
    response:
      status: 200
```

## Complete Annotated Example

```yaml
# User API - Complete Example
# Author: Dev Team
# Last updated: 2024-01-15

# Endpoint 1: List all users
- endpoint:
    method: GET                    # HTTP method
    path: /users                   # URL path
    response:                      # Response configuration
      status: 200                  # HTTP 200 OK
      headers:                     # Response headers
        Content-Type: application/json
        X-Total-Count: "3"
      body: >                      # Inline JSON (breaks removed)
        [
          {"id": 1, "name": "Alice"},
          {"id": 2, "name": "Bob"},
          {"id": 3, "name": "Carol"}
        ]

# Endpoint 2: Get user by ID
- endpoint:
    method: GET
    path: /users/:id               # :id is a dynamic parameter
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

# Endpoint 3: Create new user
- endpoint:
    method: POST
    path: /users
    response:
      status: 201                  # HTTP 201 Created
      headers:
        Content-Type: application/json
        Location: /users/4
      body: >
        {
          "id": 4,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "created_at": "2024-01-15T10:30:00Z"
        }

# Endpoint 4: Error - User not found
- endpoint:
    method: GET
    path: /users/999
    response:
      status: 404                  # HTTP 404 Not Found
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "User not found",
          "code": "USER_NOT_FOUND",
          "message": "User with ID 999 does not exist"
        }
```

## Best Practices

### ✅ Do

1. **Use consistent indentation (2 spaces)**

   ```yaml
   - endpoint:
       method: GET    # 2 spaces
       path: /users   # 2 spaces
   ```

2. **Add explanatory comments**

   ```yaml
   # Health check endpoint for monitoring
   - endpoint:
       method: GET
       path: /health
   ```

3. **Use `>` for inline JSON**

   ```yaml
   body: >
     {"key": "value"}
   ```

4. **Group related endpoints**

   ```yaml
   # === USER ENDPOINTS ===
   - endpoint: ...
   - endpoint: ...

   # === PRODUCT ENDPOINTS ===
   - endpoint: ...
   ```

5. **Order by method and path**

   ```yaml
   - GET /users
   - GET /users/:id
   - POST /users
   - PUT /users/:id
   - DELETE /users/:id
   ```

### ❌ Avoid

1. **Tabs for indentation**

   ```yaml
   - endpoint:
    method: GET    # ❌ Tab causes error
   ```

2. **Unnecessary quotes**

   ```yaml
   method: "GET"       # ❌ Unnecessary
   method: GET         # ✅ Better
   ```

3. **JSON without `>`**

   ```yaml
   body: {"key": "value"}   # ❌ Can break with complex strings
   body: >                  # ✅ Always works
     {"key": "value"}
   ```

4. **Endpoints without comments in large files**

   ```yaml
   # ✅ Good practice in large files
   # Authentication - User login
   - endpoint:
       method: POST
       path: /auth/login
   ```

## YAML Troubleshooting

### Problem: "YAML parse error: mapping values are not allowed"

**Cause:** Colon without space or quotes

```yaml
# ❌ Wrong
path: http://example.com    # colon in URL confuses parser

# ✅ Correct
path: "http://example.com"  # use quotes
```

### Problem: "YAML parse error: did not find expected key"

**Cause:** Incorrect indentation

```yaml
# ❌ Wrong
- endpoint:
  method: GET     # Should have 4 spaces (2 levels)

# ✅ Correct
- endpoint:
    method: GET   # 4 spaces
```

### Problem: Broken JSON in body

**Cause:** Didn't use `>` for multi-line

```yaml
# ❌ Wrong
body: {
  "key": "value"
}

# ✅ Correct
body: >
  {
    "key": "value"
  }
```

## YAML Validation

### Online

- [YAML Lint](http://www.yamllint.com/) - validates syntax
- [YAML to JSON](https://onlineyamltools.com/convert-yaml-to-json) - see how it will be parsed

### Editors

- **VS Code**: "YAML" extension by Red Hat
- **Sublime**: "YAML Nav" extension
- **Vim**: "vim-yaml" plugin

### Command line

```bash
# Validate syntax
yamllint moclojer.yml

# See how moclojer will parse
moclojer --validate moclojer.yml
```

## Comparison with Other Formats

### YAML vs JSON

```yaml
# YAML - More readable
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        {"users": []}
```

```json
// JSON - More verbose
[
  {
    "endpoint": {
      "method": "GET",
      "path": "/users",
      "response": {
        "status": 200,
        "body": "{\"users\": []}"
      }
    }
  }
]
```

**YAML wins in:**

- Readability (50% fewer characters)
- Native comments
- Multi-line strings

**JSON wins in:**

- Faster parsing
- Universal support

### YAML vs EDN

For most users, **YAML is simpler**. Use EDN only if you:

- Work with Clojure
- Need complex data structures
- Want programmatic integration

See [EDN Format Guide](edn-format.md) for details.

## Next Steps

Now that you've mastered YAML, explore:

1. **[Path Parameters](../parameters/path-parameters.md)** - Dynamic parameters in URLs
2. **[Template Variables](../templates/template-variables.md)** - Dynamic responses
3. **[Configuration Spec](../../reference/configuration-spec.md)** - Complete reference

## See Also

- [OpenAPI Format](openapi-format.md) - Import OpenAPI specs
- [Postman Format](postman-format.md) - Use Postman Collections
- [YAML Specification](https://yaml.org/spec/1.2/spec.html) - Official YAML 1.2 spec
