---
description: >-
  Aprenda a sintaxe YAML para configurar seus mocks no moclojer. Guia completo
  com exemplos práticos e boas práticas.
---

# Formato YAML de Configuração

O YAML é o formato de configuração mais comum no moclojer. Ele é simples de ler e escrever, não requer conhecimento de programação, e é perfeito para definir APIs mock de forma declarativa.

## Por que YAML?

**Vantagens:**
- ✅ **Legível**: Parece inglês, fácil de entender
- ✅ **Simples**: Menos verboso que JSON ou XML
- ✅ **Estruturado**: Mantém hierarquia clara
- ✅ **Comentários**: Pode documentar inline

**Quando usar YAML:**
- Você está começando com moclojer
- Precisa de configuração simples e clara
- Quer colaborar com não-programadores
- Prefere arquivos de configuração legíveis

## Estrutura Básica

Todo arquivo YAML do moclojer é uma **lista de endpoints**:

```yaml
- endpoint:
    method: GET
    path: /hello
    response:
      status: 200
      body: "Hello, World!"

- endpoint:
    method: POST
    path: /users
    response:
      status: 201
      body: '{"id": 1, "name": "Alice"}'
```

**Anatomia:**
- Cada endpoint começa com `- endpoint:`
- Indentação com **2 espaços** (não tabs!)
- Chaves obrigatórias: `path`, `response`
- Chave opcional mas recomendada: `method`

## Sintaxe YAML Essencial

### 1. Indentação

A indentação define a hierarquia:

```yaml
- endpoint:           # Nível 0
    method: GET       # Nível 1 (2 espaços)
    path: /users      # Nível 1
    response:         # Nível 1
      status: 200     # Nível 2 (4 espaços)
      body: "data"    # Nível 2
```

⚠️ **IMPORTANTE:**
- Use sempre **2 espaços** por nível
- Nunca misture espaços e tabs
- Ferramentas: configure seu editor para "soft tabs"

### 2. Strings

Três formas de escrever strings:

```yaml
# 1. Sem aspas (para textos simples)
path: /users

# 2. Com aspas duplas (quando tem caracteres especiais)
body: "Hello, \"World\"!"

# 3. Multi-linha com > (remove quebras de linha)
body: >
  Este texto muito longo
  será convertido em uma
  única linha.

# 4. Multi-linha com | (preserva quebras de linha)
body: |
  Linha 1
  Linha 2
  Linha 3
```

**Para JSON no body, use `>`:**

```yaml
body: >
  {
    "name": "Alice",
    "email": "alice@example.com"
  }
```

### 3. Números e Booleanos

```yaml
# Números (sem aspas)
status: 200
max-requests: 100

# Booleanos
enabled: true
disabled: false
```

### 4. Listas

```yaml
# Lista inline
tags: [moclojer, api, testing]

# Lista multi-linha (recomendado)
tags:
  - moclojer
  - api
  - testing
```

### 5. Objetos (Mapas)

```yaml
# Inline
headers: {Content-Type: application/json, X-Custom: value}

# Multi-linha (recomendado)
headers:
  Content-Type: application/json
  X-Custom: value
```

### 6. Comentários

```yaml
# Isto é um comentário
- endpoint:  # Comentário inline
    method: GET
    path: /users
    # TODO: adicionar paginação
    response:
      status: 200
```

## Exemplo Completo Comentado

```yaml
# API de Usuários - Exemplo Completo
# Autor: Equipe Dev
# Última atualização: 2024-01-15

# Endpoint 1: Listar todos os usuários
- endpoint:
    method: GET                    # Método HTTP
    path: /users                   # Caminho da URL
    response:                      # Configuração da resposta
      status: 200                  # HTTP 200 OK
      headers:                     # Headers da resposta
        Content-Type: application/json
        X-Total-Count: "3"
      body: >                      # JSON inline (quebras removidas)
        [
          {"id": 1, "name": "Alice"},
          {"id": 2, "name": "Bob"},
          {"id": 3, "name": "Carol"}
        ]

# Endpoint 2: Obter usuário por ID
- endpoint:
    method: GET
    path: /users/:id               # :id é um parâmetro dinâmico
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": "{{path-params.id}}",
          "name": "User {{path-params.id}}",
          "email": "user{{path-params.id}}@example.com"
        }

# Endpoint 3: Criar novo usuário
- endpoint:
    method: POST
    path: /users
    response:
      status: 201                  # HTTP 201 Created
      headers:
        Content-Type: application/json
        Location: /users/4
      body: >
        {
          "id": 4,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}",
          "created_at": "2024-01-15T10:30:00Z"
        }

# Endpoint 4: Erro - Usuário não encontrado
- endpoint:
    method: GET
    path: /users/999
    response:
      status: 404                  # HTTP 404 Not Found
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "User not found",
          "code": "USER_NOT_FOUND",
          "message": "User with ID 999 does not exist"
        }
```

## Boas Práticas

### ✅ Faça

1. **Use indentação consistente (2 espaços)**
   ```yaml
   - endpoint:
       method: GET    # 2 espaços
       path: /users   # 2 espaços
   ```

2. **Adicione comentários explicativos**
   ```yaml
   # Health check endpoint para monitoramento
   - endpoint:
       method: GET
       path: /health
   ```

3. **Use `>` para JSON inline**
   ```yaml
   body: >
     {"key": "value"}
   ```

4. **Agrupe endpoints relacionados**
   ```yaml
   # === USER ENDPOINTS ===
   - endpoint: ...
   - endpoint: ...

   # === PRODUCT ENDPOINTS ===
   - endpoint: ...
   ```

5. **Ordene por método e path**
   ```yaml
   - GET /users
   - GET /users/:id
   - POST /users
   - PUT /users/:id
   - DELETE /users/:id
   ```

### ❌ Evite

1. **Tabs para indentação**
   ```yaml
   - endpoint:
   	method: GET    # ❌ Tab causa erro
   ```

2. **Aspas desnecessárias**
   ```yaml
   method: "GET"       # ❌ Desnecessário
   method: GET         # ✅ Melhor
   ```

3. **JSON sem `>`**
   ```yaml
   body: {"key": "value"}   # ❌ Pode quebrar com strings complexas
   body: >                  # ✅ Sempre funciona
     {"key": "value"}
   ```

4. **Endpoints sem comentários em arquivos grandes**
   ```yaml
   # ✅ Boa prática em arquivos grandes
   # Autenticação - Login de usuário
   - endpoint:
       method: POST
       path: /auth/login
   ```

## Troubleshooting YAML

### Problema: "YAML parse error: mapping values are not allowed"

**Causa:** Dois-pontos sem espaço ou aspas

```yaml
# ❌ Errado
path: http://example.com    # dois-pontos em URL confunde parser

# ✅ Correto
path: "http://example.com"  # use aspas
```

### Problema: "YAML parse error: did not find expected key"

**Causa:** Indentação incorreta

```yaml
# ❌ Errado
- endpoint:
  method: GET     # Deveria ter 4 espaços (2 níveis)

# ✅ Correto
- endpoint:
    method: GET   # 4 espaços
```

### Problema: JSON quebrado no body

**Causa:** Não usou `>` para multi-linha

```yaml
# ❌ Errado
body: {
  "key": "value"
}

# ✅ Correto
body: >
  {
    "key": "value"
  }
```

## Validação de YAML

### Online
- [YAML Lint](http://www.yamllint.com/) - valida sintaxe
- [YAML to JSON](https://onlineyamltools.com/convert-yaml-to-json) - vê como será parseado

### Editores
- **VS Code**: extensão "YAML" by Red Hat
- **Sublime**: extensão "YAML Nav"
- **Vim**: plugin "vim-yaml"

### Linha de comando
```bash
# Validar sintaxe
yamllint moclojer.yml

# Ver como moclojer vai parsear
moclojer --validate moclojer.yml
```

## Comparação com Outros Formatos

### YAML vs JSON

```yaml
# YAML - Mais legível
- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        {"users": []}
```

```json
// JSON - Mais verboso
[
  {
    "endpoint": {
      "method": "GET",
      "path": "/users",
      "response": {
        "status": 200,
        "body": "{\"users\": []}"
      }
    }
  }
]
```

**YAML ganha em:**
- Legibilidade (50% menos caracteres)
- Comentários nativos
- Strings multi-linha

**JSON ganha em:**
- Parsing mais rápido
- Suporte universal

### YAML vs EDN

Para a maioria dos usuários, **YAML é mais simples**. Use EDN apenas se você:
- Trabalha com Clojure
- Precisa de estruturas de dados complexas
- Quer integração programática

Veja [EDN Format Guide](edn-format.md) para detalhes.

## Próximos Passos

Agora que você domina YAML, explore:

1. **[Path Parameters](../parameters/path-parameters.md)** - Parâmetros dinâmicos em URLs
2. **[Template Variables](../templates/template-variables.md)** - Respostas dinâmicas
3. **[Configuration Spec](../../reference/configuration-spec.md)** - Referência completa

## Veja Também

- [OpenAPI Format](openapi-format.md) - Importar specs OpenAPI
- [Postman Format](postman-format.md) - Usar Postman Collections
- [YAML Specification](https://yaml.org/spec/1.2/spec.html) - Spec oficial YAML 1.2
