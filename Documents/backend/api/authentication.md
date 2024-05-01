### [Kill Bill 2](../../../README.md) → [Docs](../../README.md) → [Backend](../README.md) → [API](README.md) → Authentication
---

# Authentication Flow
Kill Bill 2 uses a secure token-based authentication flow to ensure that only authorized access occurs.

## Client Sign-Up
When a user signs up for the service, they specify a username and password. The password they specified is salted and hashed, that is to say:
* A random string is appended to the password so brute-force attacks are more difficult (assuming database compromise)
* It is encoded (hashed many times) using Argon 2 so it's not stored in plaintext

## Client Authentication
When the user then wants to sign into their account, they send that username and password to the API at [/auth](routes/auth.md) to get an access key:
* Client: `POST /auth {"username": "my_user", "password": "myPa$$w0rd!"}`
* Server: `{"data": {"key": "eWzb...=="}, ...}`

The access key is then used in future authenticated requests as the `Authorization` header.
* Client: `POST /some/method` with header `Authorization: Bearer a9b3...==`

The server will expire these keys automatically if unusual access occurs (ie: many IP changes, way too many requests) or if it's been a long time (ie: 6 months). Then, the user will be forced to sign in again.
* Client: `POST /some/method` with header `Authorization: Bearer a9b3...==`
* Server: 403 `{"success": false, "error": "Invalid access key"}`

> **Why tokens?**
>
> It's a bad idea to store the user's password directly on the device, since then it becomes very difficult to revoke an attacker's access if they compromise the token. Using access tokens, we can temporarily revoke access to an account until they sign in again, which an attacker shouldn't be able to do since the password is not stored anywhere. Besides that, many users will use the same exact password they use on other services, which we don't want to be responsible for leaking.

## More information
Authentication flow endpoints are documented in more detail in [Routes → Auth](routes/auth.md).