---
description: >-
  Learn how to implement pagination in mock APIs with moclojer. Offset/Limit,
  Cursor-based, Page-based and Link headers (RFC 5988).
---

# Pagination

Pagination is essential in APIs that return large lists of data. This guide shows how to implement different pagination strategies with moclojer.

## Why Paginate?

✅ **Performance**: Don't load 10,000 items at once
✅ **UX**: Better user experience
✅ **Bandwidth**: Less data transferred
✅ **Cost**: Less server processing

**Without pagination:**

```json
GET /users → [10,000 users] 😱
```

**With pagination:**

```json
GET /users?page=1&limit=20 → [20 users] ✅
```

---

## Pagination Strategies

### 1. Offset/Limit (Most Common)

**Concept:** Skip X items, return Y items.

**Parameters:**

- `limit` (or `per_page`): Quantity per page
- `offset` (or `skip`): How many to skip

**Math:**

```
offset = (page - 1) * limit
```

**Example:**

```yaml
- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "1000"
      body: >
        {
          "data": [
            {"id": 1, "name": "User 1"},
            {"id": 2, "name": "User 2"},
            {"id": 3, "name": "User 3"}
          ],
          "pagination": {
            "offset": "{{query-params.offset}}",
            "limit": "{{query-params.limit}}",
            "total": 1000
          }
        }
```

**Usage:**

```bash
# First page (0-19)
curl "http://localhost:8000/api/users?offset=0&limit=20"

# Second page (20-39)
curl "http://localhost:8000/api/users?offset=20&limit=20"

# Third page (40-59)
curl "http://localhost:8000/api/users?offset=40&limit=20"
```

---

### 2. Page/Limit (More Intuitive)

**Concept:** Page number + items per page.

**Parameters:**

- `page`: Page number (starts at 1)
- `limit` (or `per_page`): Items per page

**Example:**

```yaml
- endpoint:
    method: GET
    path: /api/products
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "500"
        X-Total-Pages: "50"
      body: >
        {
          "data": [
            {"id": 1, "name": "Product 1", "price": 29.99},
            {"id": 2, "name": "Product 2", "price": 39.99}
          ],
          "meta": {
            "currentPage": "{{query-params.page}}",
            "perPage": "{{query-params.limit}}",
            "totalItems": 500,
            "totalPages": 50,
            "hasNextPage": true,
            "hasPreviousPage": false
          }
        }
```

**Usage:**

```bash
# First page
curl "http://localhost:8000/api/products?page=1&limit=10"

# Second page
curl "http://localhost:8000/api/products?page=2&limit=10"

# Page 5 with 25 items
curl "http://localhost:8000/api/products?page=5&limit=25"
```

---

### 3. Cursor-Based (For Dynamic Feeds)

**Concept:** Uses a cursor (ID, timestamp) to mark position.

**Advantages:**

- Consistent even with new items
- Perfect for infinite scroll
- Doesn't allow skipping pages (more secure)

**Example:**

```yaml
- endpoint:
    method: GET
    path: /api/posts
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "data": [
            {"id": 101, "title": "Post 101", "createdAt": "2024-01-15T10:00:00Z"},
            {"id": 102, "title": "Post 102", "createdAt": "2024-01-15T11:00:00Z"},
            {"id": 103, "title": "Post 103", "createdAt": "2024-01-15T12:00:00Z"}
          ],
          "pagination": {
            "nextCursor": "{{query-params.cursor}}",
            "hasMore": true
          },
          "links": {
            "next": "/api/posts?cursor={{query-params.cursor}}&limit={{query-params.limit}}"
          }
        }
```

**Usage:**

```bash
# First request (no cursor)
curl "http://localhost:8000/api/posts?limit=10"

# Next page (use returned cursor)
curl "http://localhost:8000/api/posts?cursor=103&limit=10"

# Another page
curl "http://localhost:8000/api/posts?cursor=203&limit=10"
```

---

## Pagination Metadata

### Complete Structure

```yaml
- endpoint:
    method: GET
    path: /api/items
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "1000"
      body: >
        {
          "data": [
            {"id": 1, "name": "Item 1"}
          ],
          "meta": {
            "pagination": {
              "page": "{{query-params.page}}",
              "perPage": "{{query-params.limit}}",
              "totalPages": 100,
              "totalItems": 1000,
              "hasNextPage": true,
              "hasPreviousPage": false
            }
          },
          "links": {
            "self": "/api/items?page={{query-params.page}}&limit={{query-params.limit}}",
            "first": "/api/items?page=1&limit={{query-params.limit}}",
            "prev": null,
            "next": "/api/items?page=2&limit={{query-params.limit}}",
            "last": "/api/items?page=100&limit={{query-params.limit}}"
          }
        }
```

### Pagination Headers

```yaml
- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "1000"
        X-Page: "{{query-params.page}}"
        X-Per-Page: "{{query-params.limit}}"
        X-Total-Pages: "100"
        Link: '</api/users?page=2&limit=10>; rel="next", </api/users?page=100&limit=10>; rel="last"'
      body: >
        {
          "data": []
        }
```

---

## Practical Examples

### 1. E-commerce API (Products)

```yaml
- endpoint:
    method: GET
    path: /api/products
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "2500"
      body: >
        {
          "products": [
            {
              "id": 1,
              "name": "Laptop",
              "price": 999.99,
              "category": "{{query-params.category}}"
            },
            {
              "id": 2,
              "name": "Mouse",
              "price": 29.99,
              "category": "{{query-params.category}}"
            }
          ],
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}",
            "total": 2500,
            "totalPages": 250,
            "hasMore": true
          },
          "filters": {
            "category": "{{query-params.category}}",
            "minPrice": "{{query-params.min_price}}",
            "maxPrice": "{{query-params.max_price}}"
          }
        }
```

**Usage:**

```bash
# First page of electronics
curl "http://localhost:8000/api/products?category=electronics&page=1&limit=20"

# Second page with price filter
curl "http://localhost:8000/api/products?category=electronics&min_price=100&max_price=500&page=2&limit=20"
```

### 2. Blog API (Posts with Cursor)

```yaml
- endpoint:
    method: GET
    path: /api/posts
    response:
      status: 200
      body: >
        {
          "posts": [
            {
              "id": 101,
              "title": "Getting Started with moclojer",
              "slug": "getting-started-moclojer",
              "author": "Alice",
              "publishedAt": "2024-01-15T10:00:00Z"
            },
            {
              "id": 102,
              "title": "API Mocking Best Practices",
              "slug": "api-mocking-best-practices",
              "author": "Bob",
              "publishedAt": "2024-01-14T15:30:00Z"
            }
          ],
          "pagination": {
            "cursor": "{{query-params.cursor}}",
            "limit": "{{query-params.limit}}",
            "hasMore": true,
            "next": "/api/posts?cursor=102&limit={{query-params.limit}}"
          }
        }
```

**Usage:**

```bash
# First load
curl "http://localhost:8000/api/posts?limit=20"

# Infinite scroll (next page)
curl "http://localhost:8000/api/posts?cursor=102&limit=20"
```

### 3. Comments API (Nested Pagination)

```yaml
- endpoint:
    method: GET
    path: /api/posts/:postId/comments
    response:
      status: 200
      body: >
        {
          "postId": "{{path-params.postId}}",
          "comments": [
            {
              "id": 1,
              "author": "Alice",
              "text": "Great post!",
              "createdAt": "2024-01-15T10:00:00Z"
            },
            {
              "id": 2,
              "author": "Bob",
              "text": "Thanks for sharing!",
              "createdAt": "2024-01-15T11:00:00Z"
            }
          ],
          "pagination": {
            "page": "{{query-params.page}}",
            "perPage": "{{query-params.limit}}",
            "total": 156,
            "totalPages": 16
          }
        }
```

**Usage:**

```bash
curl "http://localhost:8000/api/posts/42/comments?page=1&limit=10"
```

### 4. API with Default Values

```yaml
# Endpoint WITHOUT parameters (uses defaults)
- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      body: >
        {
          "data": [
            {"id": 1, "name": "Alice"},
            {"id": 2, "name": "Bob"}
          ],
          "meta": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}",
            "defaults": {
              "page": "1",
              "limit": "10",
              "note": "Use defaults if page/limit is empty"
            }
          }
        }
```

**Usage:**

```bash
# Without parameters (client uses defaults from response)
curl "http://localhost:8000/api/users"

# With parameters
curl "http://localhost:8000/api/users?page=2&limit=50"
```

---

## Link Headers (RFC 5988)

Standard for pagination navigation via headers:

```yaml
- endpoint:
    method: GET
    path: /api/items
    response:
      status: 200
      headers:
        Content-Type: application/json
        Link: '</api/items?page=1>; rel="first", </api/items?page=5>; rel="prev", </api/items?page=7>; rel="next", </api/items?page=100>; rel="last"'
      body: >
        {
          "data": [
            {"id": 1, "name": "Item 1"}
          ]
        }
```

**Format:**

```
Link: <URL>; rel="relation"
```

**Relations:**

- `first`: First page
- `prev`: Previous page
- `next`: Next page
- `last`: Last page
- `self`: Current page

**GitHub API Example:**

```
Link: <https://api.github.com/repos?page=2>; rel="next",
      <https://api.github.com/repos?page=50>; rel="last"
```

---

## Sorting + Pagination

```yaml
- endpoint:
    method: GET
    path: /api/users
    response:
      status: 200
      body: >
        {
          "data": [
            {"id": 1, "name": "Alice", "createdAt": "2024-01-01T00:00:00Z"},
            {"id": 2, "name": "Bob", "createdAt": "2024-01-02T00:00:00Z"}
          ],
          "sort": {
            "field": "{{query-params.sort}}",
            "order": "{{query-params.order}}"
          },
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}"
          }
        }
```

**Usage:**

```bash
# Sort by name (A-Z), page 1
curl "http://localhost:8000/api/users?sort=name&order=asc&page=1&limit=20"

# Sort by date (most recent), page 2
curl "http://localhost:8000/api/users?sort=createdAt&order=desc&page=2&limit=20"
```

---

## Filters + Pagination

```yaml
- endpoint:
    method: GET
    path: /api/products
    response:
      status: 200
      body: >
        {
          "data": [],
          "filters": {
            "category": "{{query-params.category}}",
            "brand": "{{query-params.brand}}",
            "inStock": "{{query-params.in_stock}}"
          },
          "sort": {
            "field": "{{query-params.sort}}",
            "order": "{{query-params.order}}"
          },
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}",
            "total": 150
          }
        }
```

**Usage:**

```bash
curl "http://localhost:8000/api/products?category=electronics&brand=sony&in_stock=true&sort=price&order=asc&page=1&limit=20"
```

---

## Search + Pagination

```yaml
- endpoint:
    method: GET
    path: /api/search
    response:
      status: 200
      body: >
        {
          "query": "{{query-params.q}}",
          "results": [
            {
              "id": 1,
              "title": "Result containing {{query-params.q}}",
              "relevance": 0.95
            }
          ],
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}",
            "totalResults": 456
          }
        }
```

**Usage:**

```bash
curl "http://localhost:8000/api/search?q=moclojer&page=1&limit=10"
```

---

## Best Practices

### ✅ Do

1. **Always return metadata**

   ```json
   {
     "data": [...],
     "meta": {
       "total": 1000,
       "page": 1,
       "perPage": 20
     }
   }
   ```

2. **Use headers for totals**

   ```yaml
   headers:
     X-Total-Count: "1000"
     X-Total-Pages: "50"
   ```

3. **Provide navigation links**

   ```json
   {
     "links": {
       "next": "/api/items?page=2",
       "prev": "/api/items?page=1",
       "last": "/api/items?page=100"
     }
   }
   ```

4. **Maximum item limit**

   ```json
   {
     "meta": {
       "maxLimit": 100,
       "requestedLimit": "{{query-params.limit}}"
     }
   }
   ```

5. **Consistency in names**

   ```bash
   # ✅ Choose a pattern and follow it
   ?page=1&limit=20
   ?page=1&per_page=20

   # ❌ Don't mix
   ?page=1&limit=20
   ?offset=0&per_page=20
   ```

### ❌ Avoid

1. **Pagination without total**

   ```json
   // ❌ Hard to know how many pages exist
   {"data": [...]}

   // ✅ Include totals
   {"data": [...], "total": 1000}
   ```

2. **Very high limits**

   ```bash
   # ❌ May overload
   ?limit=10000

   # ✅ Define reasonable maximum
   ?limit=100  # max
   ```

3. **Offset without limit**

   ```bash
   # ❌ Ambiguous
   ?offset=100

   # ✅ Always together
   ?offset=100&limit=20
   ```

4. **Broken links**

   ```json
   // ❌ Invalid link
   "next": "/api/items?page=undefined"

   // ✅ Validate before returning
   "next": "/api/items?page=2&limit=20"
   ```

---

## Troubleshooting

### Problem: Client doesn't know total pages

**Solution:** Return `totalPages` or `X-Total-Pages` header

```yaml
headers:
  X-Total-Pages: "50"
body: >
  {"meta": {"totalPages": 50}}
```

### Problem: Broken navigation links

**Solution:** Use template vars correctly

```yaml
# ✅ Correct
"next": "/api/items?page=2&limit={{query-params.limit}}"

# ❌ Wrong (hardcoded)
"next": "/api/items?page=2&limit=10"
```

### Problem: Pagination + Lost filters

**Solution:** Preserve all query params in links

```yaml
"next": "/api/products?category={{query-params.category}}&page=2&limit={{query-params.limit}}"
```

---

## Strategy Comparison

| Strategy | Pros | Cons | When to Use |
|------------|------|---------|-------------|
| **Offset/Limit** | Simple, allows jumping pages | Inconsistent with changes | Traditional APIs |
| **Page/Limit** | Intuitive, easy to understand | Same offset problems | UIs with pagination |
| **Cursor** | Consistent, perfect for feeds | Can't jump, more complex | Infinite scroll, feeds |

---

## Next Steps

- **[Query Parameters](../../topics/parameters/query-parameters.md)** - Filters and search
- **[CRUD Operations](crud-operations.md)** - Complete operations
- **[Authentication Mock](authentication-mock.md)** - Simulate authentication

## See Also

- [GitHub API Pagination](https://docs.github.com/en/rest/guides/using-pagination-in-the-rest-api)
- [RFC 5988 (Link Headers)](https://tools.ietf.org/html/rfc5988)
- [Best Practices for REST API Pagination](https://www.moesif.com/blog/technical/api-design/REST-API-Design-Filtering-Sorting-and-Pagination/)
