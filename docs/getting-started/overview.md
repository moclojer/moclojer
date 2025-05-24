---
description: >-
  Learn what moclojer is, why you'd want to use it, and how it can help you build
  better applications with mock APIs.
---

# What is moclojer?

**moclojer** is a simple and efficient HTTP mock server that helps you simulate APIs for development, testing, and prototyping. Instead of depending on real APIs that might be slow, unreliable, or not yet built, moclojer lets you create fake APIs that respond exactly how you need them to.

## Why use moclojer?

### 🚀 **Faster Development**
Start building your frontend or application immediately, even when the backend APIs aren't ready yet. No more waiting for other teams or services.

### 🧪 **Better Testing**
Create predictable test scenarios by controlling exactly what your API returns. Test error cases, edge conditions, and different response formats easily.

### 🔧 **Simple Configuration**
Define your mock APIs using simple YAML files. No complex setup, no programming required for basic mocks.

### 📱 **Realistic Simulations**
Support for dynamic responses, path parameters, query parameters, WebSockets, webhooks, and more advanced features.

## How does it work?

moclojer reads configuration files (YAML, EDN, or OpenAPI) that describe your API endpoints and their responses. When a request comes in, it matches the request to your configuration and returns the appropriate response.

**Simple example:**

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
          "name": "John Doe",
          "email": "john@example.com"
        }
```

When someone makes a `GET` request to `/users/123`, moclojer responds with:

```json
{
  "id": "123",
  "name": "John Doe",
  "email": "john@example.com"
}
```

Notice how `{{path-params.id}}` automatically gets replaced with the actual ID from the URL.

## What can you do with moclojer?

- **REST API Mocking** - Complete CRUD operations with dynamic responses
- **GraphQL Mocking** - Mock GraphQL endpoints and resolvers
- **WebSocket Support** - Real-time bidirectional communication
- **Webhook Simulation** - Trigger background requests after handling requests
- **Rate Limiting** - Simulate API limits and throttling
- **Multi-Domain Support** - Handle multiple domains in one configuration
- **External Data** - Load responses from files or remote URLs
- **Integration Testing** - Use as a library in your Clojure applications

## Common use cases

### Frontend Development
Mock your backend APIs so frontend developers can work independently:

```yaml
- endpoint:
    method: GET
    path: /api/posts
    response:
      body: >
        [
          {"id": 1, "title": "First Post", "author": "Alice"},
          {"id": 2, "title": "Second Post", "author": "Bob"}
        ]
```

### API Testing
Create specific test scenarios including error conditions:

```yaml
- endpoint:
    method: POST
    path: /api/users
    response:
      status: 422
      body: >
        {
          "error": "Validation failed",
          "details": ["Email is required", "Password too short"]
        }
```

### Microservices Development
Mock dependencies between services:

```yaml
- endpoint:
    host: payment-service.local
    method: POST
    path: /charge
    response:
      status: 200
      body: >
        {
          "transaction_id": "txn_{{query-params.amount}}_{{path-params.user_id}}",
          "status": "success"
        }
```

### Third-Party API Simulation
Mock external APIs for development and testing:

```yaml
- endpoint:
    method: GET
    path: /api/weather
    response:
      body: >
        {
          "city": "{{query-params.city}}",
          "temperature": 22,
          "condition": "sunny"
        }
```

## Who uses moclojer?

- **Frontend Developers** - Building UIs without waiting for backend APIs
- **Backend Developers** - Testing API integrations and microservices
- **QA Engineers** - Creating consistent test environments
- **DevOps Teams** - Setting up development and staging environments
- **Product Teams** - Prototyping and demonstrating features

## Getting started

Ready to create your first mock API? Here's what you'll learn:

1. **[Installation](installation.md)** - Get moclojer running on your system
2. **[Your First Mock Server](your-first-mock.md)** - Create and test a simple API
3. **[Dynamic Responses](dynamic-responses.md)** - Make your mocks respond to different inputs
4. **[Multiple Endpoints](multiple-endpoints.md)** - Build a complete API with multiple routes
5. **[Real-World Example](real-world-example.md)** - Put it all together with a practical example

Each tutorial builds on the previous one, so you'll have a solid understanding of moclojer by the end.

## Need help?

- **[FAQ](../reference/faq.md)** - Common questions and answers
- **[GitHub Discussions](https://github.com/moclojer/moclojer/discussions)** - Community help and ideas
- **[GitHub Issues](https://github.com/moclojer/moclojer/issues)** - Bug reports and feature requests

Let's get started! 🚀