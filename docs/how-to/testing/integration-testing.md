---
description: >-
  Integration testing with moclojer - test your application against mock APIs,
  CI/CD integration, and automated testing workflows.
---

# Integration Testing with moclojer

Use moclojer for integration testing to validate your application against mock APIs.

## 🎯 Setup

### 1. Start Mock Server in Tests

```javascript
// test/setup.js
const { spawn } = require('child_process');

let mockServer;

beforeAll(async () => {
  mockServer = spawn('moclojer', ['--config', 'test/mocks.yml', '--port', '8000']);
  await new Promise(resolve => setTimeout(resolve, 2000)); // Wait for server
});

afterAll(() => {
  mockServer.kill();
});
```

### 2. Configure API Client

```javascript
// test/api.test.js
const API_URL = process.env.API_URL || 'http://localhost:8000';

test('fetches users', async () => {
  const response = await fetch(`${API_URL}/users`);
  const data = await response.json();
  
  expect(response.status).toBe(200);
  expect(data.users).toHaveLength(2);
});
```

## 🔄 CI/CD Integration

### GitHub Actions

```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      moclojer:
        image: ghcr.io/moclojer/moclojer:latest
        ports:
          - 8000:8000
        volumes:
          - ${{ github.workspace }}/test/mocks.yml:/app/moclojer.yml
    
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
      - run: npm install
      - run: npm test
        env:
          API_URL: http://localhost:8000
```

## 📚 See Also

- **[E2E Testing](e2e-testing.md)**
- **[Contract Testing](contract-testing.md)**
- **[Docker Deployment](../deployment/docker.md)**
