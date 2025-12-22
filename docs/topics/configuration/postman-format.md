---
description: >-
  Use your Postman Collections directly with moclojer. Import Collections
  v2.1, leverage response examples and create mocks without rewriting anything.
---

# Postman Collection Format

moclojer supports **Postman Collection v2.1**, allowing you to use your existing collections to generate mocks automatically. No need to rewrite anything!

## What Are Postman Collections?

**Postman Collections** are JSON files that group HTTP requests, widely used for:

- API documentation
- API testing
- Team sharing
- **Mock servers** (like moclojer!)

## Why Use Postman Collections?

✅ **Reusability**: Use existing Postman collections
✅ **Zero config**: moclojer converts automatically
✅ **Ready examples**: Responses already documented
✅ **Easy export**: Direct export from Postman App
✅ **Popular standard**: Whole team already uses Postman

---

## Support in moclojer

### Supported Versions

- ✅ Postman Collection v2.1 (recommended)
- ⚠️ Postman Collection v2.0 (partial support)

### Accepted Formats

- ✅ JSON (`.json`)

---

## Quick Start

### 1. Export from Postman

**In Postman App:**

1. Open your Collection
2. Click `...` (three dots)
3. **Export** → Collection v2.1
4. Save as `postman_collection.json`

### 2. Run with moclojer

```bash
moclojer --config postman_collection.json
```

### 3. Test

```bash
# moclojer creates endpoints based on collection requests
curl http://localhost:8000/users
curl http://localhost:8000/users/1
```

🎉 **It worked!** Without writing YAML!

---

## How moclojer Converts Postman Collections

### Basic Structure

**Postman Collection:**

```json
{
  "info": {
    "name": "Users API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get Users",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/users"
      },
      "response": [
        {
          "name": "Success",
          "status": "OK",
          "code": 200,
          "header": [
            {
              "key": "Content-Type",
              "value": "application/json"
            }
          ],
          "body": "[{\"id\":1,\"name\":\"Alice\"},{\"id\":2,\"name\":\"Bob\"}]"
        }
      ]
    }
  ]
}
```

**moclojer converts to:**

```yaml
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        [
          {"id": 1, "name": "Alice"},
          {"id": 2, "name": "Bob"}
        ]
```

### Path Variables

**Postman:**

```json
{
  "request": {
    "method": "GET",
    "url": {
      "raw": "{{baseUrl}}/users/:id",
      "path": ["users", ":id"],
      "variable": [
        {
          "key": "id",
          "value": "1"
        }
      ]
    }
  }
}
```

**Converted to:** `/users/:id`

### Query Parameters

**Postman:**

```json
{
  "request": {
    "method": "GET",
    "url": {
      "raw": "{{baseUrl}}/products?category=electronics&limit=10",
      "query": [
        {"key": "category", "value": "electronics"},
        {"key": "limit", "value": "10"}
      ]
    }
  }
}
```

**moclojer accepts:** `/products?category=electronics&limit=10`

### Request Body

**Postman:**

```json
{
  "request": {
    "method": "POST",
    "url": "{{baseUrl}}/users",
    "body": {
      "mode": "raw",
      "raw": "{\"name\":\"Alice\",\"email\":\"alice@example.com\"}",
      "options": {
        "raw": {
          "language": "json"
        }
      }
    }
  }
}
```

**moclojer uses the response example:**

```json
{
  "response": [
    {
      "body": "{\"id\":1,\"name\":\"Alice\",\"email\":\"alice@example.com\"}"
    }
  ]
}
```

---

## Complete Example

### Postman Collection

```json
{
  "info": {
    "name": "E-commerce API",
    "description": "API for e-commerce platform",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8000",
      "type": "string"
    }
  ],
  "item": [
    {
      "name": "Products",
      "item": [
        {
          "name": "List Products",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/products?category={{category}}&limit={{limit}}",
              "host": ["{{baseUrl}}"],
              "path": ["products"],
              "query": [
                {"key": "category", "value": "electronics"},
                {"key": "limit", "value": "10"}
              ]
            }
          },
          "response": [
            {
              "name": "Success",
              "status": "OK",
              "code": 200,
              "header": [
                {
                  "key": "Content-Type",
                  "value": "application/json"
                }
              ],
              "body": "{\"products\":[{\"id\":1,\"name\":\"Laptop\",\"price\":999.99},{\"id\":2,\"name\":\"Mouse\",\"price\":29.99}],\"total\":100}"
            }
          ]
        },
        {
          "name": "Get Product",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/products/:id",
              "host": ["{{baseUrl}}"],
              "path": ["products", ":id"],
              "variable": [
                {
                  "key": "id",
                  "value": "1"
                }
              ]
            }
          },
          "response": [
            {
              "name": "Success",
              "status": "OK",
              "code": 200,
              "body": "{\"id\":1,\"name\":\"Laptop\",\"price\":999.99,\"description\":\"High-performance laptop\"}"
            }
          ]
        },
        {
          "name": "Create Product",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/products",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"name\":\"New Product\",\"price\":49.99}"
            }
          },
          "response": [
            {
              "name": "Created",
              "status": "Created",
              "code": 201,
              "body": "{\"id\":3,\"name\":\"New Product\",\"price\":49.99,\"createdAt\":\"2024-01-15T10:00:00Z\"}"
            }
          ]
        }
      ]
    },
    {
      "name": "Users",
      "item": [
        {
          "name": "Get Users",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/users"
          },
          "response": [
            {
              "name": "Success",
              "code": 200,
              "body": "[{\"id\":1,\"name\":\"Alice\"},{\"id\":2,\"name\":\"Bob\"}]"
            }
          ]
        }
      ]
    }
  ]
}
```

### Run

```bash
moclojer --config ecommerce_collection.json
```

### Test

```bash
# List products
curl "http://localhost:8000/products?category=electronics&limit=10"

# Specific product
curl http://localhost:8000/products/1

# Create product
curl -X POST http://localhost:8000/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Mouse", "price": 29.99}'

# List users
curl http://localhost:8000/users
```

---

## Nested Folders

Postman Collections support nested folders:

```json
{
  "item": [
    {
      "name": "API v1",
      "item": [
        {
          "name": "Users",
          "item": [
            {
              "name": "List Users",
              "request": {
                "method": "GET",
                "url": "/v1/users"
              }
            }
          ]
        },
        {
          "name": "Products",
          "item": [
            {
              "name": "List Products",
              "request": {
                "method": "GET",
                "url": "/v1/products"
              }
            }
          ]
        }
      ]
    }
  ]
}
```

**moclojer processes recursively** all folder levels.

---

## Variables

### Collection Variables

```json
{
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8000"
    },
    {
      "key": "apiVersion",
      "value": "v1"
    }
  ],
  "item": [
    {
      "request": {
        "url": "{{baseUrl}}/{{apiVersion}}/users"
      }
    }
  ]
}
```

**moclojer resolves variables automatically:**

- `{{baseUrl}}` → ignores (uses moclojer host)
- `{{apiVersion}}` → `/v1/users`

### Environment Variables

**Postman Environments** are not directly supported.

**Workaround:** Export collection with values already resolved:

1. In Postman, select Environment
2. Export Collection (values will be inline)

---

## Headers

### Request Headers

```json
{
  "request": {
    "header": [
      {
        "key": "Authorization",
        "value": "Bearer token123"
      },
      {
        "key": "Content-Type",
        "value": "application/json"
      },
      {
        "key": "X-Custom-Header",
        "value": "custom-value",
        "disabled": false
      }
    ]
  }
}
```

**moclojer:** Request headers are informational (not validated).

### Response Headers

```json
{
  "response": [
    {
      "header": [
        {
          "key": "Content-Type",
          "value": "application/json"
        },
        {
          "key": "X-RateLimit-Limit",
          "value": "100"
        }
      ]
    }
  ]
}
```

**moclojer adds these headers to the response.**

---

## Multiple Responses (Examples)

Postman allows multiple examples per request:

```json
{
  "name": "Get User",
  "request": {
    "method": "GET",
    "url": "/users/:id"
  },
  "response": [
    {
      "name": "Success",
      "code": 200,
      "body": "{\"id\":1,\"name\":\"Alice\"}"
    },
    {
      "name": "Not Found",
      "code": 404,
      "body": "{\"error\":\"User not found\"}"
    },
    {
      "name": "Server Error",
      "code": 500,
      "body": "{\"error\":\"Internal server error\"}"
    }
  ]
}
```

**moclojer uses the first example** (usually 200 OK).

**To simulate errors:** Create separate requests

```json
{
  "name": "Get User - Not Found",
  "request": {
    "url": "/users/999"
  },
  "response": [
    {
      "code": 404,
      "body": "{\"error\":\"User not found\"}"
    }
  ]
}
```

---

## Scripts and Tests

Postman Collections can have Pre-request and Test scripts:

```json
{
  "event": [
    {
      "listen": "prerequest",
      "script": {
        "exec": [
          "pm.variables.set('timestamp', Date.now());"
        ]
      }
    },
    {
      "listen": "test",
      "script": {
        "exec": [
          "pm.test('Status is 200', () => {",
          "  pm.response.to.have.status(200);",
          "});"
        ]
      }
    }
  ]
}
```

**moclojer ignores scripts.** They are for execution in Postman, not mock.

---

## Authentication

### Bearer Token

```json
{
  "auth": {
    "type": "bearer",
    "bearer": [
      {
        "key": "token",
        "value": "{{accessToken}}"
      }
    ]
  }
}
```

**moclojer:** Auth is informational (doesn't validate tokens).

### API Key

```json
{
  "auth": {
    "type": "apikey",
    "apikey": [
      {
        "key": "in",
        "value": "header"
      },
      {
        "key": "key",
        "value": "X-API-Key"
      },
      {
        "key": "value",
        "value": "{{apiKey}}"
      }
    ]
  }
}
```

---

## Limitations and Workarounds

### 1. Multiple Responses

**Limitation:** moclojer uses only the first `response`.

**Workaround:** Create separate requests for each scenario:

```json
// Request 1: Success
{"name": "Get User - Success", "url": "/users/1", "response": [{"code": 200}]}

// Request 2: Not Found
{"name": "Get User - Not Found", "url": "/users/999", "response": [{"code": 404}]}
```

### 2. Scripts Not Executed

**Limitation:** Pre-request and Test scripts are ignored.

**Workaround:** Use scripts only in Postman, not for mock logic.

### 3. Environment Variables

**Limitation:** Environments are not loaded.

**Workaround:** Export collection with inline values.

### 4. Request Validation

**Limitation:** moclojer does not validate if request body is correct.

**Workaround:** Use tools like Postman Runner or Newman for validation.

---

## Postman vs Native YAML

| Aspect | Postman Collection | moclojer YAML |
|---------|-------------------|---------------|
| **Reusability** | Existing collections | Need to create from scratch |
| **Tools** | Complete Postman App | Text editor |
| **Verbosity** | Verbose JSON | Concise YAML |
| **Dynamic** | Static examples | Dynamic templates |
| **Learning curve** | Higher (Postman) | Lower (YAML) |
| **Sharing** | Easy (workspace) | Git files |

**When to use Postman:**

- Already have ready collections
- Team uses Postman daily
- Want visual interface
- Need rich documentation

**When to use YAML:**

- Want dynamic templates (`{{path-params.id}}`)
- Need minimalist config
- Version with Git
- CI/CD automation

---

## Converting Postman → moclojer YAML

If you want to convert permanently:

**Option 1: Manual**

1. Export Postman Collection
2. Read JSON and rewrite in YAML

**Option 2: Script (create one)**

```javascript
// postman-to-moclojer.js
const collection = require('./collection.json');

const endpoints = collection.item.map(item => ({
  endpoint: {
    method: item.request.method,
    path: extractPath(item.request.url),
    response: {
      status: item.response[0].code,
      body: item.response[0].body
    }
  }
}));

console.log(YAML.stringify(endpoints));
```

---

## Best Practices

### ✅ Do

1. **Use response examples**

   ```json
   {
     "response": [
       {
         "name": "Success Example",
         "code": 200,
         "body": "..."  // ← Always include!
       }
     ]
   }
   ```

2. **Organize with folders**

   ```json
   {
     "item": [
       {
         "name": "Users",
         "item": [...]  // Group by resource
       },
       {
         "name": "Products",
         "item": [...]
       }
     ]
   }
   ```

3. **Document requests**

   ```json
   {
     "name": "Get User by ID",
     "request": {
       "description": "Retrieves a single user by their unique ID"
     }
   }
   ```

4. **Versioning in name**

   ```json
   {
     "info": {
       "name": "My API v2.1",
       "version": "2.1.0"
     }
   }
   ```

### ❌ Avoid

1. **Collections without examples**

   ```json
   // ❌ moclojer doesn't know what to return
   {
     "response": []
   }
   ```

2. **Hardcoded absolute URLs**

   ```json
   // ❌ Use variables
   "url": "http://localhost:8000/users"

   // ✅ Use {{baseUrl}}
   "url": "{{baseUrl}}/users"
   ```

3. **Script dependency**

   ```json
   // ❌ Scripts don't run in moclojer
   "event": [{"script": {"exec": ["..."]}}]
   ```

---

## Troubleshooting

### "Invalid Postman Collection format"

**Cause:** Unsupported version or invalid JSON.

**Solution:**

```bash
# Validate JSON
cat postman_collection.json | jq .

# Check schema version
cat postman_collection.json | jq '.info.schema'
# Should be v2.1.0
```

### "No responses found"

**Cause:** Requests without response examples.

**Solution:** Add examples in Postman:

1. Make request in Postman
2. Click "Save Response" → "Save as Example"
3. Re-export Collection

### "Path variables not working"

**Cause:** Incorrect syntax.

**Solution:** Use `:paramName` in Postman

```json
{
  "url": {
    "path": ["users", ":id"]  // ✅ Correct
  }
}
```

---

## Next Steps

- **[OpenAPI Format](openapi-format.md)** - Import OpenAPI specs
- **[YAML Format](yaml-format.md)** - Native moclojer syntax
- **[Path Parameters](../parameters/path-parameters.md)** - Dynamic parameters
- **[Your First Mock](../../getting-started/postman-collections.md)** - Initial tutorial

## See Also

- [Postman Collection Format v2.1](https://schema.postman.com/json/collection/v2.1.0/docs/index.html)
- [Postman Documentation](https://learning.postman.com/docs/getting-started/introduction/)
- [Configuration Spec](../../reference/configuration-spec.md) - moclojer reference
