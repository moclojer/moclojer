---
description: >-
  Contract testing with moclojer - validate API contracts, OpenAPI specs, and
  ensure frontend-backend compatibility with automated testing.
---

# Contract Testing

Use moclojer to validate API contracts between frontend and backend teams.

## 🎯 Approach

1. **Define contract** (OpenAPI spec)
2. **Mock with moclojer** (frontend development)
3. **Validate backend** matches contract

## 📝 OpenAPI Contract

```yaml
# api-contract.yaml (OpenAPI 3.0)
openapi: 3.0.0
info:
  title: Users API
  version: 1.0.0
paths:
  /users:
    get:
      responses:
        '200':
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
```

## 🧪 Test Contract

```javascript
// contract.test.js
const Ajv = require('ajv');
const ajv = new Ajv();

test('mock response matches schema', async () => {
  const response = await fetch('http://localhost:8000/users');
  const data = await response.json();
  
  const schema = {
    type: 'array',
    items: {
      type: 'object',
      properties: {
        id: { type: 'number' },
        name: { type: 'string' }
      },
      required: ['id', 'name']
    }
  };
  
  const validate = ajv.compile(schema);
  expect(validate(data)).toBe(true);
});
```

## 📚 See Also

- **[OpenAPI Format](../../topics/configuration/openapi-format.md)**
- **[Integration Testing](integration-testing.md)**
