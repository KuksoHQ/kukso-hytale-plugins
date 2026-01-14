# Kukso Warps

A simple and efficient warp system for Hytale servers. This plugin allows players to set, delete, and teleport to specific locations with configurable warmup and cooldown times.

## Features

- **Set & Delete Warps:** Easily create warps at your current location and remove them when no longer needed.
- **List Warps:** View a list of all available warps.
- **Warmup System:** Configurable delay before teleportation occurs to prevent combat logging or instant escape.
- **Cooldown System:** Configurable cooldown period between warp uses to balance gameplay.
- **Permissions:** Granular permissions for bypassing warmup and cooldown restrictions.
- **Configuration:** Simple configuration file to adjust settings.

## Commands

| Command | Description | Usage |
| bound | bound | bound |
| `/warp` | Teleport to a specified warp. | `/warp <name>` |
| `/setwarp` | Create a new warp at your current location. | `/setwarp <name>` |
| `/delwarp` | Delete an existing warp. | `/delwarp <name>` |
| `/warps` | List all available warps. | `/warps` |

## Configuration

The configuration is located in the plugin's config folder.

```json
{
  "Warps": {},
  "Warmup": 3,
  "Cooldown": 5
}
```

*   **Warmup:** Time in seconds to wait before teleporting. Set to `0` to disable.
*   **Cooldown:** Time in seconds a player must wait before using the warp command again. Set to `0` to disable.

## Permissions

| Permission | Description |
| bound | bound |
| `kukso.warps.bypass.warmup` | Allows a player to bypass the teleportation warmup time. |
| `kukso.warps.bypass.cooldown` | Allows a player to bypass the warp command cooldown. |

## Installation

1.  Build the project using Gradle: `./gradlew build`
2.  Place the generated `.jar` file from `build/libs` into your server's `mods` or `plugins` directory.
3.  Restart the server.
