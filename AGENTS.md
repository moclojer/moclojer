# AGENTS.md

This file provides guidance to AI coding assistants (Claude Code, Cursor, GitHub Copilot, etc.) when working with code in this repository.

## Project Overview

Moclojer is an HTTP mock server written in Clojure that generates mock APIs from YAML, EDN, or OpenAPI specifications. It supports both HTTP endpoints and WebSocket connections.

## Development Commands

```bash
# Run the server
clj -M:run

# Run all tests
clj -M:test

# Run a specific test
clj -M:test -n com.moclojer.external-body.excel-test

# Lint the source code
clj -M:lint

# Start nREPL for interactive development
clj -M:nrepl

# Build JAR file
clj -A:dev -M --report stderr -m com.moclojer.build

# Deploy to Clojars (requires CLOJARS_USERNAME and CLOJARS_PASSWORD env vars)
clj -X:deploy-clojars
```

## Architecture

### Core Flow

1. **Entry Point** (`com.moclojer.core`): CLI parsing via babashka.cli, loads config and starts server
2. **Adapters** (`com.moclojer.adapters`): Transforms CLI args/env into config, generates routes from config
3. **Router** (`com.moclojer.router`): Smart router that detects spec type (moclojer vs OpenAPI) and delegates
4. **Server** (`com.moclojer.server`): http-kit server with reitit router, includes file watcher for hot reload

### Spec Processing

- **`com.moclojer.specs.moclojer`**: Converts moclojer YAML/EDN spec to reitit routes. Handles HTTP endpoints, WebSocket endpoints, template rendering (Selmer), and webhooks
- **`com.moclojer.specs.openapi`**: Converts OpenAPI v3 spec to moclojer format (path conversion: `{id}` -> `:id`)

### Key Components

- **Template Engine**: Uses Selmer for dynamic responses. Variables: `{{path-params.name}}`, `{{query-params.name}}`, `{{json-params.field}}`
- **External Body**: Supports JSON and XLSX files as response sources (`com.moclojer.external-body.*`)
- **Middleware**: Rate limiting (`middleware/rate_limit.clj`) and latency simulation (`middleware/latency.clj`)
- **Webhooks**: Async webhook calls with configurable delay and conditions (`com.moclojer.webhook`)
- **File Watcher**: Hot reload on config changes (disabled in GraalVM native image)

### WebSocket Support

WebSocket endpoints use http-kit's `as-channel` with pattern matching for messages. Config structure:
```yaml
- websocket:
    path: /ws/echo
    on-connect:
      response: "welcome"
    on-message:
      - pattern: "ping"
        response: "pong"
```

## Configuration

- Default config location: `~/.config/moclojer.yml` (XDG_CONFIG_HOME)
- Environment variables: `CONFIG`, `MOCKS`, `PORT`, `HOST`, `SENTRY_DSN`
- Supports multi-domain routing via `host` field in endpoint config

## Testing

Tests are in `test/com/moclojer/`. Resource files for tests are in `test/com/moclojer/resources/`.

## Code Style

- Namespaces follow `com.moclojer.*` convention
- Private functions use `^:private` metadata or `-` prefix (e.g., `-main`)
- Use threading macros (`->`, `->>`) for transformation pipelines
- Docstrings required for all public functions
- Logging via `com.moclojer.log/log`, never `println`
- kebab-case for functions and variables, PascalCase avoided

## Patterns

- **Atom for state**: Router uses atom for hot-reload (`*router`)
- **Specs as data**: YAML/EDN config → Clojure map → reitit routes
- **Middleware chain**: Rate limit → Latency → Parameters → Handler
- **Template rendering**: Selmer interpolates variables in responses
- **Async webhooks**: `core.async/go` blocks for delayed HTTP calls

## Architecture Decisions

- **http-kit** over ring-jetty: native WebSocket and async support
- **reitit** over compojure: data-driven routing, better for dynamic generation
- **Selmer** for templates: familiar syntax (Jinja2-like), safe by default
- **No database**: mock server is stateless by design
- **XDG compliance**: respects `XDG_CONFIG_HOME` for config location

## Extension Points

- **New spec format**: add parser in `com.moclojer.specs/`, update `router/smart-router`
- **New external-body provider**: add in `com.moclojer.external-body/`, update `type-identification`
- **New middleware**: add in `com.moclojer.middleware/`, register in `server.clj` middleware chain

## Common Pitfalls

- File watcher disabled in GraalVM native image (check `running-on-native-image?`)
- Path params need explicit type for swagger: `:id|int` not just `:id`
- WebSocket patterns are regex strings, escape special characters
- `external-body` paths are relative to execution directory
- Rate limit uses in-memory atom, resets on restart

## Key Dependencies

| Library | Purpose |
|---------|---------|
| http-kit | HTTP server + WebSocket |
| reitit | Routing + Swagger generation |
| selmer | Template engine (Jinja2-like) |
| cheshire | JSON encoding/decoding |
| io.forward/yaml | YAML parsing |
| timbre | Structured logging |
| core.async | Async operations (webhooks) |
| malli | Schema validation + inference |

## Critical Files

| File | Purpose | When to modify |
|------|---------|----------------|
| `specs/moclojer.clj` | Core spec parser, route generation | Adding new YAML/EDN features |
| `specs/openapi.clj` | OpenAPI converter | OpenAPI compatibility |
| `router.clj` | Smart router, format detection | Changing routing logic |
| `server.clj` | HTTP server, middleware chain | Adding middleware |
| `adapters.clj` | Public API for library usage | Library interface changes |
| `webhook.clj` | Async webhook execution | Webhook behavior |
| `external_body/core.clj` | External file loading | New file format support |

## Data Flow

```
YAML/EDN/OpenAPI file
        ↓
io-utils/open-file (parse to Clojure data)
        ↓
router/smart-router (detect spec format)
        ↓
specs/moclojer/->reitit or specs/openapi/->moclojer
        ↓
Reitit route definitions with handlers
        ↓
server/reitit-router (wrap with middleware chain)
        ↓
http-kit/run-server (start listening)
        ↓
Request → Middleware → Handler → selmer/render → Response
```

## Common Tasks

### Adding a new response template variable
1. Edit `specs/moclojer.clj` → `build-parameters` function
2. Add new key to the parameters map
3. Variable becomes available as `{{new-key.field}}` in templates
4. Add test in `test/com/moclojer/specs/moclojer_test.clj`

### Adding a new external-body provider (e.g., CSV)
1. Create `src/com/moclojer/external_body/csv.clj`
2. Implement function that returns parsed content as Clojure data
3. Add case in `external_body/core.clj` → `type-identification`
4. Add test with sample file in `test/com/moclojer/resources/`

### Adding a new middleware
1. Create `src/com/moclojer/middleware/new_middleware.clj`
2. Implement `wrap-*` function following Ring middleware pattern
3. Add to middleware vector in `server.clj` → `reitit-router`
4. Add test in `test/com/moclojer/middleware/`

### Adding a new spec format
1. Create `src/com/moclojer/specs/newformat.clj`
2. Implement `->moclojer` or `->reitit` conversion function
3. Update `router.clj` → `smart-router` to detect new format
4. Add test in `test/com/moclojer/specs/`

## Glossary

| Term | Meaning |
|------|---------|
| spec | Mock endpoint definition in YAML/EDN/OpenAPI format |
| endpoint | Single HTTP route with method, path, and response |
| external-body | Response body loaded from external file (JSON, XLSX) |
| path-params | URL parameters extracted from path (`:id` in `/users/:id`) |
| query-params | URL query string parameters (`?name=value`) |
| json-params | Request body parsed as JSON |
| webhook | Async HTTP call triggered after sending response |
| router | Reitit data structure defining all routes |
| handler | Function that processes request and returns response |

## Anti-patterns

**Don't do this:**

- `println` for logging → use `com.moclojer.log/log`
- Manual JSON parsing → use `cheshire/parse-string` or `data.json`
- Blocking in handlers → use `core.async/go` for delays
- Hardcoded config paths → use XDG pattern via `config/with-xdg`
- Throwing exceptions silently → log with context, return error response
- Direct `slurp` for config → use `io-utils/open-file` (handles YAML/EDN/JSON)
- Mutable state outside atoms → router state must be in atom for hot-reload

## Tests as Documentation

| Test file | Documents |
|-----------|-----------|
| `framework_test.clj` | Library usage as dependency |
| `websocket_test.clj` | WebSocket endpoint patterns |
| `webhook_test.clj` | Async webhook behavior |
| `specs/moclojer_test.clj` | Spec parsing, template variables |
| `specs/openapi_test.clj` | OpenAPI conversion |
| `external_body/*_test.clj` | External file loading |
| `middleware/*_test.clj` | Middleware behavior |
