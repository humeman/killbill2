### [Kill Bill 2](../../../../README.md) → [Docs](../../../README.md) → [Backend](../../README.md) → [API](../README.md) → Routes: /friends
---

# Routes: /friends
This contains endpoints for managing friends.

---

<details>
    <summary><code>GET</code> <code><b>/friends</b></code>: Retrieves a single friend link.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `userId1` (`str`): UUID of the first user in the friend link.
* `userId2` (`str`): UUID of the second user in the friend link.

##### Response
* `friend` ([`Friend`](../structures/friend.md)): The specified friend link.

##### Preconditions
* The specified friend link must exist.
* To retrieve a friend link not involving the authenticated user, the `ADMIN` role is required.

##### Sample request
```java
String userId1 = "6585edec-ec62-4040-bfd3-100d23eb126f";
String userId2 = "9b540942-2c2e-4acf-bc06-7d4a3d801e0f";

HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../friends?userId1=" + userId1 + "&userId2=" + userId2))
  .GET()
  .header("Authorization", "Bearer " + USER_KEY)
  .build();
```

##### Sample response
```json
{
    "success": true,
    "data": {
        "friend": {
            "fromId": "6585edec-ec62-4040-bfd3-100d23eb126f",
            "toId": "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
            "created": "2024-02-11T04:07:59.890373476Z",
            "state": "INVITED"
        }
    },
    "error": null
}
```

</details>

<details>
    <summary><code>GET</code> <code><b>/friends/list</b></code>: Lists multiple friend links.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `userId` (optional `str`): If provided, filters to only friend links involving the specified user.

##### Response
* `friends` (`list` of [`Friend`](../structures/friend.md)): Any friend links matched.

##### Preconditions
* To list all friend links, the `ADMIN` role is required.
* To list the friend links of a user other than the authenticated user, the `ADMIN` role is required.

##### Sample request
```java
HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../friends"))
  .GET()
  .header("Authorization", "Bearer " + USER_KEY)
  .build();
```

##### Sample response
```json
{
    "success": true,
    "data": {
        "friends": [
            {
                "fromId": "6585edec-ec62-4040-bfd3-100d23eb126f",
                "toId": "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
                "created": "2024-02-11T04:07:59.890373476Z",
                "state": "INVITED"
            }
        ]
    },
    "error": null
}
```

</details>

<details>
    <summary><code>POST</code> <code><b>/friends</b></code>: Invites a user to be your friend.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `userId` (`str`): UUID of the user to be invited.

##### Response
* `friend` ([`Friend`](../structures/friend.md)): The friend link that was generated.

##### Preconditions
* The specified user must exist.
* There must not already be a friend link between the two users.

##### Sample request
```java
String body = new ObjectMapper()
    .writeValueAsString(
        Map.of(
            "userId", "9b540942-2c2e-4acf-bc06-7d4a3d801e0f"
        ));

HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../friends"))
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
        "friend": {
            "fromId": "6585edec-ec62-4040-bfd3-100d23eb126f",
            "toId": "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
            "created": "2024-02-11T04:07:59.890373476Z",
            "state": "INVITED"
        }
    },
    "error": null
}
```

</details>


<details>
    <summary><code>PUT</code> <code><b>/friends</b></code>: Accepts a friend request.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `userId` (`str`): UUID of the user who invited you.

##### Response
* `friend` ([`Friend`](../structures/friend.md)): The friend link that was modified.

##### Preconditions
* The specified user must exist.
* There must be an outstanding invitation where the `to` user is the authenticated user.

##### Sample request
```java
String body = new ObjectMapper()
    .writeValueAsString(
        Map.of(
            "userId", "6585edec-ec62-4040-bfd3-100d23eb126f"
        ));

HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../friends"))
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
        "friend": {
            "fromId": "6585edec-ec62-4040-bfd3-100d23eb126f",
            "toId": "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
            "created": "2024-02-11T04:07:59.890373476Z",
            "state": "FRIENDS"
        }
    },
    "error": null
}
```

</details>


<details>
    <summary><code>DELETE</code> <code><b>/friends</b></code>: Removes a friend or declines/withdraws an invitation.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `userId` (`str`): ID of the user to unfriend, decline the invitation for, or withdraw the invitation to.

##### Response
* `friend` ([`Friend`](../structures/friend.md)): The friend link that was removed.

##### Preconditions
* There must be an existing friend link between the authenticated user and `userId`.

##### Sample request
```java
String userId = "6585edec-ec62-4040-bfd3-100d23eb126f";

HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../friends?userId" = userId))
  .DELETE()
  .header("Authorization", "Bearer " + USER_KEY)
  .build();
```

##### Sample response
```json
{
    "success": true,
    "data": {
        "friend": {
            "fromId": "6585edec-ec62-4040-bfd3-100d23eb126f",
            "toId": "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
            "created": "2024-02-11T04:07:59.890373476Z",
            "state": "FRIENDS"
        }
    },
    "error": null
}
```

</details>