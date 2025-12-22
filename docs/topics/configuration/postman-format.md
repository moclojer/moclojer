---
description: >-
  Use suas Postman Collections diretamente com moclojer. Importe Collections
  v2.1, aproveite exemplos de response e crie mocks sem reescrever nada.
---

# Postman Collection Format

moclojer suporta **Postman Collection v2.1**, permitindo que você use suas collections existentes para gerar mocks automaticamente. Não precisa reescrever nada!

## O Que São Postman Collections?

**Postman Collections** são arquivos JSON que agrupam requisições HTTP, muito usados para:
- Documentação de APIs
- Testes de API
- Compartilhamento entre equipe
- **Mock servers** (como o moclojer!)

## Por Que Usar Postman Collections?

✅ **Reutilização**: Use collections existentes do Postman
✅ **Zero config**: moclojer converte automaticamente
✅ **Exemplos prontos**: Responses já documentadas
✅ **Fácil exportar**: Export direto do Postman App
✅ **Padrão popular**: Time todo já usa Postman

---

## Suporte no moclojer

### Versões Suportadas
- ✅ Postman Collection v2.1 (recomendado)
- ⚠️ Postman Collection v2.0 (suporte parcial)

### Formatos Aceitos
- ✅ JSON (`.json`)

---

## Quick Start

### 1. Exportar do Postman

**No Postman App:**
1. Abra sua Collection
2. Clique em `...` (três pontos)
3. **Export** → Collection v2.1
4. Salve como `postman_collection.json`

### 2. Rodar com moclojer

```bash
moclojer --config postman_collection.json
```

### 3. Testar

```bash
# moclojer cria endpoints baseados nas requests da collection
curl http://localhost:8000/users
curl http://localhost:8000/users/1
```

🎉 **Funcionou!** Sem escrever YAML!

---

## Como moclojer Converte Postman Collections

### Estrutura Básica

**Postman Collection:**
```json
{
  "info": {
    "name": "Users API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get Users",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/users"
      },
      "response": [
        {
          "name": "Success",
          "status": "OK",
          "code": 200,
          "header": [
            {
              "key": "Content-Type",
              "value": "application/json"
            }
          ],
          "body": "[{\"id\":1,\"name\":\"Alice\"},{\"id\":2,\"name\":\"Bob\"}]"
        }
      ]
    }
  ]
}
```

**moclojer converte para:**
```yaml
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        [
          {"id": 1, "name": "Alice"},
          {"id": 2, "name": "Bob"}
        ]
```

### Path Variables

**Postman:**
```json
{
  "request": {
    "method": "GET",
    "url": {
      "raw": "{{baseUrl}}/users/:id",
      "path": ["users", ":id"],
      "variable": [
        {
          "key": "id",
          "value": "1"
        }
      ]
    }
  }
}
```

**Convertido para:** `/users/:id`

### Query Parameters

**Postman:**
```json
{
  "request": {
    "method": "GET",
    "url": {
      "raw": "{{baseUrl}}/products?category=electronics&limit=10",
      "query": [
        {"key": "category", "value": "electronics"},
        {"key": "limit", "value": "10"}
      ]
    }
  }
}
```

**moclojer aceita:** `/products?category=electronics&limit=10`

### Request Body

**Postman:**
```json
{
  "request": {
    "method": "POST",
    "url": "{{baseUrl}}/users",
    "body": {
      "mode": "raw",
      "raw": "{\"name\":\"Alice\",\"email\":\"alice@example.com\"}",
      "options": {
        "raw": {
          "language": "json"
        }
      }
    }
  }
}
```

**moclojer usa o exemplo de response:**
```json
{
  "response": [
    {
      "body": "{\"id\":1,\"name\":\"Alice\",\"email\":\"alice@example.com\"}"
    }
  ]
}
```

---

## Exemplo Completo

### Postman Collection

```json
{
  "info": {
    "name": "E-commerce API",
    "description": "API for e-commerce platform",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8000",
      "type": "string"
    }
  ],
  "item": [
    {
      "name": "Products",
      "item": [
        {
          "name": "List Products",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/products?category={{category}}&limit={{limit}}",
              "host": ["{{baseUrl}}"],
              "path": ["products"],
              "query": [
                {"key": "category", "value": "electronics"},
                {"key": "limit", "value": "10"}
              ]
            }
          },
          "response": [
            {
              "name": "Success",
              "status": "OK",
              "code": 200,
              "header": [
                {
                  "key": "Content-Type",
                  "value": "application/json"
                }
              ],
              "body": "{\"products\":[{\"id\":1,\"name\":\"Laptop\",\"price\":999.99},{\"id\":2,\"name\":\"Mouse\",\"price\":29.99}],\"total\":100}"
            }
          ]
        },
        {
          "name": "Get Product",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/products/:id",
              "host": ["{{baseUrl}}"],
              "path": ["products", ":id"],
              "variable": [
                {
                  "key": "id",
                  "value": "1"
                }
              ]
            }
          },
          "response": [
            {
              "name": "Success",
              "status": "OK",
              "code": 200,
              "body": "{\"id\":1,\"name\":\"Laptop\",\"price\":999.99,\"description\":\"High-performance laptop\"}"
            }
          ]
        },
        {
          "name": "Create Product",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/products",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"name\":\"New Product\",\"price\":49.99}"
            }
          },
          "response": [
            {
              "name": "Created",
              "status": "Created",
              "code": 201,
              "body": "{\"id\":3,\"name\":\"New Product\",\"price\":49.99,\"createdAt\":\"2024-01-15T10:00:00Z\"}"
            }
          ]
        }
      ]
    },
    {
      "name": "Users",
      "item": [
        {
          "name": "Get Users",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/users"
          },
          "response": [
            {
              "name": "Success",
              "code": 200,
              "body": "[{\"id\":1,\"name\":\"Alice\"},{\"id\":2,\"name\":\"Bob\"}]"
            }
          ]
        }
      ]
    }
  ]
}
```

### Rodar

```bash
moclojer --config ecommerce_collection.json
```

### Testar

```bash
# Listar produtos
curl "http://localhost:8000/products?category=electronics&limit=10"

# Produto específico
curl http://localhost:8000/products/1

# Criar produto
curl -X POST http://localhost:8000/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Mouse", "price": 29.99}'

# Listar usuários
curl http://localhost:8000/users
```

---

## Nested Folders

Postman Collections suportam pastas aninhadas:

```json
{
  "item": [
    {
      "name": "API v1",
      "item": [
        {
          "name": "Users",
          "item": [
            {
              "name": "List Users",
              "request": {
                "method": "GET",
                "url": "/v1/users"
              }
            }
          ]
        },
        {
          "name": "Products",
          "item": [
            {
              "name": "List Products",
              "request": {
                "method": "GET",
                "url": "/v1/products"
              }
            }
          ]
        }
      ]
    }
  ]
}
```

**moclojer processa recursivamente** todos os níveis de folders.

---

## Variáveis

### Collection Variables

```json
{
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8000"
    },
    {
      "key": "apiVersion",
      "value": "v1"
    }
  ],
  "item": [
    {
      "request": {
        "url": "{{baseUrl}}/{{apiVersion}}/users"
      }
    }
  ]
}
```

**moclojer resolve variáveis automaticamente:**
- `{{baseUrl}}` → ignora (usa host do moclojer)
- `{{apiVersion}}` → `/v1/users`

### Environment Variables

**Postman Environments** não são suportados diretamente.

**Workaround:** Exporte collection com valores já resolvidos:
1. No Postman, selecione Environment
2. Export Collection (valores serão inline)

---

## Headers

### Request Headers

```json
{
  "request": {
    "header": [
      {
        "key": "Authorization",
        "value": "Bearer token123"
      },
      {
        "key": "Content-Type",
        "value": "application/json"
      },
      {
        "key": "X-Custom-Header",
        "value": "custom-value",
        "disabled": false
      }
    ]
  }
}
```

**moclojer:** Headers de request são informacionais (não validados).

### Response Headers

```json
{
  "response": [
    {
      "header": [
        {
          "key": "Content-Type",
          "value": "application/json"
        },
        {
          "key": "X-RateLimit-Limit",
          "value": "100"
        }
      ]
    }
  ]
}
```

**moclojer adiciona esses headers à resposta.**

---

## Múltiplas Responses (Examples)

Postman permite múltiplos exemplos por request:

```json
{
  "name": "Get User",
  "request": {
    "method": "GET",
    "url": "/users/:id"
  },
  "response": [
    {
      "name": "Success",
      "code": 200,
      "body": "{\"id\":1,\"name\":\"Alice\"}"
    },
    {
      "name": "Not Found",
      "code": 404,
      "body": "{\"error\":\"User not found\"}"
    },
    {
      "name": "Server Error",
      "code": 500,
      "body": "{\"error\":\"Internal server error\"}"
    }
  ]
}
```

**moclojer usa o primeiro exemplo** (geralmente 200 OK).

**Para simular erros:** Crie requests separadas
```json
{
  "name": "Get User - Not Found",
  "request": {
    "url": "/users/999"
  },
  "response": [
    {
      "code": 404,
      "body": "{\"error\":\"User not found\"}"
    }
  ]
}
```

---

## Scripts e Tests

Postman Collections podem ter scripts Pre-request e Tests:

```json
{
  "event": [
    {
      "listen": "prerequest",
      "script": {
        "exec": [
          "pm.variables.set('timestamp', Date.now());"
        ]
      }
    },
    {
      "listen": "test",
      "script": {
        "exec": [
          "pm.test('Status is 200', () => {",
          "  pm.response.to.have.status(200);",
          "});"
        ]
      }
    }
  ]
}
```

**moclojer ignora scripts.** Eles são para execução no Postman, não mock.

---

## Authentication

### Bearer Token

```json
{
  "auth": {
    "type": "bearer",
    "bearer": [
      {
        "key": "token",
        "value": "{{accessToken}}"
      }
    ]
  }
}
```

**moclojer:** Auth é informacional (não valida tokens).

### API Key

```json
{
  "auth": {
    "type": "apikey",
    "apikey": [
      {
        "key": "in",
        "value": "header"
      },
      {
        "key": "key",
        "value": "X-API-Key"
      },
      {
        "key": "value",
        "value": "{{apiKey}}"
      }
    ]
  }
}
```

---

## Limitações e Workarounds

### 1. Múltiplas Responses

**Limitação:** moclojer usa apenas o primeiro `response`.

**Workaround:** Crie requests separadas para cada cenário:
```json
// Request 1: Success
{"name": "Get User - Success", "url": "/users/1", "response": [{"code": 200}]}

// Request 2: Not Found
{"name": "Get User - Not Found", "url": "/users/999", "response": [{"code": 404}]}
```

### 2. Scripts Não Executados

**Limitação:** Pre-request e Test scripts são ignorados.

**Workaround:** Use scripts apenas no Postman, não para lógica de mock.

### 3. Environment Variables

**Limitação:** Environments não são carregados.

**Workaround:** Exporte collection com valores inline.

### 4. Validação de Request

**Limitação:** moclojer não valida se request body está correto.

**Workaround:** Use ferramentas como Postman Runner ou Newman para validação.

---

## Postman vs YAML nativo

| Aspecto | Postman Collection | moclojer YAML |
|---------|-------------------|---------------|
| **Reutilização** | Collections existentes | Precisa criar do zero |
| **Ferramentas** | Postman App completo | Editor de texto |
| **Verbosidade** | JSON verboso | YAML conciso |
| **Dinâmico** | Exemplos estáticos | Templates dinâmicos |
| **Curva aprendizado** | Maior (Postman) | Menor (YAML) |
| **Compartilhamento** | Fácil (workspace) | Arquivos Git |

**Quando usar Postman:**
- Já tem collections prontas
- Time usa Postman diariamente
- Quer interface visual
- Precisa de documentação rica

**Quando usar YAML:**
- Quer templates dinâmicos (`{{path-params.id}}`)
- Precisa de config minimalista
- Versionar com Git
- Automação em CI/CD

---

## Convertendo Postman → moclojer YAML

Se quiser converter permanentemente:

**Opção 1: Manual**
1. Exportar Postman Collection
2. Ler JSON e reescrever em YAML

**Opção 2: Script (criar um)**
```javascript
// postman-to-moclojer.js
const collection = require('./collection.json');

const endpoints = collection.item.map(item => ({
  endpoint: {
    method: item.request.method,
    path: extractPath(item.request.url),
    response: {
      status: item.response[0].code,
      body: item.response[0].body
    }
  }
}));

console.log(YAML.stringify(endpoints));
```

---

## Boas Práticas

### ✅ Faça

1. **Use exemplos de response**
   ```json
   {
     "response": [
       {
         "name": "Success Example",
         "code": 200,
         "body": "..."  // ← Sempre inclua!
       }
     ]
   }
   ```

2. **Organize com folders**
   ```json
   {
     "item": [
       {
         "name": "Users",
         "item": [...]  // Agrupe por recurso
       },
       {
         "name": "Products",
         "item": [...]
       }
     ]
   }
   ```

3. **Documente requests**
   ```json
   {
     "name": "Get User by ID",
     "request": {
       "description": "Retrieves a single user by their unique ID"
     }
   }
   ```

4. **Versionamento no nome**
   ```json
   {
     "info": {
       "name": "My API v2.1",
       "version": "2.1.0"
     }
   }
   ```

### ❌ Evite

1. **Collections sem examples**
   ```json
   // ❌ moclojer não sabe o que retornar
   {
     "response": []
   }
   ```

2. **URLs absolutas hardcoded**
   ```json
   // ❌ Use variáveis
   "url": "http://localhost:8000/users"

   // ✅ Use {{baseUrl}}
   "url": "{{baseUrl}}/users"
   ```

3. **Dependência de scripts**
   ```json
   // ❌ Scripts não rodam em moclojer
   "event": [{"script": {"exec": ["..."]}}]
   ```

---

## Troubleshooting

### "Invalid Postman Collection format"

**Causa:** Versão não suportada ou JSON inválido.

**Solução:**
```bash
# Validar JSON
cat postman_collection.json | jq .

# Verificar schema version
cat postman_collection.json | jq '.info.schema'
# Deve ser v2.1.0
```

### "No responses found"

**Causa:** Requests sem exemplos de response.

**Solução:** Adicionar examples no Postman:
1. Faça request no Postman
2. Clique "Save Response" → "Save as Example"
3. Re-export Collection

### "Path variables not working"

**Causa:** Sintaxe incorreta.

**Solução:** Use `:paramName` no Postman
```json
{
  "url": {
    "path": ["users", ":id"]  // ✅ Correto
  }
}
```

---

## Próximos Passos

- **[OpenAPI Format](openapi-format.md)** - Importar specs OpenAPI
- **[YAML Format](yaml-format.md)** - Sintaxe nativa do moclojer
- **[Path Parameters](../parameters/path-parameters.md)** - Parâmetros dinâmicos
- **[Your First Mock](../../getting-started/postman-collections.md)** - Tutorial inicial

## Veja Também

- [Postman Collection Format v2.1](https://schema.postman.com/json/collection/v2.1.0/docs/index.html)
- [Postman Documentation](https://learning.postman.com/docs/getting-started/introduction/)
- [Configuration Spec](../../reference/configuration-spec.md) - Referência do moclojer
