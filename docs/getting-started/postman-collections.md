---
description: >-
  Use your existing Postman Collections directly with moclojer. No conversion needed - just export and run!
---

# Using Postman Collections

If you already have API specifications in Postman, you can use them directly with moclojer without any conversion. This is perfect when you have existing Postman Collections and want to quickly spin up a mock server.

## What is Postman Collection Support?

Moclojer can read **Postman Collection v2.1** JSON files and automatically convert them into working mock endpoints. This means you can:

- ✅ Export collections from Postman
- ✅ Use them directly with moclojer
- ✅ No manual conversion or rewriting needed
- ✅ All your response examples become live endpoints

## Quick Start

### Step 1: Export from Postman

1. Open Postman and find your collection
2. Click the **three dots (...)** next to the collection name
3. Select **Export**
4. Choose **Collection v2.1** format
5. Save the JSON file (e.g., `my-api.json`)

### Step 2: Run with moclojer

```bash
# Using environment variable
CONFIG=my-api.json moclojer

# Or using CLI parameter
moclojer --config my-api.json

# With custom port
CONFIG=my-api.json PORT=3000 moclojer
```

That's it! Your Postman Collection is now a live mock server! 🎉

## How It Works

Moclojer reads your Postman Collection and:

1. **Detects the format automatically** - No need to specify it's a Postman Collection
2. **Extracts all requests** - Both top-level and nested in folders
3. **Uses response examples** - The first saved example becomes the mock response
4. **Converts path variables** - `:id`, `:username`, etc. work automatically
5. **Includes headers** - Response headers from your examples are preserved
6. **Respects status codes** - Each example's status code is used

## Example

Let's say you have this Postman Collection:

```json
{
  "info": {
    "name": "Users API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get User",
      "request": {
        "method": "GET",
        "url": "http://localhost:8000/users/:id"
      },
      "response": [
        {
          "name": "Success",
          "code": 200,
          "header": [
            {
              "key": "Content-Type",
              "value": "application/json"
            }
          ],
          "body": "{\"id\": 1, \"name\": \"Alice\", \"email\": \"alice@example.com\"}"
        }
      ]
    }
  ]
}
```

After running `CONFIG=users-api.json moclojer`, you can:

```bash
curl http://localhost:8000/users/123
# Returns: {"id": 1, "name": "Alice", "email": "alice@example.com"}
```

## Response Examples in Postman

For moclojer to work properly, your Postman requests need **saved response examples**. Here's how to add them:

### Adding a Response Example

1. **Make a request** in Postman (or use an existing one)
2. Click the **"Save Response"** button → **"Save as Example"**
3. **Edit the example**:
   - Set the status code (200, 404, etc.)
   - Add headers if needed
   - Write the response body (JSON, XML, text, etc.)
4. **Save the example**

### Multiple Examples

If a request has multiple examples, moclojer uses the **first one**. To choose which example to use:

- Reorder examples in Postman (drag and drop)
- Or keep only the example you want

## Nested Folders

Postman Collections can organize requests into folders. Moclojer handles this automatically:

```
Users API
├── Users
│   ├── List Users (GET /users)
│   ├── Get User (GET /users/:id)
│   └── Create User (POST /users)
└── Health
    └── Health Check (GET /health)
```

All requests become endpoints, regardless of folder nesting. The folder structure is just for organization.

## Path Variables

Postman and moclojer both use the same syntax for path variables:

| Postman URL | Moclojer Path | Works? |
|-------------|---------------|---------|
| `http://localhost:8000/users/:id` | `/users/:id` | ✅ Yes |
| `http://localhost:8000/posts/:postId/comments/:commentId` | `/posts/:postId/comments/:commentId` | ✅ Yes |
| `{{baseUrl}}/users/:id` | `/users/:id` | ✅ Yes (variables ignored) |

## What's Supported

| Feature | Supported | Notes |
|---------|-----------|-------|
| HTTP Methods | ✅ GET, POST, PUT, DELETE, PATCH, etc. | All standard methods |
| Path parameters | ✅ `:id`, `:name`, etc. | Automatic conversion |
| Response status codes | ✅ 200, 404, 500, etc. | From examples |
| Response headers | ✅ All headers | From examples |
| Response body | ✅ JSON, text, etc. | From examples |
| Nested folders | ✅ Unlimited depth | Flattened to endpoints |
| Multiple requests | ✅ Unlimited | All become endpoints |
| Request body | ⚠️ Ignored | Only responses are mocked |
| Request headers | ⚠️ Ignored | Only responses are mocked |
| Pre-request scripts | ❌ Not supported | Postman-specific |
| Tests | ❌ Not supported | Postman-specific |
| Variables | ❌ Not supported | Use environment vars instead |

## Complete Example

Let's create a complete Postman Collection for a pet store:

### 1. Create in Postman

Create a collection with these requests:

**GET /pets** - List all pets
- Response example: `200 OK`
- Body: `[{"id": 1, "name": "Caramelo", "type": "dog"}]`

**GET /pets/:id** - Get one pet
- Response example: `200 OK`
- Body: `{"id": 1, "name": "Caramelo", "type": "dog"}`

**POST /pets** - Create a pet
- Response example: `201 Created`
- Body: `{"id": 2, "name": "New Pet", "type": "cat"}`

**DELETE /pets/:id** - Delete a pet
- Response example: `204 No Content`
- Body: (empty)

### 2. Export

Export as "Collection v2.1" → `petstore.json`

### 3. Run

```bash
CONFIG=petstore.json moclojer
```

### 4. Test

```bash
# List pets
curl http://localhost:8000/pets

# Get specific pet
curl http://localhost:8000/pets/123

# Create pet
curl -X POST http://localhost:8000/pets \
  -H "Content-Type: application/json" \
  -d '{"name":"Rex","type":"dog"}'

# Delete pet
curl -X DELETE http://localhost:8000/pets/123
```

## Tips and Best Practices

### ✅ Do:

- **Save response examples** for every request you want to mock
- **Use realistic data** in your examples
- **Set correct status codes** (200, 201, 404, etc.)
- **Include headers** when they matter (Content-Type, etc.)
- **Organize with folders** for clarity
- **Use descriptive names** for requests and examples

### ❌ Don't:

- **Forget response examples** - Without them, you get default `200 {}` response
- **Use Postman variables in URLs** - They won't be resolved (use full paths)
- **Rely on pre-request scripts** - They won't run in moclojer
- **Expect request validation** - moclojer doesn't validate request bodies

## Comparison with Other Formats

| Feature | Postman Collection | YAML/EDN | OpenAPI |
|---------|-------------------|----------|---------|
| **Ease of setup** | ⭐⭐⭐⭐⭐ Click and export | ⭐⭐⭐ Write by hand | ⭐⭐⭐⭐ Generate from code |
| **Visual editing** | ⭐⭐⭐⭐⭐ Postman UI | ⭐ Text editor | ⭐⭐ Various tools |
| **Dynamic responses** | ❌ Static examples | ✅ Full templating | ⚠️ Requires mocks file |
| **Team collaboration** | ⭐⭐⭐⭐⭐ Built for teams | ⭐⭐⭐ Version control | ⭐⭐⭐⭐ Standard format |
| **Best for** | Quick mocks from existing collections | Custom logic and templates | API-first design |

## Next Steps

Now that you know how to use Postman Collections with moclojer:

- **Need dynamic responses?** See [Dynamic Responses](dynamic-responses.md) to learn about templates
- **Want to write YAML instead?** See [Your First Mock Server](your-first-mock.md)
- **Looking for advanced features?** Check [Advanced Features](../advanced/) section

## Troubleshooting

### "No endpoints found"

**Problem**: moclojer starts but no endpoints work

**Solution**: Make sure your Postman requests have saved response examples

---

### "Getting 200 with empty {}"

**Problem**: Endpoints return `{"status": 200, "body": "{}"}`

**Solution**: Add response examples to your Postman requests

---

### "Path variables not working"

**Problem**: `/users/:id` always returns the same data

**Solution**: This is expected with Postman Collections. They use static examples. For dynamic responses based on path variables, use [YAML format with templates](dynamic-responses.md)

---

**Ready to create your first mock?** Export a Postman Collection and give it a try! 🚀
