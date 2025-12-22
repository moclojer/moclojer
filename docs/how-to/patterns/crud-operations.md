---
description: >-
  Learn how to implement complete CRUD operations (Create, Read, Update, Delete)
  with moclojer. Practical guide with tested examples.
---

# CRUD Operations (Create, Read, Update, Delete)

This guide shows how to implement a complete RESTful API with all CRUD operations using moclojer. You'll learn the correct patterns for each operation and how to structure your responses.

## What Are CRUD Operations?

**CRUD** is an acronym for the four basic data persistence operations:

| Operation | HTTP Method | Action | Example |
|----------|-------------|------|---------|
| **C**reate | POST | Create new resource | `POST /users` |
| **R**ead | GET | Read resource(s) | `GET /users`, `GET /users/1` |
| **U**pdate | PUT/PATCH | Update resource | `PUT /users/1`, `PATCH /users/1` |
| **D**elete | DELETE | Remove resource | `DELETE /users/1` |

## Example API: Task Management System

Let's create a complete task management API with all CRUD operations.

### Task Structure

```json
{
  "id": 1,
  "title": "Buy milk",
  "description": "Go to the store and buy 2L of milk",
  "completed": false,
  "priority": "medium",
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

---

## CREATE - Create Resources

### POST - Create New Task

```yaml
- endpoint:
    method: POST
    path: /api/tasks
    response:
      status: 201    # 201 Created
      headers:
        Content-Type: application/json
        Location: /api/tasks/1
      body: >
        {
          "id": 1,
          "title": "{{json-params.title}}",
          "description": "{{json-params.description}}",
          "completed": false,
          "priority": "{{json-params.priority}}",
          "createdAt": "2024-01-15T10:00:00Z",
          "updatedAt": "2024-01-15T10:00:00Z"
        }
```

**Test:**

```bash
curl -X POST http://localhost:8000/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Buy milk",
    "description": "Go to the store",
    "priority": "medium"
  }'
```

**Response (201 Created):**

```json
{
  "id": 1,
  "title": "Buy milk",
  "description": "Go to the store",
  "completed": false,
  "priority": "medium",
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

### Required Field Validation

```yaml
# Specific endpoint for validation (should come BEFORE the success endpoint)
- endpoint:
    method: POST
    path: /api/tasks
    response:
      status: 400    # 400 Bad Request
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Validation failed",
          "message": "Title is required",
          "code": "VALIDATION_ERROR",
          "details": [
            {
              "field": "title",
              "message": "Title cannot be empty"
            }
          ]
        }
```

**Note:** In real production, you would validate the received JSON. In mocks, you can create specific endpoints to simulate errors.

---

## READ - Read Resources

### GET - List All Tasks

```yaml
- endpoint:
    method: GET
    path: /api/tasks
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "3"
      body: >
        {
          "data": [
            {
              "id": 1,
              "title": "Buy milk",
              "description": "Go to the store",
              "completed": false,
              "priority": "medium",
              "createdAt": "2024-01-15T10:00:00Z"
            },
            {
              "id": 2,
              "title": "Study moclojer",
              "description": "Read complete documentation",
              "completed": true,
              "priority": "high",
              "createdAt": "2024-01-15T11:00:00Z"
            },
            {
              "id": 3,
              "title": "Exercise",
              "description": "30 minutes of running",
              "completed": false,
              "priority": "low",
              "createdAt": "2024-01-15T12:00:00Z"
            }
          ],
          "meta": {
            "total": 3,
            "page": 1,
            "perPage": 10
          }
        }
```

**Test:**

```bash
curl http://localhost:8000/api/tasks
```

### GET with Pagination

```yaml
- endpoint:
    method: GET
    path: /api/tasks
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "100"
      body: >
        {
          "data": [
            {
              "id": 1,
              "title": "Task on page {{query-params.page}}",
              "completed": false
            }
          ],
          "meta": {
            "total": 100,
            "page": "{{query-params.page}}",
            "perPage": "{{query-params.limit}}",
            "totalPages": 10
          },
          "links": {
            "first": "/api/tasks?page=1&limit={{query-params.limit}}",
            "prev": "/api/tasks?page={{query-params.page}}&limit={{query-params.limit}}",
            "next": "/api/tasks?page={{query-params.page}}&limit={{query-params.limit}}",
            "last": "/api/tasks?page=10&limit={{query-params.limit}}"
          }
        }
```

**Test:**

```bash
curl "http://localhost:8000/api/tasks?page=1&limit=10"
```

### GET - Get Specific Task

```yaml
- endpoint:
    method: GET
    path: /api/tasks/:id|int
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "title": "Task {{path-params.id}}",
          "description": "Description for task {{path-params.id}}",
          "completed": false,
          "priority": "medium",
          "createdAt": "2024-01-15T10:00:00Z",
          "updatedAt": "2024-01-15T10:00:00Z",
          "tags": ["work", "important"],
          "assignee": {
            "id": 1,
            "name": "John Doe"
          }
        }
```

**Test:**

```bash
curl http://localhost:8000/api/tasks/1
curl http://localhost:8000/api/tasks/42
```

### GET - Task Not Found

```yaml
- endpoint:
    method: GET
    path: /api/tasks/999
    response:
      status: 404    # 404 Not Found
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Not Found",
          "message": "Task with ID 999 not found",
          "code": "TASK_NOT_FOUND"
        }
```

**Test:**

```bash
curl http://localhost:8000/api/tasks/999
```

### GET with Filters

```yaml
- endpoint:
    method: GET
    path: /api/tasks
    response:
      status: 200
      body: >
        {
          "filters": {
            "completed": "{{query-params.completed}}",
            "priority": "{{query-params.priority}}",
            "search": "{{query-params.q}}"
          },
          "data": [
            {
              "id": 1,
              "title": "Filtered task",
              "completed": "{{query-params.completed}}",
              "priority": "{{query-params.priority}}"
            }
          ]
        }
```

**Test:**

```bash
curl "http://localhost:8000/api/tasks?completed=false&priority=high"
curl "http://localhost:8000/api/tasks?q=buy"
```

---

## UPDATE - Update Resources

### PUT - Replace Complete Task

**PUT replaces the entire resource** - all fields must be sent.

```yaml
- endpoint:
    method: PUT
    path: /api/tasks/:id|int
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "title": "{{json-params.title}}",
          "description": "{{json-params.description}}",
          "completed": {{json-params.completed}},
          "priority": "{{json-params.priority}}",
          "createdAt": "2024-01-15T10:00:00Z",
          "updatedAt": "2024-01-15T15:30:00Z"
        }
```

**Test:**

```bash
curl -X PUT http://localhost:8000/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Buy milk and bread",
    "description": "Go to the store and buy 2L of milk and 1 French bread",
    "completed": false,
    "priority": "high"
  }'
```

### PATCH - Partial Update

**PATCH updates only specific fields** - send only what changed.

```yaml
- endpoint:
    method: PATCH
    path: /api/tasks/:id|int
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "title": "{{json-params.title}}",
          "description": "{{json-params.description}}",
          "completed": {{json-params.completed}},
          "priority": "{{json-params.priority}}",
          "updatedAt": "2024-01-15T16:00:00Z",
          "message": "Task updated successfully"
        }
```

**Test (update only completed):**

```bash
curl -X PATCH http://localhost:8000/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"completed": true}'
```

**Test (update title and priority):**

```bash
curl -X PATCH http://localhost:8000/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New title",
    "priority": "urgent"
  }'
```

### Mark Task as Complete (Custom Action)

```yaml
- endpoint:
    method: PATCH
    path: /api/tasks/:id|int/complete
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "completed": true,
          "completedAt": "2024-01-15T16:30:00Z",
          "message": "Task {{path-params.id}} marked as complete"
        }
```

**Test:**

```bash
curl -X PATCH http://localhost:8000/api/tasks/1/complete
```

### Update Validation Error

```yaml
- endpoint:
    method: PUT
    path: /api/tasks/:id|int
    response:
      status: 422    # 422 Unprocessable Entity
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Validation failed",
          "message": "Invalid priority value",
          "code": "VALIDATION_ERROR",
          "details": [
            {
              "field": "priority",
              "message": "Priority must be one of: low, medium, high, urgent"
            }
          ]
        }
```

---

## DELETE - Remove Resources

### DELETE - Remove Task

```yaml
- endpoint:
    method: DELETE
    path: /api/tasks/:id|int
    response:
      status: 204    # 204 No Content (no body)
```

**Test:**

```bash
curl -X DELETE http://localhost:8000/api/tasks/1
# Empty response with status 204
```

### DELETE with Confirmation (alternative)

Some prefer to return 200 with message:

```yaml
- endpoint:
    method: DELETE
    path: /api/tasks/:id|int
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "deleted": true,
          "deletedAt": "2024-01-15T17:00:00Z",
          "message": "Task {{path-params.id}} deleted successfully"
        }
```

**Test:**

```bash
curl -X DELETE http://localhost:8000/api/tasks/1
```

### DELETE - Task Not Found

```yaml
- endpoint:
    method: DELETE
    path: /api/tasks/999
    response:
      status: 404
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Not Found",
          "message": "Task with ID 999 not found",
          "code": "TASK_NOT_FOUND"
        }
```

### Bulk DELETE (Clear Completed)

```yaml
- endpoint:
    method: DELETE
    path: /api/tasks/completed
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "deleted": 5,
          "message": "5 completed tasks deleted successfully"
        }
```

**Test:**

```bash
curl -X DELETE http://localhost:8000/api/tasks/completed
```

---

## Complete API: Single File

Here's the complete `tasks-api.yml` file with all CRUD operations:

```yaml
# =====================
# CREATE (POST)
# =====================

# Create new task
- endpoint:
    method: POST
    path: /api/tasks
    response:
      status: 201
      headers:
        Content-Type: application/json
        Location: /api/tasks/1
      body: >
        {
          "id": 1,
          "title": "{{json-params.title}}",
          "description": "{{json-params.description}}",
          "completed": false,
          "priority": "{{json-params.priority}}",
          "createdAt": "2024-01-15T10:00:00Z",
          "updatedAt": "2024-01-15T10:00:00Z"
        }

# =====================
# READ (GET)
# =====================

# List all tasks
- endpoint:
    method: GET
    path: /api/tasks
    response:
      status: 200
      headers:
        Content-Type: application/json
        X-Total-Count: "3"
      body: >
        {
          "data": [
            {"id": 1, "title": "Buy milk", "completed": false},
            {"id": 2, "title": "Study moclojer", "completed": true},
            {"id": 3, "title": "Exercise", "completed": false}
          ],
          "meta": {"total": 3, "page": 1, "perPage": 10}
        }

# Get task not found (specific before generic!)
- endpoint:
    method: GET
    path: /api/tasks/999
    response:
      status: 404
      body: >
        {
          "error": "Not Found",
          "message": "Task with ID 999 not found"
        }

# Get task by ID
- endpoint:
    method: GET
    path: /api/tasks/:id|int
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "title": "Task {{path-params.id}}",
          "description": "Description for task {{path-params.id}}",
          "completed": false,
          "priority": "medium",
          "createdAt": "2024-01-15T10:00:00Z",
          "updatedAt": "2024-01-15T10:00:00Z"
        }

# =====================
# UPDATE (PUT/PATCH)
# =====================

# Update complete task (PUT)
- endpoint:
    method: PUT
    path: /api/tasks/:id|int
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "title": "{{json-params.title}}",
          "description": "{{json-params.description}}",
          "completed": {{json-params.completed}},
          "priority": "{{json-params.priority}}",
          "updatedAt": "2024-01-15T15:30:00Z"
        }

# Partial update (PATCH)
- endpoint:
    method: PATCH
    path: /api/tasks/:id|int
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "updated": true,
          "updatedAt": "2024-01-15T16:00:00Z"
        }

# =====================
# DELETE
# =====================

# Delete task not found (specific first!)
- endpoint:
    method: DELETE
    path: /api/tasks/999
    response:
      status: 404
      body: >
        {"error": "Not Found", "message": "Task 999 not found"}

# Delete task
- endpoint:
    method: DELETE
    path: /api/tasks/:id|int
    response:
      status: 204
```

---

## Testing the Complete API

### Complete Test Script

```bash
#!/bin/bash
API_URL="http://localhost:8000/api/tasks"

echo "=== CREATE - Create task ==="
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{"title": "New task", "description": "Test", "priority": "high"}'
echo -e "\n"

echo "=== READ - List all ==="
curl $API_URL
echo -e "\n"

echo "=== READ - Get specific ==="
curl $API_URL/1
echo -e "\n"

echo "=== UPDATE - Update complete (PUT) ==="
curl -X PUT $API_URL/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Updated", "description": "PUT test", "completed": false, "priority": "medium"}'
echo -e "\n"

echo "=== UPDATE - Partial update (PATCH) ==="
curl -X PATCH $API_URL/1 \
  -H "Content-Type: application/json" \
  -d '{"completed": true}'
echo -e "\n"

echo "=== DELETE - Remove task ==="
curl -X DELETE $API_URL/1
echo -e "\n"

echo "=== READ - Task not found ==="
curl $API_URL/999
echo -e "\n"
```

Save as `test-crud.sh`, give permission and run:

```bash
chmod +x test-crud.sh
./test-crud.sh
```

---

## Best Practices

### ✅ Do

1. **Use appropriate status codes**
   - `201 Created` for POST
   - `200 OK` for GET/PUT/PATCH
   - `204 No Content` for DELETE
   - `404 Not Found` when resource doesn't exist
   - `422 Unprocessable Entity` for validation errors

2. **Return the created/updated resource**

   ```json
   // POST response
   {
     "id": 1,
     "...": "complete data of created resource"
   }
   ```

3. **Use specific routes before generic ones**

   ```yaml
   - path: /api/tasks/999  # specific 404
   - path: /api/tasks/:id  # generic (comes after!)
   ```

4. **Include metadata in lists**

   ```json
   {
     "data": [...],
     "meta": {"total": 100, "page": 1}
   }
   ```

5. **`Location` header on created resources**

   ```yaml
   headers:
     Location: /api/tasks/1
   ```

### ❌ Avoid

1. **DELETE returning 200 with deleted resource**

   ```yaml
   # ❌ Prefer 204 No Content
   DELETE /tasks/1 → 200 {"id": 1, "deleted": true}

   # ✅ REST standard
   DELETE /tasks/1 → 204 (no body)
   ```

2. **GET modifying data**

   ```yaml
   # ❌ NEVER!
   GET /tasks/1/delete

   # ✅ Use DELETE
   DELETE /tasks/1
   ```

3. **ID fields in POST body**

   ```json
   // ❌ Client should not send ID
   POST /tasks {"id": 123, "title": "..."}

   // ✅ Server generates ID
   POST /tasks {"title": "..."}
   // Response: {"id": 1, "title": "..."}
   ```

---

## Next Steps

- **[Pagination How-to](pagination.md)** - Implement pagination
- **[Authentication Mock](authentication-mock.md)** - Simulate authentication
- **[Error Handling](error-handling.md)** - Error patterns
- **[HTTP Methods](../../topics/endpoints/http-methods.md)** - Methods reference

## See Also

- [Your First Mock](../../getting-started/your-first-mock.md) - Initial tutorial
- [Dynamic Responses](../../getting-started/dynamic-responses.md) - Dynamic responses
- [REST API Example](../../examples/rest-api/basic-crud.md) - Complete example
