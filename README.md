<img src="doc/assets/logo.png" width="250px">

Simple and efficient HTTP mock server with specification in `yaml`.

**`YAML` example**

```yaml
# This mock register route: GET /hello-world
- endpoint:
    # Note: the method could be omitted because GET is the default
    method: GET
    path: /hello-world
    response:
      # Note: the status could be omitted because 200 is the default
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "hello": "Hello, World!"
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

We use git submodule for integration with the **OpenAPI v3** specification, you need to update the git submodule code.

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
