[![moclojer logo](doc/assets/logo.png)](https://github.com/avelino/moclojer)

Simple and efficient HTTP mock server with specification in `yaml` or `edn`.

> **Hot Reload** support, when updating the configuration file (`yaml` or `edn`) the new settings are reloaded automatically

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

**`EDN` example**

```edn
{:endpoint {:method :get
            :path "/pets"
            :response {:status 200
                       :headers {:content-type  "applicantion/json"}
                       :body {:pets [{:name "Uber" :type "dog"}
                                     {:name "Pinpolho" :type "cat"}]}}
            :router-name :get-all-pets}}

{:endpoint {:method :get
            :path "/pet/:id"
            :response {:status 200
                       :headers {:content-type  "applicantion/json"}
                       :body {:id 1 :name "uber" :type "dog"}}
            :router-name :get-pet-by-id}}
```

## Body template

The return from the endpoint (_body_) can be dynamic we have a template renderer system, and it is possible to read the parameters passed to the endpoint.

- `path-params`: the parameters passed to the endpoint `/hello/:username`
- `query-params`: the parameters passed in _query string_ to the endpoint `?param1=value1&param2=value2`
- `json-params`: the parameters passed in _data request_ to the endpoint `{"param1": "value1"}`

**Example**

```json
{
  "path-params": "{{path-params.param1}}",
  "query-params": "{{query-params.param1}}",
  "json-params": "{{json-params.param1}}"
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

you can config a mock server with edn file as well

```shell
CONFIG="moclojer.edn" clojure -X:run
```

## dev environment

We use git submodule for integration with the [**OpenAPI v3** specification](https://github.com/OAI/OpenAPI-Specification), you need to update the git submodule code.

```sh
git submodule update -f --init
```

## docker

**image:** `ghcr.io/avelino/moclojer:latest`

```sh
docker run -it \
  -v $(pwd)/moclojer.yml:/app/moclojer.yml \
  ghcr.io/avelino/moclojer:latest
```

to use the `edn` format, you must pass the following parameters to docker:
`-e CONFIG=moclojer.edn -v $(pwd)/moclojer.edn:/app/moclojer.edn`

## run

```sh
clj -X:run
```

## test

```sh
clj -M:test
```
