---
description: Moclojer now supports webhooks, allowing for background requests to other APIs or endpoints.

# Webhook support (background request)

Is it possible to add a **trigger** when receiving a request on an endpoint (for the _api world_ we call it a webhook):

```yaml
- endpoint:
    method: POST
    path: /with-webhook
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {"id": 123}
    webhook:
      sleep-time: 60
      url: https://moclojer.com/api/webhook
      method: POST
      body: >
        {"id": 123, "another-field": "it's working"}
```

Upon receiving a request for the `/with-webhook` endpoint, the moclojer will respond with status `200` and body `{"id": 123}`.
In addition, it will make a request to the `https://moclojer.com/api/webhook` endpoint with method `POST` and body `{"id": 123, "another-field": "it's working"}`.

* `sleep-time` _(field is optional, default value of `60 seconds`)_: is used to delay the request to the webhook endpoint, if you want to simulate a long processing time before sending the request.

> Moclojer will not wait for the response from the webhook endpoint; it will only send the request and continue to respond to the original request. This process is asynchronous.
