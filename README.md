# moclojer

[![moclojer](https://github.com/moclojer/moclojer/raw/main/docs/assets/logo.png)](https://github.com/moclojer/moclojer)

Simple and efficient HTTP mock server with specification in `yaml`, `edn` or `OpenAPI`.

> 💾 Download the binary with the latest version of moclojer to test on your computer [here](https://github.com/moclojer/moclojer/releases/latest).

[![tests](https://github.com/moclojer/moclojer/actions/workflows/tests.yml/badge.svg?branch=main)](https://github.com/moclojer/moclojer/actions/workflows/tests.yml) [![linter](https://github.com/moclojer/moclojer/actions/workflows/linter.yml/badge.svg?branch=main)](https://github.com/moclojer/moclojer/actions/workflows/linter.yml)

[**📖 See the complete documentation for moclojer here**](https://avelino.run/projects/moclojer/), if you want to contribute (or complement) the documentation, it is [here](https://github.com/avelino/avelino.run/blob/main/content/projects/moclojer.md).

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
* **port **_**(default)**_**:** `8000`_, if you want to change the port set the environment variable `PORT`_

```
docker run -it \
  -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/moclojer/moclojer:dev
```

**we keep two versions:**

* `dev`: version of the main branch
* `latest`: latest stable version

## manual (Linux & macOS)

```
bash < <(curl -s https://raw.githubusercontent.com/moclojer/moclojer/main/install.sh)
```

> If you are using Linux you maybe need `sudo`.

## CLI Usage

* `clj -M:run [OPTIONS]`
* `java -jar moclojer.jar [OPTIONS]`
* `moclojer_Linux [OPTIONS]`

### Options

#### -c, --config

Config path or the CONFIG environment variable. \[Default: moclojer.yml]

#### -m, --mocks

OpenAPI v3 mocks path or the MOCKS environment variable.

#### -h, --help

Show help information

#### -v, --version

Show version information

## 💻 dev environment

We use git submodule for integration with the [**OpenAPI v3** specification](https://github.com/OAI/OpenAPI-Specification), you need to update the git submodule code.

```
git submodule update -f --init
```

### run

```
clj -M:run
```

### test

```
clj -M:test
```

### `moclojer.jar` generate

```
clj -A:dev -M --report stderr -m moclojer.build
```
