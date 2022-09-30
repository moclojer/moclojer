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
