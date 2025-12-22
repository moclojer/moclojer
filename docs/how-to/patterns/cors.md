---
description: >-
  Configure CORS (Cross-Origin Resource Sharing) in moclojer for local development,
  testing, and cross-domain API access with proper headers.
---

# CORS Configuration

Configure CORS headers in moclojer for cross-origin requests.

## 🎯 Enable CORS

### Environment Variable

```bash
MOCLOJER_ENABLE_CORS=true moclojer --config mocks.yml
```

### Manual Headers

```yaml
- endpoint:
    method: OPTIONS
    path: /api/users
    response:
      status: 204
      headers:
        Access-Control-Allow-Origin: "*"
        Access-Control-Allow-Methods: "GET, POST, PUT, DELETE, OPTIONS"
        Access-Control-Allow-Headers: "Content-Type, Authorization"
        Access-Control-Max-Age: "86400"
      body: ""

- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      headers:
        Access-Control-Allow-Origin: "*"
        Content-Type: application/json
      body: >
        {"users": []}
```

## 🔒 Specific Origins

```yaml
headers:
  Access-Control-Allow-Origin: "https://myapp.com"
  Access-Control-Allow-Credentials: "true"
```

## ✅ Best Practices

**Development:**

- Use `Access-Control-Allow-Origin: "*"`

**Production:**

- Specify exact origins
- Avoid wildcards
- Use credentials carefully

## 📚 See Also

- **[Header Parameters](../../topics/parameters/header-parameters.md)**
- **[Response Structure](../../topics/endpoints/response-structure.md)**
