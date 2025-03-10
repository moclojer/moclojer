---
description: Simulate failures, latency and data corruption to test your application's resilience
---

# Chaos Testing

Moclojer provides Chaos Testing features to help you test how your application behaves under adverse conditions. You can simulate failures, add latency, and corrupt responses in a controlled manner.

## Basic Configuration

To add Chaos Testing to an endpoint, include the `chaos` configuration in the endpoint metadata:

```yaml
- endpoint:
    method: GET
    path: /chaos-example
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {"message": "Endpoint with chaos testing"}
    chaos:
      latency:
        enabled: true
        min-ms: 100
        max-ms: 1000
        probability: 0.3
      failures:
        enabled: true
        probability: 0.1
      corruption:
        enabled: true
        probability: 0.1
```

## Configuration Options

### Latency (`latency`)

Adds random delays to responses:

- `enabled`: Enable/disable latency simulation
- `min-ms`: Minimum delay in milliseconds
- `max-ms`: Maximum delay in milliseconds
- `probability`: Probability (0-1) of applying delay

### Failures (`failures`)

Simulates failures by returning 500 errors:

- `enabled`: Enable/disable failure simulation
- `probability`: Probability (0-1) of returning an error

### Corruption (`corruption`)

Modifies responses to simulate data corruption:

- `enabled`: Enable/disable data corruption
- `probability`: Probability (0-1) of corrupting the response

## Examples

### Simulating High Latency

```yaml
- endpoint:
    method: GET
    path: /high-latency
    response:
      status: 200
      body: "OK"
    chaos:
      latency:
        enabled: true
        min-ms: 1000    # 1 second
        max-ms: 5000    # 5 seconds
        probability: 0.5 # 50% of requests
```

### Simulating Frequent Failures

```yaml
- endpoint:
    method: GET
    path: /unstable
    response:
      status: 200
      body: "OK"
    chaos:
      failures:
        enabled: true
        probability: 0.3 # 30% failure rate
```

### Combining Multiple Effects

```yaml
- endpoint:
    method: GET
    path: /chaos
    response:
      status: 200
      body: "OK"
    chaos:
      latency:
        enabled: true
        min-ms: 100
        max-ms: 500
        probability: 0.2
      failures:
        enabled: true
        probability: 0.1
      corruption:
        enabled: true
        probability: 0.1
```

## Testing Chaos Features

You can test the effects using curl:

```bash
# Make multiple requests to see different behaviors
for i in {1..10}; do
  time curl -i http://localhost:8000/chaos
done
```

Note that:
- Some requests will be slower than others
- Some will fail with 500 errors
- Some will have corrupted data in the response