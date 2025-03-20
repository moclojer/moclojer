<p align="center">
  <a href="https://github.com/moclojer/moclojer">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://github.com/moclojer/moclojer/raw/main/docs/assets/logo.png">
      <img src="https://github.com/moclojer/moclojer/raw/main/docs/assets/logo.png" height="128">
    </picture>
    <h1 align="center">moclojer</h1>
  </a>
</p>

<p align="center">
  <a aria-label="CLAs Agree" href="https://cla-assistant.io/moclojer/moclojer" target="_blank">
    <img src="https://img.shields.io/badge/CLAs-ff009e.svg?style=for-the-badge&labelColor=000000&label=agree">
  </a>
  <a aria-label="License" href="https://github.com/moclojer/moclojer/blob/main/LICENSE">
    <img alt="" src="https://img.shields.io/badge/MIT-ff009e.svg?style=for-the-badge&labelColor=000000&label=LICENSE">
  </a>
  <a aria-label="Join the community on GitHub" href="https://github.com/moclojer/moclojer/discussions">
    <img alt="" src="https://img.shields.io/badge/Join%20the%20community-ff009e.svg?style=for-the-badge&labelColor=000000&label=Discussion">
  </a>

  <img src="https://github.com/moclojer/moclojer/actions/workflows/linter.yml/badge.svg?branch=main" alt="linter">

  <a aria-label="CI Tests" href="https://github.com/moclojer/moclojer/actions/workflows/tests.yml">
    <img src="https://github.com/moclojer/moclojer/actions/workflows/tests.yml/badge.svg?branch=main" alt="tests">
  </a>
</p>

<p align="center">
  <a href="https://www.producthunt.com/posts/moclojer?utm_source=badge-featured&utm_medium=badge&utm_souce=badge-moclojer" target="_blank"><img src="https://api.producthunt.com/widgets/embed-image/v1/featured.svg?post_id=449961&theme=neutral" alt="moclojer - &#0032;Simple&#0032;and&#0032;efficient&#0032;HTTP&#0032;mock&#0032;server&#0032;with&#0032;easy&#0032;spec | Product Hunt" style="width: 250px; height: 54px;" width="250" height="54" /></a>
</p>

Simple and efficient HTTP mock server with specification written in `yaml`, `edn` or `OpenAPI`.

> 💾 Download the `.jar` file with the latest version of moclojer to test on your computer [here](https://github.com/moclojer/moclojer/releases/latest).

[**📖 See the complete documentation for moclojer here**](https://docs.moclojer.com/), if you want to contribute (or complement) the documentation, it is [here](https://github.com/moclojer/moclojer/tree/main/docs).

**`YAML` example**

```yaml
# This mock register route: GET /hello/:username
- endpoint:
    # Note: the method could be omitted because GET is the default
    method: GET
    path: /hello/:username
    response:
      # Note: the status could be omitted because 200 is the default
      status: 200
      headers:
        Content-Type: application/json
      # Note: the body will receive the value passed in the url using the
      # :username placeholder
      body: >
        {
          "hello": "{{path-params.username}}!"
        }
```

**WebSocket Support**

Moclojer also supports WebSocket connections with a simple configuration approach:

```yaml
# WebSocket echo server
- websocket:
    path: /ws/echo
    on-connect:
      # Message sent when client connects
      response: '{"status": "connected", "message": "Welcome to WebSocket Echo!"}'
    on-message:
      # Simple echo for "ping" message
      - pattern: "ping"
        response: "pong"
      # Echo back any JSON content with an "echo" field
      - pattern: '{"echo": "{{json-params.echo}}"}'
        response: '{"echoed": "{{json-params.echo}}"}'
```

You can test the WebSocket connection using tools like `websocat`:

```sh
websocat "ws://localhost:8000/ws/echo" --text
```

## docker

* **image:** `ghcr.io/moclojer/moclojer:latest`
* **port _(default)_:** `8000`_, if you want to change the port set the environment variable `PORT`_

```sh
docker run -it \
  -p 8000:8000 -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/moclojer/moclojer:latest
```

**We have two versions available:**

* `dev`: main branch docker image
* `latest`: latest stable version image

## manual installation

We distribute via the `.jar` file, you need to have Java installed on your operating system.

```sh
bash < <(curl -s https://raw.githubusercontent.com/moclojer/moclojer/main/install.sh)
```

> If you are using Linux you maybe need `sudo`.

## CLI Usage

* `clj -M:run [OPTIONS]`
* `java -jar moclojer.jar [OPTIONS]`
* `moclojer_Linux [OPTIONS]`

### Options

parameter | description
--- | ---
`-c, --config` | Config path or the CONFIG environment variable. \[**default:** `~/.config/moclojer.yml`\]
`-m, --mocks` | OpenAPI v3 mocks path or the MOCKS environment variable.
`-f, --format` | Output and logging format. Either `println` or `json`.
`-h, --help` | Show help information
`-v, --version` | Show version information

**sentry:** set environment var `SENTRY_DSN` ([sentry doc](https://docs.sentry.io/platforms/node/guides/azure-functions/configuration/options/#dsn)), automatic send backtrace to <sentry.io>

> **Config** uses `XDG_CONFIG_HOME` to fetch the default moclojer configuration file, if you want to set a different directory you must use the `-c` or environment variable `CONFIG`

## 💻 dev environment

moclojer is written in **Clojure**, to run the commands below we assume you have clojure _installed_ on your operating system.

**run:**

```sh
clj -M:run
```

**test:**

```sh
clj -M:test
```

> _if you want to run a specific test:_ `clj -M:test -n com.moclojer.external-body.excel-test`

**`moclojer.jar` generate:**

```sh
clj -A:dev -M --report stderr -m com.moclojer.build
```

## framework integrations

We distribute the library via [Clojars](https://clojars.org/com.moclojer/moclojer).

### Clojure CLI/deps.edn

```clojure
com.moclojer/moclojer {:mvn/version "0.3.1"}
```

### Leiningen/Boot

```clojure
[com.moclojer/moclojer "0.3.1"]
```

### [`git`](https://clojure.org/guides/deps_and_cli#_using_git_libraries) in `deps.edn`

```edn
{:deps
 {com.moclojer/moclojer {:git/url "https://github.com/moclojer/moclojer.git"
                         :git/tag "v0.3.1"
                         :git/sha "c4ca0f2cfcfbe47de6eb0c601b26106190e20793"}}}
```

### example of use

```clj
(ns my-app.core
  (:require [com.moclojer.adapters :as adapters]
            [com.moclojer.server :as server]))

(def *router
  "create a router from a config map"
  (adapters/generate-routes
   [{:endpoint
     {:method "GET"
      :path "/example"
      :response {:status 200
                 :headers {:Content-Type "application/json"}
                 :body {:id 123}}}}]))

(defn -main
  "start the server"
  [& args]
  (server/start-server! *router))
```
