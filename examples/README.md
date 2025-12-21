# Moclojer Examples

This directory contains example configurations for moclojer in different formats.

## Postman Collection Example

The `petstore-postman-collection.json` file is a complete Postman Collection v2.1 example that can be used directly with moclojer.

### How to use:

**1. Run with moclojer:**

```bash
# Using environment variable
CONFIG=examples/petstore-postman-collection.json moclojer

# Using CLI parameter
moclojer --config examples/petstore-postman-collection.json

# With custom port
CONFIG=examples/petstore-postman-collection.json PORT=3000 moclojer
```

**2. Test the endpoints:**

```bash
# List all pets
curl http://localhost:8000/pets

# Get specific pet
curl http://localhost:8000/pets/1

# Create new pet
curl -X POST http://localhost:8000/pets \
  -H "Content-Type: application/json" \
  -d '{"name":"Rex","type":"dog","age":2}'

# Update pet
curl -X PUT http://localhost:8000/pets/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Pet","age":4}'

# Delete pet
curl -X DELETE http://localhost:8000/pets/1

# Health check
curl http://localhost:8000/health
```

### Features demonstrated:

- ✅ HTTP methods: GET, POST, PUT, DELETE
- ✅ Path parameters (`:id`)
- ✅ Response examples with status codes
- ✅ Custom headers
- ✅ Nested folders for organization
- ✅ Different response formats

### Creating your own Postman Collection for moclojer:

1. **In Postman**, create or use an existing collection
2. Add **response examples** to your requests:
   - Click on a request
   - Click "Save Response" → "Save as Example"
   - Add status code, headers, and body
3. **Export the collection**:
   - Right-click on collection → Export
   - Select "Collection v2.1"
   - Save the JSON file
4. **Use with moclojer**:
   - `CONFIG=your-collection.json moclojer`

### Tips:

- Moclojer uses the **first response example** from each request
- Path variables (`:id`, `:username`, etc.) are automatically detected
- Nested folders are flattened - all requests become endpoints
- Disabled headers in Postman are ignored by moclojer
- If no response example exists, moclojer returns `200 OK` with empty JSON `{}`

## Server Examples

The `server/` directory contains examples of using moclojer as a library in Clojure applications.

See [server/README.md](server/README.md) for more details.
