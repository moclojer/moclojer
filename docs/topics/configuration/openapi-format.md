---
description: >-
  Aprenda a usar especificações OpenAPI (Swagger) com moclojer. Importe suas
  specs OpenAPI 3.0/3.1 e gere mocks automaticamente.
---

# OpenAPI Format (Swagger)

Moclojer suporta **OpenAPI 3.0 e 3.1** (anteriormente conhecido como Swagger), permitindo que você use suas especificações de API existentes para gerar mocks automaticamente. Não precisa reescrever nada em YAML do moclojer!

## O Que É OpenAPI?

**OpenAPI Specification** é um padrão para descrever APIs RESTful de forma legível por máquinas. É amplamente usado para:
- Documentação de APIs
- Geração automática de SDKs
- Validação de contratos
- **Mock servers** (como o moclojer!)

## Por Que Usar OpenAPI com Moclojer?

✅ **Reutilização**: Use suas specs OpenAPI existentes
✅ **Padronização**: OpenAPI é um padrão da indústria
✅ **Zero configuração**: Moclojer converte automaticamente
✅ **Validação**: Specs OpenAPI incluem schemas de validação
✅ **Documentação viva**: Sua spec é documentação + mock

## Suporte OpenAPI no Moclojer

### Versões Suportadas
- ✅ OpenAPI 3.0.x
- ✅ OpenAPI 3.1.x
- ⚠️ Swagger 2.0 (suporte parcial - recomenda-se converter para 3.x)

### Formatos Aceitos
- ✅ JSON (`.json`)
- ✅ YAML (`.yml`, `.yaml`)

## Início Rápido

### 1. Criar ou Obter uma Spec OpenAPI

Exemplo mínimo (`openapi.yml`):

```yaml
openapi: 3.0.0
info:
  title: Users API
  version: 1.0.0
paths:
  /users:
    get:
      summary: List all users
      responses:
        '200':
          description: Success
          content:
            application/json:
              schema:
                type: array
                items:
                  type: object
                  properties:
                    id:
                      type: integer
                    name:
                      type: string
              example:
                - id: 1
                  name: Alice
                - id: 2
                  name: Bob
```

### 2. Iniciar Moclojer com OpenAPI

```bash
# Usando arquivo local
moclojer --config openapi.yml

# Ou especificar porta
moclojer --config openapi.yml --port 3000

# Habilitar CORS
moclojer --config openapi.yml --enable-cors
```

### 3. Testar

```bash
curl http://localhost:8000/users
```

**Resposta:**
```json
[
  {"id": 1, "name": "Alice"},
  {"id": 2, "name": "Bob"}
]
```

🎉 **Pronto!** Moclojer converteu automaticamente sua spec OpenAPI em endpoints funcionais.

---

## Como Moclojer Converte OpenAPI

### Conversão Automática

Moclojer detecta automaticamente que é uma spec OpenAPI e converte:

```yaml
# OpenAPI spec
paths:
  /users/{id}:          # → path: /users/:id
    get:                # → method: GET
      responses:
        '200':          # → status: 200
          content:
            application/json:
              example:  # → body
                id: 1
                name: John
```

**Resulta em (equivalente moclojer interno):**
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
          "id": 1,
          "name": "John"
        }
```

### Path Parameters

OpenAPI usa `{param}`, moclojer converte para `:param`:

```yaml
# OpenAPI
paths:
  /users/{userId}/posts/{postId}:
    get:
      parameters:
        - name: userId
          in: path
          schema:
            type: integer
        - name: postId
          in: path
          schema:
            type: integer
```

**Convertido para:** `/users/:userId/posts/:postId`

**Com tipos:**
- `type: integer` → `:userId|int`
- `type: string` → `:userId|string`

### Query Parameters

```yaml
# OpenAPI
paths:
  /products:
    get:
      parameters:
        - name: category
          in: query
          schema:
            type: string
        - name: limit
          in: query
          schema:
            type: integer
```

**Moclojer entende e aceita:**
```bash
curl "http://localhost:8000/products?category=electronics&limit=10"
```

### Request Body

```yaml
# OpenAPI
paths:
  /users:
    post:
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                name:
                  type: string
                email:
                  type: string
            example:
              name: John Doe
              email: john@example.com
```

**Moclojer usa o `example` para a resposta.**

---

## Exemplos OpenAPI Completos

### Exemplo 1: API Simples de Usuários

```yaml
openapi: 3.0.0
info:
  title: Users API
  description: API for managing users
  version: 1.0.0

servers:
  - url: http://localhost:8000
    description: Local mock server

paths:
  /users:
    get:
      summary: List all users
      operationId: listUsers
      tags:
        - users
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            default: 1
        - name: limit
          in: query
          schema:
            type: integer
            default: 10
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                type: object
                properties:
                  data:
                    type: array
                    items:
                      $ref: '#/components/schemas/User'
                  meta:
                    type: object
                    properties:
                      total:
                        type: integer
                      page:
                        type: integer
              example:
                data:
                  - id: 1
                    name: Alice Johnson
                    email: alice@example.com
                  - id: 2
                    name: Bob Smith
                    email: bob@example.com
                meta:
                  total: 100
                  page: 1

    post:
      summary: Create a new user
      operationId: createUser
      tags:
        - users
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserInput'
            example:
              name: Carol Davis
              email: carol@example.com
      responses:
        '201':
          description: User created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
              example:
                id: 3
                name: Carol Davis
                email: carol@example.com
                createdAt: "2024-01-15T10:00:00Z"

  /users/{id}:
    get:
      summary: Get user by ID
      operationId: getUserById
      tags:
        - users
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: User found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
              example:
                id: 1
                name: Alice Johnson
                email: alice@example.com
                createdAt: "2024-01-01T00:00:00Z"
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
              example:
                error: "Not Found"
                message: "User with ID 999 not found"

    put:
      summary: Update user
      operationId: updateUser
      tags:
        - users
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserInput'
      responses:
        '200':
          description: User updated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'

    delete:
      summary: Delete user
      operationId: deleteUser
      tags:
        - users
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '204':
          description: User deleted

components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
          format: int64
        name:
          type: string
        email:
          type: string
          format: email
        createdAt:
          type: string
          format: date-time

    UserInput:
      type: object
      required:
        - name
        - email
      properties:
        name:
          type: string
          minLength: 1
        email:
          type: string
          format: email

    Error:
      type: object
      properties:
        error:
          type: string
        message:
          type: string
```

**Testar:**
```bash
# Listar usuários
curl http://localhost:8000/users

# Com paginação
curl "http://localhost:8000/users?page=1&limit=5"

# Obter usuário específico
curl http://localhost:8000/users/1

# Criar usuário
curl -X POST http://localhost:8000/users \
  -H "Content-Type: application/json" \
  -d '{"name": "New User", "email": "new@example.com"}'

# Atualizar usuário
curl -X PUT http://localhost:8000/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Updated Name", "email": "updated@example.com"}'

# Deletar usuário
curl -X DELETE http://localhost:8000/users/1
```

### Exemplo 2: API de E-commerce

```yaml
openapi: 3.0.0
info:
  title: E-commerce API
  version: 1.0.0

paths:
  /products:
    get:
      summary: List products
      parameters:
        - name: category
          in: query
          schema:
            type: string
        - name: minPrice
          in: query
          schema:
            type: number
        - name: maxPrice
          in: query
          schema:
            type: number
      responses:
        '200':
          description: Products list
          content:
            application/json:
              example:
                products:
                  - id: 1
                    name: "Laptop"
                    price: 999.99
                    category: "electronics"
                  - id: 2
                    name: "Mouse"
                    price: 29.99
                    category: "electronics"

  /products/{productId}:
    get:
      summary: Get product details
      parameters:
        - name: productId
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: Product details
          content:
            application/json:
              example:
                id: 1
                name: "Laptop"
                description: "High-performance laptop"
                price: 999.99
                category: "electronics"
                stock: 50

  /cart:
    post:
      summary: Add to cart
      requestBody:
        content:
          application/json:
            example:
              productId: 1
              quantity: 2
      responses:
        '200':
          description: Added to cart
          content:
            application/json:
              example:
                cartId: "cart-123"
                items:
                  - productId: 1
                    quantity: 2
                    subtotal: 1999.98
                total: 1999.98

  /orders:
    post:
      summary: Create order
      requestBody:
        content:
          application/json:
            example:
              cartId: "cart-123"
              shippingAddress:
                street: "123 Main St"
                city: "New York"
                zipCode: "10001"
      responses:
        '201':
          description: Order created
          content:
            application/json:
              example:
                orderId: "order-456"
                status: "pending"
                total: 1999.98
                createdAt: "2024-01-15T10:00:00Z"
```

---

## Múltiplas Respostas por Status Code

OpenAPI permite definir múltiplas respostas:

```yaml
paths:
  /users/{id}:
    get:
      responses:
        '200':
          description: Success
          content:
            application/json:
              example:
                id: 1
                name: "John"
        '404':
          description: Not found
          content:
            application/json:
              example:
                error: "User not found"
        '500':
          description: Server error
          content:
            application/json:
              example:
                error: "Internal server error"
```

**Moclojer usa o primeiro `example` encontrado (geralmente 200).**

Para simular erros, crie endpoints específicos:
```yaml
# No moclojer, adicione endpoints separados para erros
- endpoint:
    path: /users/999
    response:
      status: 404
      body: '{"error": "User not found"}'
```

---

## Headers e Content-Type

OpenAPI define headers automaticamente:

```yaml
responses:
  '200':
    description: Success
    headers:
      X-RateLimit-Limit:
        schema:
          type: integer
        example: 100
      X-RateLimit-Remaining:
        schema:
          type: integer
        example: 99
    content:
      application/json:
        example:
          data: []
```

**Moclojer adiciona automaticamente:**
- `Content-Type` baseado em `content`
- Headers customizados definidos em `headers`

---

## Schemas e $ref

OpenAPI usa `$ref` para reutilizar schemas:

```yaml
components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
        name:
          type: string

paths:
  /users/{id}:
    get:
      responses:
        '200':
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
              example:
                id: 1
                name: "Alice"
```

**Moclojer resolve `$ref` e usa o `example` fornecido.**

---

## Security Schemes (Autenticação)

OpenAPI define esquemas de segurança:

```yaml
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

security:
  - bearerAuth: []

paths:
  /protected:
    get:
      summary: Protected endpoint
      security:
        - bearerAuth: []
      responses:
        '200':
          description: Success
          content:
            application/json:
              example:
                message: "You are authenticated!"
        '401':
          description: Unauthorized
          content:
            application/json:
              example:
                error: "Unauthorized"
```

**Nota:** Moclojer **não valida autenticação** automaticamente. Para simular:

```yaml
# Adicionar endpoint para token inválido
- endpoint:
    path: /protected
    response:
      status: 401
      body: '{"error": "Unauthorized"}'

# E endpoint para sucesso (deve vir depois)
- endpoint:
    path: /protected
    response:
      status: 200
      body: '{"message": "You are authenticated!"}'
```

---

## Ferramentas Úteis

### Editores OpenAPI

1. **Swagger Editor** (online)
   - https://editor.swagger.io/
   - Valida spec em tempo real

2. **VS Code Extension**
   - "OpenAPI (Swagger) Editor" by 42Crunch
   - Autocompletion e validação

3. **Stoplight Studio**
   - https://stoplight.io/studio
   - Editor visual

### Validação de Specs

```bash
# Swagger CLI (Node.js)
npm install -g @apidevtools/swagger-cli
swagger-cli validate openapi.yml

# Spectral (linting avançado)
npm install -g @stoplight/spectral-cli
spectral lint openapi.yml
```

### Geração de Specs

```bash
# A partir de código existente
# Spring Boot (Java)
# FastAPI (Python) gera automaticamente
# Express (Node.js) com swagger-jsdoc
```

---

## Limitações e Workarounds

### 1. Validação de Request

**Limitação:** Moclojer não valida requests contra o schema.

```yaml
# Schema define que 'name' é obrigatório
requestBody:
  schema:
    required: ['name']
```

**Moclojer aceita qualquer request**, mesmo sem `name`.

**Workaround:** Use ferramentas como Prism para validação real.

### 2. Múltiplas Respostas por Status

**Limitação:** Moclojer usa apenas um `example` por endpoint.

**Workaround:** Crie endpoints separados para cada status code.

### 3. Callbacks e Links

**Limitação:** OpenAPI 3.x suporta callbacks e links complexos, moclojer ignora.

### 4. oneOf, anyOf, allOf

**Limitação:** Schemas complexos não são processados.

**Workaround:** Use `example` explícito.

---

## OpenAPI vs YAML Nativo do Moclojer

| Aspecto | OpenAPI | Moclojer YAML |
|---------|---------|---------------|
| **Padrão** | Indústria (portável) | Específico moclojer |
| **Verbosidade** | Mais verboso | Mais conciso |
| **Ferramentas** | Muitas (editores, validadores) | Poucas |
| **Documentação** | Spec = documentação | Apenas mock |
| **Dinâmico** | Exemplos estáticos | Templates dinâmicos |
| **Validação** | Schema validation (com ferramentas) | Nenhuma |
| **Curva de aprendizado** | Maior (spec complexa) | Menor (YAML simples) |

**Quando usar OpenAPI:**
- Já tem specs OpenAPI existentes
- Quer gerar SDKs/documentação
- Precisa de padronização entre equipes
- API vai além de mocks (produção)

**Quando usar YAML nativo:**
- Quer respostas dinâmicas com templates
- Precisa de mocks rápidos e simples
- Não precisa de portabilidade
- Quer configuração minimalista

---

## Combinando OpenAPI + Moclojer YAML

Você pode usar **ambos** no mesmo projeto:

```bash
# Estrutura de arquivos
project/
├── openapi.yml        # Spec OpenAPI (endpoints principais)
├── mocks-extras.yml   # Mocks customizados com templates
└── mocks-errors.yml   # Simulações de erro
```

**Iniciar com múltiplos arquivos:**
```bash
# Moclojer não suporta múltiplos configs diretamente
# Workaround: combinar em um arquivo ou usar proxy
```

**Alternativa:** Converter OpenAPI para moclojer YAML:
```bash
# Ferramentas de conversão (criar um script)
# openapi.yml → moclojer.yml
```

---

## Boas Práticas

### ✅ Faça

1. **Use `examples` em todos os endpoints**
   ```yaml
   responses:
     '200':
       content:
         application/json:
           example:  # ← SEMPRE inclua!
             id: 1
             name: "John"
   ```

2. **Defina tipos de path parameters**
   ```yaml
   parameters:
     - name: id
       in: path
       schema:
         type: integer  # → Moclojer usa :id|int
   ```

3. **Organize com tags**
   ```yaml
   paths:
     /users:
       get:
         tags: [users]
     /products:
       get:
         tags: [products]
   ```

4. **Use `$ref` para reutilizar**
   ```yaml
   components:
     schemas:
       Error:
         type: object
   paths:
     /users:
       get:
         responses:
           '404':
             content:
               application/json:
                 schema:
                   $ref: '#/components/schemas/Error'
   ```

### ❌ Evite

1. **Specs sem `examples`**
   ```yaml
   # ❌ Moclojer não sabe o que retornar
   responses:
     '200':
       content:
         application/json:
           schema:
             type: object
   ```

2. **Caminhos complexos sem tipos**
   ```yaml
   # ⚠️ Sem tipo, aceita qualquer string
   /users/{id}:  # Defina type: integer!
   ```

---

## Troubleshooting

### "OpenAPI spec not detected"

**Causa:** Falta campo obrigatório `openapi`.

**Solução:**
```yaml
# ✅ Adicione no topo
openapi: 3.0.0
info:
  title: My API
  version: 1.0.0
```

### "No examples found"

**Causa:** Spec sem `example` ou `examples`.

**Solução:** Adicione examples explícitos:
```yaml
responses:
  '200':
    content:
      application/json:
        example:  # ← Adicione isto!
          data: []
```

### "Path parameters not working"

**Causa:** Sintaxe incorreta.

**Solução:**
```yaml
# ✅ OpenAPI usa {param}
/users/{id}:

# ❌ Não use :param no OpenAPI
/users/:id:
```

---

## Próximos Passos

- **[Postman Format](postman-format.md)** - Usar Postman Collections
- **[YAML Format](yaml-format.md)** - Sintaxe nativa do moclojer
- **[Path Parameters](../parameters/path-parameters.md)** - Parâmetros dinâmicos
- **[HTTP Methods](../endpoints/http-methods.md)** - GET, POST, PUT, DELETE

## Veja Também

- [OpenAPI 3.1 Specification](https://spec.openapis.org/oas/v3.1.0)
- [Swagger Editor](https://editor.swagger.io/)
- [OpenAPI Examples](https://github.com/OAI/OpenAPI-Specification/tree/main/examples)
- [Configuration Spec](../../reference/configuration-spec.md)
