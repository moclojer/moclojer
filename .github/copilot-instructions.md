# GitHub Copilot Instructions for Moclojer

## Project Overview

Moclojer is a simple and efficient HTTP async framework for Clojure. It provides a lightweight, performant solution for building HTTP APIs and web applications with asynchronous capabilities.

## Key Features

- HTTP async framework built for Clojure
- Simple and efficient design
- Lightweight with minimal overhead
- Framework integrations support
- Distributed via Clojars

## Code Style and Standards

### Clojure Conventions

- Follow standard Clojure naming conventions (kebab-case for functions and variables)
- Use meaningful function and variable names that reflect HTTP/async concepts
- Prefer pure functions when possible
- Use proper docstrings for all public functions
- Follow the project's existing indentation and formatting style
- Use threading macros (`->`, `->>`) appropriately for readability

### Project Structure

- Main source code in `src/` directory
- Tests in `test/` directory
- Use `deps.edn` for dependency management
- Follow namespace conventions: `moclojer.*`
- Separate concerns: routing, middleware, handlers, and async operations

## Development Guidelines

### HTTP Framework Development

- When working with HTTP operations, prioritize async/non-blocking approaches
- Implement proper error handling for HTTP requests and responses
- Use appropriate status codes and response formats
- Consider performance implications of async operations
- Ensure thread safety in concurrent scenarios

### API Development

- Maintain RESTful principles where applicable
- Ensure proper input validation and sanitization
- Implement consistent error responses across endpoints
- Use appropriate HTTP methods (GET, POST, PUT, DELETE, etc.)
- Document API changes and new endpoints

### Async Programming

- Leverage Clojure's async capabilities (core.async, futures, promises)
- Handle backpressure appropriately in async operations
- Use proper error handling in async contexts
- Consider timeout handling for long-running operations
- Ensure proper resource cleanup in async scenarios

### Testing

- Write comprehensive tests for all new features
- Include unit tests for individual functions
- Add integration tests for HTTP endpoints
- Test async operations thoroughly with proper timing considerations
- Mock external dependencies appropriately

### Performance Considerations

- Profile async operations for performance bottlenecks
- Consider memory usage in long-running async operations
- Optimize for low latency in HTTP responses
- Use appropriate data structures for performance-critical paths

## Specific Instructions

### YAML Format

```yaml
# Example moclojer specification in YAML
endpoints:
  - endpoint: "/api/users"
    method: "GET"
    response:
      status: 200
      headers:
        Content-Type: "application/json"
      body:
        users:
          - id: 1
            name: "John Doe"
            email: "john@example.com"
          - id: 2
            name: "Jane Smith"
            email: "jane@example.com"

  - endpoint: "/api/users/:id"
    method: "GET"
    response:
      status: 200
      headers:
        Content-Type: "application/json"
      body:
        id: "{{path.id}}"
        name: "User {{path.id}}"
        email: "user{{path.id}}@example.com"

  - endpoint: "/api/users"
    method: "POST"
    response:
      status: 201
      headers:
        Content-Type: "application/json"
      body:
        id: "{{uuid}}"
        name: "{{body.name}}"
        email: "{{body.email}}"
        created_at: "{{now}}"
```

### EDN Format

```edn
;; Example moclojer specification in EDN
{:endpoints
 [{:endpoint "/api/users"
   :method "GET"
   :response {:status 200
              :headers {"Content-Type" "application/json"}
              :body {:users [{:id 1
                              :name "John Doe"
                              :email "john@example.com"}
                             {:id 2
                              :name "Jane Smith"
                              :email "jane@example.com"}]}}}

  {:endpoint "/api/users/:id"
   :method "GET"
   :response {:status 200
              :headers {"Content-Type" "application/json"}
              :body {:id "{{path.id}}"
                     :name "User {{path.id}}"
                     :email "user{{path.id}}@example.com"}}}

  {:endpoint "/api/users"
   :method "POST"
   :response {:status 201
              :headers {"Content-Type" "application/json"}
              :body {:id "{{uuid}}"
                     :name "{{body.name}}"
                     :email "{{body.email}}"
                     :created_at "{{now}}"}}}]}
```

### OpenAPI Format

```yaml
# Example moclojer specification in OpenAPI format
openapi: 3.0.0
info:
  title: Users API
  version: 1.0.0
paths:
  /api/users:
    get:
      summary: Get all users
      responses:
        '200':
          description: List of users
          content:
            application/json:
              schema:
                type: object
                properties:
                  users:
                    type: array
                    items:
                      $ref: '#/components/schemas/User'
    post:
      summary: Create a new user
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateUser'
      responses:
        '201':
          description: User created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'

  /api/users/{id}:
    get:
      summary: Get user by ID
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: User details
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'

components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
        name:
          type: string
        email:
          type: string
        created_at:
          type: string
          format: date-time
    CreateUser:
      type: object
      required:
        - name
        - email
      properties:
        name:
          type: string
        email:
          type: string
```



### When generating code:

1. Always consider the async nature of the framework
2. Include proper error handling for HTTP operations
3. Use appropriate Clojure idioms and patterns
4. Ensure thread safety where applicable
5. Consider performance implications of HTTP operations
6. Use meaningful variable names that reflect HTTP concepts (request, response, handler, etc.)

### When working with HTTP:

1. Use proper HTTP status codes
2. Implement appropriate content-type handling
3. Consider request/response middleware patterns
4. Handle edge cases (malformed requests, timeouts, etc.)
5. Ensure proper logging for debugging

### When working with async operations:

1. Use core.async channels appropriately
2. Handle exceptions in async contexts
3. Consider backpressure and flow control
4. Implement proper timeout handling
5. Ensure resource cleanup

### When reviewing code:

1. Check for proper async operation handling
2. Verify HTTP response consistency
3. Ensure proper testing coverage for async scenarios
4. Review for potential performance bottlenecks
5. Validate adherence to Clojure best practices
6. Check for proper error handling in async contexts

### Dependencies

- Use the project's existing dependency management approach
- Consider compatibility with async operations
- Ensure new dependencies don't conflict with existing ones
- Document any new dependencies and their purpose
- Prefer lightweight dependencies that align with the framework's philosophy

### Framework Integration

- Maintain compatibility with existing Clojure web frameworks
- Ensure proper integration patterns for middleware
- Consider interoperability with Ring-compatible libraries
- Document integration examples and patterns

## Documentation

- Update README.md for significant changes
- Document new HTTP endpoints and middleware
- Include examples for new async features
- Maintain clear docstrings for public functions
- Provide integration examples for different use cases
- Document performance characteristics of new features
