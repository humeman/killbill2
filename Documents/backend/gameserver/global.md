### [Kill Bill 2](../../../../README.md) → [Docs](../../../README.md) → [Backend](../../README.md) → [Game Server](../README.md) → Global Commands
---

# Global Commands
This contains client and server commands available in all game types.

---
### Commands (Client → Server)

<details>
    <summary><code>COMMAND_PING</code>: Pings the server.</summary>

##### Type
Command (Client → Server)

##### Request data
* `type` (`str`) = `COMMAND_PING`

##### Response data
* `type` (`str`) = `RESP_PING`
* `message` (`str`) = An arbitrary response string from the server

##### Sample equest
```json
{
    "messageId": "6585edec-ec62-4040-bfd3-100d23eb126f",
    "data": {
        "type": "COMMAND_PING"
    }
}
```

##### Sample response
```json
{
   "success": true,
   "ackMessageId": "6585edec-ec62-4040-bfd3-100d23eb126f",
   "data": {
        "type": "RESP_PING",
        "message": "Pong!"
   }
}
```

</details>

<details>
    <summary><code>COMMAND_CONNECT</code>: Connects to the server.</summary>

##### Type
Command (Client → Server)

##### Notes
Connection is required before any other game-related commands are accepted.

##### Request data
* `type` (`str`) = `COMMAND_CONNECT`

##### Response data
* `type` (`str`) = `EMPTY`

##### Sample request
```json
{
    "messageId": "6585edec-ec62-4040-bfd3-100d23eb126f",
    "data": {
        "type": "COMMAND_CONNECT"
    }
}
```

##### Sample response
```json
{
   "success": true,
   "ackMessageId": "6585edec-ec62-4040-bfd3-100d23eb126f",
   "data": {
        "type": "RESP_CONNECT"
   }
}
```
</details>

<details>
    <summary><code>COMMAND_DISCONNECT</code>: Disconnects from the server.</summary>

##### Type
Command (Client → Server)

##### Request data
* `type` (`str`) = `COMMAND_DISCONNECT`

##### Response data
* `type` (`str`) = `EMPTY`

##### Sample request
```json
{
    "messageId": "6585edec-ec62-4040-bfd3-100d23eb126f",
    "data": {
        "type": "COMMAND_DISCONNECT"
    }
}
```

##### Sample response
```json
{
   "success": true,
   "ackMessageId": "6585edec-ec62-4040-bfd3-100d23eb126f",
   "data": {
        "type": "EMPTY"
   }
}
```
</details>

<details>
    <summary><code>COMMAND_HEARTBEAT</code>: Sends a heartbeat ping.</summary>

##### Type
Command (Client → Server)

##### Notes
After 10s without any heartbeats, a client is disconnected.

##### Request data
* `type` (`str`) = `COMMAND_HEARTBEAT`

##### Response data
* `type` (`str`) = `EMPTY`

##### Sample request
```json
{
    "messageId": "6585edec-ec62-4040-bfd3-100d23eb126f",
    "data": {
        "type": "COMMAND_HEARTBEAT"
    }
}
```

##### Sample response
```json
{
   "success": true,
   "ackMessageId": "6585edec-ec62-4040-bfd3-100d23eb126f",
   "data": {
        "type": "EMPTY"
   }
}
```
</details>


---
### Client Commands (Server → Client)