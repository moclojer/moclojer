---
description: >-
  Understand moclojer response structure - status codes, headers, body formats, and templates.
  Create consistent, well-structured API responses.
---

# Response Structure Guide

Learn how to structure responses in moclojer for consistent, professional APIs.

## 📋 Response Components

### Complete Structure

```yaml
- endpoint:
    method: GET
    path: /users/:id
    response:
      status: 200                    # HTTP status code
      headers:                        # Response headers
        Content-Type: application/json
        X-Custom-Header: value
      body: >                         # Response body
        {
          "id": {{path-params.id}},
          "name": "User"
        }
```

## 🎯 Status Codes

### Success (2xx)
```yaml
status: 200  # OK - successful GET, PUT, DELETE
status: 201  # Created - successful POST
status: 204  # No Content - successful DELETE (no body)
```

### Client Errors (4xx)
```yaml
status: 400  # Bad Request - validation error
status: 401  # Unauthorized - authentication required
status: 404  # Not Found - resource doesn't exist
status: 422  # Unprocessable Entity - validation failed
```

### Server Errors (5xx)
```yaml
status: 500  # Internal Server Error
status: 503  # Service Unavailable
```

## 📤 Body Formats

### JSON (Most Common)
```yaml
body: >
  {
    "status": "success",
    "data": {
      "id": {{path-params.id}},
      "name": "{{json-params.name}}"
    }
  }
```

### Plain Text
```yaml
body: "Hello World"
```

### XML
```yaml
headers:
  Content-Type: application/xml
body: >
  <?xml version="1.0"?>
  <user>
    <id>{{path-params.id}}</id>
    <name>User</name>
  </user>
```

## ✅ Best Practices

### Consistent Format
```yaml
# ✅ Good - Consistent structure
body: >
  {
    "status": "success",
    "data": {...},
    "meta": {...}
  }
```

### Error Responses
```yaml
# ✅ Good - Informative errors
status: 404
body: >
  {
    "error": "Not Found",
    "message": "User with ID 123 not found",
    "code": "USER_NOT_FOUND",
    "timestamp": "{{now}}"
  }
```

## 📚 See Also

- **[HTTP Methods](http-methods.md)**
- **[Template Variables](../templates/template-variables.md)**
- **[Error Handling](../../how-to/patterns/error-handling.md)**
