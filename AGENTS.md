# AGENTS.md

This file provides guidance to AI coding assistants (Claude Code, Cursor, GitHub Copilot, etc.) when working with code in this repository.

## Project Overview

Moclojer is an HTTP mock server written in Clojure that generates mock APIs from YAML, EDN, OpenAPI, or Postman Collection specifications. It supports both HTTP endpoints and WebSocket connections.

**⚠️ IMPORTANT: Code changes REQUIRE documentation updates!** See [Documentation Requirements](#documentation-requirements) section.

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
3. **Router** (`com.moclojer.router`): Smart router that detects spec type (moclojer vs OpenAPI vs Postman) and delegates
4. **Server** (`com.moclojer.server`): http-kit server with reitit router, includes file watcher for hot reload

### Spec Processing

- **`com.moclojer.specs.moclojer`**: Converts moclojer YAML/EDN spec to reitit routes. Handles HTTP endpoints, WebSocket endpoints, template rendering (Selmer), and webhooks
- **`com.moclojer.specs.openapi`**: Converts OpenAPI v3 spec to moclojer format (path conversion: `{id}` -> `:id`)
- **`com.moclojer.specs.postman`**: Converts Postman Collection v2.1 to moclojer format. Processes nested folders, extracts response examples, converts path variables

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

## Documentation Requirements

**CRITICAL: Always update documentation when making changes!**

### When to Update Documentation

Update documentation in `docs/` for ANY of these changes:

1. **New Features**
   - Add tutorial/guide in `docs/getting-started/` or `docs/advanced/`
   - Update `docs/SUMMARY.md` to include new page
   - Update `docs/README.md` if it affects learning path
   - Add examples in `examples/` directory

2. **New Configuration Options**
   - Update `docs/reference/configuration-spec.md`
   - Add examples in relevant tutorial pages
   - Update CLI reference if applicable

3. **API Changes**
   - Update affected tutorial pages
   - Update troubleshooting guide if needed
   - Add migration notes in release notes

4. **Bug Fixes**
   - Update FAQ if it's a common issue
   - Update troubleshooting guide
   - Add to release notes

### Documentation Checklist

When adding a new feature (like Postman Collection support):

- [ ] Create detailed guide in `docs/getting-started/` or `docs/advanced/`
- [ ] Add entry to `docs/SUMMARY.md`
- [ ] Update `docs/README.md` (getting started section)
- [ ] Update `docs/getting-started/overview.md` (mentions + examples)
- [ ] Update main `README.md` (project root)
- [ ] Update `CLAUDE.md` (this file) with technical details
- [ ] Add example files to `examples/` directory
- [ ] Create `examples/README.md` if needed
- [ ] Add to release notes in `docs/releases/next.md`

### Documentation Structure

```
docs/
├── README.md              # Main docs entry, learning paths
├── SUMMARY.md             # Table of contents for all docs
├── getting-started/       # Beginner tutorials (progressive)
│   ├── overview.md        # What is moclojer? Why use it?
│   ├── installation.md    # Getting moclojer installed
│   ├── postman-collections.md  # Using Postman Collections
│   ├── your-first-mock.md # First YAML mock tutorial
│   └── ...
├── advanced/              # Advanced features
│   ├── websocket-support.md
│   ├── external-bodies.md
│   └── ...
├── reference/             # Technical reference
│   ├── configuration-spec.md
│   ├── faq.md
│   └── ...
└── releases/              # Release notes
    ├── next.md            # Unreleased changes
    └── v*.md              # Version releases
```

### Documentation Style Guide

- **Write for beginners** - Assume no prior moclojer knowledge
- **Use progressive disclosure** - Simple → Complex
- **Include working examples** - Test every code snippet
- **Show, don't just tell** - Practical examples over theory
- **Use clear headings** - Easy to scan and find information
- **Add troubleshooting** - Common issues and solutions
- **Cross-reference** - Link to related docs
- **Keep it up-to-date** - Documentation debt is real debt

### Documentation Testing

Before submitting changes:

1. **Test all code examples** - They must actually work
2. **Check all links** - No broken references
3. **Review formatting** - Proper Markdown rendering
4. **Verify completeness** - No missing steps or TODOs
5. **Read as a beginner** - Is it clear without context?

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
| `specs/postman.clj` | Postman Collection converter | Postman compatibility |
| `router.clj` | Smart router, format detection | Changing routing logic |
| `server.clj` | HTTP server, middleware chain | Adding middleware |
| `adapters.clj` | Public API for library usage | Library interface changes |
| `webhook.clj` | Async webhook execution | Webhook behavior |
| `external_body/core.clj` | External file loading | New file format support |

## Data Flow

```
YAML/EDN/OpenAPI/Postman file
        ↓
io-utils/open-file (parse to Clojure data)
        ↓
router/smart-router (detect spec format: :moclojer, :openapi, :postman)
        ↓
specs/moclojer/->reitit or specs/openapi/->moclojer or specs/postman/->moclojer
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
5. **📚 Update `docs/topics/templates/template-variables.md` with new variable**

### Adding a new external-body provider (e.g., CSV)
1. Create `src/com/moclojer/external_body/csv.clj`
2. Implement function that returns parsed content as Clojure data
3. Add case in `external_body/core.clj` → `type-identification`
4. Add test with sample file in `test/com/moclojer/resources/`
5. **📚 Update `docs/advanced/external-bodies.md` with CSV example**

### Adding a new middleware
1. Create `src/com/moclojer/middleware/new_middleware.clj`
2. Implement `wrap-*` function following Ring middleware pattern
3. Add to middleware vector in `server.clj` → `reitit-router`
4. Add test in `test/com/moclojer/middleware/`
5. **📚 If user-facing: create `docs/advanced/new-middleware.md` guide**

### Adding a new spec format
1. Create `src/com/moclojer/specs/newformat.clj`
2. Implement `->moclojer` or `->reitit` conversion function
3. Update `router.clj` → `smart-router` to detect new format
4. Add test in `test/com/moclojer/specs/`
5. **📚 DOCUMENTATION (REQUIRED):**
   - Create `docs/getting-started/newformat.md` tutorial
   - Update `docs/SUMMARY.md` with new page
   - Update `docs/README.md` and `docs/getting-started/overview.md`
   - Update main `README.md` to mention new format
   - Add example file to `examples/` directory
   - Update `CLAUDE.md` (this file)

**Example: Postman Collection support (see `specs/postman.clj`)**
- Detection: `smart-router` checks for `:info` and `:item` keys (Postman Collection v2.1 structure)
- Conversion: `->moclojer` processes nested folders recursively, extracts response examples from `:response` array
- Path handling: Converts both string URLs and object URLs to moclojer path format
- Headers: Filters disabled headers, preserves enabled ones
- Status codes: Uses `:code` from response examples, defaults to 200
- Test file: `specs/postman_test.clj` includes nested folder tests, URL format variations, disabled headers

## Glossary

| Term | Meaning |
|------|---------|
| spec | Mock endpoint definition in YAML/EDN/OpenAPI/Postman format |
| endpoint | Single HTTP route with method, path, and response |
| external-body | Response body loaded from external file (JSON, XLSX) |
| path-params | URL parameters extracted from path (`:id` in `/users/:id`) |
| query-params | URL query string parameters (`?name=value`) |
| json-params | Request body parsed as JSON |
| webhook | Async HTTP call triggered after sending response |
| router | Reitit data structure defining all routes |
| handler | Function that processes request and returns response |
| postman collection | JSON export from Postman with API requests and response examples |

## Anti-patterns

**Don't do this:**

- `println` for logging → use `com.moclojer.log/log`
- Manual JSON parsing → use `cheshire/parse-string` or `data.json`
- Blocking in handlers → use `core.async/go` for delays
- Hardcoded config paths → use XDG pattern via `config/with-xdg`
- Throwing exceptions silently → log with context, return error response
- Direct `slurp` for config → use `io-utils/open-file` (handles YAML/EDN/JSON)
- Mutable state outside atoms → router state must be in atom for hot-reload
- **📚 Code without documentation → ALWAYS update `docs/` when adding features**

## Tests as Documentation

| Test file | Documents |
|-----------|-----------|
| `framework_test.clj` | Library usage as dependency |
| `websocket_test.clj` | WebSocket endpoint patterns |
| `webhook_test.clj` | Async webhook behavior |
| `specs/moclojer_test.clj` | Spec parsing, template variables |
| `specs/openapi_test.clj` | OpenAPI conversion |
| `specs/postman_test.clj` | Postman Collection conversion, nested folders, path variables |
| `external_body/*_test.clj` | External file loading |
| `middleware/*_test.clj` | Middleware behavior |
