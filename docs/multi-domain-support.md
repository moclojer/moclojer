---
description: moclojer supports multi-domain (host)
---

# Multi-domain support

To add a specific domain for the endpoint just put the tag “host” for which domain the endpoint will respond to the request:

```yaml
- endpoint:
    host: sub.moclojer.com
    method: GET
    path: /multihost-sub
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "domain": "sub.moclojer.com"
        }
```

**Swagger:**

{% swagger method="get" path="/multihost-sub" baseUrl="sub.moclojer.com" summary="" %}

{% swagger-description %}
Specific domain for the endpoint (aka baseUrl).
{% endswagger-description %}

{% swagger-response status="200: OK" description="" %}

```json
{
  "domain": "sub.moclojer.com"
}
```

{% endswagger-response %}
{% endswagger %}
