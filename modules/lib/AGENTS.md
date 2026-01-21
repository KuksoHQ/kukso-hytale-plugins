# AGENTS.md - KuksoHyLib Integration Guide

This guide is for developers and AI agents integrating **KuksoHyLib** into Hytale plugins.

## Quick Start

Add KuksoLib as a dependency in your `manifest.json`:
```json
{
  "Dependencies": ["com.kukso.hy.lib:KuksoHyLib"]
}
```

---

## ColorMan - Chat Colorization

**Package:** `com.kukso.hy.lib.util.ColorMan`

Translates Minecraft-style color codes to Hytale's `Message` format.

### Supported Codes

| Code | Color | Code | Color |
|------|-------|------|-------|
| `&0` | Black | `&8` | Dark Gray |
| `&1` | Dark Blue | `&9` | Blue |
| `&2` | Dark Green | `&a` | Green |
| `&3` | Dark Aqua | `&b` | Aqua |
| `&4` | Dark Red | `&c` | Red |
| `&5` | Dark Purple | `&d` | Light Purple |
| `&6` | Gold | `&e` | Yellow |
| `&7` | Gray | `&f` | White |

**Format codes:** `&l` (bold), `&o` (italic), `&r` (reset)
**Hex colors:** `&#RRGGBB` (e.g., `&#FF5733`)

### Usage

```java
import com.kukso.hy.lib.util.ColorMan;
import com.hypixel.hytale.server.core.Message;

// Simple color
Message msg = ColorMan.translate("&aHello, World!");

// Multiple colors
Message msg = ColorMan.translate("&4Red &e&lYellow Bold &r&7Reset to gray");

// Hex colors
Message msg = ColorMan.translate("&#FF5733This is orange!");

// Send to player
player.sendMessage(ColorMan.translate("&aWelcome to the server!"));
```

### Notes
- Only `bold` and `italic` formatting are supported (Hytale API limitation)
- `&n`, `&m`, `&k` codes are silently ignored
- Color codes reset formatting; use `&l` after a color code to maintain bold

---

## LocaleMan - Localization System

**Package:** `com.kukso.hy.lib.locale.LocaleMan`

Multi-language support based on player's client language.

### Setup

1. Create locale files in `mods/KuksoHyLib/locales/`:

**en_US.json:**
```json
{
  "prefix": "&e[MyPlugin]&r",
  "messages": {
    "welcome": "&aWelcome, &e{player}&a!",
    "goodbye": "&7Goodbye, &e{player}&7!"
  },
  "errors": {
    "no_permission": "&cYou don't have permission."
  }
}
```

**tr_TR.json:**
```json
{
  "prefix": "&e[Eklentim]&r",
  "messages": {
    "welcome": "&aHosgeldin, &e{player}&a!",
    "goodbye": "&7Gorusuruz, &e{player}&7!"
  },
  "errors": {
    "no_permission": "&cYetkiniz yok."
  }
}
```

### Usage

```java
import com.kukso.hy.lib.locale.LocaleMan;
import com.hypixel.hytale.server.core.Message;
import java.util.Map;

// Basic usage - automatically uses player's language
Message msg = LocaleMan.get(playerRef, "messages.welcome");

// With placeholders
Message msg = LocaleMan.get(playerRef, "messages.welcome",
    Map.of("player", playerRef.getUsername()));

// Raw string for specific locale
String text = LocaleMan.getRaw("en_US", "messages.welcome");

// Check player's detected locale
String locale = LocaleMan.getPlayerLocale(playerRef);

// Check if a locale is loaded
boolean hasGerman = LocaleMan.hasLocale("de_DE");

// Get all loaded locales
Set<String> locales = LocaleMan.getLoadedLocales();
```

### Fallback Chain
1. Player's locale (e.g., `tr_TR`)
2. Default locale (`en_US`)
3. Key itself (with warning logged)

### Key Format
Use dot-notation for nested keys: `"messages.welcome"`, `"errors.no_permission"`

---

## ComponentMan - Entity Component System Utilities

**Package:** `com.kukso.hy.lib.util.ComponentMan`

Helper utilities for Hytale's ECS, using reflection to handle API variations.

### Usage

```java
import com.kukso.hy.lib.util.ComponentMan;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

// Get a component from an entity
MyComponent comp = ComponentMan.getComponent(entityStore, playerRef, MyComponent.TYPE);

// Add or update a component
ComponentMan.addComponent(entityStore, playerRef, MyComponent.TYPE, myComponent);

// Check if entity has a component
boolean has = ComponentMan.hasComponent(entityStore, playerRef, MyComponent.TYPE);

// Get command arguments (reflection-based)
List<String> args = ComponentMan.getArgs(commandContext);
```

### Creating Custom Components

```java
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.lang.reflect.Constructor;

public class MyComponent implements Component<EntityStore> {

    // Register ComponentType via reflection (constructor is protected)
    public static ComponentType<EntityStore, MyComponent> TYPE;

    static {
        try {
            Constructor<ComponentType> ctor = ComponentType.class.getDeclaredConstructor(
                Class.class, String.class
            );
            ctor.setAccessible(true);
            TYPE = ctor.newInstance(MyComponent.class, "myplugin:mycomponent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String data;

    public MyComponent(String data) {
        this.data = data;
    }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    @Override
    public MyComponent clone() {
        return new MyComponent(this.data);
    }
}
```

---

## Custom Data Directory Pattern

By default, Hytale plugins store data in a folder based on the group ID (e.g., `mods/com.kukso.hy.lib/`). This can be messy. You can use a **custom data directory** for cleaner folder names.

### The Pattern

Instead of relying on the plugin's default data folder, define your own path:

```java
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import java.nio.file.Path;

public class MyPlugin extends JavaPlugin {

    @Override
    protected void setup() {
        // Define a clean folder name instead of using the default group-based path
        Path dataDir = Path.of("mods/MyPlugin");

        // Pass this path to your managers
        ConfigManager.init(this, dataDir);
        LocaleMan.init(this, dataDir);
    }
}
```

### Folder Structure Comparison

| Approach | Resulting Path |
|----------|----------------|
| Default (group-based) | `mods/com.example.myplugin/config.json` |
| Custom data directory | `mods/MyPlugin/config.json` |

### Benefits

1. **Cleaner paths** - `mods/MyPlugin/` instead of `mods/com.example.myplugin/`
2. **User-friendly** - Server admins can easily find and edit config files
3. **Consistent naming** - Match your plugin's display name
4. **Full control** - Place files wherever makes sense for your plugin

### Implementation Example

```java
public class MyPlugin extends JavaPlugin {

    private static final Path DATA_DIR = Path.of("mods/MyPlugin");

    @Override
    protected void setup() {
        // Ensure directory exists
        try {
            java.nio.file.Files.createDirectories(DATA_DIR);
        } catch (java.io.IOException e) {
            getLogger().atWarning().log("Failed to create data directory");
        }

        // Initialize managers with custom path
        ConfigManager.init(this, DATA_DIR);

        // Your config will be at: mods/MyPlugin/config.json
        // Your locales will be at: mods/MyPlugin/locales/en_US.json
    }
}
```

### File Layout

```
mods/
├── MyPlugin/
│   ├── config.json
│   └── locales/
│       ├── en_US.json
│       └── tr_TR.json
└── AnotherPlugin/
    └── config.json
```

This pattern is used by KuksoHyLib itself (`mods/KuksoHyLib/`) and is recommended for all plugins.

---

## Summary

| Module | Entry Point | Purpose |
|--------|-------------|---------|
| ColorMan | `ColorMan.translate(text)` | Color codes to Message |
| LocaleMan | `LocaleMan.get(player, key)` | Multi-language messages |
| ComponentMan | `ComponentMan.getComponent(...)` | ECS utilities |

For economy functionality, see [ECONOMY.md](./ECONOMY.md) for reference implementation patterns.

For more details, see [CLAUDE.md](./CLAUDE.md).
