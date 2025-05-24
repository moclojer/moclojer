---
description: Moclojer now supports WebSockets, allowing real-time bidirectional communication
---

# WebSocket support

It is possible to configure WebSocket endpoints for real-time bidirectional communication:

```yaml
# WebSocket echo server
- websocket:
    path: /ws/echo
    on-connect:
      # Message sent when client connects
      response: '{"status": "connected", "message": "Welcome to WebSocket Echo!"}'
    on-message:
      # Simple echo for "ping" message
      - pattern: "ping"
        response: "pong"
      # Echo any JSON content with "echo" field
      - pattern: '{"echo": "{{json-params.echo}}"}'
        response: '{"echoed": "{{json-params.echo}}"}'
```

When receiving a connection at the `/ws/echo` endpoint, Moclojer will respond with `{"status": "connected", "message": "Welcome to WebSocket Echo!"}`.

When the client sends the message `ping`, the server will respond with `pong`.

If the client sends a JSON with the `echo` field, such as `{"echo": "hello world"}`, the server will respond with `{"echoed": "hello world"}`.

## WebSocket Properties

* `path`: The URL path for the WebSocket endpoint
* `on-connect`: Configuration for the client connection event
  * `response`: Message sent to the client when it connects
* `on-message`: List of message patterns and their respective responses
  * `pattern`: Message pattern to be matched
  * `response`: Response to be sent when the pattern is found

## Using templates in responses

Just like HTTP endpoints, you can use templates in WebSocket responses:

```yaml
- websocket:
    path: /ws/user/:username
    on-connect:
      response: '{"status": "connected", "message": "Welcome, {{path-params.username}}!"}'
    on-message:
      - pattern: '{"action": "get-profile"}'
        response: '{"user": "{{path-params.username}}", "profile": {"joined": "2025-03-20"}}'
```

You can access parameters from different sources:

* `path-params`: URL path parameters (like `:username` in the example above)
* `query-params`: URL query parameters
* `json-params`: Fields in received JSON messages

## Testing WebSocket connections

You can test WebSocket connections using tools like [websocat](https://github.com/vi/websocat) or [wscat](https://github.com/websockets/wscat):

```sh
# Using websocat
websocat "ws://localhost:8000/ws/echo" --text

# Using wscat
wscat -c ws://localhost:8000/ws/echo
```

**Example interaction:**

```text
Connected to ws://localhost:8000/ws/echo
< {"status": "connected", "message": "Welcome to WebSocket Echo!"}
> ping
< pong
> {"echo": "hello world"}
< {"echoed": "hello world"}
```

> WebSocket support in Moclojer significantly simplifies the development and testing of applications that use real-time communication.

**Swagger:**

{% swagger method="get" path="/ws/echo" baseUrl="" summary="WebSocket Echo Server" %}

{% swagger-description %}
WebSocket endpoint that responds to "ping" messages with "pong" and echoes JSON messages.
{% endswagger-description %}

{% swagger-response status="101: Switching Protocols" description="WebSocket connection established" %}
```json
{
  "status": "connected",
  "message": "Welcome to WebSocket Echo!"
}
```
{% endswagger-response %}
{% endswagger %}