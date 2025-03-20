---
description: Reference for the Moclojer configuration file format
---

# Moclojer Configuration Specification

This reference guide provides details about the YAML syntax for Moclojer configuration files.

## Overview

Moclojer uses YAML configuration files to define mock endpoints. Each configuration file contains a list of endpoint definitions that the mock server will respond to.

## Example Configuration

```yaml
- endpoint:
    method: GET
    path: /hello/:username
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "hello": "{{path-params.username}}!"
        }

- endpoint:
    method: POST
    path: /users
    body: |
      {
        "name": "string",
        "email": "string"
      }
    response:
      status: 201
      headers:
        Content-Type: application/json
      body: >
        {
          "id": 123,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}"
        }
```

## Configuration File Structure

A Moclojer configuration file is a YAML file containing an array of endpoint objects.

## Endpoint

Each endpoint object has the following structure:

| Key | Required | Type | Description |
|-----|----------|------|-------------|
| `endpoint` | Yes | Object | Contains the configuration for this mock endpoint |

### Endpoint Object

| Key | Required | Type | Description |
|-----|----------|------|-------------|
| `method` | No | String | HTTP method for this endpoint. Default: `GET` |
| `host` | No | String | Domain this endpoint should respond to. Used with [multi-domain support](multi-domain-support.md) |
| `path` | Yes | String | URL path pattern with optional parameter types (e.g., `/users/:id\|int`) |
| `query` | No | Object | Query parameter definitions with types |
| `body` | No | String/Object | Expected request body schema |
| `response` | Yes | Object | Response configuration |
| `rate-limit` | No | Object | Rate limiting configuration for this endpoint |
| `webhook` | No | Object | Webhook configuration to trigger after handling the request |

## Path Parameters

Moclojer supports typed path parameters using the syntax `:paramName\|type`:

```yaml
path: /users/:id|int/posts/:slug|string
```

Available types:

- `string` (default)
- `int`
- `uuid`
- `date`
- `boolean`

## Query Parameters

Query parameters can be defined and validated:

```yaml
query:
  limit: int
  offset: int
  search: string
```

## Response Object

| Key | Required | Type | Description |
|-----|----------|------|-------------|
| `status` | No | Integer | HTTP status code. Default: `200` |
| `headers` | No | Object | HTTP response headers |
| `body` | No | String/Object | Response body content |
| `external-body` | No | Object | External body configuration for loading response from external source |

## Templates

Moclojer supports templates in the response body using the Handlebars-like syntax:

```yaml
body: >
  {
    "user": "{{path-params.username}}",
    "queryParam": "{{query-params.q}}",
    "jsonData": "{{json-params.field}}"
  }
```

Available template variables:

- `path-params`: URL path parameters
- `query-params`: URL query parameters
- `json-params`: JSON body parameters
- `header-params`: Request headers

## Rate Limiting

Rate limiting can be configured per endpoint:

```yaml
rate-limit:
  window-ms: 60000    # 1 minute window
  max-requests: 10    # Allow 10 requests per window
  key-fn: remote-addr # Use client IP as the rate limit key
```

| Key | Required | Type | Description |
|-----|----------|------|-------------|
| `window-ms` | No | Integer | Time window in milliseconds. Default: `900000` (15 minutes) |
| `max-requests` | No | Integer | Maximum requests per window. Default: `100` |
| `key-fn` | No | String | Function to determine rate limit key. Default: `remote-addr` |

## Webhook

Configure webhooks to be triggered after handling a request:

```yaml
webhook:
  sleep-time: 60      # Sleep time in seconds before triggering webhook
  url: https://example.com/webhook
  method: POST
  body: >
    {"id": 123, "status": "processed"}
  if: path-params.project-name = "moclojer"  # Conditional trigger
```

| Key | Required | Type | Description |
|-----|----------|------|-------------|
| `sleep-time` | No | Integer | Delay in seconds before triggering webhook |
| `url` | Yes | String | Webhook URL |
| `method` | No | String | HTTP method for webhook. Default: `POST` |
| `body` | No | String/Object | Webhook request body |
| `if` | No | String | Conditional expression for triggering webhook |

## External Body

Load response body from external sources:

```yaml
external-body:
  provider: json
  path: path/to/response.json
```

```yaml
external-body:
  provider: json
  path: https://api.example.com/data
```

| Key | Required | Type | Description |
|-----|----------|------|-------------|
| `provider` | Yes | String | Provider type (`json`, `yaml`, `xml`, etc.) |
| `path` | Yes | String | Path to external resource (file or URL) |

## Multiple Domains Support

Moclojer can handle multiple domains in a single configuration:

```yaml
- endpoint:
    host: api.example.com
    method: GET
    path: /v1/resource
    response:
      status: 200
      body: '{"domain": "api.example.com"}'

- endpoint:
    host: admin.example.com
    method: GET
    path: /v1/resource
    response:
      status: 200
      body: '{"domain": "admin.example.com"}'
```

## Full Configuration Example

```yaml
- endpoint:
    method: GET
    path: /users/:id|int
    query:
      fields: string
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "name": "User {{path-params.id}}",
          "fields": "{{query-params.fields}}"
        }
    rate-limit:
      window-ms: 60000
      max-requests: 100

- endpoint:
    method: POST
    path: /users
    body: |
      {
        "name": "string",
        "email": "string"
      }
    response:
      status: 201
      headers:
        Content-Type: application/json
      body: >
        {
          "id": 123,
          "name": "{{json-params.name}}",
          "email": "{{json-params.email}}"
        }
    webhook:
      sleep-time: 5
      url: https://webhook.example.com/new-user
      method: POST
      body: >
        {"userId": 123, "name": "{{json-params.name}}"}

- endpoint:
    host: api.example.com
    method: GET
    path: /products/:id|int
    response:
      status: 200
      external-body:
        provider: json
        path: data/products/{{path-params.id}}.json
```
