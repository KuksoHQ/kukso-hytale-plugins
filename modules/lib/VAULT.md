# VAULT.md - KuksoLib Service Provider API

This document outlines the design and implementation plan for a **Vault-like abstraction layer** in KuksoLib for Hytale. Similar to how Vault provides unified APIs for Minecraft plugins, KuksoLib will provide standardized interfaces for Economy, Permissions, and Chat systems.

## Overview

### What is Vault?

Vault is a Minecraft plugin that provides abstraction APIs for:
- **Economy** - Currency management, transactions, bank accounts
- **Permissions** - Permission node checking, group management
- **Chat** - Prefix/suffix management, chat formatting

Plugins depend on Vault's API instead of individual economy/permission plugins, allowing server admins to swap backends without breaking functionality.

### KuksoLib's Goal

KuksoLib will provide the same abstraction layer for Hytale:
- **Single dependency** - Plugins only depend on KuksoLib
- **Swappable backends** - Server admins choose their preferred economy/permission/chat plugins
- **Standardized API** - Consistent method signatures across all implementations
- **Response objects** - Rich feedback for transactions and operations

---

## Architecture

### Service Provider Pattern

```
┌─────────────────────────────────────────────────────────────────┐
│                        YOUR PLUGIN                              │
│                   (depends on KuksoLib API)                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         KUKSOLIB                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ Economy API │  │Permission API│  │   Chat API  │             │
│  │  Interface  │  │   Interface  │  │  Interface  │             │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
│         │                │                │                     │
│         ▼                ▼                ▼                     │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │              ServiceManager (Registry)                      ││
│  │   - Registers provider implementations                      ││
│  │   - Priority-based provider selection                       ││
│  │   - Runtime provider switching                              ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│  KuksoEconomy   │ │   LuckPerms     │ │   ChatManager   │
│  (Built-in)     │ │   (External)    │ │   (External)    │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

### Package Structure

```
com.kukso.hy.lib.service/
├── ServiceManager.java          # Central registry for all services
├── ServicePriority.java         # Priority enum (Lowest, Low, Normal, High, Highest)
├── ServiceProvider.java         # Wrapper class for registered providers
│
├── economy/
│   ├── Economy.java             # Main economy interface
│   ├── EconomyResponse.java     # Transaction response object
│   ├── ResponseType.java        # SUCCESS, FAILURE, NOT_IMPLEMENTED
│   └── impl/
│       └── KuksoEconomy.java    # Built-in SQLite/MySQL implementation
│
├── permission/
│   ├── Permission.java          # Main permission interface
│   └── impl/
│       └── DefaultPermission.java  # Fallback using Hytale's native system
│
└── chat/
    ├── Chat.java                # Main chat interface
    └── impl/
        └── DefaultChat.java     # Fallback using ColorMan
```

---

## Economy API

### Interface Definition

```java
package com.kukso.hy.lib.service.economy;

import hytale.server.world.entity.player.Player;
import java.util.List;
import java.util.UUID;

/**
 * The main Economy interface that all economy providers must implement.
 * Plugins should use ServiceManager.getEconomy() to obtain an instance.
 */
public interface Economy {

    // ==================== Provider Info ====================

    /**
     * Gets the name of the economy provider.
     * @return Name of the economy provider
     */
    String getName();

    /**
     * Checks if this economy provider is enabled and ready.
     * @return true if the economy provider is enabled
     */
    boolean isEnabled();

    // ==================== Currency Info ====================

    /**
     * Gets the name of the currency in singular form.
     * @return Currency name singular (e.g., "Dollar", "Coin", "Gold")
     */
    String currencyNameSingular();

    /**
     * Gets the name of the currency in plural form.
     * @return Currency name plural (e.g., "Dollars", "Coins", "Gold")
     */
    String currencyNamePlural();

    /**
     * Format the amount into a human-readable string.
     * @param amount Amount to format
     * @return Formatted string (e.g., "$1,234.56" or "1,234 Coins")
     */
    String format(double amount);

    /**
     * Returns the number of fractional digits supported.
     * @return Number of digits after decimal (0 for whole numbers only)
     */
    int fractionalDigits();

    // ==================== Account Methods ====================

    /**
     * Checks if a player has an account.
     * @param player Player to check
     * @return true if player has an account
     */
    boolean hasAccount(Player player);

    /**
     * Checks if a player has an account by UUID (for offline players).
     * @param uuid Player UUID
     * @return true if player has an account
     */
    boolean hasAccount(UUID uuid);

    /**
     * Creates a player account if one doesn't exist.
     * @param player Player to create account for
     * @return true if account was created, false if already exists
     */
    boolean createPlayerAccount(Player player);

    // ==================== Balance Methods ====================

    /**
     * Gets the balance of a player.
     * @param player Player to check
     * @return Current balance
     */
    double getBalance(Player player);

    /**
     * Gets the balance of a player by UUID (for offline players).
     * @param uuid Player UUID
     * @return Current balance
     */
    double getBalance(UUID uuid);

    /**
     * Checks if a player has at least the specified amount.
     * @param player Player to check
     * @param amount Amount to check
     * @return true if player has at least that amount
     */
    boolean has(Player player, double amount);

    // ==================== Transaction Methods ====================

    /**
     * Withdraws an amount from a player's account.
     * @param player Player to withdraw from
     * @param amount Amount to withdraw (must be positive)
     * @return EconomyResponse with transaction result
     */
    EconomyResponse withdraw(Player player, double amount);

    /**
     * Deposits an amount into a player's account.
     * @param player Player to deposit to
     * @param amount Amount to deposit (must be positive)
     * @return EconomyResponse with transaction result
     */
    EconomyResponse deposit(Player player, double amount);

    /**
     * Transfers money from one player to another.
     * @param from Source player
     * @param to Destination player
     * @param amount Amount to transfer
     * @return EconomyResponse with transaction result
     */
    default EconomyResponse transfer(Player from, Player to, double amount) {
        EconomyResponse withdrawResponse = withdraw(from, amount);
        if (!withdrawResponse.isSuccess()) {
            return withdrawResponse;
        }
        EconomyResponse depositResponse = deposit(to, amount);
        if (!depositResponse.isSuccess()) {
            // Rollback the withdrawal
            deposit(from, amount);
            return depositResponse;
        }
        return new EconomyResponse(
            amount,
            getBalance(from),
            ResponseType.SUCCESS,
            "Transfer successful"
        );
    }

    // ==================== Bank Methods (Optional) ====================

    /**
     * Checks if this economy supports banks.
     * @return true if banks are supported
     */
    default boolean hasBankSupport() {
        return false;
    }

    /**
     * Creates a bank with the given name.
     * @param name Bank name
     * @param owner Owner player
     * @return EconomyResponse with result
     */
    default EconomyResponse createBank(String name, Player owner) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    /**
     * Deletes a bank.
     * @param name Bank name
     * @return EconomyResponse with result
     */
    default EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    /**
     * Gets the balance of a bank.
     * @param name Bank name
     * @return EconomyResponse with balance
     */
    default EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    /**
     * Checks if a bank exists.
     * @param name Bank name
     * @return true if bank exists
     */
    default boolean bankExists(String name) {
        return false;
    }

    /**
     * Withdraws from a bank.
     * @param name Bank name
     * @param amount Amount to withdraw
     * @return EconomyResponse with result
     */
    default EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    /**
     * Deposits to a bank.
     * @param name Bank name
     * @param amount Amount to deposit
     * @return EconomyResponse with result
     */
    default EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    /**
     * Checks if a player is the owner of a bank.
     * @param name Bank name
     * @param player Player to check
     * @return true if player is the bank owner
     */
    default boolean isBankOwner(String name, Player player) {
        return false;
    }

    /**
     * Checks if a player is a member of a bank.
     * @param name Bank name
     * @param player Player to check
     * @return true if player is a bank member
     */
    default boolean isBankMember(String name, Player player) {
        return false;
    }

    /**
     * Gets list of all banks.
     * @return List of bank names
     */
    default List<String> getBanks() {
        return List.of();
    }
}
```

### Response Object

```java
package com.kukso.hy.lib.service.economy;

/**
 * Represents the result of an economy transaction.
 */
public class EconomyResponse {
    private final double amount;
    private final double balance;
    private final ResponseType type;
    private final String errorMessage;

    public EconomyResponse(double amount, double balance, ResponseType type, String errorMessage) {
        this.amount = amount;
        this.balance = balance;
        this.type = type;
        this.errorMessage = errorMessage;
    }

    /**
     * Amount that was transacted.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * New balance after transaction.
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Response type (SUCCESS, FAILURE, NOT_IMPLEMENTED).
     */
    public ResponseType getType() {
        return type;
    }

    /**
     * Error message if transaction failed.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Convenience method to check if transaction was successful.
     */
    public boolean isSuccess() {
        return type == ResponseType.SUCCESS;
    }
}

/**
 * Response type enumeration.
 */
public enum ResponseType {
    SUCCESS,           // Transaction completed successfully
    FAILURE,           // Transaction failed (insufficient funds, etc.)
    NOT_IMPLEMENTED    // Feature not supported by provider
}
```

---

## Permission API

### Interface Definition

```java
package com.kukso.hy.lib.service.permission;

import hytale.server.world.entity.player.Player;
import java.util.List;
import java.util.UUID;

/**
 * The main Permission interface for permission providers.
 * Plugins should use ServiceManager.getPermission() to obtain an instance.
 */
public interface Permission {

    // ==================== Provider Info ====================

    /**
     * Gets the name of the permission provider.
     * @return Name of the permission provider
     */
    String getName();

    /**
     * Checks if this permission provider is enabled.
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Checks if this provider supports groups.
     * @return true if groups are supported
     */
    boolean hasGroupSupport();

    /**
     * Checks if this provider supports superperms bridge.
     * @return true if superperms is supported
     */
    boolean hasSuperPermsCompat();

    // ==================== Permission Check ====================

    /**
     * Checks if a player has a permission node.
     * @param player Player to check
     * @param permission Permission node
     * @return true if player has the permission
     */
    boolean has(Player player, String permission);

    /**
     * Checks if an offline player has a permission node.
     * @param uuid Player UUID
     * @param permission Permission node
     * @return true if player has the permission
     */
    boolean has(UUID uuid, String permission);

    /**
     * Checks if a player has a permission in a specific world.
     * @param player Player to check
     * @param world World name
     * @param permission Permission node
     * @return true if player has the permission in that world
     */
    default boolean has(Player player, String world, String permission) {
        return has(player, permission);
    }

    // ==================== Permission Add/Remove ====================

    /**
     * Adds a permission to a player.
     * @param player Player to modify
     * @param permission Permission to add
     * @return true if successful
     */
    boolean playerAdd(Player player, String permission);

    /**
     * Removes a permission from a player.
     * @param player Player to modify
     * @param permission Permission to remove
     * @return true if successful
     */
    boolean playerRemove(Player player, String permission);

    /**
     * Adds a temporary permission to a player.
     * @param player Player to modify
     * @param permission Permission to add
     * @param seconds Duration in seconds
     * @return true if successful
     */
    default boolean playerAddTransient(Player player, String permission, long seconds) {
        return false; // Not all providers support this
    }

    // ==================== Group Methods ====================

    /**
     * Gets all groups defined in the permission system.
     * @return Array of group names
     */
    String[] getGroups();

    /**
     * Gets the primary group of a player.
     * @param player Player to check
     * @return Primary group name, or empty string if none
     */
    String getPrimaryGroup(Player player);

    /**
     * Gets all groups a player is in.
     * @param player Player to check
     * @return Array of group names
     */
    String[] getPlayerGroups(Player player);

    /**
     * Checks if a player is in a specific group.
     * @param player Player to check
     * @param group Group name
     * @return true if player is in the group
     */
    boolean playerInGroup(Player player, String group);

    /**
     * Adds a player to a group.
     * @param player Player to add
     * @param group Group name
     * @return true if successful
     */
    boolean playerAddGroup(Player player, String group);

    /**
     * Removes a player from a group.
     * @param player Player to remove
     * @param group Group name
     * @return true if successful
     */
    boolean playerRemoveGroup(Player player, String group);

    // ==================== Group Permission Methods ====================

    /**
     * Checks if a group has a permission.
     * @param group Group name
     * @param permission Permission node
     * @return true if group has the permission
     */
    default boolean groupHas(String group, String permission) {
        return false;
    }

    /**
     * Adds a permission to a group.
     * @param group Group name
     * @param permission Permission to add
     * @return true if successful
     */
    default boolean groupAdd(String group, String permission) {
        return false;
    }

    /**
     * Removes a permission from a group.
     * @param group Group name
     * @param permission Permission to remove
     * @return true if successful
     */
    default boolean groupRemove(String group, String permission) {
        return false;
    }
}
```

---

## Chat API

### Interface Definition

```java
package com.kukso.hy.lib.service.chat;

import hytale.server.world.entity.player.Player;
import java.util.UUID;

/**
 * The main Chat interface for chat formatting providers.
 * Plugins should use ServiceManager.getChat() to obtain an instance.
 */
public interface Chat {

    // ==================== Provider Info ====================

    /**
     * Gets the name of the chat provider.
     * @return Name of the chat provider
     */
    String getName();

    /**
     * Checks if this chat provider is enabled.
     * @return true if enabled
     */
    boolean isEnabled();

    // ==================== Prefix/Suffix ====================

    /**
     * Gets the prefix for a player.
     * @param player Player to check
     * @return Player's prefix, or empty string if none
     */
    String getPlayerPrefix(Player player);

    /**
     * Gets the suffix for a player.
     * @param player Player to check
     * @return Player's suffix, or empty string if none
     */
    String getPlayerSuffix(Player player);

    /**
     * Sets the prefix for a player.
     * @param player Player to modify
     * @param prefix New prefix
     */
    void setPlayerPrefix(Player player, String prefix);

    /**
     * Sets the suffix for a player.
     * @param player Player to modify
     * @param suffix New suffix
     */
    void setPlayerSuffix(Player player, String suffix);

    // ==================== Group Prefix/Suffix ====================

    /**
     * Gets the prefix for a group.
     * @param group Group name
     * @return Group's prefix, or empty string if none
     */
    String getGroupPrefix(String group);

    /**
     * Gets the suffix for a group.
     * @param group Group name
     * @return Group's suffix, or empty string if none
     */
    String getGroupSuffix(String group);

    /**
     * Sets the prefix for a group.
     * @param group Group name
     * @param prefix New prefix
     */
    void setGroupPrefix(String group, String prefix);

    /**
     * Sets the suffix for a group.
     * @param group Group name
     * @param suffix New suffix
     */
    void setGroupSuffix(String group, String suffix);

    // ==================== Display Name ====================

    /**
     * Gets the display name for a player (with formatting).
     * @param player Player to check
     * @return Formatted display name
     */
    default String getPlayerDisplayName(Player player) {
        String prefix = getPlayerPrefix(player);
        String suffix = getPlayerSuffix(player);
        String name = player.getName();
        return prefix + name + suffix;
    }

    // ==================== Meta Data (Optional) ====================

    /**
     * Gets a metadata value for a player.
     * @param player Player to check
     * @param key Metadata key
     * @return Metadata value, or null if not set
     */
    default String getPlayerMeta(Player player, String key) {
        return null;
    }

    /**
     * Sets a metadata value for a player.
     * @param player Player to modify
     * @param key Metadata key
     * @param value Metadata value
     */
    default void setPlayerMeta(Player player, String key, String value) {
        // No-op by default
    }

    /**
     * Gets a metadata value for a group.
     * @param group Group name
     * @param key Metadata key
     * @return Metadata value, or null if not set
     */
    default String getGroupMeta(String group, String key) {
        return null;
    }

    /**
     * Sets a metadata value for a group.
     * @param group Group name
     * @param key Metadata key
     * @param value Metadata value
     */
    default void setGroupMeta(String group, String key, String value) {
        // No-op by default
    }
}
```

---

## Service Manager

### Central Registry

```java
package com.kukso.hy.lib.service;

import com.kukso.hy.lib.service.economy.Economy;
import com.kukso.hy.lib.service.permission.Permission;
import com.kukso.hy.lib.service.chat.Chat;
import hytale.server.plugin.JavaPlugin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Central registry for all KuksoLib services.
 * Manages registration, priority, and retrieval of service providers.
 */
public class ServiceManager {

    private static final Map<Class<?>, TreeSet<ServiceProvider<?>>> providers = new ConcurrentHashMap<>();

    // ==================== Registration ====================

    /**
     * Registers an economy provider.
     * @param plugin Plugin registering the provider
     * @param provider Economy implementation
     * @param priority Registration priority
     */
    public static void registerEconomy(JavaPlugin plugin, Economy provider, ServicePriority priority) {
        register(Economy.class, plugin, provider, priority);
    }

    /**
     * Registers a permission provider.
     * @param plugin Plugin registering the provider
     * @param provider Permission implementation
     * @param priority Registration priority
     */
    public static void registerPermission(JavaPlugin plugin, Permission provider, ServicePriority priority) {
        register(Permission.class, plugin, provider, priority);
    }

    /**
     * Registers a chat provider.
     * @param plugin Plugin registering the provider
     * @param provider Chat implementation
     * @param priority Registration priority
     */
    public static void registerChat(JavaPlugin plugin, Chat provider, ServicePriority priority) {
        register(Chat.class, plugin, provider, priority);
    }

    /**
     * Generic service registration method.
     */
    private static <T> void register(Class<T> serviceClass, JavaPlugin plugin, T provider, ServicePriority priority) {
        providers.computeIfAbsent(serviceClass, k -> new TreeSet<>((a, b) -> {
            // Higher priority first
            int priorityCompare = Integer.compare(b.getPriority().ordinal(), a.getPriority().ordinal());
            if (priorityCompare != 0) return priorityCompare;
            // Then by plugin name for consistency
            return a.getPlugin().getName().compareTo(b.getPlugin().getName());
        })).add(new ServiceProvider<>(plugin, provider, priority));

        plugin.getLogger().at(Level.INFO).log(
            "Registered " + serviceClass.getSimpleName() + " provider: " +
            provider.getClass().getSimpleName() + " (priority: " + priority + ")"
        );
    }

    // ==================== Retrieval ====================

    /**
     * Gets the highest priority economy provider.
     * @return Economy instance, or null if none registered
     */
    public static Economy getEconomy() {
        return getProvider(Economy.class);
    }

    /**
     * Gets the highest priority permission provider.
     * @return Permission instance, or null if none registered
     */
    public static Permission getPermission() {
        return getProvider(Permission.class);
    }

    /**
     * Gets the highest priority chat provider.
     * @return Chat instance, or null if none registered
     */
    public static Chat getChat() {
        return getProvider(Chat.class);
    }

    /**
     * Generic provider retrieval.
     */
    @SuppressWarnings("unchecked")
    private static <T> T getProvider(Class<T> serviceClass) {
        TreeSet<ServiceProvider<?>> set = providers.get(serviceClass);
        if (set == null || set.isEmpty()) {
            return null;
        }
        return (T) set.first().getProvider();
    }

    /**
     * Gets all registered providers for a service type.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getProviders(Class<T> serviceClass) {
        TreeSet<ServiceProvider<?>> set = providers.get(serviceClass);
        if (set == null) {
            return List.of();
        }
        List<T> result = new ArrayList<>();
        for (ServiceProvider<?> sp : set) {
            result.add((T) sp.getProvider());
        }
        return result;
    }

    // ==================== Unregistration ====================

    /**
     * Unregisters all services from a plugin.
     * @param plugin Plugin to unregister
     */
    public static void unregisterAll(JavaPlugin plugin) {
        for (TreeSet<ServiceProvider<?>> set : providers.values()) {
            set.removeIf(sp -> sp.getPlugin().equals(plugin));
        }
    }

    /**
     * Clears all registered services (for shutdown).
     */
    public static void shutdown() {
        providers.clear();
    }

    // ==================== Status ====================

    /**
     * Checks if any economy provider is registered.
     */
    public static boolean hasEconomy() {
        return getEconomy() != null;
    }

    /**
     * Checks if any permission provider is registered.
     */
    public static boolean hasPermission() {
        return getPermission() != null;
    }

    /**
     * Checks if any chat provider is registered.
     */
    public static boolean hasChat() {
        return getChat() != null;
    }
}
```

### Priority Enum

```java
package com.kukso.hy.lib.service;

/**
 * Priority levels for service registration.
 * Higher priority providers are preferred.
 */
public enum ServicePriority {
    LOWEST,    // Fallback/default implementations
    LOW,       // Low priority
    NORMAL,    // Standard priority
    HIGH,      // High priority
    HIGHEST    // Override all others
}
```

### Provider Wrapper

```java
package com.kukso.hy.lib.service;

import hytale.server.plugin.JavaPlugin;

/**
 * Wrapper for a registered service provider.
 */
public class ServiceProvider<T> {
    private final JavaPlugin plugin;
    private final T provider;
    private final ServicePriority priority;

    public ServiceProvider(JavaPlugin plugin, T provider, ServicePriority priority) {
        this.plugin = plugin;
        this.provider = provider;
        this.priority = priority;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public T getProvider() {
        return provider;
    }

    public ServicePriority getPriority() {
        return priority;
    }
}
```

---

## Usage Examples

### For Plugin Developers (Using KuksoLib API)

```java
import com.kukso.hy.lib.service.ServiceManager;
import com.kukso.hy.lib.service.economy.Economy;
import com.kukso.hy.lib.service.economy.EconomyResponse;
import com.kukso.hy.lib.service.permission.Permission;
import com.kukso.hy.lib.service.chat.Chat;

public class MyPlugin extends JavaPlugin {

    @Override
    public void start() {
        // Check if economy is available
        if (!ServiceManager.hasEconomy()) {
            getLogger().at(Level.WARNING).log("No economy provider found!");
            return;
        }

        Economy econ = ServiceManager.getEconomy();
        Permission perms = ServiceManager.getPermission();
        Chat chat = ServiceManager.getChat();

        // Example: Give player money
        Player player = /* get player */;
        EconomyResponse response = econ.deposit(player, 100.0);
        if (response.isSuccess()) {
            player.sendMessage("You received " + econ.format(100.0) + "!");
        } else {
            player.sendMessage("Error: " + response.getErrorMessage());
        }

        // Example: Check permission
        if (perms != null && perms.has(player, "myshop.admin")) {
            // Player has admin access
        }

        // Example: Get player display name
        if (chat != null) {
            String displayName = chat.getPlayerDisplayName(player);
            // Use displayName in chat messages
        }
    }
}
```

### For Provider Developers (Implementing the API)

```java
import com.kukso.hy.lib.service.ServiceManager;
import com.kukso.hy.lib.service.ServicePriority;
import com.kukso.hy.lib.service.economy.Economy;
import com.kukso.hy.lib.service.economy.EconomyResponse;
import com.kukso.hy.lib.service.economy.ResponseType;

public class MyEconomyPlugin extends JavaPlugin {

    @Override
    public void start() {
        // Register our economy implementation
        ServiceManager.registerEconomy(this, new MyEconomyImpl(), ServicePriority.NORMAL);
    }

    @Override
    public void shutdown() {
        // Unregister when shutting down
        ServiceManager.unregisterAll(this);
    }

    // Our economy implementation
    private class MyEconomyImpl implements Economy {
        @Override
        public String getName() {
            return "MyEconomyPlugin";
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public String currencyNameSingular() {
            return "Coin";
        }

        @Override
        public String currencyNamePlural() {
            return "Coins";
        }

        @Override
        public String format(double amount) {
            return String.format("%.2f Coins", amount);
        }

        @Override
        public int fractionalDigits() {
            return 2;
        }

        // ... implement remaining methods
    }
}
```

---

## Built-in Implementations

### KuksoEconomy (Built-in Economy)

KuksoLib provides a built-in economy implementation with:

- **SQLite backend** (default) - Zero configuration required
- **MySQL backend** (optional) - For multi-server setups
- **Configurable currency** - Name, symbol, decimal places
- **Transaction logging** - Full audit trail
- **Top balances** - `/balance top` support

Configuration (`plugins/KuksoLib/economy.yml`):
```yaml
# Database settings
database:
  type: sqlite  # or 'mysql'
  mysql:
    host: localhost
    port: 3306
    database: kuksolib
    username: root
    password: ""

# Currency settings
currency:
  name-singular: "Coin"
  name-plural: "Coins"
  symbol: "$"
  format: "{symbol}{amount}"  # e.g., "$1,234.56"
  decimal-places: 2

# Starting balance
starting-balance: 100.0

# Transaction logging
logging:
  enabled: true
  log-file: "transactions.log"
```

### DefaultPermission (Fallback Permission)

Uses Hytale's native permission system:
- Checks `Player.hasPermission()` directly
- No group support
- Basic permission checking only

### DefaultChat (Fallback Chat)

Uses ColorMan for basic formatting:
- No prefix/suffix storage
- Returns empty strings for missing data
- Integrates with ColorMan for color code translation

---

## Implementation Roadmap

### Phase 1: Core Infrastructure
- [ ] Implement `ServiceManager` with registration/retrieval
- [ ] Implement `ServicePriority` enum
- [ ] Implement `ServiceProvider` wrapper class
- [ ] Add service status commands to `/kuksolib`

### Phase 2: Economy API
- [ ] Define `Economy` interface
- [ ] Define `EconomyResponse` and `ResponseType`
- [ ] Implement `KuksoEconomy` with SQLite backend
- [ ] Add MySQL support to `KuksoEconomy`
- [ ] Add economy commands (`/balance`, `/pay`, `/eco`)
- [ ] Add transaction logging

### Phase 3: Permission API
- [ ] Define `Permission` interface
- [ ] Implement `DefaultPermission` fallback
- [ ] Add group support infrastructure
- [ ] Document integration with external permission plugins

### Phase 4: Chat API
- [ ] Define `Chat` interface
- [ ] Implement `DefaultChat` fallback
- [ ] Add prefix/suffix storage
- [ ] Integrate with LocaleMan for localized prefixes

### Phase 5: Testing & Documentation
- [ ] Write unit tests for all services
- [ ] Create example plugins demonstrating usage
- [ ] Write developer documentation
- [ ] Create migration guide from other economy plugins

---

## Permissions

KuksoLib service commands use the following permissions:

| Permission | Description | Default |
|------------|-------------|---------|
| `kuksolib.admin` | Full admin access | OP |
| `kuksolib.economy.balance` | View own balance | All |
| `kuksolib.economy.balance.others` | View others' balance | OP |
| `kuksolib.economy.pay` | Send money to players | All |
| `kuksolib.economy.admin` | Admin economy commands | OP |

---

## Events

KuksoLib will fire events that other plugins can listen to:

```java
// Economy events
EconomyTransactionEvent      // Fired on deposit/withdraw
EconomyTransferEvent         // Fired on player-to-player transfer
EconomyBalanceChangeEvent    // Fired when balance changes

// Service events
ServiceProviderRegisterEvent // Fired when a provider registers
ServiceProviderChangeEvent   // Fired when active provider changes
```

---

## Differences from Minecraft Vault

| Feature | Vault (Minecraft) | KuksoLib (Hytale) |
|---------|-------------------|-------------------|
| Registration | Bukkit ServiceManager | KuksoLib ServiceManager |
| Thread Safety | Plugin-dependent | ConcurrentHashMap-based |
| Default Economy | None | Built-in SQLite economy |
| Localization | None | Integrated LocaleMan |
| Color Codes | Bukkit ChatColor | ColorMan (§ and & codes) |
| Bank Support | Full | Optional (via interface) |

---

## FAQ

**Q: Do I need to soft-depend on KuksoLib?**
A: Yes, if you want to use the services but not require them. Use `depend` if your plugin requires economy/permissions.

**Q: Can I have multiple economy plugins?**
A: Yes, but only the highest priority one will be used. Use `ServiceManager.getProviders(Economy.class)` to access all.

**Q: How do I migrate from another economy plugin?**
A: KuksoLib will provide migration commands for popular economy plugins once the ecosystem develops.

**Q: Is the economy persistent across server restarts?**
A: Yes, the built-in KuksoEconomy uses SQLite/MySQL for persistent storage.

---

## Contributing

To add support for a new economy/permission/chat plugin:

1. Create a bridge plugin that depends on both KuksoLib and the target plugin
2. Implement the appropriate interface (`Economy`, `Permission`, or `Chat`)
3. Register with `ServiceManager` in your plugin's `start()` method
4. Submit a PR to be listed as an official bridge

---

## License

This service provider API is part of KuksoLib and follows the same license.
