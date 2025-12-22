---
description: >-
  End-to-end testing with moclojer - Cypress, Playwright, and Selenium integration
  for full user flow testing with mocked backends.
---

# E2E Testing with moclojer

Use moclojer in E2E tests to mock backend APIs while testing user flows.

## 🎯 With Cypress

```javascript
// cypress/e2e/user-flow.cy.js
describe('User Registration Flow', () => {
  beforeEach(() => {
    // moclojer running on localhost:8000
    cy.visit('http://localhost:3000');
  });

  it('registers a new user', () => {
    cy.get('[data-testid="register-button"]').click();
    cy.get('[name="email"]').type('user@example.com');
    cy.get('[name="password"]').type('password123');
    cy.get('[type="submit"]').click();
    
    cy.contains('Registration successful').should('be.visible');
  });
});
```

## 🔧 Docker Compose Setup

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "3000:3000"
    environment:
      - API_URL=http://moclojer:8000
  
  moclojer:
    image: ghcr.io/moclojer/moclojer:latest
    volumes:
      - ./mocks.yml:/app/moclojer.yml
    ports:
      - "8000:8000"
  
  e2e:
    image: cypress/included:latest
    depends_on:
      - app
      - moclojer
    environment:
      - CYPRESS_baseUrl=http://app:3000
    volumes:
      - ./cypress:/cypress
```

## 📚 See Also

- **[Integration Testing](integration-testing.md)**
- **[Contract Testing](contract-testing.md)**
