---
description: Control request rates to your endpoints with configurable limits and windows
---

# Rate Limiting

Moclojer provides built-in rate limiting capabilities to protect your mock endpoints from excessive requests. You can configure rate limits per endpoint with customizable windows and request limits.

## Basic Configuration

To add rate limiting to an endpoint, include the `rate-limit` configuration in your endpoint's metadata:

```yaml
- endpoint:
    method: GET
    path: /rate-limited
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {"message": "Rate limited endpoint"}
    rate-limit:
      window-ms: 60000    # 1 minute window
      max-requests: 10    # Allow 10 requests per window
      key-fn: remote-addr # Use client IP as the rate limit key
```

## Configuration Options

- `window-ms`: Time window in milliseconds for the rate limit (default: 15 minutes)
- `max-requests`: Maximum number of requests allowed within the window (default: 100)
- `key-fn`: Function to extract the rate limit key from the request (default: client IP address)

## Rate Limit Response Headers

When using rate limiting, Moclojer adds the following headers to responses:

- `X-RateLimit-Limit`: Maximum number of requests allowed in the window
- `X-RateLimit-Remaining`: Number of requests remaining in the current window
- `X-RateLimit-Reset`: Time when the current window expires (Unix timestamp)

## Rate Limit Exceeded Response

When a client exceeds the rate limit, they receive a `429 Too Many Requests` response:

```json
{
  "error": "Rate limit exceeded"
}
```

## Examples

### Basic Rate Limiting

```yaml
- endpoint:
    method: GET
    path: /basic-limit
    response:
      status: 200
      body: "OK"
    rate-limit:
      window-ms: 60000    # 1 minute
      max-requests: 5     # 5 requests per minute
```

### Different Keys for Rate Limiting

You can use different keys to track rate limits. For example, you might want to limit by API key instead of IP address:

```yaml
- endpoint:
    method: GET
    path: /api-key-limit
    response:
      status: 200
      body: "OK"
    rate-limit:
      window-ms: 3600000  # 1 hour
      max-requests: 1000  # 1000 requests per hour
      key-fn: api-key     # Use 'api-key' header as the rate limit key
```

### High-Traffic Endpoint

For endpoints that can handle more traffic:

```yaml
- endpoint:
    method: GET
    path: /high-traffic
    response:
      status: 200
      body: "OK"
    rate-limit:
      window-ms: 1000     # 1 second
      max-requests: 50    # 50 requests per second
```

## Testing Rate Limits

You can test rate limits using curl:

```bash
# First request - should succeed
curl -i http://localhost:8000/rate-limited

# Make multiple requests quickly to see rate limiting in action
for i in {1..20}; do
  curl -i http://localhost:8000/rate-limited
done
```

Watch the `X-RateLimit-Remaining` header decrease and eventually receive a 429 response when the limit is exceeded.
