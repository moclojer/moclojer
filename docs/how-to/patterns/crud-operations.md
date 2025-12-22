---
description: >-
  Aprenda a implementar operações CRUD completas (Create, Read, Update, Delete)
  com moclojer. Guia prático com exemplos testados.
---

# CRUD Operations (Create, Read, Update, Delete)

Este guia mostra como implementar uma API RESTful completa com todas as operações CRUD usando moclojer. Você vai aprender os padrões corretos para cada operação e como estruturar suas respostas.

## O Que São Operações CRUD?

**CRUD** é um acrônimo para as quatro operações básicas de persistência de dados:

| Operação | Método HTTP | Ação | Exemplo |
|----------|-------------|------|---------|
| **C**reate | POST | Criar novo recurso | `POST /users` |
| **R**ead | GET | Ler recurso(s) | `GET /users`, `GET /users/1` |
| **U**pdate | PUT/PATCH | Atualizar recurso | `PUT /users/1`, `PATCH /users/1` |
| **D**elete | DELETE | Remover recurso | `DELETE /users/1` |

## API Exemplo: Sistema de Tasks (Tarefas)

Vamos criar uma API completa de gerenciamento de tarefas com todas as operações CRUD.

### Estrutura da Tarefa

```json
{
  "id": 1,
  "title": "Comprar leite",
  "description": "Ir ao mercado e comprar 2L de leite",
  "completed": false,
  "priority": "medium",
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

---

## CREATE - Criar Recursos

### POST - Criar Nova Tarefa

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

**Testar:**

```bash
curl -X POST http://localhost:8000/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Comprar leite",
    "description": "Ir ao mercado",
    "priority": "medium"
  }'
```

**Resposta (201 Created):**

```json
{
  "id": 1,
  "title": "Comprar leite",
  "description": "Ir ao mercado",
  "completed": false,
  "priority": "medium",
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

### Validação de Campos Obrigatórios

```yaml
# Endpoint específico para validação (deve vir ANTES do endpoint de sucesso)
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

**Nota:** Em produção real, você validaria o JSON recebido. No mock, você pode criar endpoints específicos para simular erros.

---

## READ - Ler Recursos

### GET - Listar Todas as Tarefas

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
              "title": "Comprar leite",
              "description": "Ir ao mercado",
              "completed": false,
              "priority": "medium",
              "createdAt": "2024-01-15T10:00:00Z"
            },
            {
              "id": 2,
              "title": "Estudar moclojer",
              "description": "Ler a documentação completa",
              "completed": true,
              "priority": "high",
              "createdAt": "2024-01-15T11:00:00Z"
            },
            {
              "id": 3,
              "title": "Fazer exercícios",
              "description": "30 minutos de corrida",
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

**Testar:**

```bash
curl http://localhost:8000/api/tasks
```

### GET com Paginação

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

**Testar:**

```bash
curl "http://localhost:8000/api/tasks?page=1&limit=10"
```

### GET - Obter Tarefa Específica

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

**Testar:**

```bash
curl http://localhost:8000/api/tasks/1
curl http://localhost:8000/api/tasks/42
```

### GET - Tarefa Não Encontrada

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

**Testar:**

```bash
curl http://localhost:8000/api/tasks/999
```

### GET com Filtros

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

**Testar:**

```bash
curl "http://localhost:8000/api/tasks?completed=false&priority=high"
curl "http://localhost:8000/api/tasks?q=comprar"
```

---

## UPDATE - Atualizar Recursos

### PUT - Substituir Tarefa Completa

**PUT substitui o recurso inteiro** - todos os campos devem ser enviados.

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

**Testar:**

```bash
curl -X PUT http://localhost:8000/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Comprar leite e pão",
    "description": "Ir ao mercado comprar 2L de leite e 1 pão francês",
    "completed": false,
    "priority": "high"
  }'
```

### PATCH - Atualizar Parcialmente

**PATCH atualiza apenas campos específicos** - envie só o que mudou.

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

**Testar (atualizar apenas completed):**

```bash
curl -X PATCH http://localhost:8000/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"completed": true}'
```

**Testar (atualizar título e prioridade):**

```bash
curl -X PATCH http://localhost:8000/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Novo título",
    "priority": "urgent"
  }'
```

### Marcar Tarefa como Completa (Ação Customizada)

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

**Testar:**

```bash
curl -X PATCH http://localhost:8000/api/tasks/1/complete
```

### Erro de Validação em Atualização

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

## DELETE - Remover Recursos

### DELETE - Remover Tarefa

```yaml
- endpoint:
    method: DELETE
    path: /api/tasks/:id|int
    response:
      status: 204    # 204 No Content (sem body)
```

**Testar:**

```bash
curl -X DELETE http://localhost:8000/api/tasks/1
# Resposta vazia com status 204
```

### DELETE com Confirmação (alternativa)

Alguns preferem retornar 200 com mensagem:

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

**Testar:**

```bash
curl -X DELETE http://localhost:8000/api/tasks/1
```

### DELETE - Tarefa Não Encontrada

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

### DELETE em Massa (Limpar Completadas)

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

**Testar:**

```bash
curl -X DELETE http://localhost:8000/api/tasks/completed
```

---

## API Completa: Arquivo Único

Aqui está o arquivo `tasks-api.yml` completo com todas as operações CRUD:

```yaml
# =====================
# CREATE (POST)
# =====================

# Criar nova tarefa
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

# Listar todas as tarefas
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
            {"id": 1, "title": "Comprar leite", "completed": false},
            {"id": 2, "title": "Estudar moclojer", "completed": true},
            {"id": 3, "title": "Fazer exercícios", "completed": false}
          ],
          "meta": {"total": 3, "page": 1, "perPage": 10}
        }

# Obter tarefa não encontrada (específico antes de genérico!)
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

# Obter tarefa por ID
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

# Atualizar tarefa completa (PUT)
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

# Atualizar parcialmente (PATCH)
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

# Deletar tarefa não encontrada (específico antes!)
- endpoint:
    method: DELETE
    path: /api/tasks/999
    response:
      status: 404
      body: >
        {"error": "Not Found", "message": "Task 999 not found"}

# Deletar tarefa
- endpoint:
    method: DELETE
    path: /api/tasks/:id|int
    response:
      status: 204
```

---

## Testando a API Completa

### Script de Teste Completo

```bash
#!/bin/bash
API_URL="http://localhost:8000/api/tasks"

echo "=== CREATE - Criar tarefa ==="
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{"title": "Nova tarefa", "description": "Teste", "priority": "high"}'
echo -e "\n"

echo "=== READ - Listar todas ==="
curl $API_URL
echo -e "\n"

echo "=== READ - Obter específica ==="
curl $API_URL/1
echo -e "\n"

echo "=== UPDATE - Atualizar completa (PUT) ==="
curl -X PUT $API_URL/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Atualizada", "description": "PUT test", "completed": false, "priority": "medium"}'
echo -e "\n"

echo "=== UPDATE - Atualizar parcial (PATCH) ==="
curl -X PATCH $API_URL/1 \
  -H "Content-Type: application/json" \
  -d '{"completed": true}'
echo -e "\n"

echo "=== DELETE - Remover tarefa ==="
curl -X DELETE $API_URL/1
echo -e "\n"

echo "=== READ - Tarefa não encontrada ==="
curl $API_URL/999
echo -e "\n"
```

Salve como `test-crud.sh`, dê permissão e execute:

```bash
chmod +x test-crud.sh
./test-crud.sh
```

---

## Boas Práticas

### ✅ Faça

1. **Use status codes apropriados**
   - `201 Created` para POST
   - `200 OK` para GET/PUT/PATCH
   - `204 No Content` para DELETE
   - `404 Not Found` quando recurso não existe
   - `422 Unprocessable Entity` para erros de validação

2. **Retorne o recurso criado/atualizado**

   ```json
   // POST response
   {
     "id": 1,
     "...": "dados completos do recurso criado"
   }
   ```

3. **Use rotas específicas antes de genéricas**

   ```yaml
   - path: /api/tasks/999  # 404 específico
   - path: /api/tasks/:id  # genérico (vem depois!)
   ```

4. **Inclua metadata em listas**

   ```json
   {
     "data": [...],
     "meta": {"total": 100, "page": 1}
   }
   ```

5. **Header `Location` em recursos criados**

   ```yaml
   headers:
     Location: /api/tasks/1
   ```

### ❌ Evite

1. **DELETE retornando 200 com recurso deletado**

   ```yaml
   # ❌ Prefira 204 No Content
   DELETE /tasks/1 → 200 {"id": 1, "deleted": true}

   # ✅ Padrão REST
   DELETE /tasks/1 → 204 (sem body)
   ```

2. **GET modificando dados**

   ```yaml
   # ❌ NUNCA!
   GET /tasks/1/delete

   # ✅ Use DELETE
   DELETE /tasks/1
   ```

3. **Campos ID no body de POST**

   ```json
   // ❌ Cliente não deve enviar ID
   POST /tasks {"id": 123, "title": "..."}

   // ✅ Servidor gera o ID
   POST /tasks {"title": "..."}
   // Response: {"id": 1, "title": "..."}
   ```

---

## Próximos Passos

- **[Pagination How-to](pagination.md)** - Implementar paginação
- **[Authentication Mock](authentication-mock.md)** - Simular autenticação
- **[Error Handling](error-handling.md)** - Padrões de erros
- **[HTTP Methods](../../topics/endpoints/http-methods.md)** - Referência de métodos

## Veja Também

- [Your First Mock](../../getting-started/your-first-mock.md) - Tutorial inicial
- [Dynamic Responses](../../getting-started/dynamic-responses.md) - Respostas dinâmicas
- [REST API Example](../../examples/rest-api/basic-crud.md) - Exemplo completo
