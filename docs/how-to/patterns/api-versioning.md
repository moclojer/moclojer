---
description: >-
  API versioning strategies with moclojer - URL versioning, header versioning, and
  managing multiple API versions simultaneously for backward compatibility.
---

# API Versioning Patterns

Implement API versioning in moclojer to support multiple API versions.

## 🎯 Versioning Strategies

### 1. URL Versioning

```yaml
# v1 endpoints
- endpoint:
    method: GET
    path: /api/v1/users
    response:
      status: 200
      body: >
        {"users": [{"id": 1, "name": "Alice"}], "version": "1.0"}

# v2 endpoints
- endpoint:
    method: GET
    path: /api/v2/users
    response:
      status: 200
      body: >
        {
          "data": [{"id": 1, "fullName": "Alice Johnson"}],
          "meta": {"version": "2.0"}
        }
```

### 2. Header Versioning

```yaml
- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      headers:
        X-API-Version: "{{header-params.Accept-Version}}"
      body: >
        {
          "version": "{{header-params.Accept-Version}}",
          "users": []
        }
```

## ✅ Best Practices

- Use semantic versioning (v1, v2, v3)
- Maintain backward compatibility
- Document version differences
- Deprecate gradually

## 📚 See Also

- **[Path Patterns](../../topics/endpoints/path-patterns.md)**
- **[Header Parameters](../../topics/parameters/header-parameters.md)**
