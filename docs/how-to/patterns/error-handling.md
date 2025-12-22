---
description: >-
  Comprehensive error handling patterns for moclojer - validation errors, not found, server errors,
  and consistent error response formats. Build robust APIs.
---

# Error Handling Patterns

Learn to create consistent, informative error responses in moclojer.

## 📋 Common Error Patterns

### 404 Not Found
```yaml
- endpoint:
    method: GET
    path: /users/999
    response:
      status: 404
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Not Found",
          "message": "User with ID 999 not found",
          "code": "USER_NOT_FOUND",
          "timestamp": "{{now}}"
        }
```

### 400 Bad Request
```yaml
- endpoint:
    method: POST
    path: /users/invalid
    response:
      status: 400
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Bad Request",
          "message": "Invalid request data",
          "code": "INVALID_REQUEST"
        }
```

### 422 Validation Error
```yaml
- endpoint:
    method: POST
    path: /users/validation-error
    response:
      status: 422
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Validation Failed",
          "code": "VALIDATION_ERROR",
          "errors": [
            {
              "field": "email",
              "message": "Invalid email format"
            },
            {
              "field": "password",
              "message": "Password too short"
            }
          ]
        }
```

### 401 Unauthorized
```yaml
- endpoint:
    method: GET
    path: /protected/unauthorized
    response:
      status: 401
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Unauthorized",
          "message": "Authentication required",
          "code": "AUTH_REQUIRED"
        }
```

### 403 Forbidden
```yaml
- endpoint:
    method: DELETE
    path: /admin/forbidden
    response:
      status: 403
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Forbidden",
          "message": "Insufficient permissions",
          "code": "FORBIDDEN"
        }
```

### 500 Internal Server Error
```yaml
- endpoint:
    method: GET
    path: /error/server
    response:
      status: 500
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Internal Server Error",
          "message": "An unexpected error occurred",
          "code": "INTERNAL_ERROR",
          "request_id": "req_{{now|timestamp}}"
        }
```

## ✅ Best Practices

**Consistent Format:**
```yaml
{
  "error": "Error Type",
  "message": "Human-readable message",
  "code": "MACHINE_READABLE_CODE",
  "timestamp": "2024-01-15T10:30:00Z",
  "request_id": "req_123"
}
```

**Include:**
- ✅ Error type
- ✅ Readable message
- ✅ Machine-readable code
- ✅ Timestamp
- ✅ Request ID (for tracking)

**Field-Level Errors:**
```yaml
{
  "error": "Validation Failed",
  "errors": [
    {"field": "email", "message": "Required"},
    {"field": "age", "message": "Must be positive"}
  ]
}
```

## 📚 See Also

- **[Response Structure](../../topics/endpoints/response-structure.md)**
- **[Authentication Mock](authentication-mock.md)**
- **[CRUD Operations](crud-operations.md)**
