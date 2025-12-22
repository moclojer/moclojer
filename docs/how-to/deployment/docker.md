---
description: >-
  Aprenda a rodar moclojer com Docker. Imagens oficiais, Docker Compose,
  volumes, networks e deployment production-ready.
---

# Docker Deployment

moclojer oferece imagens Docker oficiais para facilitar deploy e desenvolvimento. Este guia mostra como usar Docker e Docker Compose para rodar seu mock server.

## Por Que Usar Docker?

✅ **Isolamento**: Ambiente consistente em qualquer máquina
✅ **Portabilidade**: Funciona em dev, staging e produção
✅ **Fácil deployment**: Pull, run, pronto!
✅ **Versionamento**: Travar versões específicas
✅ **CI/CD**: Integração fácil com pipelines

---

## Imagem Oficial

### Repositório

```bash
ghcr.io/moclojer/moclojer:latest
```

**Tags disponíveis:**
- `latest` - Última versão estável
- `v0.4.0` - Versão específica
- `main` - Build da branch main (bleeding edge)

### Verificar Versão

```bash
docker run --rm ghcr.io/moclojer/moclojer:latest --version
```

---

## Quick Start

### 1. Criar Arquivo de Configuração

Crie `moclojer.yml`:

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

### 2. Rodar com Docker

```bash
docker run -it \
  -p 8000:8000 \
  -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/moclojer/moclojer:latest \
  --config /app/moclojer.yml
```

### 3. Testar

```bash
curl http://localhost:8000/hello
curl http://localhost:8000/users
```

🎉 **Funciona!**

---

## Bind Mount vs Volume

### Bind Mount (Desenvolvimento)

**Vantagens:** Edita arquivo local, mudanças refletem imediatamente

```bash
docker run -it \
  -p 8000:8000 \
  -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/moclojer/moclojer:latest \
  --config /app/moclojer.yml --watch
```

**Com `--watch`:** Recarrega automaticamente quando arquivo muda!

### Named Volume (Produção)

```bash
# Criar volume
docker volume create moclojer-config

# Copiar config para o volume
docker run --rm \
  -v moclojer-config:/config \
  -v $(pwd)/moclojer.yml:/source/moclojer.yml \
  busybox cp /source/moclojer.yml /config/

# Rodar moclojer
docker run -d \
  --name moclojer \
  -p 8000:8000 \
  -v moclojer-config:/config \
  ghcr.io/moclojer/moclojer:latest \
  --config /config/moclojer.yml
```

---

## Docker Compose

### Setup Básico

Crie `docker-compose.yml`:

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

**Rodar:**
```bash
docker-compose up
```

**Rodar em background:**
```bash
docker-compose up -d
```

**Ver logs:**
```bash
docker-compose logs -f moclojer
```

**Parar:**
```bash
docker-compose down
```

### Com Hot-Reload (Dev)

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

### Com Variáveis de Ambiente

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

**Criar `.env`:**
```env
MOCLOJER_PORT=3000
ENV=staging
ENABLE_CORS=true
```

**Rodar:**
```bash
docker-compose up
```

### Com Health Check

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

**Verificar status:**
```bash
docker-compose ps
```

---

## Múltiplos Ambientes

### Estrutura de Arquivos

```
project/
├── docker-compose.yml
├── mocks/
│   ├── dev.yml         # Ambiente de desenvolvimento
│   ├── staging.yml     # Ambiente de staging
│   └── prod.yml        # Ambiente de produção
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

**Rodar:**
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

### Com Outros Serviços

```yaml
version: '3.8'

services:
  # Sua aplicação
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
    # Não expõe porta externa (apenas internal network)
    expose:
      - "8000"
```

**Aplicação acessa:** `http://moclojer:8000/api/users`

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

### Docker Compose em CI

```bash
# Script de CI
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

## Produção

### docker-compose.prod.yml

```yaml
version: '3.8'

services:
  moclojer:
    image: ghcr.io/moclojer/moclojer:v0.4.0  # Versão fixada!
    container_name: moclojer-prod
    restart: always
    ports:
      - "8080:8000"
    volumes:
      - ./mocks/prod.yml:/app/moclojer.yml:ro
      - moclojer-logs:/var/log/moclojer
    command: --config /app/moclojer.yml --host 0.0.0.0
    environment:
      - MOCLOJER_ENABLE_CORS=false  # Desabilitar em prod
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

### Com Nginx Reverse Proxy

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

### Container não inicia

**Problema:** `docker: Error response from daemon: Conflict`

**Solução:** Remover container antigo
```bash
docker rm -f moclojer
docker-compose down
```

### Arquivo de config não encontrado

**Problema:** `Config file not found`

**Solução:** Verificar mount
```bash
# Listar arquivos dentro do container
docker run --rm \
  -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/moclojer/moclojer:latest \
  ls -la /app/

# Deve mostrar moclojer.yml
```

### Porta já em uso

**Problema:** `Bind for 0.0.0.0:8000 failed: port is already allocated`

**Solução:** Mudar porta do host
```bash
docker run -p 8001:8000 ...
# Ou
docker-compose -f docker-compose.yml up  # com ports: "8001:8000"
```

### Hot-reload não funciona

**Problema:** Mudanças no arquivo não recarregam

**Causas:**
1. Não passou `--watch`
2. Usando imagem nativa (GraalVM) - não suporta watch

**Solução:**
```bash
# Verificar se é nativa
docker run --rm ghcr.io/moclojer/moclojer:latest --version

# Se for nativa, usar JAR version (se disponível)
# Ou reiniciar container manualmente
docker-compose restart moclojer
```

### Permissões no volume

**Problema:** Permission denied

**Solução:** Ajustar permissões
```bash
chmod 644 moclojer.yml
```

---

## Boas Práticas

### ✅ Faça

1. **Use versões fixadas em produção**
   ```yaml
   image: ghcr.io/moclojer/moclojer:v0.4.0  # ✅
   # image: ghcr.io/moclojer/moclojer:latest  # ❌ Em prod
   ```

2. **Read-only volumes quando possível**
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

5. **Logging configurado**
   ```yaml
   logging:
     driver: "json-file"
     options:
       max-size: "10m"
       max-file: "3"
   ```

### ❌ Evite

1. **`:latest` em produção**
2. **Rodar como root** (se possível, use USER no Dockerfile)
3. **Expor portas desnecessárias**
4. **Volumes sem :ro quando não precisa escrever**

---

## Exemplos Completos

### E2E Testing Setup

```yaml
# docker-compose.test.yml
version: '3.8'

services:
  # Mock de API externa
  mock-api:
    image: ghcr.io/moclojer/moclojer:latest
    volumes:
      - ./mocks/external-api.yml:/app/moclojer.yml:ro
    expose:
      - "8000"

  # Sua aplicação
  app:
    build: .
    environment:
      - EXTERNAL_API_URL=http://mock-api:8000
    depends_on:
      - mock-api

  # Testes
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

**Rodar testes:**
```bash
docker-compose -f docker-compose.test.yml run --rm tests
```

---

## Próximos Passos

- **[Kubernetes Deployment](kubernetes.md)** - Deploy em K8s
- **[Cloud Run](cloud-run.md)** - Deploy no Google Cloud
- **[CI/CD Integration](../testing/integration-testing.md)** - Integração contínua

## Veja Também

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [CLI Reference](../../reference/cli-reference.md) - Flags e opções
- [Troubleshooting Guide](../../reference/troubleshooting.md) - Problemas comuns
