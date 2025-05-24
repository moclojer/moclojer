---
description: >-
  Quick and easy installation guide for moclojer. Choose from Docker, standalone JAR,
  or package installation methods.
---

# Installation

Get moclojer running on your system in just a few minutes. Choose the installation method that works best for your setup.

## Quick Start (Recommended)

The fastest way to try moclojer is with Docker:

```bash
docker run -it -p 8000:8000 ghcr.io/moclojer/moclojer:latest
```

This will start moclojer on port 8000 with a default configuration. You can now test it by visiting `http://localhost:8000` in your browser.

## Installation Methods

### 🐳 Docker (Recommended)

Docker is the easiest way to get started, especially for trying moclojer or using it in development.

**Requirements:**
- Docker installed on your system

**Basic usage:**
```bash
# Run with default configuration
docker run -it -p 8000:8000 ghcr.io/moclojer/moclojer:latest

# Run with your own configuration file
docker run -it \
  -p 8000:8000 \
  -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/moclojer/moclojer:latest
```

**Available Docker tags:**
- `latest` - Latest stable release
- `dev` - Latest development version from main branch

**Custom port:**
```bash
# Run on port 3000 instead of 8000
docker run -it -p 3000:3000 -e PORT=3000 ghcr.io/moclojer/moclojer:latest
```

### ☕ Standalone JAR

The JAR file works on any system with Java installed. Perfect for CI/CD, scripts, or when you don't want to use Docker.

**Requirements:**
- Java 11 or higher

**Download and run:**
```bash
# Download the latest version
curl -L -o moclojer.jar https://github.com/moclojer/moclojer/releases/latest/download/moclojer.jar

# Run it
java -jar moclojer.jar

# Or with a custom configuration
java -jar moclojer.jar --config my-config.yml
```

**Quick installation script:**
```bash
bash < <(curl -s https://raw.githubusercontent.com/moclojer/moclojer/main/install.sh)
```

> **Note:** On Linux, you might need `sudo` for the installation script.

### 📦 Native Binary (Linux)

For Linux systems, we provide a native binary that doesn't require Java.

**Download:**
```bash
# Download and make executable
curl -L -o moclojer https://github.com/moclojer/moclojer/releases/latest/download/moclojer_Linux
chmod +x moclojer

# Move to your PATH (optional)
sudo mv moclojer /usr/local/bin/
```

**Usage:**
```bash
./moclojer --config moclojer.yml
```

### 🔧 From Source (Clojure)

If you're a Clojure developer or want to contribute to moclojer.

**Requirements:**
- Clojure CLI tools installed
- Git

**Clone and run:**
```bash
git clone https://github.com/moclojer/moclojer.git
cd moclojer
clj -M:run
```

**Build your own JAR:**
```bash
clj -A:dev -M --report stderr -m com.moclojer.build
```

## Verification

After installation, verify that moclojer is working:

1. **Create a simple configuration file** named `moclojer.yml`:

```yaml
- endpoint:
    method: GET
    path: /hello
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "message": "Hello from moclojer!",
          "timestamp": "{{now}}"
        }
```

2. **Start moclojer** with your configuration:

```bash
# Using Docker
docker run -it -p 8000:8000 -v $(pwd)/moclojer.yml:/app/moclojer.yml ghcr.io/moclojer/moclojer:latest

# Using JAR
java -jar moclojer.jar --config moclojer.yml

# Using native binary
./moclojer --config moclojer.yml
```

3. **Test the endpoint**:

```bash
curl http://localhost:8000/hello
```

You should see:
```json
{
  "message": "Hello from moclojer!",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Configuration

By default, moclojer looks for configuration files in these locations:

1. `./moclojer.yml` (current directory)
2. `~/.config/moclojer.yml` (user config directory)
3. `/etc/moclojer.yml` (system config)

You can specify a custom configuration file with the `--config` option:

```bash
moclojer --config /path/to/your/config.yml
```

## Environment Variables

Configure moclojer using environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | `8000` |
| `CONFIG` | Configuration file path | `~/.config/moclojer.yml` |
| `MOCKS` | Mocks file path (alternative to CONFIG) | - |
| `SENTRY_DSN` | Sentry DSN for error reporting | - |

**Example:**
```bash
PORT=3000 CONFIG=./my-config.yml java -jar moclojer.jar
```

## Command Line Options

| Option | Description |
|--------|-------------|
| `-c, --config PATH` | Configuration file path |
| `-m, --mocks PATH` | OpenAPI v3 mocks file path |
| `-f, --format FORMAT` | Output format (`println` or `json`) |
| `-h, --help` | Show help information |
| `-v, --version` | Show version information |

## Troubleshooting

### Port already in use
If port 8000 is already in use:
```bash
# Use a different port
PORT=3001 docker run -it -p 3001:3001 ghcr.io/moclojer/moclojer:latest
```

### Java not found
Make sure Java 11+ is installed:
```bash
java -version
```

### Configuration file not found
Ensure your configuration file exists and is readable:
```bash
ls -la moclojer.yml
```

### Docker permission denied
On Linux, you might need to add your user to the docker group:
```bash
sudo usermod -aG docker $USER
```

## Next Steps

Now that moclojer is installed, let's create your first mock server:

👉 **[Your First Mock Server](your-first-mock.md)**

## Need Help?

- **[FAQ](../reference/faq.md)** - Common installation issues
- **[GitHub Issues](https://github.com/moclojer/moclojer/issues)** - Report bugs or get help
- **[GitHub Discussions](https://github.com/moclojer/moclojer/discussions)** - Community support