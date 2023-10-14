# moclojer

[![moclojer](https://github.com/moclojer/moclojer/raw/main/docs/assets/logo.png)](https://github.com/moclojer/moclojer)
[![CLA assistant](https://cla-assistant.io/readme/badge/moclojer/moclojer)](https://cla-assistant.io/moclojer/moclojer)

Simple and efficient HTTP mock server with specification written in `yaml`, `edn` or `OpenAPI`.

> 💾 Download the binary with the latest version of moclojer to test on your computer [here](https://github.com/moclojer/moclojer/releases/latest).

[![tests](https://github.com/moclojer/moclojer/actions/workflows/tests.yml/badge.svg?branch=main)](https://github.com/moclojer/moclojer/actions/workflows/tests.yml) [![linter](https://github.com/moclojer/moclojer/actions/workflows/linter.yml/badge.svg?branch=main)](https://github.com/moclojer/moclojer/actions/workflows/linter.yml)

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

## docker

* **image:** `ghcr.io/moclojer/moclojer:dev`
* **port _(default)_:** `8000`_, if you want to change the port set the environment variable `PORT`_

```sh
docker run -it \
  -p 8000:8000 -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/moclojer/moclojer:dev
```

**we Two available versions:**

* `dev`: main branch docker image
* `latest`: latest stable version image

## manual (Linux & macOS)

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
`-h, --help` | Show help information
`-v, --version` | Show version information

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

> _if you want to run a specific test:_ `clj -M:test -n moclojer.external-body.xlsx-test`

**`moclojer.jar` generate:**

```sh
clj -A:dev -M --report stderr -m moclojer.build
```
