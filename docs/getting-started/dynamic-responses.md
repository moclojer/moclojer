---
description: >-
  Learn how to create dynamic responses that change based on request data using moclojer's
  powerful template system. Make your mocks more realistic and flexible.
---

# Dynamic Responses

In the previous tutorial, you created static endpoints that always return the same response. Now you'll learn how to make your responses dynamic - changing based on the data sent in the request. This makes your mocks much more realistic and useful for testing.

## What you'll learn

- How to use path parameters in URLs
- How to capture and use query parameters
- How to access JSON data from request bodies
- How to use template variables to create dynamic content
- How to test dynamic responses

## What you'll build

An enhanced user API that responds dynamically to different inputs:
- `GET /users/:id` - Returns user data based on the ID in the URL
- `GET /users` - Filters users based on query parameters
- `POST /users` - Creates users with data from the request body
- `GET /search` - Search functionality with dynamic results

## Prerequisites

- Completed the [Your First Mock Server](your-first-mock.md) tutorial
- moclojer running on your system
- Basic understanding of HTTP requests

## Step 1: Understanding template variables

Template variables are placeholders in your responses that get replaced with actual data from the request. They use the `{{variable}}` syntax.

**Available template variables:**
- `{{path-params.name}}` - Values from URL path (e.g., `:id`, `:username`)
- `{{query-params.name}}` - Values from query string (e.g., `?search=value`)
- `{{json-params.name}}` - Values from JSON request body
- `{{header-params.name}}` - Values from HTTP headers

## Step 2: Path parameters

Path parameters let you capture parts of the URL and use them in your response.

Create a new `moclojer.yml` file:

```yaml
- endpoint:
    method: GET
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": "{{path-params.id}}",
          "name": "User {{path-params.id}}",
          "email": "user{{path-params.id}}@example.com",
          "created_at": "2024-01-15T10:30:00Z"
        }

- endpoint:
    method: GET
    path: /users/:id/profile/:section
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "user_id": "{{path-params.id}}",
          "section": "{{path-params.section}}",
          "data": {
            "title": "{{path-params.section}} for User {{path-params.id}}",
            "last_updated": "2024-01-15T10:30:00Z"
          }
        }
```

**Test it:**

Start your server and try these requests:

```bash
# Different user IDs
curl http://localhost:8000/users/123
curl http://localhost:8000/users/456

# Different profile sections
curl http://localhost:8000/users/123/profile/settings
curl http://localhost:8000/users/456/profile/preferences
```

Notice how the responses change based on the values in the URL!

## Step 3: Query parameters

Query parameters come after the `?` in URLs and let you pass additional data.

Add this endpoint to your configuration:

```yaml
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "filters": {
            "role": "{{query-params.role}}",
            "department": "{{query-params.department}}",
            "active": "{{query-params.active}}"
          },
          "pagination": {
            "page": "{{query-params.page}}",
            "limit": "{{query-params.limit}}"
          },
          "users": [
            {
              "id": 1,
              "name": "Alice Johnson",
              "role": "{{query-params.role}}",
              "department": "{{query-params.department}}"
            },
            {
              "id": 2,
              "name": "Bob Smith",
              "role": "{{query-params.role}}",
              "department": "{{query-params.department}}"
            }
          ]
        }

- endpoint:
    method: GET
    path: /search
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "query": "{{query-params.q}}",
          "category": "{{query-params.category}}",
          "results": [
            {
              "id": 1,
              "title": "Result 1 for '{{query-params.q}}'",
              "category": "{{query-params.category}}"
            },
            {
              "id": 2,
              "title": "Result 2 for '{{query-params.q}}'",
              "category": "{{query-params.category}}"
            }
          ],
          "total": 2
        }
```

**Test it:**

```bash
# Filter users by role and department
curl "http://localhost:8000/users?role=admin&department=engineering"

# Search with different queries
curl "http://localhost:8000/search?q=javascript&category=tutorials"
curl "http://localhost:8000/search?q=python&category=examples"

# Pagination
curl "http://localhost:8000/users?page=2&limit=10"
```

## Step 4: JSON body parameters

For POST, PUT, and PATCH requests, you can access data from the JSON request body.

Add these endpoints:

```yaml
- endpoint:
    method: POST
    path: /users
    response:
      status: 201
      headers:
        Content-Type: application/json
      body: >
        {
          "id": 12345,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "role": "{{json-params.role}}",
          "department": "{{json-params.department}}",
          "created_at": "2024-01-15T10:30:00Z",
          "message": "User '{{json-params.name}}' created successfully"
        }

- endpoint:
    method: PUT
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": "{{path-params.id}}",
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "role": "{{json-params.role}}",
          "updated_at": "2024-01-15T10:30:00Z",
          "message": "User {{path-params.id}} updated with name '{{json-params.name}}'"
        }

- endpoint:
    method: POST
    path: /orders
    response:
      status: 201
      headers:
        Content-Type: application/json
      body: >
        {
          "order_id": "ORD-{{json-params.user_id}}-12345",
          "user_id": "{{json-params.user_id}}",
          "items": [
            {
              "product": "{{json-params.product}}",
              "quantity": "{{json-params.quantity}}",
              "price": "{{json-params.price}}"
            }
          ],
          "total": "{{json-params.price}}",
          "status": "confirmed",
          "created_at": "2024-01-15T10:30:00Z"
        }
```

**Test it:**

```bash
# Create a new user
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "role": "developer",
    "department": "engineering"
  }' \
  http://localhost:8000/users

# Update a user
curl -X PUT \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith",
    "email": "johnsmith@example.com",
    "role": "senior developer"
  }' \
  http://localhost:8000/users/123

# Create an order
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "123",
    "product": "Laptop",
    "quantity": 1,
    "price": "999.99"
  }' \
  http://localhost:8000/orders
```

## Step 5: Header parameters

You can also access HTTP headers in your responses:

```yaml
- endpoint:
    method: GET
    path: /profile
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "user_agent": "{{header-params.User-Agent}}",
          "authorization": "{{header-params.Authorization}}",
          "content_type": "{{header-params.Content-Type}}",
          "custom_header": "{{header-params.X-Custom-Header}}",
          "message": "Headers received successfully"
        }
```

**Test it:**

```bash
curl -H "Authorization: Bearer token123" \
     -H "X-Custom-Header: custom-value" \
     http://localhost:8000/profile
```

## Step 6: Combining multiple parameters

Real APIs often use multiple types of parameters. Here's a comprehensive example:

```yaml
- endpoint:
    method: POST
    path: /api/:version/users/:id/messages
    response:
      status: 201
      headers:
        Content-Type: application/json
      body: >
        {
          "message_id": "MSG-{{path-params.id}}-{{json-params.recipient_id}}",
          "api_version": "{{path-params.version}}",
          "sender_id": "{{path-params.id}}",
          "recipient_id": "{{json-params.recipient_id}}",
          "subject": "{{json-params.subject}}",
          "body": "{{json-params.message}}",
          "priority": "{{query-params.priority}}",
          "user_agent": "{{header-params.User-Agent}}",
          "created_at": "2024-01-15T10:30:00Z",
          "status": "sent"
        }
```

**Test it:**

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -H "User-Agent: MyApp/1.0" \
  -d '{
    "recipient_id": "456",
    "subject": "Hello World",
    "message": "This is a test message"
  }' \
  "http://localhost:8000/api/v1/users/123/messages?priority=high"
```

## Step 7: Handling missing parameters

moclojer gracefully handles missing parameters by showing them as empty strings:

```yaml
- endpoint:
    method: GET
    path: /debug
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "path_param": "{{path-params.missing}}",
          "query_param": "{{query-params.missing}}",
          "note": "Missing parameters appear as empty strings"
        }
```

## Understanding data types

When using template variables, remember:

- **Strings need quotes**: `"name": "{{json-params.name}}"`
- **Numbers don't need quotes**: `"age": {{json-params.age}}`
- **Booleans don't need quotes**: `"active": {{json-params.active}}`

**Example with mixed types:**

```yaml
- endpoint:
    method: POST
    path: /products
    response:
      status: 201
      body: >
        {
          "name": "{{json-params.name}}",
          "price": {{json-params.price}},
          "in_stock": {{json-params.in_stock}},
          "category": "{{json-params.category}}"
        }
```

## Your complete dynamic configuration

Here's your complete `moclojer.yml` with all the dynamic examples:

```yaml
# Path parameters
- endpoint:
    method: GET
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": "{{path-params.id}}",
          "name": "User {{path-params.id}}",
          "email": "user{{path-params.id}}@example.com",
          "created_at": "2024-01-15T10:30:00Z"
        }

# Query parameters
- endpoint:
    method: GET
    path: /search
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "query": "{{query-params.q}}",
          "category": "{{query-params.category}}",
          "results": [
            {
              "id": 1,
              "title": "Result 1 for '{{query-params.q}}'",
              "category": "{{query-params.category}}"
            }
          ]
        }

# JSON body parameters
- endpoint:
    method: POST
    path: /users
    response:
      status: 201
      headers:
        Content-Type: application/json
      body: >
        {
          "id": 12345,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "created_at": "2024-01-15T10:30:00Z",
          "message": "User '{{json-params.name}}' created successfully"
        }

# Combined parameters
- endpoint:
    method: POST
    path: /api/:version/users/:id/messages
    response:
      status: 201
      headers:
        Content-Type: application/json
      body: >
        {
          "message_id": "MSG-{{path-params.id}}-{{json-params.recipient_id}}",
          "api_version": "{{path-params.version}}",
          "sender_id": "{{path-params.id}}",
          "recipient_id": "{{json-params.recipient_id}}",
          "subject": "{{json-params.subject}}",
          "priority": "{{query-params.priority}}",
          "created_at": "2024-01-15T10:30:00Z"
        }

# Header parameters
- endpoint:
    method: GET
    path: /profile
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "user_agent": "{{header-params.User-Agent}}",
          "authorization": "{{header-params.Authorization}}",
          "message": "Headers received successfully"
        }
```

## What you've accomplished

✅ **Path parameters** - Capture values from URLs  
✅ **Query parameters** - Use URL query strings  
✅ **JSON body parameters** - Access request body data  
✅ **Header parameters** - Read HTTP headers  
✅ **Combined parameters** - Use multiple parameter types together  
✅ **Data type handling** - Work with strings, numbers, and booleans

## Next steps

Your mock APIs are now much more realistic and flexible! In the next tutorial, you'll learn how to organize multiple endpoints and create a complete API with proper structure.

👉 **[Multiple Endpoints Tutorial](multiple-endpoints.md)** - Learn to organize and structure larger APIs

## Need help?

- **Confused about templates?** See [Template System Overview](../topics/templates/template-system.md)
- **Want more examples?** Check [Common Patterns](../examples/common-patterns.md)
- **Have questions?** Join the [community discussions](https://github.com/moclojer/moclojer/discussions)