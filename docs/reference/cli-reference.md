---
description: >-
  Referência completa da interface de linha de comando (CLI) do moclojer.
  Todos os flags, opções e exemplos de uso.
---

# CLI Reference

Este documento descreve todas as opções disponíveis na linha de comando do moclojer.

## Sintaxe Básica

```bash
moclojer [OPTIONS]
```

## Opções Globais

### `--config` ou `-c`

Especifica o caminho para o arquivo de configuração.

**Tipo:** String (caminho de arquivo)
**Padrão:** `~/.config/moclojer.yml` (ou `$XDG_CONFIG_HOME/moclojer.yml`)
**Formatos suportados:** `.yml`, `.yaml`, `.edn`, `.json`

**Exemplos:**

```bash
# YAML
moclojer --config api-mock.yml
moclojer -c ./config/mocks.yaml

# EDN
moclojer --config mocks.edn

# JSON (OpenAPI ou Postman Collection)
moclojer --config openapi.json
moclojer --config postman_collection.json
```

**Notas:**

- Caminhos relativos são resolvidos a partir do diretório atual
- Se não especificado, moclojer busca `moclojer.yml` no XDG_CONFIG_HOME
- Pode ser um arquivo local ou URL (veja `--remote-config`)

---

### `--port` ou `-p`

Define a porta onde o servidor HTTP vai escutar.

**Tipo:** Integer
**Padrão:** `8000`
**Range válido:** `1-65535`

**Exemplos:**

```bash
moclojer --port 3000
moclojer -p 8080
```

**Variável de ambiente:** `MOCLOJER_PORT`

```bash
export MOCLOJER_PORT=3000
moclojer  # Usa porta 3000
```

**Precedência:** CLI > ENV > Padrão

---

### `--host` ou `-h`

Define o endereço IP onde o servidor vai escutar.

**Tipo:** String (IP address)
**Padrão:** `0.0.0.0` (todas as interfaces)

**Exemplos:**

```bash
# Aceitar conexões de qualquer interface
moclojer --host 0.0.0.0

# Apenas localhost (mais seguro em dev)
moclojer --host 127.0.0.1

# Interface específica
moclojer --host 192.168.1.100
```

**Variável de ambiente:** `MOCLOJER_HOST`

```bash
export MOCLOJER_HOST=127.0.0.1
moclojer
```

**Segurança:**

- `0.0.0.0` - Aceita de qualquer rede (cuidado em produção!)
- `127.0.0.1` - Apenas local (recomendado para dev)

---

### `--enable-cors`

Habilita CORS (Cross-Origin Resource Sharing) para todas as respostas.

**Tipo:** Boolean flag
**Padrão:** `false`

**Exemplo:**

```bash
moclojer --enable-cors
```

**O que faz:**
Adiciona os seguintes headers a todas as respostas:

```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
Access-Control-Allow-Headers: *
```

**Quando usar:**

- Desenvolvimento frontend em localhost diferente
- APIs consumidas por browsers
- Aplicações SPA (React, Vue, Angular)

**Quando NÃO usar:**

- Produção (use configuração de CORS específica)
- APIs internas sem necessidade de browser access

---

### `--watch` ou `-w`

Habilita hot-reload: recarrega configuração quando o arquivo muda.

**Tipo:** Boolean flag
**Padrão:** `false` (true em modo dev)

**Exemplo:**

```bash
moclojer --watch --config api.yml
```

**Comportamento:**

1. Moclojer monitora o arquivo de config
2. Quando detecta mudança, recarrega automaticamente
3. Servidor continua rodando sem interrupção
4. Rotas são atualizadas instantaneamente

**Limitações:**

- **Não disponível em binário nativo (GraalVM)** - requer JVM
- Monitora apenas o arquivo de config principal (não includes)

**Uso recomendado:**

```bash
# Desenvolvimento
moclojer --watch --config mocks.yml

# Produção (sem watch)
moclojer --config mocks.yml
```

---

### `--help`

Exibe ajuda com todas as opções disponíveis.

**Exemplo:**

```bash
moclojer --help
```

**Output:**

```
moclojer - Simple and efficient HTTP mock server

Usage: moclojer [OPTIONS]

Options:
  -c, --config PATH       Configuration file path
  -p, --port PORT         Server port (default: 8000)
  -h, --host HOST         Server host (default: 0.0.0.0)
  --enable-cors           Enable CORS headers
  -w, --watch             Watch config file for changes
  --version               Show version
  --help                  Show this help
```

---

### `--version` ou `-v`

Exibe a versão do moclojer.

**Exemplo:**

```bash
moclojer --version
# moclojer version 0.4.0
```

---

## Variáveis de Ambiente

Todas as opções podem ser configuradas via variáveis de ambiente:

| Variável | Equivalente CLI | Exemplo |
|----------|-----------------|---------|
| `MOCLOJER_PORT` | `--port` | `export MOCLOJER_PORT=3000` |
| `MOCLOJER_HOST` | `--host` | `export MOCLOJER_HOST=127.0.0.1` |
| `MOCLOJER_CONFIG` | `--config` | `export MOCLOJER_CONFIG=./api.yml` |
| `MOCLOJER_ENABLE_CORS` | `--enable-cors` | `export MOCLOJER_ENABLE_CORS=true` |
| `MOCLOJER_WATCH` | `--watch` | `export MOCLOJER_WATCH=true` |

**Precedência:** CLI > ENV > Config File > Padrão

---

## Exemplos Práticos

### Desenvolvimento Local

```bash
# Setup básico
moclojer --config dev-mocks.yml --port 3000 --enable-cors --watch

# Com variáveis de ambiente
export MOCLOJER_PORT=3000
export MOCLOJER_ENABLE_CORS=true
moclojer --config dev-mocks.yml --watch
```

### Integração com Docker

```bash
# Bind mount do config
docker run -it -p 8000:8000 \
  -v $(pwd)/mocks.yml:/app/mocks.yml \
  ghcr.io/moclojer/moclojer:latest \
  --config /app/mocks.yml

# Com variáveis de ambiente
docker run -it -p 3000:3000 \
  -e MOCLOJER_PORT=3000 \
  -e MOCLOJER_ENABLE_CORS=true \
  -v $(pwd)/mocks.yml:/app/mocks.yml \
  ghcr.io/moclojer/moclojer:latest \
  --config /app/mocks.yml
```

### CI/CD

```bash
# GitHub Actions / GitLab CI
- name: Start Mock Server
  run: |
    moclojer --config test-mocks.yml --port 8080 &
    sleep 2  # Wait for server to start

- name: Run E2E Tests
  run: npm test
  env:
    API_URL: http://localhost:8080
```

### Multi-config com Scripts

```bash
#!/bin/bash
# start-mocks.sh

case "$1" in
  dev)
    moclojer --config dev-mocks.yml --watch --enable-cors
    ;;
  test)
    moclojer --config test-mocks.yml --port 9000
    ;;
  staging)
    moclojer --config staging-mocks.yml --port 8080
    ;;
  *)
    echo "Usage: $0 {dev|test|staging}"
    exit 1
    ;;
esac
```

**Uso:**

```bash
chmod +x start-mocks.sh
./start-mocks.sh dev
```

---

## Configuração via Arquivo

Algumas opções podem ser definidas no próprio arquivo de configuração:

```yaml
# moclojer.yml

# Configurações globais (opcional)
config:
  port: 8000
  host: 0.0.0.0
  enable-cors: true

# Endpoints
- endpoint:
    method: GET
    path: /hello
    response:
      body: "Hello, World!"
```

**Nota:** CLI e ENV sempre sobrescrevem valores do arquivo.

---

## Troubleshooting CLI

### "Address already in use"

**Causa:** Porta já está sendo usada por outro processo.

**Solução:**

```bash
# Descobrir quem está usando a porta 8000
lsof -i :8000

# Usar outra porta
moclojer --port 8001

# Ou matar o processo
kill -9 <PID>
```

---

### "Config file not found"

**Causa:** Arquivo não existe no caminho especificado.

**Solução:**

```bash
# Verificar se arquivo existe
ls -la moclojer.yml

# Usar caminho absoluto
moclojer --config /absolute/path/to/mocks.yml

# Verificar diretório atual
pwd
moclojer --config ./mocks.yml
```

---

### "YAML parse error"

**Causa:** Sintaxe YAML inválida no arquivo de config.

**Solução:**

```bash
# Validar YAML online
# http://www.yamllint.com/

# Ou com yamllint (se instalado)
yamllint moclojer.yml

# Ver detalhes do erro
moclojer --config mocks.yml 2>&1 | grep -A 5 "parse error"
```

---

### Hot-reload não funciona

**Causa:** Usando binário nativo (GraalVM) ou não passou `--watch`.

**Solução:**

```bash
# Verificar se é binário nativo
moclojer --version
# Se output mencionar "native" ou "graalvm", hot-reload não está disponível

# Usar JVM version
java -jar moclojer.jar --watch --config mocks.yml

# Ou sempre passar --watch
moclojer --watch --config mocks.yml
```

---

## Dicas Avançadas

### Alias úteis

```bash
# ~/.bashrc ou ~/.zshrc

alias mock='moclojer --config ./moclojer.yml --watch --enable-cors'
alias mock-test='moclojer --config ./test-mocks.yml --port 9000'
alias mock-prod='moclojer --config ./prod-mocks.yml --port 8080'
```

**Uso:**

```bash
mock        # Inicia com config padrão e watch
mock-test   # Inicia ambiente de testes
```

---

### Verificar se servidor está UP

```bash
#!/bin/bash
# wait-for-mock.sh

PORT=${1:-8000}
TIMEOUT=${2:-30}

echo "Waiting for moclojer on port $PORT..."
for i in $(seq 1 $TIMEOUT); do
  if curl -s http://localhost:$PORT/health >/dev/null 2>&1; then
    echo "✅ Moclojer is ready!"
    exit 0
  fi
  sleep 1
done

echo "❌ Timeout waiting for moclojer"
exit 1
```

**Uso em CI:**

```bash
moclojer --config mocks.yml &
./wait-for-mock.sh 8000 30
npm test
```

---

### Debug mode (verbose logging)

Atualmente moclojer não tem flag `--verbose`, mas você pode usar:

```bash
# Capturar logs
moclojer --config mocks.yml 2>&1 | tee moclojer.log

# Ver apenas erros
moclojer --config mocks.yml 2>&1 | grep -i error

# Monitorar requests em tempo real
# (adicione logging no config ou use proxy)
```

---

## Próximos Passos

- **[Configuration Spec](configuration-spec.md)** - Referência do formato de config
- **[Environment Variables](environment-variables.md)** - Todas as variáveis de ambiente
- **[Troubleshooting](troubleshooting.md)** - Guia de resolução de problemas
- **[Docker Deployment](../how-to/deployment/docker.md)** - Deploy com Docker

## Veja Também

- [Installation Guide](../getting-started/installation.md) - Como instalar moclojer
- [Your First Mock](../getting-started/your-first-mock.md) - Tutorial inicial
- [FAQ](faq.md) - Perguntas frequentes
