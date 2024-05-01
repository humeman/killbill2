### [Kill Bill 2](../../../README.md) → [Docs](../../README.md) → [Backend](../README.md) → [API](README.md) → Basics
---

# HTTP API
This document contains documentation on interacting with the Kill Bill 2 HTTP API.

## Table of Contents
* **Routes**
    * [`/` Root](routes/root.md)
    * [`/auth` Authentication](routes/auth.md)
    * [`/admin` Admin controls](routes/admin.md)
    * [`/users` User management](routes/users.md)
    * [`/friends` Friend management](routes/friends.md)
    * [`/games` Game management](routes/games.md)
* [**Authentication Instructions**](authentication.md)

## Basics
When sending a request to the server, you will generally include either a query or response body, along with an authentication header.

For:
* GET or DELETE methods: Query (In the URL: `/my/endpoint?queryvar=queryvalue&queryvar2=queryvalue2`)
* POST or PUT methods: Body (JSON: `{"var": "value", "var2": "value2"}`)

When a method requires authentication (the only exclusions to this are `GET /`, `POST /auth/signin`, and `POST /users/`), you will have to send an authentication token.

This token is stored in the `Authorization` header of your request in the following format:

`Authorization: Bearer your_key_goes_here`

You can learn how to get this token in the [authentication docs](authentication.md).

## Response bodies
Responses are always returned in JSON format. If the request completed successfully, you will get a 200 OK response as follows:
```json
{
    "success": true,
    "data": {
        ...object data here...
    },
    "error": null
}
```

When a request fails, you will get one of the following response codes:
* `400 BAD_REQUEST`: A part of your request query/body is invalid.
* `401 UNAUTHORIZED`: Invalid authentication details or insufficient permissions.
* `500 INTERNAL_SERVER_ERROR`: Something unexpected went wrong with the server and the request cannot be fulfilled.
* `503 SERVICE_UNAVAILABLE`: A transient error has occurred, like a failure to connect to the database or other service. The request may be successful if retried.

And the body will include additional information as to why the request failed:
```json
{
    "success": false,
    "data": null,
    "error": "AuthenticationFailure: Invalid key."
}
```