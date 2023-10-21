---
description: extended return support (not specified in the configuration file)
---

# External body

By default, moclojer reads the `endpoint.response.body` parameter for the endpoint return, but when we need a "long" return (with lots of characters) the specification file (yaml example) makes it difficult to maintain and leaves it with low visibility (confusing).

To solve this problem you can stop using `endpoint.response.body` and use `endpoint.response.external-body` which has the following structure:

```yaml
external-body:
  provider: json
  path: files-path.json
```

**We support two providers _(file type)_:**

* `json`
* `xlsx` **(excel)**

> it is possible to use template rendering to declare the `path` of the file, see more [here](template.md).

## `json` type

```yaml
- endpoint:
    method: GET
    path: /external-body-text
    response:
      status: 200
      headers:
        Content-Type: application/json
      external-body:
        provider: json
        path: test/moclojer/resources/text-plan.json
```

## http request (API proxy)

You can use the `json` provider to make URL (site) requests and have it returned to the endpoint:

```yaml
- endpoint:
    method: GET
    path: /pokemon/phanpy
    response:
      status: 200
      headers:
        Content-Type: application/json
      external-body:
        provider: json
        path: https://pokeapi.co/api/v2/pokemon/phanpy
```

## `xlsx` Excel type

This is where the use of moclojer starts to get different, as it is possible to _"transform"_ an **Excel** spreadsheet into an API return (`json`).

```yaml
- endpoint:
    method: GET
    path: /xlsx
    response:
      status: 200
      headers:
        Content-Type: application/json
      external-body:
        provider: xlsx
        path: excel-sample.xlsx
        sheet-name: test
```

**Excel spreadsheet example ([`excel-sample.xlsx`](https://github.com/moclojer/moclojer/blob/main/test/moclojer/resources/excel-sample.xlsx)):**

name | langs
-- | --
avelino | clojure, go
chicao | clojure, python

**response _(expected)_:**

```json
[{"name": "avelino", "langs": "clojure, go"},
{"name": "chicao","langs": "clojure, python"}]
```
