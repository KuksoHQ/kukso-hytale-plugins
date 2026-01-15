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

Multi-language support made simple. Reach players worldwide.

```java
import com.kukso.hy.lib.locale.Locale;
import com.kukso.hy.lib.locale.LocaleManager;

// Load language files from resources/lang/
LocaleManager locales = new LocaleManager("lang/");
locales.loadLocale("en_US");
locales.loadLocale("es_ES");
locales.loadLocale("de_DE");

// Get translated message with placeholders
String message = locales.get(player, "welcome.message",
    "player", player.getName(),
    "online", server.getOnlineCount());

player.sendMessage(message);
```

**Language file example** (`lang/en_US.yml`):
```yaml
welcome:
  message: "Welcome, {player}! There are {online} players online."
  first_join: "Welcome to the server for the first time, {player}!"

errors:
  no_permission: "&cYou don't have permission to do that."
  not_enough_coins: "&cYou need {required} coins. You have {current}."
```

**Features:**
- YAML-based language files
- Placeholder support with key-value pairs
- Per-player language preferences
- Fallback language support
- Hot-reload capabilities

---

### Chat Colorization Module

Bring your chat messages to life with colors, gradients, and formatting.

```java
import com.kukso.hy.lib.chat.ChatColor;
import com.kukso.hy.lib.chat.Gradient;

// Basic color codes
String colored = ChatColor.colorize("&aGreen &bAqua &cRed");

// Hex color support
String hex = ChatColor.colorize("&#FF5733This is orange!");

// Gradient text
String gradient = Gradient.apply("Rainbow Text",
    "#FF0000", "#FF7F00", "#FFFF00", "#00FF00", "#0000FF", "#8B00FF");

// Predefined gradients
String fire = Gradient.FIRE.apply("Hot Message");
String ocean = Gradient.OCEAN.apply("Cool Message");

player.sendMessage(gradient);
```

**Features:**
- Legacy color code support (`&a`, `&b`, etc.)
- Full hex color support (`&#RRGGBB`)
- Gradient text with unlimited color stops
- Predefined gradient presets (Fire, Ocean, Rainbow, Sunset, etc.)
- Format codes (bold, italic, underline, strikethrough)
- RGB animations for dynamic effects

---

### In-Game Economy Module

A complete economy system ready to integrate into your mods.

```java
import com.kukso.hy.lib.economy.Economy;
import com.kukso.hy.lib.economy.EconomyManager;
import com.kukso.hy.lib.economy.Transaction;

EconomyManager economy = new EconomyManager();

// Check balance
double balance = economy.getBalance(player);

// Add/remove funds
economy.deposit(player, 100.0);
economy.withdraw(player, 50.0);

// Transfer between players
Transaction transaction = economy.transfer(sender, receiver, 250.0);
if (transaction.isSuccessful()) {
    sender.sendMessage("&aTransfer complete!");
} else {
    sender.sendMessage("&c" + transaction.getFailureReason());
}

// Transaction logging
economy.getTransactionHistory(player, 10); // Last 10 transactions
```

**Features:**
- SQLite and MySQL storage backends
- Thread-safe operations
- Transaction history and logging
- Configurable currency formatting
- Vault-like API for cross-mod compatibility
- Balance top leaderboards
- Offline player support

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

- Java 17 or higher
- HytaleServer API

## Quick Start

```java
import com.kukso.hy.lib.KuksoLib;

public class MyMod extends HytaleMod {

    @Override
    public void onEnable() {
        // Initialize the library
        KuksoLib.init(this);

        // Now you can use all modules
        getLogger().info("MyMod enabled with kukso-hy-lib!");
    }

    @Override
    public void onDisable() {
        KuksoLib.shutdown();
    }
}
```

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
