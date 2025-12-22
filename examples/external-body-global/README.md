# External Body Global Configuration Example

This example demonstrates how to use the global `external-body` folder configuration to avoid repeating paths across multiple endpoints.

## Structure

```
external-body-global/
├── config.yml           # Main configuration with global folder setting
├── data/                # Base folder for all external bodies
│   ├── users.json       # Users list
│   ├── products.json    # Products list
│   └── users/           # User details by ID
│       └── 1.json
└── README.md
```

## Benefits

**Without global configuration:**

```yaml
- endpoint:
    path: /api/users
    response:
      external-body:
        provider: json
        path: examples/external-body-global/data/users.json  # Full path

- endpoint:
    path: /api/products
    response:
      external-body:
        provider: json
        path: examples/external-body-global/data/products.json  # Repetitive!
```

**With global configuration:**

```yaml
# Set base folder once
- external-body:
    folder: examples/external-body-global/data

# Use short filenames
- endpoint:
    path: /api/users
    response:
      external-body:
        path: users.json  # Much cleaner!

- endpoint:
    path: /api/products
    response:
      external-body:
        path: products.json  # No repetition!
```

## Running

### Local (JAR or Binary)

```bash
# From project root with JAR
CONFIG=$(pwd)/examples/external-body-global/config.yml java -jar moclojer.jar

# Or with binary
moclojer --config examples/external-body-global/config.yml

# Or using environment variable
CONFIG=examples/external-body-global/config.yml moclojer
```

### Docker

```bash
# Run with Docker (mounting both config and data)
docker run -it \
  -p 8000:8000 \
  -v $(pwd)/examples/external-body-global/docker-config.yml:/app/moclojer.yml \
  -v $(pwd)/examples/external-body-global/data:/config/data \
  ghcr.io/moclojer/moclojer:latest
```

**Note:** The `docker-config.yml` uses absolute paths (`/config/data`) optimized for container environments, while `config.yml` uses relative paths for local development.

## Testing

```bash
# Get all users
curl http://localhost:8000/api/users

# Get all products
curl http://localhost:8000/api/products

# Get user by ID (dynamic path)
curl http://localhost:8000/api/users/1
```

## Use Cases

This pattern is especially useful for:

- **Docker/Kubernetes deployments** - Mount a volume and configure the base path once
- **Multiple environments** - Change the folder path based on environment
- **Clean configuration** - Reduce duplication and improve readability
- **Team collaboration** - Easier to understand and maintain

## See Also

- [External Bodies Documentation](../../docs/advanced/external-bodies.md#-global-configuration)
- [Docker Deployment Guide](../../docs/how-to/deployment/docker.md)
