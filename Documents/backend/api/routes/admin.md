### [Kill Bill 2](../../../../README.md) → [Docs](../../../README.md) → [Backend](../../README.md) → [API](../README.md) → Routes: /admin
---

# Routes: /admin
This contains endpoints for initializing the API with an admin account and retrieving an admin key using a secret admin init key.

---

<details>
    <summary><code>POST</code> <code><b>/admin/create_admin</b></code>: Creates the first admin user.</summary>

##### Authentication
Admin init key required.

##### Parameters
None

##### Response
* `user` ([`User`](../structures/user.md)): User that was created
* `key` (`str`): Regular authorization key for this admin user

##### Preconditions
* `admin` user must not exist yet

##### Notes
This will always create a user by the username `admin` (reserved) with no password or email linked. In order to create keys to this account in the future, the admin init key is required.

##### Sample request
```java
HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../admin/create_admin"))
  .POST()
  .header("Authorization", "Bearer " + ADMIN_INIT_KEY)
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
        },
       "key": "eyJ1c2VySWQi...."
   },
   "error": null
}
```

</details>

<details>
    <summary><code>GET</code> <code><b>/admin/get_admin_key</b></code>: Retrieves a user authentication key for the `admin` account.</summary>

##### Authentication
Admin init key required.

##### Parameters
None

##### Response
* `key` (`str`): User authorization key for this admin user

##### Preconditions
* `admin` user must exist

##### Sample request
```java
HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../admin/get_admin_key"))
  .GET()
  .header("Authorization", "Bearer " + ADMIN_INIT_KEY)
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