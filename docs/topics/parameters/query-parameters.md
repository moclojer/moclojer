---
description: >-
  Learn how to use query parameters in moclojer to create endpoints that
  respond to filters, pagination, search, and sorting.
---

# Query Parameters

Query parameters are values passed in the URL after the `?` character, used to filter, paginate, sort, or search data. They are essential for creating flexible and dynamic RESTful APIs.

## What Are Query Parameters?

**Anatomy of a URL with query params:**

```
https://api.example.com/users?role=admin&status=active&limit=10
                          └─┬─┘ └──┬───┘ └────┬────┘ └──┬──┘
                            │      │          │         │
                            │      │          │         └─ limit=10
                            │      │          └─────────── status=active
                            │      └──────────────────── role=admin
                            └───────────────────────── Separator ?
```

**Characteristics:**

- Start with `?` after the path
- `key=value` pairs separated by `&`
- Optional (unlike path params)
- Ideal for filters, pagination, search

## Basic Syntax

### Accessing Query Parameters

Use templates `{{query-params.keyName}}`:

```yaml
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        {
          "filters": {
            "role": "{{query-params.role}}",
            "status": "{{query-params.status}}"
          },
          "results": []
        }
```

**Test:**

```bash
curl "http://localhost:8000/users?role=admin&status=active"
```

**Response:**

```json
{
  "filters": {
    "role": "admin",
    "status": "active"
  },
  "results": []
}
```

### Optional Query Parameters

All query params are optional by default:

```yaml
- endpoint:
    method: GET
    path: /products
    response:
      status: 200
      body: >
        {
          "category": "{{query-params.category}}",
          "minPrice": "{{query-params.min_price}}",
          "maxPrice": "{{query-params.max_price}}"
        }
```

**Works with any combination:**

```bash
# All parameters
curl "http://localhost:8000/products?category=electronics&min_price=100&max_price=500"

# Only some
curl "http://localhost:8000/products?category=electronics"

# No parameters
curl "http://localhost:8000/products"
# {"category": "", "minPrice": "", "maxPrice": ""}
```

## Common Use Cases

### 1. Pagination

```yaml
- endpoint:
    method: GET
    path: /posts
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "1000"
      body: >
        {
          "page": "{{query-params.page}}",
          "limit": "{{query-params.limit}}",
          "total": 1000,
          "posts": [
            {"id": 1, "title": "Post 1"},
            {"id": 2, "title": "Post 2"},
            {"id": 3, "title": "Post 3"}
          ],
          "pagination": {
            "currentPage": "{{query-params.page}}",
            "perPage": "{{query-params.limit}}",
            "totalPages": 100
          }
        }
```

**Usage:**

```bash
curl "http://localhost:8000/posts?page=1&limit=10"
curl "http://localhost:8000/posts?page=2&limit=20"
```

### 2. Filters

```yaml
- endpoint:
    method: GET
    path: /products
    response:
      status: 200
      body: >
        {
          "filters": {
            "category": "{{query-params.category}}",
            "brand": "{{query-params.brand}}",
            "inStock": "{{query-params.in_stock}}"
          },
          "products": [
            {
              "id": 1,
              "name": "Product matching filters",
              "category": "{{query-params.category}}"
            }
          ]
        }
```

**Usage:**

```bash
curl "http://localhost:8000/products?category=electronics&brand=sony&in_stock=true"
```

### 3. Search

```yaml
- endpoint:
    method: GET
    path: /search
    response:
      status: 200
      body: >
        {
          "query": "{{query-params.q}}",
          "type": "{{query-params.type}}",
          "results": [
            {
              "id": 1,
              "title": "Result containing: {{query-params.q}}",
              "type": "{{query-params.type}}"
            }
          ]
        }
```

**Usage:**

```bash
curl "http://localhost:8000/search?q=moclojer&type=documentation"
```

### 4. Sorting

```yaml
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        {
          "sort": {
            "field": "{{query-params.sort}}",
            "order": "{{query-params.order}}"
          },
          "users": [
            {"id": 1, "name": "Alice", "createdAt": "2024-01-01"},
            {"id": 2, "name": "Bob", "createdAt": "2024-01-02"}
          ],
          "meta": {
            "sortedBy": "{{query-params.sort}}",
            "sortOrder": "{{query-params.order}}"
          }
        }
```

**Usage:**

```bash
curl "http://localhost:8000/users?sort=name&order=asc"
curl "http://localhost:8000/users?sort=createdAt&order=desc"
```

### 5. Field Selection

```yaml
- endpoint:
    method: GET
    path: /users/:id
    response:
      status: 200
      body: >
        {
          "id": "{{path-params.id}}",
          "requestedFields": "{{query-params.fields}}",
          "data": {
            "id": "{{path-params.id}}",
            "name": "User {{path-params.id}}",
            "email": "user@example.com",
            "note": "Fields requested: {{query-params.fields}}"
          }
        }
```

**Usage:**

```bash
# Only some fields
curl "http://localhost:8000/users/1?fields=id,name,email"

# All fields
curl "http://localhost:8000/users/1"
```

## Combining Multiple Query Parameters

```yaml
- endpoint:
    method: GET
    path: /api/products
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "filters": {
            "category": "{{query-params.category}}",
            "minPrice": "{{query-params.min_price}}",
            "maxPrice": "{{query-params.max_price}}",
            "inStock": "{{query-params.in_stock}}"
          },
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}"
          },
          "sort": {
            "field": "{{query-params.sort}}",
            "order": "{{query-params.order}}"
          },
          "search": "{{query-params.q}}",
          "products": []
        }
```

**Complete usage example:**

```bash
curl "http://localhost:8000/api/products?category=electronics&min_price=100&max_price=1000&in_stock=true&page=1&limit=20&sort=price&order=asc&q=laptop"
```

## Query Parameters with Path Parameters

Query params work perfectly with path params:

```yaml
- endpoint:
    method: GET
    path: /users/:userId/posts
    response:
      status: 200
      body: >
        {
          "userId": "{{path-params.userId}}",
          "filters": {
            "status": "{{query-params.status}}",
            "category": "{{query-params.category}}"
          },
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}"
          },
          "posts": [
            {
              "id": 1,
              "authorId": "{{path-params.userId}}",
              "status": "{{query-params.status}}",
              "title": "Post by User {{path-params.userId}}"
            }
          ]
        }
```

**Usage:**

```bash
curl "http://localhost:8000/users/42/posts?status=published&page=1&limit=10"
```

## Default Values and Missing Values

### Query Param Not Provided

When a query param is not passed, the template returns an empty string:

```yaml
- endpoint:
    path: /users
    response:
      body: >
        {
          "limit": "{{query-params.limit}}",
          "page": "{{query-params.page}}"
        }
```

**Test without parameters:**

```bash
curl "http://localhost:8000/users"
# {"limit": "", "page": ""}
```

### Simulating Default Values

Use multiple endpoints with precedence:

```yaml
# 1. Endpoint WITHOUT query params (uses default values)
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        {
          "page": "1",
          "limit": "10",
          "note": "Using default values"
        }

# 2. Endpoint WITH query params (custom values)
# NOTE: Moclojer doesn't differentiate presence/absence of query params
# Both endpoints above will respond. Use logic in client.
```

**Better alternative:**

```yaml
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        {
          "page": "{{query-params.page}}",
          "limit": "{{query-params.limit}}",
          "defaults": {
            "page": "1",
            "limit": "10"
          },
          "note": "Client should use defaults.page if page is empty"
        }
```

## Arrays in Query Parameters

URLs can have multiple values for the same key:

```
/users?id=1&id=2&id=3
/tags?tag=javascript&tag=clojure&tag=api
```

**In moclojer:**

```yaml
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        {
          "requestedIds": "{{query-params.id}}",
          "note": "Multiple IDs: 1, 2, 3"
        }
```

⚠️ **Limitation:** Moclojer currently returns only the **last value** when there are duplicates.

**Workaround:** Use delimiters

```bash
# Instead of: ?id=1&id=2&id=3
curl "http://localhost:8000/users?ids=1,2,3"
```

```yaml
response:
  body: >
    {
      "ids": "{{query-params.ids}}",
      "note": "Client splits by comma: [1, 2, 3]"
    }
```

## Special Characters

Query parameters must be URL-encoded:

| Character | Encoding | Example |
|-----------|----------|---------|
| Space | `%20` or `+` | `q=hello%20world` |
| `&` | `%26` | `company=A%26B` |
| `=` | `%3D` | `equation=x%3D5` |
| `#` | `%23` | `tag=%23moclojer` |
| `?` | `%3F` | `query=what%3F` |

**Example:**

```bash
# Search: "moclojer & API testing"
curl "http://localhost:8000/search?q=moclojer%20%26%20API%20testing"
```

Tools like `curl` and browsers do encoding automatically.

## Complete Practical Examples

### Example 1: E-commerce API

```yaml
- endpoint:
    method: GET
    path: /api/products
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "250"
      body: >
        {
          "meta": {
            "total": 250,
            "page": "{{query-params.page}}",
            "perPage": "{{query-params.limit}}",
            "totalPages": 25
          },
          "filters": {
            "category": "{{query-params.category}}",
            "brand": "{{query-params.brand}}",
            "minPrice": "{{query-params.min_price}}",
            "maxPrice": "{{query-params.max_price}}",
            "onSale": "{{query-params.on_sale}}",
            "inStock": "{{query-params.in_stock}}"
          },
          "sort": {
            "by": "{{query-params.sort_by}}",
            "order": "{{query-params.order}}"
          },
          "search": "{{query-params.q}}",
          "products": [
            {
              "id": 1,
              "name": "Product matching filters",
              "price": 299.99,
              "category": "{{query-params.category}}"
            }
          ]
        }
```

**Usage:**

```bash
curl "http://localhost:8000/api/products?category=laptops&brand=dell&min_price=500&max_price=1500&on_sale=true&in_stock=true&sort_by=price&order=asc&page=1&limit=20&q=gaming"
```

### Example 2: Blog API

```yaml
- endpoint:
    method: GET
    path: /api/posts
    response:
      status: 200
      body: >
        {
          "filters": {
            "author": "{{query-params.author}}",
            "category": "{{query-params.category}}",
            "tag": "{{query-params.tag}}",
            "published": "{{query-params.published}}"
          },
          "search": "{{query-params.q}}",
          "dateRange": {
            "from": "{{query-params.from}}",
            "to": "{{query-params.to}}"
          },
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}"
          },
          "posts": [
            {
              "id": 1,
              "title": "Post about {{query-params.q}}",
              "author": "{{query-params.author}}",
              "publishedAt": "2024-01-15"
            }
          ]
        }
```

**Usage:**

```bash
curl "http://localhost:8000/api/posts?author=john&category=tech&tag=api&published=true&q=moclojer&from=2024-01-01&to=2024-12-31&page=1&limit=10"
```

### Example 3: Analytics API

```yaml
- endpoint:
    method: GET
    path: /api/analytics
    response:
      status: 200
      body: >
        {
          "timeRange": {
            "start": "{{query-params.start_date}}",
            "end": "{{query-params.end_date}}"
          },
          "metrics": "{{query-params.metrics}}",
          "dimensions": "{{query-params.dimensions}}",
          "filters": "{{query-params.filters}}",
          "granularity": "{{query-params.granularity}}",
          "data": [
            {
              "date": "{{query-params.start_date}}",
              "metrics": "{{query-params.metrics}}",
              "value": 1250
            }
          ]
        }
```

**Usage:**

```bash
curl "http://localhost:8000/api/analytics?start_date=2024-01-01&end_date=2024-01-31&metrics=pageviews,sessions&dimensions=country,device&granularity=day"
```

## Best Practices

### ✅ Do

1. **Use descriptive and consistent names**

   ```bash
   # ✅ Good
   ?page=1&limit=10&sort_by=name

   # ❌ Avoid
   ?p=1&l=10&s=name
   ```

2. **Use snake_case or camelCase consistently**

   ```bash
   # ✅ snake_case
   ?min_price=100&max_price=500

   # ✅ camelCase
   ?minPrice=100&maxPrice=500

   # ❌ Mixed
   ?min_price=100&maxPrice=500
   ```

3. **Boolean values as strings**

   ```yaml
   # Template always returns string
   "inStock": "{{query-params.in_stock}}"

   # Client interprets "true"/"false"
   ```

4. **Document optional parameters**

   ```yaml
   # In response, include documentation
   response:
     body: >
       {
         "_docs": {
           "params": {
             "page": "Page number (default: 1)",
             "limit": "Items per page (default: 10)",
             "sort": "Sort field (default: createdAt)"
           }
         }
       }
   ```

### ❌ Avoid

1. **Required parameters as query params**

   ```yaml
   # ❌ ID should be path param
   path: /users
   # ?id=123

   # ✅ Use path param for IDs
   path: /users/:id
   ```

2. **Sensitive data in query params**

   ```bash
   # ❌ NEVER do this!
   ?password=secret123
   ?api_key=sk_live_abc123

   # ✅ Use headers or body
   Authorization: Bearer token
   ```

3. **Query params too complex**

   ```bash
   # ❌ Too complex for query param
   ?filter={"and":[{"field":"age","op":">","value":18}]}

   # ✅ Use POST with body for complex filters
   ```

## Troubleshooting

### Problem: Query param returns empty when should have value

**Cause 1:** Parameter name doesn't match

```yaml
# ❌ Wrong
URL: ?limit=10
Template: {{query-params.lim}}  # Typo!

# ✅ Correct
Template: {{query-params.limit}}
```

**Cause 2:** Spaces in name

```yaml
# ❌ Doesn't work
{{query-params.min price}}

# ✅ Use underscore or camelCase
{{query-params.min_price}}
{{query-params.minPrice}}
```

### Problem: Special character breaks URL

**Solution:** Use URL encoding

```bash
# ❌ Breaks
curl "http://localhost:8000/search?q=A&B"

# ✅ Encode
curl "http://localhost:8000/search?q=A%26B"
```

### Problem: Number comes as string

**Cause:** Templates always return strings

```yaml
# ❌ Returns "10" (string)
{"limit": "{{query-params.limit}}"}

# ✅ Returns 10 (number) - no quotes!
{"limit": {{query-params.limit}}}
```

⚠️ **Caution:** Without quotes, if parameter is empty, JSON becomes invalid!

**Safe solution:**

```yaml
# Client does conversion
{"limit": "{{query-params.limit}}"}
# Client: parseInt(data.limit) || 10
```

## Next Steps

Now that you've mastered query parameters:

1. **[Path Parameters](path-parameters.md)** - Parameters in the URL
2. **[Body Parameters](body-parameters.md)** - Data in the request body
3. **[Template Variables](../templates/template-variables.md)** - Complete reference

## See Also

- [Pagination How-to](../../how-to/patterns/pagination.md) - Implement pagination
- [Dynamic Responses Tutorial](../../getting-started/dynamic-responses.md) - Practical tutorial
- [Request Matching](../request-matching.md) - How moclojer chooses endpoints
