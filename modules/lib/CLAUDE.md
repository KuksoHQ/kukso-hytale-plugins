# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**kukso-hy-lib** is a core library for the Hytale Mods ecosystem, providing utilities for GUI building, localization, chat colorization, and economy systems. Built by developers with 5+ years of Minecraft plugin experience.

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

The library uses a hierarchical subcommand system with clear separation of concerns:

**Core Components:**
1. **CmdInterface** - Defines the contract for all subcommands
   - Handles name, aliases, permissions, description, usage
   - Provides `execute()` and `tabComplete()` methods
   - Permission checking (both group and specific permissions) is done by CmdManager before calling execute()
   - Supports `getPermissionGroup()` to control OP requirements:
     - Return `null` (default) for OP-only commands
     - Return `GameMode.Adventure` to allow all players

2. **CmdManager** - Central command dispatcher (extends CommandBase)
   - Registers with Hytale as `/kuksolib` (aliases: `/klib`, `/kl`)
   - Routes subcommands: `/kuksolib <subcommand> [args...]`
   - Manages alias-to-command mapping
   - Handles permission checking before delegating to subcommands

3. **CmdRegistrar** - Static registration utility
   - Called from `Main.start()` to register all commands
   - Creates CmdManager instance and registers all subcommands
   - Registers the manager with plugin's command registry

**Subcommands location:** `com.kukso.hy.lib.command.sub.*`
- Each subcommand implements `CmdInterface`
- Examples: HelpCmd, ReloadCmd, VersionCmd

### Adding New Commands

To add a new subcommand:

1. Create a class in `com.kukso.hy.lib.command.sub` implementing `CmdInterface`:
```java
public class MyCmd implements CmdInterface {
    @Override
    public String getName() { return "mycommand"; }

    @Override
    public List<String> getAliases() { return List.of("mc"); }

    @Override
    public List<String> getPermissions() { return List.of("kuksolib.mycommand"); }

    @Override
    public GameMode getPermissionGroup() {
        // Return GameMode.Adventure for player-accessible commands
        // Return null (or omit this method) for OP-only commands
        return GameMode.Adventure;
    }

    @Override
    public String getDescription() { return "Does something cool"; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // Implementation - permissions already checked by CmdManager
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
```

**Permission System:**
- Commands have two layers of permission checks:
  1. **Permission Group** (`getPermissionGroup()`): Controls base access level
     - `null` (default) = Requires `kuksolib.admin` or `*` permission (admin/OP only)
     - `GameMode.Adventure` = All players can access
  2. **Specific Permissions** (`getPermissions()`): Fine-grained permission nodes
     - If empty, anyone with group access can use the command
     - If specified, sender must have at least one of the listed permissions
- Examples:
  - `HelpCmd`, `VersionCmd`: `GameMode.Adventure` + no specific permissions = all players
  - `ReloadCmd`: `null` (requires `kuksolib.admin` or `*`) + `kuksolib.reload` permission
- The wildcard permission `*` grants access to all commands

2. Register it in `CmdRegistrar.register()`:
```java
mgr.register(new MyCmd());
```

### Hytale Plugin Structure

The plugin follows Hytale's manifest.json specification:
- **Group** - Maven-style groupId
- **Name** - Display name of the plugin
- **Main** - Fully qualified main class name
- **Dependencies/OptionalDependencies** - Other plugins required
- **IncludesAssetPack** - Whether plugin contains game assets

## Module Structure (Planned)

The README describes several modules that are planned or in development:
- **GUI Module** - Interactive in-game menus with fluent builder API
- **Localization Module** - YAML-based multi-language support
- **Chat Colorization** - Color codes, hex colors, and gradients
- **Economy Module** - Complete economy system with SQLite/MySQL backend

These modules are documented in the README but may not all be implemented yet. Check the actual source code structure when working on features.

## Development Notes

- The project uses Java 25 to match Hytale's runtime
- HytaleServer.jar dependency is loaded from the user's Hytale installation
- The build system auto-detects Hytale installation path for Windows, macOS, and Linux
- Plugin manifest is auto-updated during build with version from version.txt
- Use Hytale's Message API for formatted chat output (supports colors, bold, italic, etc.)

## Code Conventions

- Package structure: `com.kukso.hy.lib.<module>.<feature>`
- Commands use the CmdInterface pattern for consistency
- Singleton pattern for main plugin instance (`Main.getInstance()`)
- Use Hytale's logger: `plugin.getLogger().at(Level.INFO).log("message")` (Static instance in the Main class is LOGGER)