# Kukso Hytale

Gradle monorepo for Kukso Hytale mods.

## Modules

- `:lib` - `KuksoLib`, the shared Hytale mod library.
- `:econ` - `KuksoEcon`.
- `:warps` - `KuksoWarps`.

## Build

Hytale server classes are resolved from a local Hytale installation. Do not commit Hytale game jars or upload them as CI secrets.

Set `HYTALE_HOME` to the Hytale home directory:

```powershell
$env:HYTALE_HOME = "C:\Users\burak\AppData\Roaming\Hytale"
```

The default server jar path is:

```text
$HYTALE_HOME/install/release/package/game/2026.03.26-89796e57b/Server/HytaleServer.jar
```

You can override the local install with `HYTALE_HOME`, `-PhytaleHome=...`, or `-Phytale_home=...`. You can also override `patchline` and `game_build` when needed.

```powershell
.\gradlew.bat projects
.\gradlew.bat build
```

GitHub Actions Hytale jobs require a self-hosted runner labeled `hytale` with `HYTALE_HOME` configured.

Hytale modules are not published to Maven until Kukso has a package repository. Releases publish platform jars and KuksoLib JavaDocs only.

Release tags use `repo-module-vX.Y.Z`, for example `kukso-hytale-warps-v1.2.0`.
Release jars use `KuksoModule-Platform-X.Y.Z.jar`, for example `KuksoWarps-Hytale-1.2.0.jar`.
