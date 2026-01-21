# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**kukso-hy-lib** is a core library for the Hytale Mods ecosystem, providing utilities for GUI building, localization, chat colorization, and ECS helpers. Built by developers with 5+ years of Minecraft plugin experience.

The project is a Hytale server plugin (mod) that depends on the HytaleServer.jar API and follows the Hytale plugin manifest structure.

## Build Commands

### Basic Operations
```bash
# Build the project (compiles and creates JAR)
./gradlew build

# Clean build artifacts
./gradlew clean

# Build without tests
./gradlew build -x test

# Run tests
./gradlew test
```

### Development Server
```bash
# Generate VSCode launch configuration
./gradlew generateVSCodeLaunch

# For IntelliJ IDEA, the run configuration is auto-generated
# Look for 'HytaleServer' in your run configurations
```

The server runs from `./run` directory and loads:
- The built plugin from `src/main`
- Assets from your Hytale installation
- Optional user mods if `load_user_mods=true` in gradle.properties

### Version Management
Version is stored in `version.txt` and automatically synced to `manifest.json` during build.

## Project Configuration

Key properties in `gradle.properties`:
- `java_version` - Currently 25 (game runs on Java 25)
- `includes_pack` - Set to `true` if plugin includes assets
- `patchline` - Usually `release` or `pre-release`
- `hytale_home` - Auto-detected, or set manually for custom installations

## Architecture

### Plugin Lifecycle
The main plugin class is `com.kukso.hy.lib.Main` (extends `JavaPlugin`):
- `setup()` - Called during plugin initialization, sets up the singleton instance
- `start()` - Called when plugin starts, registers commands
- `shutdown()` - Called on plugin disable

### Command System Architecture

The library supports two command types: **tree commands** (parent with subcommands) and **standalone commands**.

**Core Components:**

1. **CommandInterface** (`com.kukso.hy.lib.command.CommandInterface`)
   - Contract for all command types (both standalone and subcommands)
   - Handles name, aliases, permissions, description, usage
   - Provides `execute()` and `tabComplete()` methods
   - Permission checking is done by the base classes before calling `execute()`
   - Supports `getPermissionGroup()` to control OP requirements:
     - Return `null` (default) for OP-only commands
     - Return `GameMode.Adventure` to allow all players

2. **CommandRegistrar** - Static utility for registering commands
   - `standalone(plugin, cmd)` - Register a standalone command (e.g., `/warps`)
   - `standaloneAll(plugin, cmd1, cmd2, ...)` - Register multiple standalone commands
   - `tree(plugin, name, description)` - Create a tree command manager
   - `treeWithAliases(plugin, name, description, aliases...)` - Tree command with aliases

3. **CommandTreeBase** (package-private) - Manages subcommand routing
   - Routes subcommands: `/parent <subcommand> [args...]`
   - Manages alias-to-command mapping
   - Handles permission checking before delegating to subcommands
   - Uses `setAllowsExtraArguments(true)` to accept subcommand arguments

4. **CommandSingleBase** (package-private) - Wraps standalone commands
   - Wraps a `CommandInterface` for direct registration with Hytale
   - Handles permission checking and argument parsing

5. **CommandBootstrap** - Internal KuksoLib command registration
   - Called from `Main.start()` to register all KuksoLib commands
   - Sets up the `/kuksolib` tree command and standalone commands

**Command Locations:** `com.kukso.hy.lib.command`
- Subcommands use `CmdSub*` prefix (e.g., `CmdSubHelp`, `CmdSubReload`)
- Standalone commands use `CmdSingle*` prefix (e.g., `CmdSinglePlayerInfo`)

### Adding New Commands

**Option 1: Add a subcommand under `/kuksolib`**

Create a class implementing `CommandInterface` with `CmdSub` prefix:
```java
class CmdSubMyCommand implements CommandInterface {
    @Override
    public String getName() { return "mycommand"; }

    @Override
    public List<String> getAliases() { return List.of("mc"); }

    @Override
    public GameMode getPermissionGroup() {
        return GameMode.Adventure; // or null for OP-only
    }

    @Override
    public String getDescription() { return "Does something cool"; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // Implementation - permissions already checked
        return true;
    }
}
```

Register in `CommandBootstrap.register()`:
```java
mgr.register(new CmdSubMyCommand());
```

**Option 2: Add a standalone command (e.g., `/mycommand`)**

Create a class implementing `CommandInterface` with `CmdSingle` prefix:
```java
class CmdSingleMyCommand implements CommandInterface {
    @Override
    public String getName() { return "mycommand"; }

    @Override
    public List<String> getAliases() { return List.of("mc"); }

    @Override
    public GameMode getPermissionGroup() {
        return GameMode.Adventure;
    }

    @Override
    public String getDescription() { return "Does something cool"; }

    @Override
    public String getUsage() { return "/mycommand [args]"; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        return true;
    }
}
```

Register in `CommandBootstrap.register()`:
```java
CommandRegistrar.standalone(plugin, new CmdSingleMyCommand());
```

**Option 3: Create your own tree command (for other plugins)**

```java
// In your plugin's start() method:
CommandTreeBase mgr = CommandRegistrar.treeWithAliases(plugin, "myplugin", "My plugin commands", "mp");
mgr.register(new MyHelpCmd(mgr));
mgr.register(new MyReloadCmd());
```

**Permission System:**
- Commands have two layers of permission checks:
  1. **Permission Group** (`getPermissionGroup()`): Controls base access level
     - `null` (default) = Requires `{commandName}.admin` or `*` permission (admin/OP only)
     - `GameMode.Adventure` = All players can access
  2. **Specific Permissions** (`getPermissions()`): Fine-grained permission nodes
     - If empty, anyone with group access can use the command
     - If specified, sender must have at least one of the listed permissions
- Examples:
  - `CmdSubHelp`, `CmdSubVersion`, `CmdSubTest`: `GameMode.Adventure` + no specific permissions = all players
  - `CmdSubReload`: `null` (requires `kuksolib.admin` or `*`) + `kuksolib.reload` permission
- The wildcard permission `*` grants access to all commands

### Hytale Plugin Structure

The plugin follows Hytale's manifest.json specification:
- **Group** - Maven-style groupId
- **Name** - Display name of the plugin
- **Main** - Fully qualified main class name
- **Dependencies/OptionalDependencies** - Other plugins required
- **IncludesAssetPack** - Whether plugin contains game assets

## Built-in Commands

### Tree Command: `/kuksolib` (aliases: `/klib`, `/kl`)

Subcommands:
- **help** (`?`) - Shows available commands
- **version** (`ver`, `v`) - Displays plugin version and Java info
- **reload** (`rl`) - Reloads the plugin configuration (OP-only)
- **test** (`t`) - Testing utilities for KuksoLib features
  - `test chatcolor` - Demonstrates ColorMan's formatting capabilities
  - `test locale` - Demonstrates LocaleMan's localization system

### Standalone Commands

- **/playerinfo** (`/pinfo`, `/pi`) - Display information about a player

**Example:** `/kuksolib test chatcolor` will display:
- Rainbow-like text
- Mixed formatting (bold, italic, reset)
- Hex color examples
- All standard Minecraft colors
- Complex color combinations

## Utility Classes

### ColorMan (`com.kukso.hy.lib.util.ColorMan`)

Translates Minecraft-style color codes to Hytale's Message format.

**Supported Features:**
- Legacy color codes: `&0-9`, `&a-f` (standard 16 Minecraft colors)
- Format codes: `&l` (bold), `&o` (italic)
- Hex colors: `&#RRGGBB` (e.g., `&#FF5733`)
- Reset code: `&r` (resets all formatting)

**Usage:**
```java
import com.kukso.hy.lib.util.ColorMan;

// Simple color
Message msg = ColorMan.translate("&aHello!");
// Result: Green "Hello!"

// Multiple colors in one message (fully supported!)
Message msg = ColorMan.translate("&4Hel&clo &bWo&1rld!");
// Result: "Hel" in dark red, "lo " in red, "Wo" in aqua, "rld!" in dark blue

// Multiple colors and formatting
Message msg = ColorMan.translate("&e&lKuksoLib &7- &aReady!");
// Result: Yellow bold "KuksoLib", gray " - ", green "Ready!"

// Hex colors
Message msg = ColorMan.translate("&#FF5733This is orange!");

// Complex combinations
Message msg = ColorMan.translate("&l&4Bold Red &r&oItalic White &a&lBold Green");
```

**Limitations:**
- Only `bold` and `italic` are supported (Hytale Message API limitation)
- Format codes `&n` (underlined), `&m` (strikethrough), `&k` (obfuscated) are ignored

**How It Works:**
- ColorMan parses the text into segments, each with its own color and formatting
- Uses Hytale's `Message.insert()` to chain segments together
- Each color change creates a new segment with independent formatting

### LocaleMan (`com.kukso.hy.lib.locale.LocaleMan`)

Native localization system for multi-language support based on player's client language.

**Package Structure:**
```
com.kukso.hy.lib.locale/
├── LocaleMan.java          # Main static utility (entry point)
├── LocaleCache.java        # Player language cache (ConcurrentHashMap)
├── LocaleLoader.java       # JSON file loading/parsing (uses Gson)
├── LocaleListener.java     # Event listeners for player join/disconnect
└── PlaceholderResolver.java # {placeholder} replacement utility
```

**File Structure:**
```
plugins/KuksoLib/locales/
├── en_US.json   (default, always present)
├── tr_TR.json
├── de_DE.json
└── ...
```

**JSON Locale File Format:**
```json
{
  "prefix": "&e[KuksoLib]&r",
  "messages": {
    "welcome": "&aWelcome to the server, &e{player}&a!",
    "goodbye": "&7Goodbye, &e{player}&7!"
  },
  "commands": {
    "reload": {
      "success": "&aConfiguration reloaded!",
      "error": "&cReload failed: {error}"
    }
  },
  "errors": {
    "no_permission": "&cNo permission."
  }
}
```

**Usage:**
```java
import com.kukso.hy.lib.locale.LocaleMan;

// Basic usage - gets message in player's language
Message msg = LocaleMan.get(playerRef, "messages.welcome");

// With placeholders
Message msg = LocaleMan.get(playerRef, "messages.welcome",
    Map.of("player", playerName));

// Raw string for specific locale (for other plugins)
String text = LocaleMan.getRaw("en_US", "messages.welcome");

// Check player's locale
String locale = LocaleMan.getPlayerLocale(playerRef);

// Get all loaded locales
Set<String> locales = LocaleMan.getLoadedLocales();
```

**Fallback Chain:**
1. Player's locale (e.g., `tr_TR`)
2. Default locale (`en_US`)
3. Key itself (with warning logged)

**Lifecycle Integration:**
- `LocaleMan.init(plugin)` - Called automatically in `Main.setup()`
- `LocaleMan.shutdown()` - Called automatically in `Main.shutdown()`
- `LocaleMan.reload()` - Called by `/kuksolib reload` command

**Adding New Locale Files:**
1. Create a JSON file in `src/main/resources/locales/` (e.g., `de_DE.json`)
2. Copy structure from `en_US.json` and translate values
3. The file is auto-extracted to plugin data folder on first run
4. Players with matching client language will see translations

**Thread Safety:**
- Uses `ConcurrentHashMap` for both cache and translations
- Atomic reload: builds new map, then swaps reference
- Safe for concurrent access from multiple threads

## Module Structure (Planned)

The README describes several modules that are planned or in development:
- **GUI Module** - Interactive in-game menus with fluent builder API
- **Localization Module** - JSON-based multi-language support (IMPLEMENTED - see LocaleMan above)
- **Chat Colorization** - Color codes, hex colors, and gradients (IMPLEMENTED - see ColorMan above)
- **ECS Utilities** - Entity Component System helpers (IMPLEMENTED - see ComponentMan)

Economy functionality has been moved to a separate plugin. See [ECONOMY.md](./ECONOMY.md) for reference patterns.

Check the actual source code structure when working on features.

## Development Notes

- The project uses Java 25 to match Hytale's runtime
- HytaleServer.jar dependency is loaded from the user's Hytale installation
- The build system auto-detects Hytale installation path for Windows, macOS, and Linux
- Plugin manifest is auto-updated during build with version from version.txt
- Use Hytale's Message API for formatted chat output (supports colors, bold, italic, etc.)

## Code Conventions

- Package structure: `com.kukso.hy.lib.<module>.<feature>`
- Commands implement `CommandInterface` and follow naming conventions:
  - Subcommands: `CmdSub*` prefix (e.g., `CmdSubHelp`)
  - Standalone commands: `CmdSingle*` prefix (e.g., `CmdSinglePlayerInfo`)
- Singleton pattern for main plugin instance (`Main.getInstance()`)
- Use Hytale's logger: `plugin.getLogger().at(Level.INFO).log("message")` (Static instance in the Main class is LOGGER)
- For colored messages, prefer `ColorMan.translate("&atext")` over manual `Message.raw().color()` calls