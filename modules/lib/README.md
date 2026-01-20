# kukso-hy-lib

A core library for the Kukso Hytale Mods ecosystem. Built by developers with extensive Minecraft plugin experience, now bringing that expertise to Hytale modding.

## Overview

**kukso-hy-lib** provides essential utilities and modules that streamline Hytale mod development. Instead of reinventing the wheel for every mod, this library offers battle-tested solutions for common modding needs.

## Features

### GUI Module

Create interactive in-game menus and interfaces with ease.

```java
import com.kukso.hy.lib.gui.GuiBuilder;
import com.kukso.hy.lib.gui.GuiSlot;

GuiBuilder gui = new GuiBuilder("Shop Menu", 6) // 6 rows
    .setSlot(13, new GuiSlot(Items.DIAMOND_SWORD)
        .setDisplayName("&bLegendary Sword")
        .setLore("&7Click to purchase", "&aPrice: 500 coins")
        .onClick(player -> {
            // Handle purchase logic
        }))
    .onClose(player -> {
        // Cleanup logic
    });

gui.open(player);
```

**Features:**
- Declarative GUI building with fluent API
- Click event handling per slot
- Pagination support for large inventories
- Animated GUI elements
- Template system for reusable layouts

---

### Localization Module

Multi-language support based on player's client language setting.

```java
import com.kukso.hy.lib.locale.LocaleMan;

// Basic usage - automatically uses player's language
Message msg = LocaleMan.get(playerRef, "messages.welcome");
player.sendMessage(msg);

// With placeholders
Message msg = LocaleMan.get(playerRef, "messages.welcome",
    Map.of("player", player.getUsername()));

// Raw string for specific locale
String text = LocaleMan.getRaw("en_US", "messages.welcome");

// Check what locales are loaded
Set<String> loaded = LocaleMan.getLoadedLocales();
```

**Language file example** (`locales/en_US.json`):
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
- Player language detection from client settings
- Fallback chain: Player locale → Default locale (en_US) → Key name
- Thread-safe with concurrent access support
- Hot-reload via `/kuksolib reload`
- Color code support in translations (integrates with ColorMan)

---

### Chat Colorization Module

Translate Minecraft-style color codes to Hytale's Message format.

```java
import com.kukso.hy.lib.util.ColorMan;

// Basic color codes - returns Hytale Message object
Message msg = ColorMan.translate("&aGreen &bAqua &cRed");

// Multiple colors in one message
Message msg = ColorMan.translate("&4Hel&clo &bWo&1rld!");
// Result: "Hel" dark red, "lo " red, "Wo" aqua, "rld!" dark blue

// Hex color support
Message msg = ColorMan.translate("&#FF5733This is orange!");

// Formatting codes
Message msg = ColorMan.translate("&l&4Bold Red &r&oItalic White");

// Combined with localization
Message msg = LocaleMan.get(player, "messages.welcome"); // Already colored!

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

### In-Game Economy Module

A complete economy system ready to integrate into your mods.

```java
import com.kukso.hy.lib.service.ServiceManager;
import com.kukso.hy.lib.economy.Economy;
import com.kukso.hy.lib.economy.EconomyResponse;

Economy economy = ServiceManager.getEconomy();

// Check balance
double balance = economy.getBalance(player);

// Add/remove funds
economy.deposit(player, 100.0);
economy.withdraw(player, 50.0);

// Transfer between players
EconomyResponse response = economy.transfer(sender, receiver, 250.0);
if (response.isSuccess()) {
    sender.sendMessage(ColorMan.translate("&aTransfer complete!"));
} else {
    sender.sendMessage(ColorMan.translate("&c" + response.getErrorMessage()));
}
```

**Features:**
- ECS-based runtime storage
- Thread-safe operations
- Configurable currency formatting
- Vault-like API for cross-mod compatibility

---

## Lightweight Architecture

KuksoLib uses a **lazy activation** pattern for its modules. This means:

- **Economy, Permission, and Chat modules are dormant by default**
- Modules only activate when a plugin registers to use that service
- No event listeners or ECS components are loaded until needed
- Servers that don't need all features have zero overhead from unused modules

### How It Works

When your plugin calls `ServiceManager.registerEconomy(...)`, KuksoLib:
1. Activates the Economy module
2. Registers ECS components for currency tracking
3. Sets up event listeners for player join/quit

If no plugin ever registers an economy provider, none of this happens.

### Currencies Defined by Plugins

Currencies are NOT defined in KuksoLib's config. Instead, economy plugins register their currencies programmatically:

```java
// In your economy plugin
public class MyEconomyPlugin extends JavaPlugin implements Economy {
    @Override
    public void start() {
        // Register currencies
        CurrencyManager.register(new Currency(
            "gold", "Gold", "Gold", "Gold", "G", "{amount}{symbol}", 0, 100.0, "myeconomy:gold"
        ));
        CurrencyManager.register(new Currency(
            "gems", "Gems", "Gem", "Gems", "💎", "{amount} {symbol}", 0, 0.0, "myeconomy:gems"
        ));
        CurrencyManager.setDefaultCurrency("gold");

        // Register as provider - this activates the economy module
        ServiceManager.registerEconomy(this, this, ServicePriority.NORMAL);
    }
}
```

### Example

```java
// This plugin only uses Economy - Permission and Chat modules stay dormant
public class MyEconomyPlugin extends JavaPlugin {
    @Override
    public void start() {
        ServiceManager.registerEconomy(this, new MyEconomy(), ServicePriority.NORMAL);
        // Only Economy module is now active
    }
}
```

### Benefits

| Benefit | Description |
|---------|-------------|
| Zero overhead | Unused modules consume no resources |
| Faster startup | Only load what you need |
| Lower memory | No unnecessary objects created |
| Cleaner logs | No "module loaded" for unused features |

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

KuksoLib is a standalone plugin. Simply include it as a dependency in your plugin's `manifest.json`:

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
