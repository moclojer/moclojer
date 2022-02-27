[![moclojer logo](doc/assets/logo.png)](https://github.com/avelino/moclojer)

Simple and efficient HTTP mock server with specification in `yaml`.

[![tests](https://github.com/avelino/moclojer/actions/workflows/tests.yml/badge.svg?branch=main)](https://github.com/avelino/moclojer/actions/workflows/tests.yml)
[![linter](https://github.com/avelino/moclojer/actions/workflows/linter.yml/badge.svg?branch=main)](https://github.com/avelino/moclojer/actions/workflows/linter.yml)

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

## Body template

The return from the endpoint (_body_) can be dynamic we have a template renderer system, and it is possible to read the parameters passed to the endpoint.

- `path-params`: the parameters passed to the endpoint `/hello/:username`
- `query-params`: the parameters passed in _query string_ to the endpoint `?param1=value1&param2=value2`

**Example**

```json
{
  "path-params": "{{path-params.param1}}",
  "query-params": "{{query-params.param1}}"
}
```

## OpenAPI Integration

You can easily mock all routes routes from a OpenAPI v3 specification.
For this, you will need to define one response for each operation.

> Example `mocks.yaml`

```yaml
listPets:
  status: 200
  headers:
    Content-Type: application/json
  body: >
    []
```

Then call `moclojer` passing both OpenAPI spec and mocks as paramters:

```shell
CONFIG="petstore.yaml" MOCKS="mocks.yaml" clojure -X:run
```

## dev environment

We use git submodule for integration with the [**OpenAPI v3** specification](https://github.com/OAI/OpenAPI-Specification), you need to update the git submodule code.

```sh
git submodule update -f --init
```

## run

```sh
clj -X:run
```

## test

```sh
clj -M:test
```
