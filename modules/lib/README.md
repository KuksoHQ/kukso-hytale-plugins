# kukso-hy-lib

A core library for the Kukso Hytale Mods ecosystem. Built by developers with Minecraft plugin experience, now bringing that expertise to Hytale modding.

## Overview

**kukso-hy-lib** provides essential utilities and modules that streamline Hytale mod development. Instead of reinventing the wheel for every mod, this library offers battle-tested solutions for common modding needs.

## Features

### Localization Module

Multi-language support based on player's client language setting.

Note: Hytale doesn't support it yet. This module works with only 1 <default> language. But you can still implement it in your plugin, playerRef always returns EN client language for now.

```java
import com.kukso.hy.lib.util.LocaleUtil;

// Basic usage - automatically uses player's language
Message msg = LocaleUtil.get(playerRef, "messages.welcome");
player.sendMessage(msg);

// With placeholders
Message msg = LocaleUtil.get(playerRef, "messages.welcome",
    Map.of("player", player.getUsername()));

// Raw string for specific locale
String text = LocaleUtil.getRaw("en_US", "messages.welcome");

// Check what locales are loaded
Set<String> loaded = LocaleUtil.getLoadedLocales();
```

**Language file example** (`/mods/KuksoHyLib/locales/en_US.json`):
```json
{
  "prefix": "&e[MyPlugin]&r",
  "messages": {
    "welcome": "&aWelcome, &e{player}&a!",
    "goodbye": "&7Goodbye, &e{player}&7!"
  },
  "errors": {
    "no_permission": "&cYou don't have permission to do that.",
    "not_enough_coins": "&cYou need {required} coins. You have {current}."
  }
}
```

**Features:**
- JSON-based language files (no external dependencies)
- Automatic placeholder resolution with `{placeholder}` syntax
- Player language detection from client settings (Hytale doesn't support this yet)
- Fallback chain: Player locale → Default locale (en_US) → Key name (Hytale doesn't support this yet)
- Thread-safe with concurrent access support
- Hot-reload via `/kuksolib reload`
- Color code support in translations (integrates with ColorMan)

---

### Chat Colorization Module

Translate Minecraft-style color codes to Hytale's Message format.

```java
import com.kukso.hy.lib.util.ColorUtil;

// Basic color codes - returns Hytale Message object
Message msg = ColorUtil.colorThis("&aGreen &bAqua &cRed");

// Multiple colors in one message
Message msg = ColorUtil.colorThis("&4Hel&clo &bWo&1rld!");
// Result: "Hel" dark red, "lo " red, "Wo" aqua, "rld!" dark blue

// Hex color support
Message msg = ColorUtil.colorThis("&#FF5733This is orange!");

// Formatting codes
Message msg = ColorUtil.colorThis("&l&4Bold Red &r&oItalic White");

// Combined with localization
Message msg = LocaleUtil.get(player, "messages.welcome"); // Already colored!

player.sendMessage(msg);
```

**Supported Codes:**
- Legacy colors: `&0-9`, `&a-f` (standard 16 Minecraft colors)
- Hex colors: `&#RRGGBB` (e.g., `&#FF5733`)
- Bold: `&l`
- Italic: `&o`
- Reset: `&r` (resets color and formatting)

**Features:**
- Full legacy color code support
- Hex color support for custom colors
- Multiple colors in a single message
- Format codes (bold, italic)
- Seamless integration with LocaleMan
- Returns native Hytale Message objects

**Note:** Underline (`&n`), strikethrough (`&m`), and obfuscated (`&k`) are not supported by Hytale's Message API.

---

### Component Util

---

### Permission Util

---



## Installation

### Gradle

```groovy
repositories {
    maven { url 'https://repo.kukso.com/releases' }
}

dependencies {
    implementation 'com.kukso.hy:kukso-hy-lib:1.0-SNAPSHOT'
}
```

### Maven

```xml
<repository>
    <id>kukso-repo</id>
    <url>https://repo.kukso.com/releases</url>
</repository>

<dependency>
    <groupId>com.kukso.hy</groupId>
    <artifactId>kukso-hy-lib</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Requirements

- Java 25 (matches Hytale runtime)
- HytaleServer API

## Quick Start

KuksoHyLib is a standalone plugin. Simply include it as a dependency in your plugin's `manifest.json`:

```json
{
  "Dependencies": ["com.kukso.hy.lib:KuksoLib"]
}
```

Then use the utilities in your plugin:

```java
import com.kukso.hy.lib.util.ColorMan;
import com.kukso.hy.lib.locale.LocaleMan;

public class MyPlugin extends JavaPlugin {

    @Override
    protected void start() {
        // Send colored messages
        player.sendMessage(ColorMan.translate("&aHello, &e" + player.getUsername() + "&a!"));

        // Or use localization with automatic color support
        player.sendMessage(LocaleMan.get(player, "messages.welcome",
            Map.of("player", player.getUsername())));
    }
}
```

## Built-in Commands

Access via `/kuksolib` (aliases: `/klib`, `/kl`):

- **help** - Shows available commands
- **version** - Displays plugin version
- **reload** - Reloads configuration and locales (OP-only)
- **test chatcolor** - Demonstrates color formatting
- **test locale** - Demonstrates localization system

## Documentation

Full documentation available at: [docs.kukso.com/hy-lib](https://docs.kukso.com/hy-lib)

## Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) before submitting a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- Discord: [discord.gg/kukso](https://discord.gg/kukso)
- Issues: [GitHub Issues](https://github.com/kukso/kukso-hy-lib/issues)

---

Made with experience from 5+ years of Minecraft plugin development.
