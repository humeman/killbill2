### [Kill Bill 2](../../../README.md) → [Docs](../../README.md) → [Backend](../README.md) → [Game Server](README.md) → Basics
---

# Game Server
This document contains documentation on interacting with the Kill Bill 2 UDP Game Server.

## Table of Contents
* [**Global Commands**](global.md): Applies to any game type
* [**Basic Commands**](basic.md): Basic testing game type

## Client to Server Basics
When sending a request to the server, you will always include a JSON request body in the following format:

```json
{
    "messageId": "an-arbitrary-uuid",
    "data": {
        "type": "COMMAND_PING",
        ...extra data as command requires...
    }
}
```

And the server will acknowledge the command with a response:
```json
{
    "success": true,
    "ackMessageId": "an-arbitrary-uuid",
    "data": {
        "type": "RESP_PING",
        "response": "Pong!"
    }
}
```

Or, if the command requires no response, you'll get:
```json
{
    "success": true,
    "ackMessageId": "an-arbitrary-uuid",
    "data": {
        "type": "EMPTY"
    }
}
```

All methods require authentication with a game token. Your game token, which is completely separate from your regular user authentication token, is given you when sending a connect request over the HTTP API to [`POST /games/connect`](../api/routes/games.md), and persists for the duration of the game it's referring to. This token is stored in the `Authorization` header of your request in the following format:

`Authorization: Bearer your_key_goes_here`

## Server to Client Basics
The server will also send commands back to the client periodically, whether this be to record a player event or request updated details. They will be sent in the following format:
```json
{
    "messageId": "an-arbitrary-uuid",
    "data": {
        "type": "CLIENT_COMMAND_UPDATE_LOCATION"
    }
}
```

The client then responds with:
```json
{
    "ackMessageId": "the-same-message-uuid",
    "data": {
        "type": "CLIENT_RESPONSE_UPDATE_LOCATION",
        "x": 123.456,
        "y": 456.789
    }
}
```

## Request Bodies (client or server `COMMAND`)
Request bodies use this basic format:
* `messageId` (`str`): An arbitrary UUID which will be acked in the response.
* `data` (`object`):
    * `type` (`MessageDataType`): Type of the command (prefixed with `COMMAND_` or `CLIENT_COMMAND_`)
    * `*data`: Extra data depending on the data type

## Response Bodies (client or server `RESP`)
Response bodies use this basic format:
* `success` (optional `bool`): `true` if the command worked as expected. The server will always include this. 
* `ackMessageId` (`str`): The UUID acked in this response, if one was specified.
* `data` (`object`):
    * `type` (`MessageDataType`): Type of the command (prefixed with `RESP_` or `CLIENT_RESP_`)
    * `*data`: Extra data depending on the data type

When a request fails, you will get one of the following response types:
* `RESP_INVALID_ARGUMENT_EXCEPTION`: A part of your request query/body is invalid.
* `RESP_AUTHENTICATION_FAILURE`: Invalid authentication details or insufficient permissions.
* `RESP_INTERNAL_SERVER_ERROR`: Something unexpected went wrong with the server and the request cannot be fulfilled.
* `RESP_ILLEGAL_STATE_EXCEPTION`: Some action in this command would cause an illegal state.

And the body will include additional information as to why the request failed:
```json
{
    "success": false,
    "ackMessageId": "message-uuid-which-triggered-this-response",
    "data": {
        "type": "RESP_INTERNAL_SERVER_ERROR",
        "reason": "Something happened :("
    }
}
```

## Demo Ack Flow (worst-case)
The ack flow allows us to guarantee the delivery of each command and the receipt of any responses. Here's a sample for how it works when things go wrong.

1. The server sends a command to the client:
   ```json
   {
        "messageId": "ecb56130-5a60-493d-8550-30d4a56b2300",
        "data": {
            "type": "CLIENT_COMMAND_REFRESH_USERS",
            "users": {
                "uuid1": {
                    "coordinates": [17, 32],
                    "color": 12345678
                },
                "uuid2": {
                    "coordinates": [102, -35],
                    "color": 23456789
                }
            }
        }
   }
   ```

2. The client does not receive the message. The server has a configured ACK_DEADLINE of 500ms, so after that time is up with no ack, the client will get a request for resend:
   ```json
   {
        "messageId": "ecb56130-5a60-493d-8550-30d4a56b2300",
        "data": {
            "type": "CLIENT_COMMAND_REFRESH_USERS",
            "users": {
                "uuid1": {
                    "coordinates": [17, 32],
                    "color": 12345678
                },
                "uuid2": {
                    "coordinates": [102, -35],
                    "color": 23456789
                }
            }
        }
   }
   ```

3. The client, assuming it got the message the first time, will send out its reply without re-running the command. If the command wasn't received (message ID doesn't exist), it'll be executed.
    ```json
    {
        "data": {
            "type": "CLIENT_RESP_NO_SUCH_MESSAGE",
            "messageId": "b8dc6427-4161-4a5a-916a-6d756a21bb34"
        }
    }
    ```

The exact same can happen assuming the client and server swapped places.