### [Kill Bill 2](../../../../README.md) → [Docs](../../../README.md) → [Backend](../../README.md) → [API](../README.md) → Routes: /games
---

# Routes: /games
This contains endpoints for managing games.

---

<details>
    <summary><code>GET</code> <code><b>/games</b></code>: Retrieves a game.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `id` (`str`): UUID of the game.

##### Response
* `game` ([`Game`](../structures/game.md)): The specified game.

##### Preconditions
* The specified game must be active.
* If retrieving a game that the user is not invited to or the host of, the `ADMIN` role is required.

##### Sample request
```java
String gameId = "1fb2cb43-fe62-4c18-b44e-e57ef6fd02f3";

HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../games?id=" + gameId))
  .GET()
  .header("Authorization", "Bearer " + USER_KEY)
  .build();
```

##### Sample response
```json
{
    "success": true,
    "data": {
        "game": {
            "name": "My Cool Game",
            "id": "1fb2cb43-fe62-4c18-b44e-e57ef6fd02f3",
            "created": "2024-02-07T23:15:20.678Z",
            "hostId": "6585edec-ec62-4040-bfd3-100d23eb126f"
        },
        "users": [
            "6585edec-ec62-4040-bfd3-100d23eb126f",
            "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
            "d80094f7-f827-49a5-a00b-8a7c0cd22a01"
        ]
    },
    "error": null
}
```

</details>


<details>
    <summary><code>GET</code> <code><b>/games/list</b></code>: Lists multiple games.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `userId` (`str`): If specified, filters results to only include games this user is invited to or hosting.

##### Response
* `games` (`list` of [`Game`](../structures/game.md)): Any games matched.

##### Preconditions
* To list all games, the `ADMIN` role is required.
* To list the games of a user other than the authenticated user, the `ADMIN` role is required.

##### Sample request
```java
HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../games/list"))
  .GET()
  .header("Authorization", "Bearer " + USER_KEY)
  .build();
```

##### Sample response
```json
{
    "success": true,
    "data": {
        "games": [
            {
                "game": {
                    "name": "My Cool Game",
                    "id": "1fb2cb43-fe62-4c18-b44e-e57ef6fd02f3",
                    "created": "2024-02-07T23:15:20.678Z",
                    "hostId": "6585edec-ec62-4040-bfd3-100d23eb126f"
                },
                "users": [
                    "6585edec-ec62-4040-bfd3-100d23eb126f",
                    "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
                    "d80094f7-f827-49a5-a00b-8a7c0cd22a01"
                ]
            }
        ]
    },
    "error": null
}
```

</details>


<details>
    <summary><code>POST</code> <code><b>/games</b></code>: Creates a new game.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `name` (`str`): A short description of the game.
* `users` (`list` of `str`): The UUIDs of any users to invite to the game.

##### Response
* `game` ([`Game`](../structures/game.md)): The game that was created.

##### Preconditions
* Each user can only host one game at a time.
* The `users` list cannot include the host.
* There may not be more than 4 invitees.

##### Sample request
```java
String body = new ObjectMapper()
    .writeValueAsString(
        Map.of(
            "name", "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
            "users", new String[] {
                "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
                "d80094f7-f827-49a5-a00b-8a7c0cd22a01"
            }
        ));

HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../games"))
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
        "game": {
            "name": "My Cool Game",
            "id": "1fb2cb43-fe62-4c18-b44e-e57ef6fd02f3",
            "created": "2024-02-07T23:15:20.678Z",
            "hostId": "6585edec-ec62-4040-bfd3-100d23eb126f"
        },
        "users": [
            "6585edec-ec62-4040-bfd3-100d23eb126f",
            "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
            "d80094f7-f827-49a5-a00b-8a7c0cd22a01"
        ]
    },
    "error": null
}
```

</details>

<details>
    <summary><code>DELETE</code> <code><b>/games</b></code>: Deletes a game.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `id` (`str`): UUID of the game to delete.

##### Response
* `game` ([`Game`](../structures/game.md)): The game that was deleted.

##### Preconditions
* Only the host can delete a game, unless the authenticated user has the `ADMIN` role.
* The game must be active.

##### Sample request
```java
String id = "1fb2cb43-fe62-4c18-b44e-e57ef6fd02f3";

HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../games?id=" + id))
  .DELETE()
  .header("Authorization", "Bearer " + USER_KEY)
  .build();
```

##### Sample response
```json
{
    "success": true,
    "data": {
        "game": {
            "name": "My Cool Game",
            "id": "1fb2cb43-fe62-4c18-b44e-e57ef6fd02f3",
            "created": "2024-02-07T23:15:20.678Z",
            "hostId": "6585edec-ec62-4040-bfd3-100d23eb126f"
        },
        "users": [
            "6585edec-ec62-4040-bfd3-100d23eb126f",
            "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
            "d80094f7-f827-49a5-a00b-8a7c0cd22a01"
        ]
    },
    "error": null
}
```

</details>


<details>
    <summary><code>POST</code> <code><b>/games/users</b></code>: Invites a user to an existing game.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `gameId` (`str`): UUID of the game to invite the user to.
* `userId` (`str`): UUID of the user being invited.

##### Response
* `game` ([`Game`](../structures/game.md)): The game that was modified.

##### Preconditions
* Only the host can invite players, unless the authenticated user has the `ADMIN` role.
* There may not be more than 5 players in a game.
* The game must be active.
* The user must exist.
* The user cannot already be in the game.

##### Sample request
```java
String body = new ObjectMapper()
    .writeValueAsString(
        Map.of(
            "gameId", "1fb2cb43-fe62-4c18-b44e-e57ef6fd02f3",
            "userId", "895abcd7-e058-492f-9af0-4a3d813aa38c"
        ));

HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../games/users"))
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
        "game": {
            "name": "My Cool Game",
            "id": "1fb2cb43-fe62-4c18-b44e-e57ef6fd02f3",
            "created": "2024-02-07T23:15:20.678Z",
            "hostId": "6585edec-ec62-4040-bfd3-100d23eb126f"
        },
        "users": [
            "6585edec-ec62-4040-bfd3-100d23eb126f",
            "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
            "d80094f7-f827-49a5-a00b-8a7c0cd22a01",
            "895abcd7-e058-492f-9af0-4a3d813aa38c"
        ]
    },
    "error": null
}
```

</details>


<details>
    <summary><code>DELETE</code> <code><b>/games/users</b></code>: Removes a user from an existing game.</summary>

##### Authentication
User authentication key required.

##### Parameters
* `gameId` (`str`): UUID of the game to invite the user to.
* `userId` (`str`): UUID of the user being invited.

##### Response
* `game` ([`Game`](../structures/game.md)): The game that was modified.

##### Preconditions
* Only the host can remove players, unless the authenticated user has the `ADMIN` role.
* The host cannot be uninvited. The game must instead be deleted.
* The game must be active.
* The user must be in the game.

##### Sample request
```java
String gameId = "1fb2cb43-fe62-4c18-b44e-e57ef6fd02f3";
String userId = "895abcd7-e058-492f-9af0-4a3d813aa38c";

HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(".../games/users?gameId=" + gameId + "&userId=" + userId))
  .DELETE()
  .header("Authorization", "Bearer " + USER_KEY)
  .build();
```

##### Sample response
```json
{
    "success": true,
    "data": {
        "game": {
            "name": "My Cool Game",
            "id": "1fb2cb43-fe62-4c18-b44e-e57ef6fd02f3",
            "created": "2024-02-07T23:15:20.678Z",
            "hostId": "6585edec-ec62-4040-bfd3-100d23eb126f"
        },
        "users": [
            "6585edec-ec62-4040-bfd3-100d23eb126f",
            "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
            "d80094f7-f827-49a5-a00b-8a7c0cd22a01"
        ]
    },
    "error": null
}
```

</details>