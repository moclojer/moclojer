# moclojer

<a href="https://github.com/avelino/moclojer"><img align="right" src="https://github.com/avelino/moclojer/raw/main/doc/assets/logo.png" alt="moclojer" title="moclojer" /></a>

Simple and efficient HTTP mock server with specification in `yaml`, `edn` or `OpenAPI`.

> 💾 Download the binary with the latest version of moclojer to test on your computer [here](https://github.com/avelino/moclojer/releases/latest).

[![tests](https://github.com/avelino/moclojer/actions/workflows/tests.yml/badge.svg?branch=main)](https://github.com/avelino/moclojer/actions/workflows/tests.yml)
[![linter](https://github.com/avelino/moclojer/actions/workflows/linter.yml/badge.svg?branch=main)](https://github.com/avelino/moclojer/actions/workflows/linter.yml)

**[📖 See the complete documentation for moclojer here](https://avelino.run/projects/moclojer/)**, if you want to contribute (or complement) the documentation, it is [here](https://github.com/avelino/avelino.run/blob/main/content/projects/moclojer.md).

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

**image:** `ghcr.io/avelino/moclojer:latest`

```sh
docker run -it \
  -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/avelino/moclojer:latest
```

## 💻 dev environment

We use git submodule for integration with the [**OpenAPI v3** specification](https://github.com/OAI/OpenAPI-Specification), you need to update the git submodule code.

```sh
git submodule update -f --init
```

### run

```sh
clj -X:run
```

### test

```sh
clj -M:test
```

### `moclojer.jar` generate

```sh
clj -A:dev -M --report stderr -m moclojer.build
```
