---
description: >-
  Learn about moclojer's powerful template system that allows you to create dynamic
  responses based on request data, making your mocks more realistic and flexible.
---

# Template System Overview

moclojer's template system is what makes your mock APIs dynamic and realistic. Instead of returning the same static response every time, templates let you create responses that change based on the incoming request data.

## What are templates?

Templates are special placeholders in your response configuration that get replaced with actual values when a request is processed. They use a simple `{{variable}}` syntax that's easy to read and write.

**Example:**
```yaml
- endpoint:
    method: GET
    path: /users/:id
    response:
      body: >
        {
          "id": "{{path-params.id}}",
          "name": "User {{path-params.id}}",
          "timestamp": "{{now}}"
        }
```

When someone requests `/users/123`, the response becomes:
```json
{
  "id": "123",
  "name": "User 123",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Why use templates?

### 🎯 **Realistic Testing**
Simulate how real APIs behave by returning different data based on the request:

```yaml
- endpoint:
    method: GET
    path: /products/:category
    response:
      body: >
        {
          "category": "{{path-params.category}}",
          "products": [
            {"id": 1, "name": "{{path-params.category}} Product 1"},
            {"id": 2, "name": "{{path-params.category}} Product 2"}
          ]
        }
```

### 🔄 **Dynamic Responses**
Create responses that reflect the input data:

```yaml
- endpoint:
    method: POST
    path: /orders
    response:
      body: >
        {
          "order_id": "ORD-{{json-params.user_id}}-{{now}}",
          "user_id": {{json-params.user_id}},
          "total": {{json-params.total}},
          "status": "confirmed"
        }
```

### 🧪 **Flexible Testing**
Test different scenarios without creating multiple endpoint configurations:

```yaml
- endpoint:
    method: GET
    path: /api/status
    response:
      body: >
        {
          "environment": "{{query-params.env}}",
          "debug": {{query-params.debug}},
          "timestamp": "{{now}}"
        }
```

## Template Variables

moclojer provides several types of template variables:

### Path Parameters
Extract values from URL paths:
```yaml
path: /users/:id/posts/:postId
body: >
  {
    "user_id": "{{path-params.id}}",
    "post_id": "{{path-params.postId}}"
  }
```

### Query Parameters
Use URL query string values:
```yaml
# For request: /search?q=javascript&limit=10
body: >
  {
    "query": "{{query-params.q}}",
    "limit": {{query-params.limit}},
    "results": []
  }
```

### JSON Body Parameters
Access data from JSON request bodies:
```yaml
# For POST with body: {"name": "John", "email": "john@example.com"}
body: >
  {
    "id": 123,
    "name": "{{json-params.name}}",
    "email": "{{json-params.email}}",
    "created_at": "{{now}}"
  }
```

### Request Headers
Use values from HTTP headers:
```yaml
body: >
  {
    "user_agent": "{{header-params.User-Agent}}",
    "authorization": "{{header-params.Authorization}}",
    "content_type": "{{header-params.Content-Type}}"
  }
```

### Built-in Functions
Special functions for common needs:
```yaml
body: >
  {
    "timestamp": "{{now}}",
    "random_id": "{{uuid}}",
    "server_time": "{{now}}"
  }
```

## Common Patterns

### Echo Responses
Return the same data that was sent:
```yaml
- endpoint:
    method: POST
    path: /echo
    response:
      body: >
        {
          "received": {
            "name": "{{json-params.name}}",
            "email": "{{json-params.email}}"
          },
          "processed_at": "{{now}}"
        }
```

### Personalized Responses
Create user-specific responses:
```yaml
- endpoint:
    method: GET
    path: /profile/:username
    response:
      body: >
        {
          "username": "{{path-params.username}}",
          "welcome_message": "Hello, {{path-params.username}}!",
          "last_login": "{{now}}"
        }
```

### Search Results
Dynamic search responses:
```yaml
- endpoint:
    method: GET
    path: /search
    response:
      body: >
        {
          "query": "{{query-params.q}}",
          "page": {{query-params.page}},
          "results": [
            {"title": "Result 1 for {{query-params.q}}"},
            {"title": "Result 2 for {{query-params.q}}"}
          ]
        }
```

### API Keys and Authentication
Handle authentication scenarios:
```yaml
- endpoint:
    method: GET
    path: /protected
    response:
      body: >
        {
          "user": "authenticated",
          "api_key": "{{query-params.api_key}}",
          "access_level": "full"
        }
```

## Template Best Practices

### 1. **Use Meaningful Names**
```yaml
# Good
path: /users/:userId/orders/:orderId

# Less clear
path: /users/:id1/orders/:id2
```

### 2. **Handle Missing Values**
Provide defaults for optional parameters:
```yaml
body: >
  {
    "page": {{query-params.page}},
    "limit": {{query-params.limit}},
    "default_message": "Page {{query-params.page}} with {{query-params.limit}} items"
  }
```

### 3. **Keep Templates Readable**
Break complex templates into logical sections:
```yaml
body: >
  {
    "user": {
      "id": "{{path-params.id}}",
      "name": "{{json-params.name}}"
    },
    "metadata": {
      "created_at": "{{now}}",
      "source": "{{header-params.User-Agent}}"
    }
  }
```

### 4. **Use Consistent Formatting**
Follow JSON formatting rules:
```yaml
# Strings need quotes
"name": "{{json-params.name}}"

# Numbers don't need quotes
"count": {{query-params.count}}

# Booleans don't need quotes
"active": {{json-params.active}}
```

## Advanced Template Features

### Nested Object Access
Access nested properties from JSON:
```yaml
# For body: {"user": {"profile": {"name": "John"}}}
body: >
  {
    "welcome": "Hello, {{json-params.user.profile.name}}!"
  }
```

### Conditional Content
Simple conditional logic:
```yaml
body: >
  {
    "message": "{{#if query-params.admin}}Admin access granted{{else}}Regular user{{/if}}"
  }
```

### Array Handling
Work with array data:
```yaml
# For body: {"tags": ["javascript", "tutorial"]}
body: >
  {
    "first_tag": "{{json-params.tags.0}}",
    "tag_count": {{json-params.tags.length}}
  }
```

## Error Handling

Templates gracefully handle missing data:

```yaml
- endpoint:
    method: GET
    path: /user-info
    response:
      body: >
        {
          "user_id": "{{query-params.id}}",
          "note": "If id parameter is missing, it shows as empty string"
        }
```

## What's Next?

Now that you understand the template system basics, dive deeper into specific areas:

- **[Template Variables](template-variables.md)** - Complete reference of all available variables
- **[Dynamic Content](dynamic-content.md)** - Advanced techniques for dynamic responses  
- **[Advanced Templating](advanced-templating.md)** - Complex templating patterns and best practices

## Examples in Action

See templates in real-world scenarios:
- **[REST API Example](../../examples/rest-api-mocking.md)** - Complete API with templates
- **[E-commerce API](../../examples/ecommerce-api.md)** - Product catalog with dynamic data
- **[User Management](../../examples/user-management.md)** - User CRUD operations

## Need Help?

- **[Template Variables Reference](template-variables.md)** - All available template variables
- **[FAQ](../../reference/faq.md)** - Common template questions
- **[GitHub Discussions](https://github.com/moclojer/moclojer/discussions)** - Community help