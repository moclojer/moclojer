---
description: >-
  Learn how to run moclojer with Docker. Official images, Docker Compose,
  volumes, networks and production-ready deployment.
---

# Docker Deployment

moclojer offers official Docker images to simplify deployment and development. This guide shows how to use Docker and Docker Compose to run your mock server.

## Why Use Docker?

✅ **Isolation**: Consistent environment on any machine
✅ **Portability**: Works in dev, staging and production
✅ **Easy deployment**: Pull, run, done!
✅ **Versioning**: Lock specific versions
✅ **CI/CD**: Easy integration with pipelines

---

## Official Image

### Repository

```bash
ghcr.io/moclojer/moclojer:latest
```

**Available tags:**

- `latest` - Latest stable version
- `v0.4.0` - Specific version
- `main` - Build from main branch (bleeding edge)

### Check Version

```bash
docker run --rm ghcr.io/moclojer/moclojer:latest --version
```

---

## Quick Start

### 1. Create Configuration File

Create `moclojer.yml`:

```yaml
- endpoint:
    method: GET
    path: /hello
    response:
      status: 200
      body: >
        {
          "message": "Hello from Docker!",
          "timestamp": "2024-01-15T10:00:00Z"
        }

- endpoint:
    method: GET
    path: /users
    response:
      status: 200
      body: >
        [
          {"id": 1, "name": "Alice"},
          {"id": 2, "name": "Bob"}
        ]
```

### 2. Run with Docker

```bash
docker run -it \
  -p 8000:8000 \
  -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/moclojer/moclojer:latest \
  --config /app/moclojer.yml
```

### 3. Test

```bash
curl http://localhost:8000/hello
curl http://localhost:8000/users
```

🎉 **It works!**

---

## Bind Mount vs Volume

### Bind Mount (Development)

**Advantages:** Edit local file, changes reflect immediately

```bash
docker run -it \
  -p 8000:8000 \
  -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/moclojer/moclojer:latest \
  --config /app/moclojer.yml --watch
```

**With `--watch`:** Automatically reloads when file changes!

### Named Volume (Production)

```bash
# Create volume
docker volume create moclojer-config

# Copy config to volume
docker run --rm \
  -v moclojer-config:/config \
  -v $(pwd)/moclojer.yml:/source/moclojer.yml \
  busybox cp /source/moclojer.yml /config/

# Run moclojer
docker run -d \
  --name moclojer \
  -p 8000:8000 \
  -v moclojer-config:/config \
  ghcr.io/moclojer/moclojer:latest \
  --config /config/moclojer.yml
```

---

## Docker Compose

### Basic Setup

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  moclojer:
    image: ghcr.io/moclojer/moclojer:latest
    container_name: moclojer
    ports:
      - "8000:8000"
    volumes:
      - ./moclojer.yml:/app/moclojer.yml:ro
    command: --config /app/moclojer.yml
    restart: unless-stopped
```

**Run:**

```bash
docker-compose up
```

**Run in background:**

```bash
docker-compose up -d
```

**View logs:**

```bash
docker-compose logs -f moclojer
```

**Stop:**

```bash
docker-compose down
```

### With Hot-Reload (Dev)

```yaml
version: '3.8'

services:
  moclojer-dev:
    image: ghcr.io/moclojer/moclojer:latest
    ports:
      - "8000:8000"
    volumes:
      - ./moclojer.yml:/app/moclojer.yml
    command: --config /app/moclojer.yml --watch
    environment:
      - MOCLOJER_ENABLE_CORS=true
```

### With Environment Variables

```yaml
version: '3.8'

services:
  moclojer:
    image: ghcr.io/moclojer/moclojer:latest
    ports:
      - "${MOCLOJER_PORT:-8000}:8000"
    volumes:
      - ./mocks/${ENV:-dev}.yml:/app/moclojer.yml:ro
    command: --config /app/moclojer.yml
    environment:
      - MOCLOJER_ENABLE_CORS=${ENABLE_CORS:-true}
      - MOCLOJER_HOST=0.0.0.0
```

**Create `.env`:**

```env
MOCLOJER_PORT=3000
ENV=staging
ENABLE_CORS=true
```

**Run:**

```bash
docker-compose up
```

### With Health Check

```yaml
version: '3.8'

services:
  moclojer:
    image: ghcr.io/moclojer/moclojer:latest
    ports:
      - "8000:8000"
    volumes:
      - ./moclojer.yml:/app/moclojer.yml:ro
    command: --config /app/moclojer.yml
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 10s
```

**Check status:**

```bash
docker-compose ps
```

---

## Multiple Environments

### File Structure

```
project/
├── docker-compose.yml
├── mocks/
│   ├── dev.yml         # Development environment
│   ├── staging.yml     # Staging environment
│   └── prod.yml        # Production environment
└── .env
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  moclojer:
    image: ghcr.io/moclojer/moclojer:${MOCLOJER_VERSION:-latest}
    ports:
      - "${PORT:-8000}:8000"
    volumes:
      - ./mocks/${ENV}.yml:/app/moclojer.yml:ro
    command: --config /app/moclojer.yml
    restart: unless-stopped
```

### .env files

**`.env.dev`:**

```env
ENV=dev
PORT=8000
MOCLOJER_VERSION=latest
```

**`.env.staging`:**

```env
ENV=staging
PORT=8001
MOCLOJER_VERSION=v0.4.0
```

**`.env.prod`:**

```env
ENV=prod
PORT=8080
MOCLOJER_VERSION=v0.4.0
```

**Run:**

```bash
# Development
docker-compose --env-file .env.dev up

# Staging
docker-compose --env-file .env.staging up

# Production
docker-compose --env-file .env.prod up -d
```

---

## Networking

### With Other Services

```yaml
version: '3.8'

services:
  # Your application
  app:
    image: myapp:latest
    ports:
      - "3000:3000"
    environment:
      - API_URL=http://moclojer:8000
    depends_on:
      - moclojer

  # Mock server
  moclojer:
    image: ghcr.io/moclojer/moclojer:latest
    volumes:
      - ./mocks/api.yml:/app/moclojer.yml:ro
    command: --config /app/moclojer.yml
    # Doesn't expose external port (only internal network)
    expose:
      - "8000"
```

**Application accesses:** `http://moclojer:8000/api/users`

### Custom Network

```yaml
version: '3.8'

networks:
  backend:
    driver: bridge

services:
  moclojer:
    image: ghcr.io/moclojer/moclojer:latest
    ports:
      - "8000:8000"
    volumes:
      - ./moclojer.yml:/app/moclojer.yml:ro
    command: --config /app/moclojer.yml
    networks:
      - backend

  app:
    image: myapp:latest
    networks:
      - backend
    environment:
      - MOCK_API=http://moclojer:8000
```

---

## CI/CD Integration

### GitHub Actions

```yaml
# .github/workflows/test.yml
name: E2E Tests with moclojer

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
          - ${{ github.workspace }}/mocks/api.yml:/app/moclojer.yml:ro
        options: >-
          --health-cmd "curl -f http://localhost:8000/health || exit 1"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v3

      - name: Wait for moclojer
        run: |
          timeout 30 bash -c 'until curl -f http://localhost:8000/health; do sleep 1; done'

      - name: Run E2E tests
        run: |
          npm test
        env:
          API_URL: http://localhost:8000
```

### GitLab CI

```yaml
# .gitlab-ci.yml
test:
  image: node:18
  services:
    - name: ghcr.io/moclojer/moclojer:latest
      alias: moclojer
      command: ["--config", "/app/moclojer.yml"]
      volumes:
        - ./mocks/api.yml:/app/moclojer.yml:ro

  variables:
    API_URL: http://moclojer:8000

  script:
    - npm install
    - npm test
```

### Docker Compose in CI

```bash
# CI script
docker-compose -f docker-compose.ci.yml up -d
docker-compose -f docker-compose.ci.yml run --rm tests
docker-compose -f docker-compose.ci.yml down
```

**docker-compose.ci.yml:**

```yaml
version: '3.8'

services:
  moclojer:
    image: ghcr.io/moclojer/moclojer:latest
    volumes:
      - ./mocks/api.yml:/app/moclojer.yml:ro
    command: --config /app/moclojer.yml

  tests:
    image: node:18
    depends_on:
      - moclojer
    environment:
      - API_URL=http://moclojer:8000
    volumes:
      - .:/app
    working_dir: /app
    command: npm test
```

---

## Production

### docker-compose.prod.yml

```yaml
version: '3.8'

services:
  moclojer:
    image: ghcr.io/moclojer/moclojer:v0.4.0  # Fixed version!
    container_name: moclojer-prod
    restart: always
    ports:
      - "8080:8000"
    volumes:
      - ./mocks/prod.yml:/app/moclojer.yml:ro
      - moclojer-logs:/var/log/moclojer
    command: --config /app/moclojer.yml --host 0.0.0.0
    environment:
      - MOCLOJER_ENABLE_CORS=false  # Disable in prod
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M

volumes:
  moclojer-logs:
```

**Deploy:**

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### With Nginx Reverse Proxy

```yaml
version: '3.8'

services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    depends_on:
      - moclojer

  moclojer:
    image: ghcr.io/moclojer/moclojer:latest
    expose:
      - "8000"
    volumes:
      - ./mocks/api.yml:/app/moclojer.yml:ro
    command: --config /app/moclojer.yml
```

**nginx.conf:**

```nginx
upstream moclojer {
    server moclojer:8000;
}

server {
    listen 80;
    server_name api.example.com;

    location / {
        proxy_pass http://moclojer;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## Troubleshooting

### Container won't start

**Problem:** `docker: Error response from daemon: Conflict`

**Solution:** Remove old container

```bash
docker rm -f moclojer
docker-compose down
```

### Config file not found

**Problem:** `Config file not found`

**Solution:** Check mount

```bash
# List files inside container
docker run --rm \
  -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/moclojer/moclojer:latest \
  ls -la /app/

# Should show moclojer.yml
```

### Port already in use

**Problem:** `Bind for 0.0.0.0:8000 failed: port is already allocated`

**Solution:** Change host port

```bash
docker run -p 8001:8000 ...
# Or
docker-compose -f docker-compose.yml up  # with ports: "8001:8000"
```

### Hot-reload not working

**Problem:** File changes don't reload

**Causes:**

1. Didn't pass `--watch`
2. Using native image (GraalVM) - doesn't support watch

**Solution:**

```bash
# Check if it's native
docker run --rm ghcr.io/moclojer/moclojer:latest --version

# If native, use JAR version (if available)
# Or restart container manually
docker-compose restart moclojer
```

### Volume permissions

**Problem:** Permission denied

**Solution:** Adjust permissions

```bash
chmod 644 moclojer.yml
```

---

## Best Practices

### ✅ Do

1. **Use fixed versions in production**

   ```yaml
   image: ghcr.io/moclojer/moclojer:v0.4.0  # ✅
   # image: ghcr.io/moclojer/moclojer:latest  # ❌ In prod
   ```

2. **Read-only volumes when possible**

   ```yaml
   volumes:
     - ./moclojer.yml:/app/moclojer.yml:ro  # Read-only
   ```

3. **Health checks**

   ```yaml
   healthcheck:
     test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
   ```

4. **Resource limits**

   ```yaml
   deploy:
     resources:
       limits:
         cpus: '1'
         memory: 512M
   ```

5. **Configure logging**

   ```yaml
   logging:
     driver: "json-file"
     options:
       max-size: "10m"
       max-file: "3"
   ```

### ❌ Avoid

1. **`:latest` in production**
2. **Running as root** (if possible, use USER in Dockerfile)
3. **Exposing unnecessary ports**
4. **Volumes without :ro when write is not needed**

---

## Complete Examples

### E2E Testing Setup

```yaml
# docker-compose.test.yml
version: '3.8'

services:
  # External API mock
  mock-api:
    image: ghcr.io/moclojer/moclojer:latest
    volumes:
      - ./mocks/external-api.yml:/app/moclojer.yml:ro
    expose:
      - "8000"

  # Your application
  app:
    build: .
    environment:
      - EXTERNAL_API_URL=http://mock-api:8000
    depends_on:
      - mock-api

  # Tests
  tests:
    image: cypress/included:latest
    depends_on:
      - app
    environment:
      - CYPRESS_baseUrl=http://app:3000
    volumes:
      - ./cypress:/cypress
    command: cypress run
```

**Run tests:**

```bash
docker-compose -f docker-compose.test.yml run --rm tests
```

---

## Next Steps

- **[Kubernetes Deployment](kubernetes.md)** - Deploy to K8s
- **[Cloud Run](cloud-run.md)** - Deploy to Google Cloud
- **[CI/CD Integration](../testing/integration-testing.md)** - Continuous integration

## See Also

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [CLI Reference](../../reference/cli-reference.md) - Flags and options
- [Troubleshooting Guide](../../reference/troubleshooting.md) - Common issues
