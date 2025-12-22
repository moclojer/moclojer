---
description: >-
  Master path patterns in moclojer - wildcards, regex, precedence, and complex routing.
  Learn to create flexible URL patterns for sophisticated API mocking.
---

# Path Patterns Guide

Learn advanced path pattern techniques to create flexible and powerful URL routing in moclojer.

## 📋 Pattern Types

### 1. Static Paths

```yaml
- endpoint:
    path: /users
- endpoint:
    path: /api/v1/products
```

### 2. Path Parameters

```yaml
# Single parameter
- endpoint:
    path: /users/:id

# Multiple parameters
- endpoint:
    path: /posts/:postId/comments/:commentId

# Typed parameters
- endpoint:
    path: /users/:id|int
    path: /posts/:slug|string
```

### 3. Wildcards

```yaml
# Match any subpath
- endpoint:
    path: /api/*

# Catch-all
- endpoint:
    path: /**
```

## 🎯 Route Precedence

moclojer matches routes in this order:

1. **Exact matches** first
2. **Path parameters** second
3. **Wildcards** last

```yaml
- endpoint:
    path: /users/me          # Matches first (exact)
- endpoint:
    path: /users/:id         # Matches second (parameter)
- endpoint:
    path: /users/*           # Matches last (wildcard)
```

## ✅ Best Practices

**Do ✅:**

- Use specific patterns before generic ones
- Type your parameters (`:id|int`)
- Document complex patterns

**Don't ❌:**

- Don't rely on order for precedence
- Avoid overlapping patterns

## 📚 See Also

- **[Path Parameters](../parameters/path-parameters.md)**
- **[HTTP Methods](http-methods.md)**
- **[Request Matching](../request-matching.md)**
