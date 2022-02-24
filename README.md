# moclojer

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

## run

```sh
clj -X:run
```

## test

```sh
clj -M:test
```
