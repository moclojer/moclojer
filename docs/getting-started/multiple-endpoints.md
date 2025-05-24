---
description: >-
  Learn how to organize and structure multiple endpoints to create a complete API.
  Build a realistic user management system with proper HTTP methods and error handling.
---

# Multiple Endpoints

In the previous tutorials, you learned to create basic endpoints and make them dynamic. Now you'll learn how to organize multiple endpoints into a cohesive API structure. You'll build a complete user management system with proper HTTP methods, error handling, and realistic responses.

## What you'll learn

- How to organize multiple related endpoints
- Using different HTTP methods properly (GET, POST, PUT, DELETE)
- Creating consistent API responses
- Implementing proper error handling
- Structuring a complete CRUD (Create, Read, Update, Delete) API
- Best practices for API design

## What you'll build

A complete user management API with these endpoints:
- `GET /users` - List all users
- `GET /users/:id` - Get a specific user
- `POST /users` - Create a new user
- `PUT /users/:id` - Update a user
- `DELETE /users/:id` - Delete a user
- `GET /users/:id/posts` - Get user's posts
- `POST /users/:id/posts` - Create a post for a user
- Error responses for various scenarios

## Prerequisites

- Completed [Dynamic Responses](dynamic-responses.md) tutorial
- Understanding of HTTP methods and status codes
- Basic knowledge of REST API principles

## Step 1: Planning your API structure

Before writing configuration, let's plan our API structure:

**Users Resource:**
- List users: `GET /users`
- Get user: `GET /users/:id`
- Create user: `POST /users`
- Update user: `PUT /users/:id`
- Delete user: `DELETE /users/:id`

**Posts Resource (nested under users):**
- Get user posts: `GET /users/:id/posts`
- Create user post: `POST /users/:id/posts`
- Get specific post: `GET /users/:id/posts/:postId`

**Error Handling:**
- 404 for not found resources
- 400 for bad requests
- 422 for validation errors

## Step 2: Create the basic CRUD endpoints

Create a new `moclojer.yml` file:

```yaml
# List all users
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "users": [
            {
              "id": 1,
              "name": "Alice Johnson",
              "email": "alice@example.com",
              "role": "admin",
              "created_at": "2024-01-01T00:00:00Z"
            },
            {
              "id": 2,
              "name": "Bob Smith",
              "email": "bob@example.com",
              "role": "user",
              "created_at": "2024-01-02T00:00:00Z"
            },
            {
              "id": 3,
              "name": "Carol Davis",
              "email": "carol@example.com",
              "role": "user",
              "created_at": "2024-01-03T00:00:00Z"
            }
          ],
          "total": 3,
          "page": 1,
          "per_page": 10
        }

# Get a specific user
- endpoint:
    method: GET
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "name": "User {{path-params.id}}",
          "email": "user{{path-params.id}}@example.com",
          "role": "user",
          "created_at": "2024-01-15T10:30:00Z",
          "updated_at": "2024-01-15T10:30:00Z",
          "profile": {
            "bio": "This is user {{path-params.id}}'s biography",
            "avatar": "https://api.example.com/avatars/{{path-params.id}}.jpg"
          }
        }

# Create a new user
- endpoint:
    method: POST
    path: /users
    response:
      status: 201
      headers:
        Content-Type: application/json
        Location: /users/{{json-params.id}}
      body: >
        {
          "id": 12345,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "role": "{{json-params.role}}",
          "created_at": "2024-01-15T10:30:00Z",
          "updated_at": "2024-01-15T10:30:00Z",
          "message": "User created successfully"
        }

# Update a user
- endpoint:
    method: PUT
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "role": "{{json-params.role}}",
          "created_at": "2024-01-01T00:00:00Z",
          "updated_at": "2024-01-15T10:30:00Z",
          "message": "User {{path-params.id}} updated successfully"
        }

# Delete a user
- endpoint:
    method: DELETE
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "message": "User {{path-params.id}} deleted successfully",
          "deleted_at": "2024-01-15T10:30:00Z"
        }
```

## Step 3: Add nested resources (user posts)

Add post-related endpoints under users:

```yaml
# Get all posts for a user
- endpoint:
    method: GET
    path: /users/:id/posts
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "user_id": {{path-params.id}},
          "posts": [
            {
              "id": 1,
              "title": "First post by user {{path-params.id}}",
              "content": "This is the content of the first post",
              "created_at": "2024-01-10T09:00:00Z",
              "updated_at": "2024-01-10T09:00:00Z",
              "status": "published"
            },
            {
              "id": 2,
              "title": "Second post by user {{path-params.id}}",
              "content": "This is the content of the second post",
              "created_at": "2024-01-12T14:30:00Z",
              "updated_at": "2024-01-12T14:30:00Z",
              "status": "published"
            }
          ],
          "total": 2,
          "page": 1,
          "per_page": 10
        }

# Create a new post for a user
- endpoint:
    method: POST
    path: /users/:id/posts
    response:
      status: 201
      headers:
        Content-Type: application/json
        Location: /users/{{path-params.id}}/posts/{{json-params.post_id}}
      body: >
        {
          "id": 54321,
          "user_id": {{path-params.id}},
          "title": "{{json-params.title}}",
          "content": "{{json-params.content}}",
          "status": "{{json-params.status}}",
          "created_at": "2024-01-15T10:30:00Z",
          "updated_at": "2024-01-15T10:30:00Z",
          "message": "Post created successfully for user {{path-params.id}}"
        }

# Get a specific post
- endpoint:
    method: GET
    path: /users/:id/posts/:postId
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.postId}},
          "user_id": {{path-params.id}},
          "title": "Post {{path-params.postId}} by user {{path-params.id}}",
          "content": "This is the detailed content of post {{path-params.postId}}",
          "status": "published",
          "created_at": "2024-01-10T09:00:00Z",
          "updated_at": "2024-01-10T09:00:00Z",
          "author": {
            "id": {{path-params.id}},
            "name": "User {{path-params.id}}",
            "email": "user{{path-params.id}}@example.com"
          },
          "tags": ["tutorial", "example", "moclojer"]
        }
```

## Step 4: Add proper error handling

Real APIs need proper error responses. Add these error endpoints:

```yaml
# User not found
- endpoint:
    method: GET
    path: /users/999
    response:
      status: 404
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Not Found",
          "message": "User with ID 999 not found",
          "code": "USER_NOT_FOUND",
          "timestamp": "2024-01-15T10:30:00Z"
        }

# Bad request for invalid user creation
- endpoint:
    method: POST
    path: /users/invalid
    response:
      status: 400
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Bad Request",
          "message": "Invalid request format",
          "code": "INVALID_REQUEST",
          "timestamp": "2024-01-15T10:30:00Z"
        }

# Validation error
- endpoint:
    method: POST
    path: /users/validation-error
    response:
      status: 422
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Validation Failed",
          "message": "The request data is invalid",
          "code": "VALIDATION_ERROR",
          "errors": [
            {
              "field": "email",
              "message": "Email is required"
            },
            {
              "field": "name",
              "message": "Name must be at least 2 characters"
            }
          ],
          "timestamp": "2024-01-15T10:30:00Z"
        }
```

## Step 5: Add filtering and pagination

Enhance your list endpoints with query parameters:

```yaml
# Enhanced user listing with filters
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "users": [
            {
              "id": 1,
              "name": "Alice Johnson",
              "email": "alice@example.com",
              "role": "admin",
              "created_at": "2024-01-01T00:00:00Z"
            },
            {
              "id": 2,
              "name": "Bob Smith", 
              "email": "bob@example.com",
              "role": "user",
              "created_at": "2024-01-02T00:00:00Z"
            }
          ],
          "pagination": {
            "total": 50,
            "page": "{{query-params.page}}",
            "per_page": "{{query-params.per_page}}",
            "total_pages": 5
          },
          "filters": {
            "role": "{{query-params.role}}",
            "search": "{{query-params.search}}",
            "sort": "{{query-params.sort}}"
          }
        }
```

## Step 6: Add API metadata endpoints

Include helpful metadata endpoints:

```yaml
# API health check
- endpoint:
    method: GET
    path: /health
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "status": "ok",
          "service": "user-management-api",
          "version": "1.0.0",
          "timestamp": "2024-01-15T10:30:00Z",
          "uptime": "24h 30m",
          "dependencies": {
            "database": "connected",
            "cache": "connected"
          }
        }

# API version info
- endpoint:
    method: GET
    path: /version
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "version": "1.0.0",
          "build": "2024-01-15",
          "commit": "abc123def",
          "environment": "development"
        }

# API documentation endpoint
- endpoint:
    method: GET
    path: /docs
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "title": "User Management API",
          "version": "1.0.0",
          "description": "A complete API for managing users and their posts",
          "endpoints": {
            "users": {
              "list": "GET /users",
              "get": "GET /users/:id",
              "create": "POST /users",
              "update": "PUT /users/:id",
              "delete": "DELETE /users/:id"
            },
            "posts": {
              "list": "GET /users/:id/posts",
              "get": "GET /users/:id/posts/:postId",
              "create": "POST /users/:id/posts"
            }
          }
        }
```

## Step 7: Test your complete API

Start your server and test all the endpoints:

```bash
# Start the server
moclojer --config moclojer.yml

# Test user CRUD operations
curl http://localhost:8000/users
curl http://localhost:8000/users/123
curl -X POST -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com", "role": "user"}' \
  http://localhost:8000/users

# Test nested resources
curl http://localhost:8000/users/123/posts
curl -X POST -H "Content-Type: application/json" \
  -d '{"title": "My First Post", "content": "Hello World!", "status": "published"}' \
  http://localhost:8000/users/123/posts

# Test with filters
curl "http://localhost:8000/users?role=admin&page=1&per_page=5"

# Test error responses
curl http://localhost:8000/users/999

# Test metadata endpoints
curl http://localhost:8000/health
curl http://localhost:8000/version
curl http://localhost:8000/docs
```

## Best practices demonstrated

Your API now follows several important best practices:

### ✅ **Consistent Response Format**
All responses have similar structure with consistent field names.

### ✅ **Proper HTTP Status Codes**
- 200 for successful GET, PUT, DELETE
- 201 for successful POST (creation)
- 404 for not found
- 400 for bad requests
- 422 for validation errors

### ✅ **RESTful URL Structure**
- `/users` for the collection
- `/users/:id` for individual resources
- `/users/:id/posts` for nested resources

### ✅ **Meaningful Error Messages**
Error responses include helpful information for debugging.

### ✅ **Resource Relationships**
Posts are properly nested under users, showing the relationship.

### ✅ **Pagination Support**
List endpoints include pagination metadata.

## Your complete API configuration

Here's your complete `moclojer.yml` with all endpoints organized:

```yaml
# === HEALTH AND METADATA ===
- endpoint:
    method: GET
    path: /health
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "status": "ok",
          "service": "user-management-api",
          "version": "1.0.0",
          "timestamp": "2024-01-15T10:30:00Z"
        }

# === USER MANAGEMENT ===
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "users": [
            {
              "id": 1,
              "name": "Alice Johnson",
              "email": "alice@example.com",
              "role": "admin",
              "created_at": "2024-01-01T00:00:00Z"
            },
            {
              "id": 2,
              "name": "Bob Smith",
              "email": "bob@example.com", 
              "role": "user",
              "created_at": "2024-01-02T00:00:00Z"
            }
          ],
          "total": 2,
          "page": "{{query-params.page}}",
          "per_page": "{{query-params.per_page}}"
        }

- endpoint:
    method: GET
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "name": "User {{path-params.id}}",
          "email": "user{{path-params.id}}@example.com",
          "role": "user",
          "created_at": "2024-01-15T10:30:00Z",
          "updated_at": "2024-01-15T10:30:00Z"
        }

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
          "created_at": "2024-01-15T10:30:00Z",
          "message": "User created successfully"
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
          "id": {{path-params.id}},
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "updated_at": "2024-01-15T10:30:00Z",
          "message": "User updated successfully"
        }

- endpoint:
    method: DELETE
    path: /users/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "message": "User {{path-params.id}} deleted successfully"
        }

# === POSTS (NESTED UNDER USERS) ===
- endpoint:
    method: GET
    path: /users/:id/posts
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "user_id": {{path-params.id}},
          "posts": [
            {
              "id": 1,
              "title": "First post by user {{path-params.id}}",
              "content": "Post content here",
              "created_at": "2024-01-10T09:00:00Z"
            }
          ],
          "total": 1
        }

- endpoint:
    method: POST
    path: /users/:id/posts
    response:
      status: 201
      headers:
        Content-Type: application/json
      body: >
        {
          "id": 54321,
          "user_id": {{path-params.id}},
          "title": "{{json-params.title}}",
          "content": "{{json-params.content}}",
          "created_at": "2024-01-15T10:30:00Z",
          "message": "Post created successfully"
        }

# === ERROR RESPONSES ===
- endpoint:
    method: GET
    path: /users/999
    response:
      status: 404
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Not Found",
          "message": "User with ID 999 not found",
          "code": "USER_NOT_FOUND"
        }
```

## What you've accomplished

✅ **Complete CRUD API** - Full Create, Read, Update, Delete operations  
✅ **Nested Resources** - Posts under users showing relationships  
✅ **Proper HTTP Methods** - GET, POST, PUT, DELETE used correctly  
✅ **Error Handling** - 404, 400, 422 responses with meaningful messages  
✅ **Consistent Structure** - All responses follow similar patterns  
✅ **Query Parameters** - Filtering and pagination support  
✅ **API Metadata** - Health checks and documentation endpoints

## Next steps

You now have a well-structured API! In the final tutorial, you'll put everything together in a real-world example that showcases advanced features.

👉 **[Real-World Example](real-world-example.md)** - Build a complete e-commerce API with advanced features

## Need help?

- **Want to understand API design better?** See [REST API Best Practices](../examples/rest-api-mocking.md)
- **Looking for more complex examples?** Check [E-commerce API Example](../examples/ecommerce-api.md)
- **Have questions?** Join the [community discussions](https://github.com/moclojer/moclojer/discussions)