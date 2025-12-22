---
description: >-
  Understand how moclojer matches requests to endpoints - method, path, headers, and query parameters.
  Learn matching precedence and conflict resolution.
---

# Request Matching Guide

Learn how moclojer matches incoming requests to configured endpoints.

## 🎯 Matching Criteria

moclojer matches requests using:

1. **HTTP Method** (GET, POST, etc.)
2. **Path Pattern** (exact → params → wildcards)
3. **Host** (if specified)

## 📊 Matching Order

```yaml
# 1. Exact path matches first
- endpoint:
    method: GET
    path: /users/me

# 2. Path parameters second
- endpoint:
    method: GET
    path: /users/:id

# 3. Wildcards last
- endpoint:
    method: GET
    path: /users/*
```

## ✅ Best Practices

- **Specific before generic** - Order matters
- **Avoid overlapping patterns** - Can cause conflicts
- **Use different methods** - Same path, different methods OK

## 📚 See Also

- **[Path Patterns](endpoints/path-patterns.md)**
- **[HTTP Methods](endpoints/http-methods.md)**
