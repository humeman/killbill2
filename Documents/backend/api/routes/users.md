### [Kill Bill 2](../../../../README.md) → [Docs](../../../README.md) → [Backend](../../README.md) → [API](../README.md) → Routes: /users
---

# Routes: /users
This contains endpoints for managing users.

---

<details>
    <summary><code>GET</code> <code><b>/users</b></code>: Retrieves a user.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `id` (optional `str`): UUID of the user to retrieve.
* `name` (optional `str`): Case-sensitive name of the user to retrieve.

(*If neither are supplied, this retrieves the authenticated user.*)

##### Response
* `user` ([`User`](../structures/user.md)): The specified user.

##### Preconditions
* The specified user must exist.
* If the specified `id` or `name` does not match the authenticated user, the `ADMIN` role is required.

##### Sample request
```java
HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../users"))
  .GET()
  .header("Authorization", "Bearer " + USER_KEY)
  .build();
```

##### Sample response
```json
{
   "success": true,
   "data": {
      "user": {
          "id": "6585edec-ec62-4040-bfd3-100d23eb126f",
          "created": "2024-02-07T01:29:49.520Z",
          "name": "admin",
          "role": "ADMIN"
      }
   },
   "error": null
}
```

</details>

<details>
    <summary><code>GET</code> <code><b>/users/list</b></code>: Lists all registered users.</summary>

##### Authentication
User authentication key with `ADMIN` role required.

##### Parameters
None

##### Response
* `users` (`list` of [`User`](../structures/user.md)): The list of users registered.

##### Preconditions
None

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
    "data": {
        "users": [
            {
                "id": "6585edec-ec62-4040-bfd3-100d23eb126f",
                "created": "2024-02-07T01:29:49.520Z",
                "name": "admin",
                "role": "ADMIN"
            },
            {
                "id": "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
                "created": "2024-02-07T01:52:11.510Z",
                "name": "testUser1",
                "role": "USER"
            },
            {
                "id": "d80094f7-f827-49a5-a00b-8a7c0cd22a01",
                "created": "2024-02-07T23:07:03.311Z",
                "name": "testUser2",
                "role": "USER"
            }
        ]
    },
    "error": null
}
```

</details>

<details>
    <summary><code>POST</code> <code><b>/users</b></code>: Creates a user.</summary>

##### Authentication
User authentication key with `ADMIN` role optional.

##### Parameters
* `username` (`str`): The user's username. 3-20 characters, alphanumeric and `_`.
* `email` (`str`): A valid email address for password resets.
* `password` (`str`): The user's password. >8 characters and must meet complexity requirements.
* `role` (optional `str`): The role type to assign to the user. Defaults to `USER`.

##### Response
* `user` ([`User`](../structures/user.md)): The user that was created.
* `key` (str): An initial user authentication key to use for future requests.

##### Preconditions
* Username must not be taken.
* If creating a user with a privileged role, the authenticated user must have the `ADMIN` role.

##### Sample request
```java
String body = new ObjectMapper()
    .writeValueAsString(
        Map.of(
            "username", "myUsername",
            "email", "linus@tecktip.today",
            "password", "aVerySecurePassword!"
        ));

HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../users"))
  .POST(BodyPublishers.ofString(body))
  .header("Authorization", "Bearer " + USER_KEY)
  .header("Content-Type", "application/json")
  .build();
```

##### Sample response
```json
{
   "success": true,
   "data": {
       "user": {
            "id": "6585edec-ec62-4040-bfd3-100d23eb126f",
            "created": "2024-02-07T01:29:49.520Z",
            "name": "myUsername",
            "role": "USER"
        },
       "key": "eyJ1c2VySWQi...."
   },
   "error": null
}
```

</details>

<details>
    <summary><code>DELETE</code> <code><b>/users</b></code>: Deletes a user.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `id` (`str`): UUID of the user to be deleted.

##### Response
* `user` ([`User`](../structures/user.md)): The user that was deleted.

##### Preconditions
* If deleting a user other than the authenticated user, the `ADMIN` role is required.
* The specified user must exist.

##### Sample request
```java
String userId = "d80094f7-f827-49a5-a00b-8a7c0cd22a01";

HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../auth?id=" + userId))
  .DELETE()
  .header("Authorization", "Bearer " + USER_KEY)
  .build();
```

##### Sample response
```json
{
    "success": true,
    "data": {
        "user": {
            "id": "d80094f7-f827-49a5-a00b-8a7c0cd22a01",
            "created": "2024-02-07T23:07:03.311Z",
            "name": "testUser2",
            "role": "USER"
        }
    },
    "error": null
}
```

</details>