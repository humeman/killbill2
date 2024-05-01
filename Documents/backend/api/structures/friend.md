### [Kill Bill 2](../../../../README.md) → [Docs](../../../README.md) → [Backend](../../README.md) → [API](../README.md) → Structures: Friend
---

# Structures: Friend
This is the structure of a Friend object returned from the API.

* `fromId` (`str`): The UUID of the player who initiated the friend link.
* `toId` (`str`): The UUID of the player who received the friend link.
* `created` (`ISO-6801 str`): The timestamp for when this friend link was created.
* `state` (`str`): This friend link's state enum. One of `INVITED`, `FRIENDS`, `BLOCKED`.

## Sample
```json
{
    "fromId": "6585edec-ec62-4040-bfd3-100d23eb126f",
    "toId": "9b540942-2c2e-4acf-bc06-7d4a3d801e0f",
    "created": "2024-02-07T23:15:20.678Z",
    "state": "FRIENDS"
}
````