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

Describing all endpoints declared in the configuration file, you can see the following:

| path                   | method | descrition                                                                                                                                                                                                                                    |
| ---------------------- | ------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `/hello/:username`     | GET    | Will take a parameter from the url called username and return the username dynamically from the response body.                                                                                                                                |
| `/hello-world`         | GET    | Static endpoint that returns content that is not dynamic.                                                                                                                                                                                     |
| `/with-params/:param1` | GET    | It will take a parameter from the url called param1 and the query string called param1, and return both parameters dynamically in the response body. Exemplifying how to consume the two types of parameters in the return from the endpoint. |
| `/first-post-route`    | POST   | It will take a parameter from the body called project, and return the project name dynamically from the response body.                                                                                                                        |
