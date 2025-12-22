---
description: >-
  Guia de resolução de problemas do moclojer. Soluções para erros comuns,
  debugging tips e como obter ajuda quando algo não funciona.
---

# Troubleshooting Guide

Este guia ajuda você a resolver problemas comuns ao usar moclojer. Os problemas estão organizados por categoria para facilitar a busca.

## 🚀 Início Rápido

**Antes de começar o troubleshooting:**

1. ✅ Verifique a versão do moclojer

   ```bash
   moclojer --version
   ```

2. ✅ Teste com configuração mínima

   ```yaml
   - endpoint:
       path: /test
       response:
         body: "ok"
   ```

3. ✅ Verifique os logs do servidor

   ```bash
   moclojer --config mocks.yml 2>&1 | tee moclojer.log
   ```

---

## 📁 Problemas de Configuração

### "Config file not found"

**Sintoma:**

```
Error: Config file not found: moclojer.yml
```

**Causas e Soluções:**

1. **Arquivo não existe no caminho especificado**

   ```bash
   # Verificar se arquivo existe
   ls -la moclojer.yml

   # Se não existe, criar
   cat > moclojer.yml <<EOF
   - endpoint:
       path: /hello
       response:
         body: "Hello!"
   EOF
   ```

2. **Caminho relativo incorreto**

   ```bash
   # ❌ Errado (busca no diretório errado)
   cd /home/user
   moclojer --config project/mocks.yml

   # ✅ Correto (use caminho absoluto ou navegue até o diretório)
   cd /home/user/project
   moclojer --config mocks.yml

   # Ou use caminho absoluto
   moclojer --config /home/user/project/mocks.yml
   ```

3. **Arquivo na localização XDG padrão**

   ```bash
   # Moclojer busca em ~/.config/moclojer.yml por padrão
   # Verificar
   ls -la ~/.config/moclojer.yml

   # Ou definir XDG_CONFIG_HOME
   export XDG_CONFIG_HOME=/my/custom/path
   moclojer  # Busca em /my/custom/path/moclojer.yml
   ```

---

### "YAML parse error"

**Sintoma:**

```
Error: YAML parse error at line 10: mapping values are not allowed here
```

**Causas e Soluções:**

1. **Indentação incorreta (espaços vs tabs)**

   ```yaml
   # ❌ Errado (mistura tabs e espaços)
   - endpoint:
    method: GET    # Tab aqui!
       path: /users   # Espaços aqui!

   # ✅ Correto (sempre 2 espaços)
   - endpoint:
       method: GET
       path: /users
   ```

   **Solução:** Configure seu editor para usar "soft tabs" (espaços)

   ```
   VS Code: "editor.insertSpaces": true, "editor.tabSize": 2
   Vim: set expandtab shiftwidth=2
   ```

2. **Dois-pontos em string sem aspas**

   ```yaml
   # ❌ Errado
   body: http://example.com

   # ✅ Correto (use aspas para URLs)
   body: "http://example.com"

   # ✅ Ou use bloco multi-linha
   body: >
     http://example.com
   ```

3. **JSON inline mal formatado**

   ```yaml
   # ❌ Errado
   body: {"key": "value"}

   # ✅ Correto (use > para JSON)
   body: >
     {"key": "value"}

   # ✅ Ou multi-linha
   body: >
     {
       "key": "value"
     }
   ```

4. **Aspas não fechadas**

   ```yaml
   # ❌ Errado
   body: "Hello

   # ✅ Correto
   body: "Hello"
   ```

**Ferramentas de validação:**

```bash
# Online
# http://www.yamllint.com/

# CLI (se yamllint estiver instalado)
yamllint moclojer.yml

# Ver linha específica do erro
sed -n '10p' moclojer.yml
```

---

### "Invalid JSON in response body"

**Sintoma:**
Response body não é JSON válido, mas você esperava JSON.

**Soluções:**

1. **Use `>` para JSON multi-linha**

   ```yaml
   # ❌ Pode quebrar
   body: {
     "key": "value"
   }

   # ✅ Sempre funciona
   body: >
     {
       "key": "value"
     }
   ```

2. **Escape de aspas em templates**

   ```yaml
   # ❌ Quebra o JSON
   body: >
     {
       "message": "Hello "{{path-params.name}}"!"
     }

   # ✅ Sem aspas extras ao redor do template
   body: >
     {
       "message": "Hello {{path-params.name}}!"
     }
   ```

3. **Números sem aspas, strings com aspas**

   ```yaml
   # Correto
   body: >
     {
       "id": {{path-params.id}},
       "name": "{{path-params.name}}"
     }
   ```

---

## 🌐 Problemas de Servidor

### "Address already in use"

**Sintoma:**

```
Error: Address already in use: 0.0.0.0:8000
```

**Causa:** Outra aplicação está usando a porta 8000.

**Soluções:**

1. **Descobrir qual processo está usando a porta**

   ```bash
   # macOS/Linux
   lsof -i :8000

   # Linux alternativo
   netstat -tlnp | grep 8000

   # Windows
   netstat -ano | findstr :8000
   ```

2. **Matar o processo**

   ```bash
   # macOS/Linux
   kill -9 <PID>

   # Ou killall
   killall -9 moclojer
   ```

3. **Usar porta diferente**

   ```bash
   moclojer --port 8001

   # Ou variável de ambiente
   export MOCLOJER_PORT=8001
   moclojer
   ```

---

### "Server started but requests timeout"

**Sintoma:**
Servidor inicia, mas requests nunca respondem.

**Causas e Soluções:**

1. **Firewall bloqueando conexões**

   ```bash
   # macOS: permitir conexões entrantes
   # System Preferences > Security > Firewall

   # Linux: verificar iptables
   sudo iptables -L

   # Temporariamente desabilitar firewall para testar
   sudo ufw disable  # Ubuntu
   ```

2. **Binding em IP errado**

   ```bash
   # Se bindou em 127.0.0.1, apenas localhost funciona
   moclojer --host 127.0.0.1  # Apenas local

   # Para aceitar de qualquer interface
   moclojer --host 0.0.0.0  # Todas as interfaces
   ```

3. **Proxy ou VPN interferindo**

   ```bash
   # Desabilitar proxy temporariamente
   unset HTTP_PROXY HTTPS_PROXY

   # Ou configurar exceções
   export NO_PROXY=localhost,127.0.0.1
   ```

---

## 🔍 Problemas de Matching

### "404 Not Found" quando deveria fazer match

**Sintoma:**
Você faz um request mas recebe 404, mesmo tendo um endpoint configurado.

**Debugging:**

1. **Verificar método HTTP**

   ```yaml
   # Se configurou GET
   - endpoint:
       method: GET
       path: /users

   # POST não vai funcionar!
   curl -X POST http://localhost:8000/users  # 404

   # Solução: usar GET
   curl http://localhost:8000/users  # 200
   ```

2. **Verificar path exato**

   ```yaml
   # Configurado
   path: /api/users

   # ❌ Não funciona
   curl http://localhost:8000/users  # 404

   # ✅ Funciona
   curl http://localhost:8000/api/users  # 200
   ```

3. **Case sensitivity**

   ```yaml
   # Configurado
   path: /Users

   # ❌ Cuidado com maiúsculas!
   curl http://localhost:8000/users  # 404

   # ✅ Deve ser exato
   curl http://localhost:8000/Users  # 200
   ```

4. **Tipo de path parameter incorreto**

   ```yaml
   # Configurado com tipo int
   path: /users/:id|int

   # ❌ String não faz match
   curl http://localhost:8000/users/abc  # 404

   # ✅ Número faz match
   curl http://localhost:8000/users/123  # 200
   ```

5. **Ordem de precedência**

   ```yaml
   # ❌ Ordem errada!
   - endpoint:
       path: /users/:id      # Match genérico ANTES

   - endpoint:
       path: /users/me       # Específico DEPOIS (nunca usado!)

   # ✅ Ordem correta
   - endpoint:
       path: /users/me       # Específico PRIMEIRO

   - endpoint:
       path: /users/:id      # Genérico DEPOIS
   ```

**Solução geral:** Adicione logging temporário

```yaml
- endpoint:
    path: /:any
    method: GET
    response:
      status: 200
      body: >
        {
          "debug": "Caught request to: /:any",
          "path": "{{path-params.any}}"
        }
```

---

### Templates não são substituídos

**Sintoma:**
Response contém `{{path-params.id}}` literal ao invés do valor.

**Causas e Soluções:**

1. **Nome do parâmetro não corresponde**

   ```yaml
   # ❌ Errado
   path: /users/:userId
   body: >
     {"id": "{{path-params.id}}"}  # id != userId!

   # ✅ Correto
   path: /users/:userId
   body: >
     {"id": "{{path-params.userId}}"}
   ```

2. **Sintaxe incorreta**

   ```yaml
   # ❌ Errado
   body: >
     {"id": "{{ path-params.id }}"}  # Espaços extras

   # ✅ Correto
   body: >
     {"id": "{{path-params.id}}"}  # Sem espaços
   ```

3. **Query param não foi passado**

   ```yaml
   path: /users
   body: >
     {"role": "{{query-params.role}}"}

   # Se não passar ?role=admin, fica vazio
   curl http://localhost:8000/users
   # {"role": ""}

   # Solução: sempre passar o param
   curl "http://localhost:8000/users?role=admin"
   # {"role": "admin"}
   ```

---

## 🔄 Problemas de Hot-Reload

### "Hot-reload não funciona"

**Sintoma:**
Você modifica moclojer.yml mas mudanças não aparecem.

**Causas e Soluções:**

1. **Não passou flag --watch**

   ```bash
   # ❌ Sem watch
   moclojer --config mocks.yml

   # ✅ Com watch
   moclojer --config mocks.yml --watch
   ```

2. **Usando binário nativo (GraalVM)**

   ```bash
   # Verificar
   moclojer --version
   # Se mencionar "native" ou "graalvm":

   # Binário nativo NÃO suporta hot-reload
   # Solução: usar JAR
   java -jar moclojer.jar --config mocks.yml --watch
   ```

3. **Editor salvando em arquivo temporário**

   ```bash
   # Alguns editores salvam em .tmp primeiro
   # Moclojer pode não detectar

   # Solução: forçar save direto
   # VS Code: "files.watcherExclude" settings
   ```

4. **Arquivo em filesystem remoto (NFS, etc)**

   ```bash
   # Hot-reload pode não funcionar em NFS/network drives
   # Solução: copiar arquivo localmente
   ```

---

## 🐳 Problemas com Docker

### "Container starts but can't connect"

**Sintoma:**
Container do moclojer inicia mas você não consegue fazer requests.

**Soluções:**

1. **Port mapping incorreto**

   ```bash
   # ❌ Errado (porta do host diferente)
   docker run -p 3000:8000 moclojer
   # Servidor escuta em 8000 dentro do container

   # ✅ Correto
   curl http://localhost:3000  # Porta do HOST
   ```

2. **Config file não montado**

   ```bash
   # ❌ Config não está no container
   docker run -p 8000:8000 moclojer --config /app/mocks.yml
   # Error: file not found

   # ✅ Mount do volume
   docker run -p 8000:8000 \
     -v $(pwd)/mocks.yml:/app/mocks.yml \
     moclojer --config /app/mocks.yml
   ```

3. **Servidor binding em 127.0.0.1 (dentro do container)**

   ```bash
   # ❌ Não acessível de fora do container
   moclojer --host 127.0.0.1

   # ✅ Bind em todas as interfaces
   moclojer --host 0.0.0.0
   ```

---

## 🌍 Problemas de CORS

### "CORS error in browser"

**Sintoma:**

```
Access to fetch at 'http://localhost:8000/api' from origin 'http://localhost:3000'
has been blocked by CORS policy
```

**Solução:**

1. **Habilitar CORS globalmente**

   ```bash
   moclojer --enable-cors --config mocks.yml
   ```

2. **Configurar CORS por endpoint**

   ```yaml
   - endpoint:
       method: OPTIONS
       path: /:path
       response:
         status: 204
         headers:
           Access-Control-Allow-Origin: "*"
           Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
           Access-Control-Allow-Headers: Content-Type, Authorization

   - endpoint:
       method: GET
       path: /api/users
       response:
         status: 200
         headers:
           Access-Control-Allow-Origin: "*"
         body: "[]"
   ```

3. **CORS específico por origem**

   ```yaml
   headers:
     Access-Control-Allow-Origin: "http://localhost:3000"
     Access-Control-Allow-Credentials: "true"
   ```

---

## 🔧 Debugging Avançado

### Habilitar Logs Verbosos

```bash
# Capturar todos os logs
moclojer --config mocks.yml 2>&1 | tee moclojer.log

# Verificar requests recebidos
grep "Request" moclojer.log

# Verificar erros
grep -i error moclojer.log
```

### Testar com curl -v (verbose)

```bash
curl -v http://localhost:8000/users

# Mostra:
# - Headers enviados
# - Headers recebidos
# - Response completo
```

### Usar Proxy para Inspecionar

```bash
# Usar Charles Proxy, Fiddler ou mitmproxy
# para ver exatamente o que está sendo enviado/recebido

# mitmproxy exemplo
mitmproxy --port 8888

# Configurar client para usar proxy
curl --proxy http://localhost:8888 http://localhost:8000/users
```

### Testar JSON com jq

```bash
# Validar se response é JSON válido
curl http://localhost:8000/users | jq .

# Se erro, JSON está malformado
```

---

## 🆘 Obtendo Ajuda

### Antes de Pedir Ajuda

Prepare as seguintes informações:

1. **Versão do moclojer**

   ```bash
   moclojer --version
   ```

2. **Sistema operacional**

   ```bash
   uname -a  # Linux/macOS
   ver       # Windows
   ```

3. **Configuração mínima que reproduz o problema**

   ```yaml
   # moclojer.yml (reduzido ao mínimo)
   - endpoint:
       path: /test
       response:
         body: "ok"
   ```

4. **Comando exato usado**

   ```bash
   moclojer --config moclojer.yml --port 8000
   ```

5. **Erro completo** (copie tudo!)

   ```
   Error: ...
   ```

6. **Request que você está fazendo**

   ```bash
   curl -v http://localhost:8000/test
   ```

### Onde Pedir Ajuda

1. **GitHub Discussions** (perguntas gerais)
   - <https://github.com/moclojer/moclojer/discussions>

2. **GitHub Issues** (bugs)
   - <https://github.com/moclojer/moclojer/issues>
   - Use template de bug report

3. **FAQ** (perguntas frequentes)
   - [FAQ](faq.md)

---

## ✅ Checklist de Troubleshooting

Quando algo não funciona, siga esta checklist:

- [ ] Verifiquei a versão do moclojer (`--version`)
- [ ] Testei com configuração mínima
- [ ] Verifiquei indentação do YAML (2 espaços, sem tabs)
- [ ] Validei YAML em yamllint.com
- [ ] Verifiquei se porta está livre (`lsof -i :8000`)
- [ ] Testei com `curl -v` para ver detalhes
- [ ] Verifiquei logs do servidor
- [ ] Método HTTP correto (GET/POST/etc)
- [ ] Path exato (case-sensitive)
- [ ] Templates com sintaxe correta (`{{path-params.id}}`)
- [ ] Para Docker: verificado port mapping e volumes
- [ ] Para CORS: habilitado `--enable-cors`

---

## 📚 Veja Também

- [FAQ](faq.md) - Perguntas frequentes
- [CLI Reference](cli-reference.md) - Todas as opções de linha de comando
- [Configuration Spec](configuration-spec.md) - Referência do YAML
- [YAML Format Guide](../topics/configuration/yaml-format.md) - Sintaxe YAML
