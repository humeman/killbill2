### [Kill Bill 2](../../../../README.md) → [Docs](../../../README.md) → [Backend](../../README.md) → [API](../README.md) → Structures: Game
---

# Structures: Game
This is the structure of a Game object returned from the API.

* `game` (`object`):
    * `name` (`str`): A user-defined name describing the lobby.
    * `id` (`str`): A 36-character UUID representing this game.
    * `created` (`ISO-6801 str`): The timestamp for when this game was created.
    * `hostId` (`str`): The UUID of the player who is hosting the game.
* `users` (`list` of `str`): The user UUIDs of all players invited to the game, including the host.

## Sample
```json
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
````