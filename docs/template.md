---
description: how the template works within the moclojer specification file
---

# Using rendering templates

**moclojer** provides a template processor to make the endpoint's return dynamic, making it easy for you to pass parameters by URI and query string into the endpoint's response.

We use the [Selmer](https://github.com/yogthos/Selmer) template engine _(is inspired by the [Django](https://www.djangoproject.com/) template engine)_, making it possible to place conditions and rules within the template. The [Selmer documentation](https://github.com/yogthos/Selmer#built-in-tags) shows examples of how to do this.

## Path params `path-params`

Parameter passed by the endpoint's **URI/URL**.

We use the `:field-name` signature to pass the dynamic parameter to the URL/URI.
To access this field in the template, simply use the following syntax: `{{path-params.field-name}}`.

**Sample:**

```yaml
- endpoint:
    method: GET
    path: /:field-name
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "path-params": "{{path-params.field-name}}"
        }   
```

## Query params `query-params`

Parameter passed **query string**.

We use the `?field-name=dynamic field name` signature to pass the dynamic parameter to the query string, generally used in the http verb `GET`.
To access this field in the template, simply use the following syntax: `{{query-params.field-name}}`.

**Sample:**

```yaml
- endpoint:
    method: GET
    path: /
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "path-params": "{{query-params.field-name}}"
        }   
```

## JSON params `json-params`

Parameter passed via **JSON**, generally used in the http verbs `POST`, `PUT` and `DELETE`.
To access this field in the template, simply use the following syntax: `{{json-params.field-name}}`.

**Sample:**

```yaml
- endpoint:
    method: GET
    path: /
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "json-params": "{{json-params.field-name}}"
        }   
```

**Sending data:**

```sh
curl -X POST http://.../
   -H 'Content-Type: application/json'
   -d '{"field-name":"dynamic field name"}'
```
