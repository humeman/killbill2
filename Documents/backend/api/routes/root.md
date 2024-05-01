### [Kill Bill 2](../../../../README.md) → [Docs](../../../README.md) → [Backend](../../README.md) → [API](../README.md) → Routes: /
---

# Routes: /
This contains general API routes which don't fit in any other category.

---

<details>
    <summary><code>GET</code> <code><b>/</b></code>: Gets API status information.</summary>

##### Authentication
None

##### Parameters
None

##### Response
* `name` (`str`): Name of the API that is running
* `version` (`str`): Package version of the API

##### Preconditions
None

##### Sample request
```java
HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../"))
  .GET()
  .build();
```

##### Sample response
```json
{
   "success": true,
   "data": {
       "name": "today.tecktip.killbill.backend.Application",
       "version": "0.0.1-SNAPSHOT"
   },
   "error": null
}
```

</details>