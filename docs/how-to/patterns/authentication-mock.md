---
description: >-
  Mock authentication and authorization in moclojer - JWT tokens, OAuth flows, API keys, and role-based
  access control. Test auth without real identity providers.
---

# Authentication Mock Guide

Learn how to mock authentication and authorization flows for testing without real auth providers.

## 📋 What You'll Build

- ✅ JWT token simulation
- ✅ API key authentication
- ✅ OAuth 2.0 flow mock
- ✅ Role-based access control
- ✅ Session management

## 🔐 JWT Token Simulation

### Login Endpoint

```yaml
- endpoint:
    method: POST
    path: /api/auth/login
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ7e2pzb24tcGFyYW1zLmVtYWlsfX0iLCJpYXQiOjE2NDAwMDAwMDB9.mock_signature",
          "user": {
            "id": 123,
            "email": "{{json-params.email}}",
            "name": "{{json-params.name}}",
            "role": "user"
          },
          "expires_in": 3600
        }
```

### Protected Endpoint

```yaml
- endpoint:
    method: GET
    path: /api/profile
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": 123,
          "email": "user@example.com",
          "name": "John Doe",
          "role": "user",
          "token_valid": true,
          "auth_header": "{{header-params.Authorization}}"
        }
```

### Unauthorized Response

```yaml
- endpoint:
    method: GET
    path: /api/protected/unauthorized
    response:
      status: 401
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Unauthorized",
          "message": "Invalid or missing authentication token",
          "code": "AUTH_REQUIRED"
        }
```

## 🔑 API Key Authentication

```yaml
- endpoint:
    method: GET
    path: /api/data
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "api_key_received": "{{header-params.X-API-Key}}",
          "valid": true,
          "data": [...]
        }

# Invalid API key
- endpoint:
    method: GET
    path: /api/data/invalid
    response:
      status: 403
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Forbidden",
          "message": "Invalid API key",
          "code": "INVALID_API_KEY"
        }
```

## 🌐 OAuth 2.0 Flow

### Authorization Endpoint

```yaml
- endpoint:
    method: GET
    path: /oauth/authorize
    response:
      status: 302
      headers:
        Location: "http://localhost:3000/callback?code=mock_auth_code_12345&state={{query-params.state}}"
      body: ""
```

### Token Exchange

```yaml
- endpoint:
    method: POST
    path: /oauth/token
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "access_token": "mock_access_token_{{json-params.code}}",
          "token_type": "Bearer",
          "expires_in": 3600,
          "refresh_token": "mock_refresh_token_{{json-params.code}}",
          "scope": "read write"
        }
```

## 👥 Role-Based Access Control

### Admin Endpoint

```yaml
- endpoint:
    method: GET
    path: /api/admin/users
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "role": "admin",
          "users": [
            {"id": 1, "email": "alice@example.com"},
            {"id": 2, "email": "bob@example.com"}
          ]
        }

# Forbidden for non-admin
- endpoint:
    method: GET
    path: /api/admin/forbidden
    response:
      status: 403
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Forbidden",
          "message": "Admin access required",
          "code": "INSUFFICIENT_PERMISSIONS"
        }
```

## 🧪 Complete Auth Flow Example

```yaml
# === AUTHENTICATION ===

# Register
- endpoint:
    method: POST
    path: /api/auth/register
    response:
      status: 201
      body: >
        {
          "user": {
            "id": 999,
            "email": "{{json-params.email}}",
            "name": "{{json-params.name}}"
          },
          "message": "Registration successful"
        }

# Login
- endpoint:
    method: POST
    path: /api/auth/login
    response:
      status: 200
      body: >
        {
          "token": "mock_jwt_token",
          "user": {
            "id": 123,
            "email": "{{json-params.email}}",
            "role": "user"
          }
        }

# Logout
- endpoint:
    method: POST
    path: /api/auth/logout
    response:
      status: 200
      body: >
        {
          "message": "Logged out successfully"
        }

# Refresh Token
- endpoint:
    method: POST
    path: /api/auth/refresh
    response:
      status: 200
      body: >
        {
          "token": "new_mock_jwt_token",
          "expires_in": 3600
        }

# === PROTECTED RESOURCES ===

# User profile
- endpoint:
    method: GET
    path: /api/me
    response:
      status: 200
      body: >
        {
          "id": 123,
          "email": "user@example.com",
          "role": "user"
        }

# Admin only
- endpoint:
    method: GET
    path: /api/admin/stats
    response:
      status: 200
      body: >
        {
          "total_users": 1000,
          "active_sessions": 45
        }
```

## 🚀 Testing Examples

```bash
# Register
curl -X POST http://localhost:8000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","name":"Alice","password":"secret123"}'

# Login
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"secret123"}'

# Access protected resource
curl http://localhost:8000/api/me \
  -H "Authorization: Bearer mock_jwt_token"

# API key auth
curl http://localhost:8000/api/data \
  -H "X-API-Key: my_secret_api_key"

# OAuth flow
curl "http://localhost:8000/oauth/authorize?client_id=123&redirect_uri=http://localhost:3000/callback&state=xyz"
```

## ✅ Best Practices

**Do ✅:**
- Simulate realistic token formats
- Include proper HTTP status codes (401, 403)
- Return meaningful error messages
- Test both success and failure scenarios

**Don't ❌:**
- Don't use real passwords or secrets
- Don't skip error responses
- Don't forget token expiration simulation

## 📚 See Also

- **[Header Parameters](../../topics/parameters/header-parameters.md)** - Auth headers
- **[Error Handling](error-handling.md)** - Auth errors
- **[Stripe Mock Example](../../examples/third-party/stripe-mock.md)** - API key auth
- **[Real-World Example](../../getting-started/real-world-example.md)** - Auth simulation

---

**💡 Tip:** Use moclojer auth mocks for frontend development, CI/CD testing, and local development without real auth providers!
