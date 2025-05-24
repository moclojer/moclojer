---
description: >-
  Step-by-step tutorial to create your first mock server with moclojer. Learn the basics
  of configuration, endpoints, and testing in under 10 minutes.
---

# Your First Mock Server

In this tutorial, you'll create a simple but functional mock API server from scratch. By the end, you'll have a working mock server that responds to HTTP requests with JSON data.

## What you'll learn

- How to write a basic moclojer configuration file
- How to define HTTP endpoints
- How to structure JSON responses
- How to start and test your mock server

## What you'll build

A simple user API with these endpoints:
- `GET /users` - List all users
- `GET /users/123` - Get a specific user
- `GET /health` - Health check endpoint

## Prerequisites

- moclojer installed ([see installation guide](installation.md))
- A text editor
- Command line access
- `curl` or a tool to make HTTP requests

## Step 1: Create your configuration file

Create a new file called `moclojer.yml` in an empty directory:

```yaml
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
          "service": "user-api",
          "timestamp": "2024-01-15T10:30:00Z"
        }
```

**What this does:**
- Creates one endpoint that responds to `GET /health`
- Returns HTTP status 200 (success)
- Sets the response content type to JSON
- Returns a simple JSON object with status information

## Step 2: Start your mock server

Open your terminal in the directory with `moclojer.yml` and run:

```bash
# Using Docker
docker run -it -p 8000:8000 -v $(pwd)/moclojer.yml:/app/moclojer.yml ghcr.io/moclojer/moclojer:latest

# Using JAR file
java -jar moclojer.jar --config moclojer.yml

# Using native binary
./moclojer --config moclojer.yml
```

You should see output similar to:
```
Starting moclojer server on port 8000...
Server started successfully!
```

## Step 3: Test your first endpoint

Open a new terminal window and test your endpoint:

```bash
curl http://localhost:8000/health
```

You should get this response:
```json
{
  "status": "ok",
  "service": "user-api",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

🎉 **Congratulations!** You've created your first mock API endpoint.

## Step 4: Add more endpoints

Now let's expand your API. Stop the server (Ctrl+C) and update your `moclojer.yml` file:

```yaml
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
          "service": "user-api",
          "timestamp": "2024-01-15T10:30:00Z"
        }

- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        [
          {
            "id": 1,
            "name": "Alice Johnson",
            "email": "alice@example.com",
            "role": "admin"
          },
          {
            "id": 2,
            "name": "Bob Smith",
            "email": "bob@example.com",
            "role": "user"
          },
          {
            "id": 3,
            "name": "Carol Davis",
            "email": "carol@example.com",
            "role": "user"
          }
        ]

- endpoint:
    method: GET
    path: /users/1
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": 1,
          "name": "Alice Johnson",
          "email": "alice@example.com",
          "role": "admin",
          "created_at": "2024-01-01T00:00:00Z",
          "last_login": "2024-01-15T09:30:00Z"
        }
```

**What's new:**
- `/users` endpoint returns a list of users
- `/users/1` endpoint returns details for a specific user
- Each response includes different data structures (array vs object)

## Step 5: Test your expanded API

Restart your server and test the new endpoints:

```bash
# Test the users list
curl http://localhost:8000/users

# Test a specific user
curl http://localhost:8000/users/1

# Test the health check still works
curl http://localhost:8000/health
```

## Step 6: Handle different HTTP methods

Let's add a POST endpoint to create users. Add this to your `moclojer.yml`:

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
          "id": 4,
          "name": "New User",
          "email": "new@example.com",
          "role": "user",
          "created_at": "2024-01-15T10:30:00Z",
          "message": "User created successfully"
        }
```

Test it:
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com"}' \
  http://localhost:8000/users
```

## Step 7: Add error responses

Real APIs return errors sometimes. Let's add a 404 response:

```yaml
- endpoint:
    method: GET
    path: /users/999
    response:
      status: 404
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "User not found",
          "code": "USER_NOT_FOUND",
          "message": "User with ID 999 does not exist"
        }
```

Test it:
```bash
curl http://localhost:8000/users/999
```

## Understanding the configuration

Let's break down what you've learned:

### Endpoint structure
```yaml
- endpoint:           # Start of endpoint definition
    method: GET       # HTTP method (GET, POST, PUT, DELETE, etc.)
    path: /users      # URL path
    response:         # What to return
      status: 200     # HTTP status code
      headers:        # HTTP headers
        Content-Type: application/json
      body: >         # Response content
        {"message": "Hello"}
```

### Key concepts
- **Method**: What HTTP verb this endpoint responds to
- **Path**: The URL pattern to match
- **Status**: HTTP status code (200=success, 404=not found, etc.)
- **Headers**: HTTP response headers (like Content-Type)
- **Body**: The actual response content

### YAML tips
- Use `>` for multi-line strings (like JSON)
- Indentation matters - use 2 spaces consistently
- Each endpoint starts with `- endpoint:`

## What you've accomplished

✅ Created a working mock API server
✅ Defined multiple endpoints with different HTTP methods
✅ Structured JSON responses
✅ Added error handling
✅ Tested everything with curl

## Your complete configuration

Here's your final `moclojer.yml`:

```yaml
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
          "service": "user-api",
          "timestamp": "2024-01-15T10:30:00Z"
        }

- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        [
          {
            "id": 1,
            "name": "Alice Johnson",
            "email": "alice@example.com",
            "role": "admin"
          },
          {
            "id": 2,
            "name": "Bob Smith",
            "email": "bob@example.com",
            "role": "user"
          },
          {
            "id": 3,
            "name": "Carol Davis",
            "email": "carol@example.com",
            "role": "user"
          }
        ]

- endpoint:
    method: GET
    path: /users/1
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": 1,
          "name": "Alice Johnson",
          "email": "alice@example.com",
          "role": "admin",
          "created_at": "2024-01-01T00:00:00Z",
          "last_login": "2024-01-15T09:30:00Z"
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
          "id": 4,
          "name": "New User",
          "email": "new@example.com",
          "role": "user",
          "created_at": "2024-01-15T10:30:00Z",
          "message": "User created successfully"
        }

- endpoint:
    method: GET
    path: /users/999
    response:
      status: 404
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "User not found",
          "code": "USER_NOT_FOUND",
          "message": "User with ID 999 does not exist"
        }
```

## Next steps

You now have a solid foundation! But your API is still static - it always returns the same responses. In the next tutorial, you'll learn how to make your responses dynamic based on the request data.

👉 **[Dynamic Responses Tutorial](dynamic-responses.md)** - Learn to use path parameters, query parameters, and templates

## Need help?

- **Stuck?** Check the [troubleshooting section](../reference/troubleshooting.md)
- **Want examples?** See [common patterns](../examples/common-patterns.md)
- **Have questions?** Join the [community discussions](https://github.com/moclojer/moclojer/discussions)