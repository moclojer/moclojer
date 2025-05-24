---
description: >-
  Complete reference guide for all template variables available in moclojer.
  Learn how to access request data and use built-in functions in your responses.
---

# Template Variables Reference

This reference guide provides a complete list of all template variables available in moclojer. Template variables use the `{{variable}}` syntax and are replaced with actual values when processing requests.

## Path Parameters

Extract values from URL path segments defined with `:parameter` syntax.

### Syntax
```yaml
path: /users/:id/posts/:postId
body: >
  {
    "user_id": "{{path-params.id}}",
    "post_id": "{{path-params.postId}}"
  }
```

### Examples

**Single parameter:**
```yaml
path: /users/:id
# Request: GET /users/123
# Result: {{path-params.id}} = "123"
```

**Multiple parameters:**
```yaml
path: /api/:version/users/:userId/posts/:postId
# Request: GET /api/v1/users/456/posts/789
# Results:
# {{path-params.version}} = "v1"
# {{path-params.userId}} = "456"
# {{path-params.postId}} = "789"
```

**Typed parameters:**
```yaml
path: /users/:id|int/profile/:section|string
# Request: GET /users/123/profile/settings
# Results:
# {{path-params.id}} = "123" (validated as integer)
# {{path-params.section}} = "settings" (validated as string)
```

### Available Types
- `string` (default) - Any string value
- `int` - Integer numbers only
- `uuid` - UUID format validation
- `date` - Date format validation
- `boolean` - Boolean values (true/false)

## Query Parameters

Access URL query string parameters that come after the `?` in URLs.

### Syntax
```yaml
# Request: GET /search?q=javascript&category=tutorials&page=2
body: >
  {
    "query": "{{query-params.q}}",
    "category": "{{query-params.category}}",
    "page": "{{query-params.page}}"
  }
```

### Examples

**Simple query parameters:**
```yaml
# Request: GET /products?sort=price&order=asc
# Results:
# {{query-params.sort}} = "price"
# {{query-params.order}} = "asc"
```

**Array parameters:**
```yaml
# Request: GET /products?tags=electronics&tags=wireless&tags=audio
# Results:
# {{query-params.tags}} = "electronics" (first value)
# Note: moclojer returns the first value for duplicate parameters
```

**URL encoded parameters:**
```yaml
# Request: GET /search?q=hello%20world&filter=name%3Ajohn
# Results:
# {{query-params.q}} = "hello world" (automatically decoded)
# {{query-params.filter}} = "name:john" (automatically decoded)
```

## JSON Body Parameters

Access data from JSON request bodies in POST, PUT, PATCH requests.

### Syntax
```yaml
# Request body: {"name": "John", "email": "john@example.com", "age": 30}
body: >
  {
    "id": 12345,
    "name": "{{json-params.name}}",
    "email": "{{json-params.email}}",
    "age": {{json-params.age}}
  }
```

### Examples

**Simple properties:**
```yaml
# Request body: {"username": "john_doe", "password": "secret123"}
# Results:
# {{json-params.username}} = "john_doe"
# {{json-params.password}} = "secret123"
```

**Nested objects:**
```yaml
# Request body: {"user": {"profile": {"name": "John", "age": 30}}}
# Results:
# {{json-params.user.profile.name}} = "John"
# {{json-params.user.profile.age}} = "30"
```

**Array elements:**
```yaml
# Request body: {"tags": ["javascript", "tutorial", "beginner"]}
# Results:
# {{json-params.tags.0}} = "javascript"
# {{json-params.tags.1}} = "tutorial"
# {{json-params.tags.2}} = "beginner"
```

**Complex nested structures:**
```yaml
# Request body:
# {
#   "order": {
#     "items": [
#       {"product": "laptop", "price": 999.99},
#       {"product": "mouse", "price": 29.99}
#     ],
#     "shipping": {"address": "123 Main St", "city": "Boston"}
#   }
# }
# Results:
# {{json-params.order.items.0.product}} = "laptop"
# {{json-params.order.items.0.price}} = "999.99"
# {{json-params.order.shipping.address}} = "123 Main St"
```

### Data Type Handling

**Strings (require quotes):**
```yaml
"name": "{{json-params.name}}"
```

**Numbers (no quotes needed):**
```yaml
"age": {{json-params.age}},
"price": {{json-params.price}}
```

**Booleans (no quotes needed):**
```yaml
"active": {{json-params.active}},
"verified": {{json-params.verified}}
```

## Header Parameters

Access HTTP request headers using the header name.

### Syntax
```yaml
body: >
  {
    "user_agent": "{{header-params.User-Agent}}",
    "content_type": "{{header-params.Content-Type}}",
    "authorization": "{{header-params.Authorization}}"
  }
```

### Examples

**Standard headers:**
```yaml
# Request headers:
# User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)
# Content-Type: application/json
# Authorization: Bearer token123
# Results:
# {{header-params.User-Agent}} = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
# {{header-params.Content-Type}} = "application/json"
# {{header-params.Authorization}} = "Bearer token123"
```

**Custom headers:**
```yaml
# Request headers:
# X-API-Key: abc123def456
# X-Request-ID: req-789
# X-User-Role: admin
# Results:
# {{header-params.X-API-Key}} = "abc123def456"
# {{header-params.X-Request-ID}} = "req-789"
# {{header-params.X-User-Role}} = "admin"
```

**Case sensitivity:**
```yaml
# Headers are case-insensitive in HTTP, but use exact case in templates:
# {{header-params.content-type}} = same as Content-Type
# {{header-params.CONTENT-TYPE}} = same as Content-Type
# {{header-params.Content-Type}} = recommended format
```

## Built-in Functions

Special template functions that provide dynamic values.

### `{{now}}`
Returns the current timestamp in ISO 8601 format.

```yaml
body: >
  {
    "created_at": "{{now}}",
    "timestamp": "{{now}}"
  }
# Result: "2024-01-15T10:30:00.000Z"
```

### `{{uuid}}`
Generates a random UUID v4.

```yaml
body: >
  {
    "id": "{{uuid}}",
    "transaction_id": "{{uuid}}"
  }
# Result: "123e4567-e89b-12d3-a456-426614174000"
```

### `{{random}}`
Generates random numbers (if supported).

```yaml
body: >
  {
    "random_id": "{{random}}",
    "session_id": "sess_{{random}}"
  }
```

## WebSocket Template Variables

Special variables available in WebSocket responses.

### `{{message}}`
The complete received message content.

```yaml
websocket:
  path: /ws/echo
  on-message:
    - pattern: ".*"
      response: >
        {
          "echo": {{message}},
          "timestamp": "{{now}}"
        }
```

### WebSocket Path Parameters
Same as HTTP path parameters.

```yaml
websocket:
  path: /ws/chat/:room_id
  on-connect:
    response: >
      {
        "room": "{{path-params.room_id}}",
        "status": "connected"
      }
```

## Advanced Usage

### Combining Multiple Parameters

```yaml
# Combine different parameter types
path: /api/:version/users/:id
body: >
  {
    "api_version": "{{path-params.version}}",
    "user_id": "{{path-params.id}}",
    "updated_name": "{{json-params.name}}",
    "client_ip": "{{header-params.X-Forwarded-For}}",
    "search_query": "{{query-params.q}}",
    "timestamp": "{{now}}"
  }
```

### Conditional Content

```yaml
# Basic conditional logic (if supported)
body: >
  {
    "message": "{{#if query-params.admin}}Admin access{{else}}Regular user{{/if}}",
    "role": "{{query-params.role}}"
  }
```

### String Interpolation

```yaml
# Embed variables within strings
body: >
  {
    "welcome_message": "Hello, {{json-params.name}}! Welcome to {{path-params.service}}",
    "full_url": "https://api.example.com{{path-params.endpoint}}?key={{query-params.api_key}}"
  }
```

## Error Handling

### Missing Parameters
When a parameter is not present in the request, it appears as an empty string:

```yaml
# If request doesn't include 'optional_param'
"value": "{{query-params.optional_param}}"
# Result: "value": ""
```

### Invalid JSON
If JSON body is malformed, json-params will be empty:

```yaml
# Invalid JSON request body
"name": "{{json-params.name}}"
# Result: "name": ""
```

### Special Characters
Template variables handle URL encoding and JSON escaping automatically:

```yaml
# Query: ?message=hello%20world%26more
"decoded": "{{query-params.message}}"
# Result: "decoded": "hello world&more"
```

## Best Practices

### 1. Use Appropriate Data Types
```yaml
# Good - numbers without quotes
"age": {{json-params.age}},
"price": {{json-params.price}}

# Bad - numbers with quotes become strings
"age": "{{json-params.age}}"
```

### 2. Provide Default Values
```yaml
# Use fallback values for optional parameters
"page": {{query-params.page}},
"default_message": "Page {{query-params.page}} or 1 if not specified"
```

### 3. Validate Parameter Types
```yaml
# Use typed path parameters for validation
path: /users/:id|int/orders/:orderId|uuid
```

### 4. Handle Missing Parameters Gracefully
```yaml
# Design responses to work with missing optional parameters
"filters": {
  "category": "{{query-params.category}}",
  "sort": "{{query-params.sort}}",
  "note": "Empty values indicate parameter not provided"
}
```

### 5. Use Meaningful Parameter Names
```yaml
# Good - clear parameter names
path: /users/:userId/posts/:postId

# Less clear
path: /users/:id1/posts/:id2
```

## Examples by Use Case

### User Profile API
```yaml
- endpoint:
    method: GET
    path: /users/:id/profile
    response:
      body: >
        {
          "user_id": "{{path-params.id}}",
          "include_private": {{query-params.private}},
          "requested_by": "{{header-params.X-User-ID}}",
          "timestamp": "{{now}}"
        }
```

### Product Search
```yaml
- endpoint:
    method: GET
    path: /products/search
    response:
      body: >
        {
          "query": "{{query-params.q}}",
          "category": "{{query-params.category}}",
          "min_price": {{query-params.min_price}},
          "max_price": {{query-params.max_price}},
          "results": []
        }
```

### Order Creation
```yaml
- endpoint:
    method: POST
    path: /orders
    response:
      body: >
        {
          "order_id": "ORD-{{uuid}}",
          "customer_id": "{{json-params.customer_id}}",
          "items": {{json-params.items}},
          "total": {{json-params.total}},
          "created_at": "{{now}}",
          "user_agent": "{{header-params.User-Agent}}"
        }
```

This reference covers all available template variables in moclojer. For more advanced templating techniques, see [Advanced Templating](advanced-templating.md).