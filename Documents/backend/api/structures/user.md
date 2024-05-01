### [Kill Bill 2](../../../../README.md) → [Docs](../../../README.md) → [Backend](../../README.md) → [API](../README.md) → Structures: User
---

# Structures: User
This is the structure of a User object returned from the API.

* `id` (`str`): A 36-character UUID representing this user.
* `created` (`ISO-6801 str`): The timestamp for when this user was registered.
* `name` (`str`): This user's username (3-20 characters, alphanumeric + _).
* `role` (`str`): This user's role enum. One of `USER`, `ADMIN`.

## Sample
```json
{
    "id": "6585edec-ec62-4040-bfd3-100d23eb126f",
    "created": "2024-02-07T01:29:49.520Z",
    "name": "admin",
    "role": "ADMIN"
}
```