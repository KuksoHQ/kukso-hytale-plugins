# KuksoHyEco - Economy Plugin Design Document

## Summary

Create a new Hytale economy plugin (`kukso-hy-eco`) that uses KuksoLib as its core library. This plugin will provide user-facing economy commands while leveraging KuksoLib's APIs for:
- **Localization** (LocaleMan)
- **String colorization** (ColorMan)
- **Economy infrastructure** (Economy API, CurrencyManager)
- **Permission checking** (Permission API)
- **Command system** (CmdInterface)

---

## Plugin Overview

**Name:** KuksoHyEco
**Package:** `com.kukso.hy.eco`
**Dependency:** `com.kukso.hy.lib:KuksoLib`

**Purpose:** Provides user-facing economy commands (`/balance`, `/pay`, `/money`, etc.) that KuksoLib deliberately does not provide (as a library-only architecture).

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                       KuksoHyEco                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Commands   │  │   Listeners  │  │   Config     │          │
│  │  /bal /pay   │  │ PlayerJoin   │  │  config.json │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
│         │                 │                 │                   │
│         ▼                 ▼                 ▼                   │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │              EcoProvider (implements Economy)               ││
│  │   - Registers currencies via CurrencyManager                ││
│  │   - Implements deposit/withdraw/transfer                    ││
│  │   - Registers as provider via ServiceManager                ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         KUKSOLIB                                │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐    │
│  │ ColorMan  │  │ LocaleMan │  │ Economy   │  │ CmdSystem │    │
│  │           │  │           │  │ API       │  │           │    │
│  └───────────┘  └───────────┘  └───────────┘  └───────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

---

## Currency Configuration

Currencies are defined by this plugin (not KuksoLib) and registered programmatically:

```java
// In EcoProvider.java
public void registerCurrencies() {
    CurrencyManager.register(new Currency(
        "coins",          // id
        "Coins",          // displayName
        "Coin",           // nameSingular
        "Coins",          // namePlural
        "$",              // symbol
        "{symbol}{amount}", // format
        2,                // decimalPlaces
        100.0,            // startingBalance
        "kuksoeco:coins"  // componentId
    ));

    CurrencyManager.setDefaultCurrency("coins");
}
```

**Optional:** Allow additional currencies via config:

```json
{
  "currencies": {
    "coins": {
      "displayName": "Coins",
      "symbol": "$",
      "format": "{symbol}{amount}",
      "decimalPlaces": 2,
      "startingBalance": 100.0
    },
    "gems": {
      "displayName": "Gems",
      "symbol": "💎",
      "format": "{amount} {symbol}",
      "decimalPlaces": 0,
      "startingBalance": 0
    }
  },
  "defaultCurrency": "coins"
}
```

---

## Commands

### Player Commands

| Command | Aliases | Permission | Description |
|---------|---------|------------|-------------|
| `/balance [player]` | `/bal`, `/money` | `kuksoeco.balance` | Check your or another player's balance |
| `/pay <player> <amount>` | `/transfer` | `kuksoeco.pay` | Send money to another player |
| `/baltop [page]` | `/balancetop` | `kuksoeco.baltop` | View richest players |

### Admin Commands

| Command | Aliases | Permission | Description |
|---------|---------|------------|-------------|
| `/eco give <player> <amount> [currency]` | - | `kuksoeco.admin.give` | Give money to a player |
| `/eco take <player> <amount> [currency]` | - | `kuksoeco.admin.take` | Take money from a player |
| `/eco set <player> <amount> [currency]` | - | `kuksoeco.admin.set` | Set a player's balance |
| `/eco reset <player>` | - | `kuksoeco.admin.reset` | Reset player to starting balance |
| `/eco reload` | - | `kuksoeco.admin.reload` | Reload configuration |

---

## Localization

Uses KuksoLib's LocaleMan for all player-facing messages:

**File:** `plugins/KuksoHyEco/locales/en_US.json`

```json
{
  "prefix": "&e[Eco]&r ",
  "messages": {
    "balance": {
      "self": "{prefix}&7Your balance: &a{balance}",
      "other": "{prefix}&7{player}'s balance: &a{balance}"
    },
    "pay": {
      "sent": "{prefix}&aYou sent &e{amount} &ato &e{player}",
      "received": "{prefix}&aYou received &e{amount} &afrom &e{player}",
      "self": "{prefix}&cYou cannot pay yourself!",
      "insufficient": "{prefix}&cInsufficient funds! You have &e{balance}"
    },
    "baltop": {
      "header": "&e&l=== Balance Top (Page {page}) ===",
      "entry": "&e{rank}. &f{player} &7- &a{balance}",
      "empty": "{prefix}&7No players found."
    },
    "admin": {
      "give": "{prefix}&aGave &e{amount} &ato &e{player}",
      "take": "{prefix}&aTook &e{amount} &afrom &e{player}",
      "set": "{prefix}&aSet &e{player}'s &abalance to &e{amount}",
      "reset": "{prefix}&aReset &e{player}'s &abalance"
    }
  },
  "errors": {
    "player_not_found": "{prefix}&cPlayer &e{player} &cnot found!",
    "invalid_amount": "{prefix}&cInvalid amount: &e{input}",
    "no_permission": "{prefix}&cYou don't have permission to do that.",
    "usage": "{prefix}&cUsage: &e{usage}"
  }
}
```

**Usage in code:**

```java
// Simple message
Message msg = LocaleMan.get(playerRef, "messages.balance.self",
    Map.of("balance", econ.format(balance)));
player.sendMessage(msg);

// With multiple placeholders
Message msg = LocaleMan.get(playerRef, "messages.pay.sent",
    Map.of(
        "amount", econ.format(amount),
        "player", target.getName()
    ));
```

---

## Package Structure

```
com.kukso.hy.eco/
├── Main.java                    # Plugin entry point
├── EcoProvider.java             # Economy interface implementation
├── config/
│   └── EcoConfig.java           # Configuration loader
├── command/
│   ├── EcoCmdManager.java       # Command dispatcher
│   └── sub/
│       ├── BalanceCmd.java      # /balance command
│       ├── PayCmd.java          # /pay command
│       ├── BaltopCmd.java       # /baltop command
│       └── admin/
│           ├── GiveCmd.java     # /eco give
│           ├── TakeCmd.java     # /eco take
│           ├── SetCmd.java      # /eco set
│           └── ResetCmd.java    # /eco reset
└── listener/
    └── PlayerJoinListener.java  # Welcome message, account creation
```

---

## Main Plugin Class

```java
package com.kukso.hy.eco;

import com.kukso.hy.lib.economy.CurrencyManager;
import com.kukso.hy.lib.service.ServiceManager;
import com.kukso.hy.lib.service.ServicePriority;
import hytale.server.plugin.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private EcoProvider economyProvider;
    private EcoConfig config;

    @Override
    protected void setup() {
        instance = this;

        // Load configuration
        config = new EcoConfig(this);
        config.load();

        // Register currencies from config
        config.registerCurrencies();

        // Create and register economy provider
        economyProvider = new EcoProvider(this);
        ServiceManager.registerEconomy(this, economyProvider, ServicePriority.NORMAL);

        // Register commands
        EcoCmdRegistrar.register(this);
    }

    @Override
    protected void start() {
        getLogger().at(Level.INFO).log("KuksoHyEco enabled!");
    }

    @Override
    public void shutdown() {
        // Unregister currencies
        for (String currencyId : CurrencyManager.getCurrencyIds()) {
            CurrencyManager.unregister(currencyId);
        }

        // Unregister as provider
        ServiceManager.unregisterAll(this);

        getLogger().at(Level.INFO).log("KuksoHyEco disabled!");
    }

    public static Main getInstance() { return instance; }
    public EcoConfig getEcoConfig() { return config; }
}
```

---

## Example Command Implementation

### BalanceCmd.java

```java
package com.kukso.hy.eco.command.sub;

import com.kukso.hy.lib.command.CmdInterface;
import com.kukso.hy.lib.locale.LocaleMan;
import com.kukso.hy.lib.service.ServiceManager;
import com.kukso.hy.lib.economy.Economy;
import hytale.server.world.GameMode;

public class BalanceCmd implements CmdInterface {

    @Override
    public String getName() { return "balance"; }

    @Override
    public List<String> getAliases() { return List.of("bal", "money"); }

    @Override
    public List<String> getPermissions() { return List.of("kuksoeco.balance"); }

    @Override
    public GameMode getPermissionGroup() {
        return GameMode.Adventure; // All players can use
    }

    @Override
    public String getDescription() { return "Check your balance"; }

    @Override
    public String getUsage() { return "/balance [player]"; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Economy econ = ServiceManager.getEconomy();
        PlayerRef playerRef = getPlayerRef(sender);

        if (args.length == 0) {
            // Show own balance
            double balance = econ.getBalance(getPlayer(sender));
            Message msg = LocaleMan.get(playerRef, "messages.balance.self",
                Map.of("balance", econ.format(balance)));
            sender.sendMessage(msg);
        } else {
            // Show another player's balance
            Player target = findPlayer(args[0]);
            if (target == null) {
                Message msg = LocaleMan.get(playerRef, "errors.player_not_found",
                    Map.of("player", args[0]));
                sender.sendMessage(msg);
                return false;
            }

            double balance = econ.getBalance(target);
            Message msg = LocaleMan.get(playerRef, "messages.balance.other",
                Map.of(
                    "player", target.getName(),
                    "balance", econ.format(balance)
                ));
            sender.sendMessage(msg);
        }

        return true;
    }
}
```

### PayCmd.java

```java
package com.kukso.hy.eco.command.sub;

public class PayCmd implements CmdInterface {

    @Override
    public String getName() { return "pay"; }

    @Override
    public List<String> getAliases() { return List.of("transfer"); }

    @Override
    public List<String> getPermissions() { return List.of("kuksoeco.pay"); }

    @Override
    public GameMode getPermissionGroup() {
        return GameMode.Adventure;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Message msg = LocaleMan.get(playerRef, "errors.usage",
                Map.of("usage", "/pay <player> <amount>"));
            sender.sendMessage(msg);
            return false;
        }

        Player target = findPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(LocaleMan.get(playerRef, "errors.player_not_found",
                Map.of("player", args[0])));
            return false;
        }

        // Prevent self-payment
        if (target.equals(getPlayer(sender))) {
            sender.sendMessage(LocaleMan.get(playerRef, "messages.pay.self"));
            return false;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(LocaleMan.get(playerRef, "errors.invalid_amount",
                Map.of("input", args[1])));
            return false;
        }

        Economy econ = ServiceManager.getEconomy();
        Player player = getPlayer(sender);

        // Check sufficient funds
        if (!econ.has(player, amount)) {
            sender.sendMessage(LocaleMan.get(playerRef, "messages.pay.insufficient",
                Map.of("balance", econ.format(econ.getBalance(player)))));
            return false;
        }

        // Perform transfer
        EconomyResponse response = econ.transfer(player, target, amount);
        if (response.isSuccess()) {
            // Notify sender
            sender.sendMessage(LocaleMan.get(playerRef, "messages.pay.sent",
                Map.of(
                    "amount", econ.format(amount),
                    "player", target.getName()
                )));

            // Notify recipient
            target.sendMessage(LocaleMan.get(getPlayerRef(target), "messages.pay.received",
                Map.of(
                    "amount", econ.format(amount),
                    "player", player.getName()
                )));
        }

        return true;
    }
}
```

---

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `kuksoeco.balance` | Check own balance | All players |
| `kuksoeco.balance.others` | Check other players' balances | All players |
| `kuksoeco.pay` | Send money to other players | All players |
| `kuksoeco.baltop` | View balance leaderboard | All players |
| `kuksoeco.admin` | All admin commands | OP |
| `kuksoeco.admin.give` | Give money to players | OP |
| `kuksoeco.admin.take` | Take money from players | OP |
| `kuksoeco.admin.set` | Set player balances | OP |
| `kuksoeco.admin.reset` | Reset player balances | OP |
| `kuksoeco.admin.reload` | Reload configuration | OP |

---

## Next Steps

1. Create a new project/repository for the actual plugin implementation
2. Set up Gradle build with KuksoLib dependency
3. Implement the core classes (Main, EcoProvider, EcoConfig)
4. Implement commands following the CmdInterface pattern
5. Create locale files for supported languages
6. Test integration with KuksoLib APIs
