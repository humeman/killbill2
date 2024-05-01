### [Kill Bill 2](../../../../README.md) → [Docs](../../../README.md) → [Backend](../../README.md) → [API](../README.md) → Structures: Key
---

# Structures: Key
This is the structure of a Key object returned from the API. These are registered to users upon sign-in and used for API requests on behalf of that user.

* `id` (`str`): A 36-character UUID representing this key.
* `userId` (`str`): The UUID of the user this key is linked to.
* `created` (`ISO-6801 str`): The timestamp for when this key was created.
* `expires` (`ISO-6801 str`): The timestamp for when this key will expire.

## Sample
```json
{
    "id": "1fb2cb43-fe62-4c18-b44e-e57ef6fd02f3",
    "userId": "6585edec-ec62-4040-bfd3-100d23eb126f",
    "created": "2024-02-07T23:15:20.678Z",
    "expires": "2024-03-07T23:15:20.678Z"
}
```