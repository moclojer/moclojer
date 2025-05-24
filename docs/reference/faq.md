---
description: >-
  Frequently asked questions about moclojer. Find quick answers to common
  installation, configuration, and usage questions.
---

# Frequently Asked Questions

## Getting Started

### What is moclojer?

moclojer is a simple and efficient HTTP mock server that helps you simulate APIs for development, testing, and prototyping. It uses YAML, EDN, or OpenAPI configuration files to define mock endpoints and their responses.

### How is moclojer different from other mock servers?

- **Simple configuration** - Uses familiar YAML syntax
- **Dynamic responses** - Powerful template system for realistic mocks
- **Multiple formats** - Supports YAML, EDN, and OpenAPI
- **WebSocket support** - Real-time communication mocking
- **No programming required** - Just configuration files
- **Clojure integration** - Can be used as a library in Clojure applications

### Do I need to know Clojure to use moclojer?

No! While moclojer is written in Clojure, you can use it entirely through YAML configuration files. Clojure knowledge is only needed if you want to use moclojer as a library in your own applications.

## Installation

### Which installation method should I choose?

- **Docker** - Best for trying moclojer or containerized environments
- **JAR file** - Good for CI/CD, scripts, or when you have Java installed
- **Native binary** - Fastest startup, Linux only, no Java required
- **From source** - For Clojure developers or contributors

### I'm getting "Java not found" errors

moclojer requires Java 11 or higher. Install Java and ensure it's in your PATH:

```bash
java -version
```

If Java isn't installed, download it from [OpenJDK](https://openjdk.org/) or use your system's package manager.

### Can I run moclojer without installing anything?

Yes! Use Docker:

```bash
docker run -it -p 8000:8000 ghcr.io/moclojer/moclojer:latest
```

### The installation script fails with permission errors

On Linux/macOS, you might need `sudo`:

```bash
sudo bash < <(curl -s https://raw.githubusercontent.com/moclojer/moclojer/main/install.sh)
```

## Configuration

### Where should I put my configuration file?

moclojer looks for configuration files in this order:
1. File specified with `--config` option
2. `./moclojer.yml` (current directory)
3. `~/.config/moclojer.yml` (user config directory)
4. `/etc/moclojer.yml` (system config)

### Can I use JSON instead of YAML?

Currently, moclojer supports YAML, EDN, and OpenAPI formats. JSON support might be added in future versions. You can easily convert JSON to YAML using online tools.

### How do I organize large configuration files?

Break your configuration into logical sections using YAML comments:

```yaml
# === USER MANAGEMENT ===
- endpoint:
    method: GET
    path: /users
    # ... configuration

# === PRODUCT CATALOG ===
- endpoint:
    method: GET
    path: /products
    # ... configuration
```

### Can I include other files in my configuration?

Currently, moclojer doesn't support file includes, but you can:
- Use external bodies to load response data from files
- Combine multiple YAML files using tools like `yq`
- Use environment variables for dynamic configuration

## Template Variables

### My template variables show as empty strings

This usually happens when:
- Parameter name doesn't match (check spelling and case)
- Parameter isn't present in the request
- JSON body is malformed

Debug by checking:
```yaml
body: >
  {
    "debug": {
      "path_id": "{{path-params.id}}",
      "query_search": "{{query-params.search}}",
      "json_name": "{{json-params.name}}"
    }
  }
```

### How do I access nested JSON properties?

Use dot notation:

```yaml
# Request: {"user": {"profile": {"name": "John"}}}
"name": "{{json-params.user.profile.name}}"
```

### Can I use template variables in headers?

Yes! Template variables work in response headers:

```yaml
response:
  headers:
    X-User-ID: "{{path-params.id}}"
    Content-Type: application/json
```

### How do I handle array data?

Access array elements by index:

```yaml
# Request: {"tags": ["javascript", "tutorial"]}
"first_tag": "{{json-params.tags.0}}",
"second_tag": "{{json-params.tags.1}}"
```

### Why are my numbers showing as strings?

Make sure you don't put quotes around numeric template variables:

```yaml
# Correct - number
"age": {{json-params.age}}

# Incorrect - string
"age": "{{json-params.age}}"
```

## HTTP Methods and Status Codes

### Can I use custom HTTP methods?

moclojer supports all standard HTTP methods (GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD). Custom methods might work, but aren't officially supported.

### How do I return different status codes?

Use the `status` field in your response:

```yaml
response:
  status: 201  # Created
  status: 404  # Not Found
  status: 422  # Unprocessable Entity
```

### Can I return the same endpoint with different status codes?

Create separate endpoint configurations for different scenarios:

```yaml
# Success case
- endpoint:
    method: GET
    path: /users/123
    response:
      status: 200

# Error case
- endpoint:
    method: GET
    path: /users/999
    response:
      status: 404
```

## Performance

### How many requests can moclojer handle?

Performance depends on your system, but moclojer can typically handle hundreds of concurrent requests. For high-load testing, monitor CPU and memory usage.

### Can I run multiple moclojer instances?

Yes! Run multiple instances on different ports:

```bash
# Instance 1
PORT=8000 moclojer --config api1.yml

# Instance 2  
PORT=8001 moclojer --config api2.yml
```

### Does moclojer cache responses?

moclojer doesn't cache responses by default - each request is processed fresh. This ensures template variables are always current.

### How do I optimize for better performance?

- Use simpler response bodies for high-volume endpoints
- Minimize complex nested template variables
- Consider using external bodies for large static responses
- Monitor system resources during load testing

## Advanced Features

### How do I simulate slow APIs?

Currently, moclojer doesn't have built-in delay functionality. You can:
- Use external tools like `tc` (traffic control) on Linux
- Add delays in your client code
- Use a proxy with delay capabilities

### Can I simulate authentication?

Yes! Use headers and conditional responses:

```yaml
# Authenticated request
- endpoint:
    method: GET
    path: /protected
    response:
      status: 200
      body: >
        {
          "user": "{{header-params.Authorization}}",
          "data": "Secret information"
        }

# Unauthorized request
- endpoint:
    method: GET
    path: /protected/unauthorized
    response:
      status: 401
      body: >
        {
          "error": "Unauthorized",
          "message": "Missing or invalid token"
        }
```

### How do I mock file uploads?

moclojer receives file upload data but currently doesn't provide special template variables for file content. You can access form data through the request body.

### Can I use moclojer with HTTPS?

moclojer runs HTTP by default. For HTTPS:
- Use a reverse proxy like nginx with SSL termination
- Run moclojer behind a load balancer with SSL
- Use Docker with SSL proxy containers

## WebSockets

### Do WebSockets work the same as HTTP endpoints?

WebSockets have their own configuration format:

```yaml
- websocket:
    path: /ws/chat
    on-connect:
      response: '{"status": "connected"}'
    on-message:
      - pattern: "ping"
        response: "pong"
```

### Can I use template variables in WebSocket responses?

Yes! WebSocket responses support the same template variables as HTTP responses, plus the special `{{message}}` variable for the received message.

### How do I test WebSocket endpoints?

Use tools like:
- `websocat` command line tool
- Browser developer tools
- WebSocket testing applications like Postman

## Troubleshooting

### moclojer starts but I get "connection refused"

Check that:
- moclojer is actually running (check terminal output)
- You're using the correct port (default is 8000)
- No firewall is blocking the connection
- Another service isn't using the same port

### My configuration file isn't being loaded

Verify:
- File exists and is readable
- YAML syntax is correct (use a YAML validator)
- File path is correct when using `--config`
- No permission issues accessing the file

### "Port already in use" error

Another service is using port 8000. Either:
- Stop the other service
- Use a different port: `PORT=3000 moclojer`
- Find what's using the port: `lsof -i :8000` (macOS/Linux)

### Template variables aren't working

Common issues:
- Typos in variable names
- Using wrong parameter type (path-params vs query-params)
- Malformed JSON in request body
- Missing parameters in the request

Debug by adding a debug endpoint:

```yaml
- endpoint:
    method: POST
    path: /debug
    response:
      body: >
        {
          "path_params": "{{path-params.id}}",
          "query_params": "{{query-params.search}}",
          "json_params": "{{json-params.name}}",
          "headers": "{{header-params.User-Agent}}"
        }
```

### Configuration changes aren't reflected

moclojer loads configuration at startup. Restart the server after making changes:
- Stop with Ctrl+C
- Start again with the same command

## Integration

### Can I use moclojer in my test suite?

Yes! moclojer is excellent for testing:

```bash
# Start moclojer in background
moclojer --config test-mocks.yml &
MOCLOJER_PID=$!

# Run your tests
npm test

# Stop moclojer
kill $MOCLOJER_PID
```

### How do I integrate with CI/CD pipelines?

Use Docker for consistent environments:

```yaml
# GitHub Actions example
- name: Start moclojer
  run: |
    docker run -d -p 8000:8000 \
      -v ${{ github.workspace }}/mocks.yml:/app/moclojer.yml \
      ghcr.io/moclojer/moclojer:latest

- name: Run tests
  run: npm test
```

### Can I generate OpenAPI specs from moclojer configs?

Currently, moclojer consumes OpenAPI specs but doesn't generate them. You can:
- Write OpenAPI specs manually
- Use tools to convert YAML to OpenAPI format
- Generate documentation from your moclojer configurations using custom scripts

## Getting Help

### Where can I get more help?

- **GitHub Discussions** - [Community Q&A and ideas](https://github.com/moclojer/moclojer/discussions)
- **GitHub Issues** - [Bug reports and feature requests](https://github.com/moclojer/moclojer/issues)
- **Documentation** - [Complete guides and references](https://docs.moclojer.com)

### How do I report a bug?

1. Check if it's already reported in [GitHub Issues](https://github.com/moclojer/moclojer/issues)
2. Create a minimal reproduction case
3. Include moclojer version, OS, and configuration details
4. Provide error messages and logs

### How do I request a feature?

1. Check [GitHub Discussions](https://github.com/moclojer/moclojer/discussions) for existing requests
2. Start a discussion explaining your use case
3. Provide examples of how the feature would work
4. Explain why existing features don't meet your needs

### Can I contribute to moclojer?

Absolutely! See the [Contributing Guide](../community/contributing.md) for:
- Setting up development environment
- Code style guidelines
- Pull request process
- Ways to contribute beyond code