### [Kill Bill 2](../../../../README.md) → [Docs](../../../README.md) → [Backend](../../README.md) → [API](../README.md) → Routes: /auth
---

# Routes: /auth
This contains endpoints for authenticating a user with a username and password.

---

<details>
    <summary><code>GET</code> <code><b>/auth</b></code>: Tests an authentication key for validity.</summary>

##### Authentication
User authentication key required.

##### Parameters
None

##### Response
None

##### Preconditions
None

##### Notes
This will throw an error if the key is invalid. No data is returned and `success` is `true` otherwise.

##### Sample request
```java
HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../auth"))
  .GET()
  .header("Authorization", "Bearer " + USER_KEY)
  .build();
```

##### Sample response
```json
{
   "success": true,
   "data": null,
   "error": null
}
```

</details>

<details>
    <summary><code>POST</code> <code><b>/auth</b></code>: Signs in a user.</summary>

##### Authentication
None

##### Parameters
Request body:
* `username` (`str`): User's username
* `password` (`str`): User's password

##### Response
* `key` (`str`): User key which was created for this sign-in request

##### Preconditions
* `username` must be the username (case sensitive) of a valid user with a password assigned, and `password` must match this user's password hash when encoded
* There must not be 10 or more active keys for this user.

##### Sample request
```java
String body = new ObjectMapper()
    .writeValueAsString(
        Map.of(
            "username", "myUsername",
            "password", "aVerySecurePassword!"
        ));

HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../auth"))
  .POST(BodyPublishers.ofString(body))
  .header("Content-Type", "application/json")
  .build();
```

##### Sample response
```json
{
   "success": true,
   "data": {
       "key": "eyJ1c2VySWQi...."
   },
   "error": null
}
```

</details>

<details>
    <summary><code>DELETE</code> <code><b>/auth</b></code>: Invalidates the provided authentication key (signs a user out).</summary>

##### Authentication
None

##### Parameters
Request body:
* `all` (optional `bool`): If true, all authentication keys for this user will be invalidated.

##### Response
None

##### Preconditions
None

##### Sample request
```java
HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../auth"))
  .DELETE()
  .header("Authorization", "Bearer " + USER_KEY)
  .build();
```

##### Sample response
```json
{
   "success": true,
   "data": null,
   "error": null
}
```

</details>