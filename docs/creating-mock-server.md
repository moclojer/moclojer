---
description: >-
  moclojer uses the specifications written in the configuration file to declare
  the endpoints, uri parameters, and its return (which can be dynamic with the
  data received in the request)
---

# Creating mock server

Create a file named `moclojer.yml` (we will use yaml because it is a more familiar format to most people, k8s did a great job diseminating this format), inside the yaml file created put the following content:

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
    method: GET
    path: /hello-world
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "hello": "Hello, World!"
        }
- endpoint:
    method: GET
    path: /with-params/:param1
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "path-params": "{{path-params.param1}}",
          "query-params": "{{query-params.param1}}"
        }
- endpoint:
    method: POST
    path: /first-post-route
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "project": "{{json-params.project}}"
        }
```

## Swagger

Describing all endpoints declared in the configuration file, you can see the following:

### `/hello/:username`

{% swagger method="get" path="/hello/:username" baseUrl="" summary="" %}

{% swagger-description %}
Will take a parameter from the url called username and return the username dynamically from the response body.
{% endswagger-description %}

{% swagger-parameter in="path" name="username" type="string" required="true" %}{% endswagger-parameter %}

{% swagger-response status="200: OK" description="" %}

```json
{
  "hello": "{{path-params.username}}!"
}
```

{% endswagger-response %}
{% endswagger %}

### `/hello-world`

{% swagger method="get" path="/hello-world" baseUrl="" summary="" %}

{% swagger-description %}
Static endpoint that returns content that is not dynamic.
{% endswagger-description %}

{% swagger-response status="200: OK" description="" %}

```json
{
  "hello": "Hello, World!"
}
```

{% endswagger-response %}

{% endswagger %}

{% swagger method="get" path="/with-params/:param1" baseUrl="" summary="" %}
{% swagger-description %}

### `/with-params/:param1`

{% swagger method="get" path="/with-params/:param1" baseUrl="" summary="" %}

{% swagger-description %}
It will take a parameter from the url called param1 and the query string called param1, and return both parameters dynamically in the response body. Exemplifying how to consume the two types of parameters in the return from the endpoint.
{% endswagger-description %}

{% swagger-parameter in="path" name="param1" type="string" required="true" %}{% endswagger-parameter %}

{% swagger-parameter in="query" name="param1" type="string" required="true" %}{% endswagger-parameter %}

{% swagger-response status="200: OK" description="" %}

```json
{
  "path-params": "{{path-params.param1}}",
  "query-params": "{{query-params.param1}}"
}
```

{% endswagger-response %}
{% endswagger %}

### `/with-params/:param1`

{% swagger method="post" path="first-post-route" baseUrl="" summary="" %}

{% swagger-description %}
It will take a parameter from the body called project, and return the project name dynamically from the response body.
{% endswagger-description %}

{% swagger-parameter in="body" name="project" type="string" required="true" %}{% endswagger-parameter %}

{% swagger-response status="200: OK" description="" %}

```json
{
  "project": "{{json-params.project}}"
}
```

{% endswagger-response %}
{% endswagger %}
