### [Kill Bill 2](../../../../README.md) → [Docs](../../../README.md) → [Backend](../../README.md) → [Game Server](../README.md) → Basic Game Commands
---

# Basic Game Commands
This contains client and server commands available in the `BASIC` game type.

---
### Commands (Client → Server)

<details>
    <summary><code>COMMAND_CHANGE_LOCATION</code>: Tells the server that the client updated their location.</summary>

##### Type
Command (Client → Server)

##### Request data
* `type` (`str`) = `COMMAND_CHANGE_LOCATION`
* `coordinates` (`double[2]`): Updated X and Y coordinates

##### Response data
* `type` (`str`) = `EMPTY`

##### Sample request
```json
{
    "messageId": "6585edec-ec62-4040-bfd3-100d23eb126f",
    "data": {
        "type": "COMMAND_CHANGE_LOCATION",
        "coordinates": [100.1, 200.2]
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
<details>
    <summary><code>COMMAND_RECV_PLAYER_LOCATIONS</code>: Requests that the client use updated user locations.</summary>

##### Type
Client Command (Server → Client)

##### Request data
* `type` (`str`) = `COMMAND_RECV_PLAYER_LOCATIONS`
* `users` (`list`):
    * `coordinates` (`int[2]`): New locations
    * `userId` (`str`): User IDs

##### Response data
* `type` (`str`) = `EMPTY`

##### Sample request
```json
{
    "messageId": "6585edec-ec62-4040-bfd3-100d23eb126f",
    "data": {
        "type": "COMMAND_RECV_PLAYER_LOCATIONS",
        "users": [
            {
                "coordinates": [100.1, 200.2],
                "userId": "cd6ad233-e195-44a2-b2b8-413b20154c0f"
            },
            {
                "coordinates": [200.1, 300.2],
                "userId": "4a024994-f129-4449-af0e-cd463bdd0a88"
            }
        ]
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