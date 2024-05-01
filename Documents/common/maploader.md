### [Kill Bill 2](../../README.md) → [Docs](../README.md) → [Common](README.md) → Map Loader
---

# Map Loader

Kill Bill 2 has a fully custom map loading system, in which maps can be created and sent over the game server to other players using the `.kbmap` file format.

This document details the creation of custom maps using the map loader.

## How do I make a new map?

1. Visit the `Frontend` project.
2. In `assets/maps`, create a new folder for your map.
3. Within this folder, create a kbconfig file for your map. This contains spawnpoints, healths, textures, etc. The file must have the extension `.kbconfig.json`, but the name is up to you.
        ```
            {
                "type": "BASIC",
                "basicConfig": {
                    "playerConfig": {
                        "PLAYER": [
                            {
                                "id": 0,
                                "spawnpoint": [-4, 1],
                                "texturePrefix": "",
                                "maxHealth": 10
                            },
                            {
                                "id": 1,
                                "spawnpoint": [-4, 2],
                                "texturePrefix": "",
                                "maxHealth": 10
                            },
                            ...
                        ],
                        "BILL": [
                            {
                                "id": 0,
                                "spawnpoint": [16, 16],
                                "texturePrefix": "",
                                "maxHealth": 20
                            }
                        ],
                        "SPECTATOR": [
                            {
                                "id": 0,
                                "spawnpoint": [0, 0],
                                "texturePrefix": "",
                                "maxHealth": -1
                            }
                        ]
                    }
                }
            }
        ```
4. The actual map, detailing map rooms/objects/entities, will go in a file ending with `.kbmap`. The name is up to you.
5. The first line of this file must have a display name to render to users. This should be between 3 and 32 characters.
        ```
        My Map Name

        ...map directives here...
        ``` 

## How do I format the file?
The `.kbmap` file uses various "directives" to specify to the backend and frontend what the map should look like. For example, we can define a room directive, stating that we want a room at a particular location:
```
room {
    location        0,0
    size            20,20
    wall            walls_white
    floor           floors_tile
}
```

This map would be created with the bottom left corner at 0,0 with a size of 20x20 and the specified wall/floor textures.

It is also valid to write this as a single line:
```
room location=0,0 size=20,20 wall=walls_white floor=floors_tile
```

## How do I test my map?
The frontend has an easy testing feature implemented. Open the game, then on the main menu, press `F5` to access the development screen. Then, select your map, and press `Go`.

Since you'll be making many changes in this process, you can also reload all textures and maps by pressing `F5` again while loaded into the development map.

---

## Cool features
### Repeatable attributes
Most directives (everything but `room`) can have multiple 'location' attributes specified to spawn duplicates. For example, this would spawn two `SOLID` `test_block` tiles at 5,1 and 5,2:
```
tile {
    texture         test_block
    flag            SOLID
    location        5,1
    location        5,2
}
```

This also works for other special attributes, like `flag` for objects or `loot_table` for chests.

### Comments
Comments are specified with `#`.
```
# A valid comment
tile {
    texture     test_block   # Also a valid comment
}
```

### Gridscale
Some directives (currently `chest`, `object`, and `entity`) support "gridscale" values. This allows you to easily offset locations and sizes.
```
object {
    size        1/2,1   # width = 0.5, height = 1
    location    3+3/4,1-1/2   # x = 3.75, y = 0.5
    location    2,3+1/3       # x = 2, y = 3.333...
}
```

## Directives
<details>
    <summary><code>room</code>: Makes a square box with walls and a floor.</summary>

##### Arguments
* **location** (int `x,y`): Coordinates to the bottom left corner of the room
* **size** (int `x,y`): Width and height of the room
* **wall** (texture): Name of the texture set for walls
* **floor** (texture): Name of the texture set for floors
* ***exclude_wall*** (optional repeatable int `x,y`): Does not add a wall at `x,y`
* ***wall_override*** (optional repeatable texture,int `texture,x,y`): Replaces the wall texture at `x,y` with `texture`

##### Sample
```
room {
    location        -6,0                # Bottom left tile is at -6, 0
    size            7,9                 # Width = 7, height = 9
    wall            walls_white         # Uses the "walls_white" set
    floor           floors_tile         # Uses the "floors_tile" set
    #exclude_wall   0,1                 # Does not add a wall at 0, 1
    #wall_override  walls_white_b,0,0   # Replaces the texture for the wall at 0,0 with walls_white_b
}
```
</details>

<details>
    <summary><code>entity</code>: Spawns in an entity at game start.</summary>

##### Arguments
* **type** (EntityType): Type of entity to spawn. One of `EMPLOYEE`, `CLAYMORE_ROOMBA`.
* **location** (repeatable float/gridscale `x,y`): Coordinates to spawn this entity at
* ***size*** (optional float/gridscale `x,y`): Width and height of the entity
* ***rotation*** (optional int): Default rotation of the entity
* ***held_item*** (optional texture): Texture of the item the entity should be holding

##### Sample
```
entity {
  type          CLAYMORE_ROOMBA   # Spawns in a ClaymoreRoomba
  location      12,3              # Spawnpoint of 12,3
  #size         2,2               # Makes it really big
  #rotation     180               # Upside down
  #held_item    dynamite          # Kaboom
}
```
</details>

<details>
    <summary><code>tile</code>: An item fixed to the grid.</summary>

##### Arguments
* **texture** (texture): Name of the texture to use
* **location** (repeatable int `x,y`): Coordinates to spawn this tile at
* ***flag*** (optional repeatable ObjectFlag): Object flags to add
* ***size*** (optional int `x,y`): Width and height of the tile, defaults to 1,1
* ***rotation*** (optional int): Rotation of the tile's texture

##### Sample
```
tile {
  texture      objects_desk_1_2     # Texture
  #flag        SOLID                # Collidable
  #flag        INTERACTABLE         # If you wanted another one
  location     7,7                  # Bottom left at 7,7
  #size        1,2                  # Makes it longer
  #location    8,8                  # More
  #location    9,9                  # MORE
  #rotation    90                   # Sideways
}
```
</details>

<details>
    <summary><code>object</code>: An item that can be placed anywhere or sized however.</summary>

##### Arguments
* **texture** (texture): Name of the texture to use
* **location** (repeatable float/gridscale `x,y`): Coordinates to spawn this object at
* ***flag*** (optional repeatable ObjectFlag): Object flags to add
* ***size*** (optional float/gridscale `x,y`): Width and height of the object, defaults to 1,1
* ***rotation*** (optional int): Rotation of the object's texture

##### Sample
```
object {
  texture      objects_mug          # Texture
  #flag        SOLID                # Collidable
  location     7+1/2,7-3/4          # Bottom left at 7.5,6.25
  #size        1/2,1/2              # Small
  #rotation    270                  # Weeeeeeee
}
```
</details>

<details>
    <summary><code>chest</code>: An item with a loot table.</summary>

##### Arguments
* **texture** (texture): Name of the texture to use
* **openTexture** (texture): Name of the texture to use when opened
* **location** (repeatable float/gridscale `x,y`): Coordinates to spawn this chest at
* **loot_table** (repeatable int,ItemType `chance,item`): Chance of roll (out of sum) and item type
* ***flag*** (optional repeatable ObjectFlag): Object flags to add (`INTERACTABLE` is added by default)
* ***size*** (optional float/gridscale `x,y`): Width and height of the object, defaults to 1,1
* ***rotation*** (optional int): Rotation of the object's texture

##### Sample
```
chest {
  texture           objects_briefcase        # Briefcase
  open_texture      objects_briefcase_open   # Open briefcase
  location          1,1                      # You can figure this one out
  # Numbers are percentages, does not have to add to 100 (ie: 2, 1, 1 is identical)
  loot_table        50,SWORD                 # SWORD has a 50/100 chance
  loot_table        25,SPEAR                 # SPEAR has a 25/100 chance
  loot_table        25,HEALTH_POTION         # HEALTH_POTION has a 25/100 chance
  #flag             SOLID                    # No walk through the chest
  #rotation         172                      # ?????
  #size             20,20                    # Really can't miss it
}
```
</details>