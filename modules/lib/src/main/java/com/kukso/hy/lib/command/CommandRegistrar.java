package com.kukso.hy.lib.command;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.PluginBase;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Utility class for registering commands with a Hytale plugin.
 * Supports both standalone commands and parent-child (tree) command structures.
 *
 * <h2>Standalone Commands</h2>
 * <p>For commands like /warps, /spawn, /home:</p>
 * <pre>{@code
 * CommandRegistrar.standalone(plugin, new WarpsCmd());
 * CommandRegistrar.standalone(plugin, new SpawnCmd(), "myspawn.admin");
 * }</pre>
 *
 * <h2>Tree Commands (Parent-Child)</h2>
 * <p>For commands like /myplugin help, /myplugin reload:</p>
 * <pre>{@code
 * TreeManager manager = CommandRegistrar.tree(plugin, "myplugin", "My plugin commands");
 * manager.register(new HelpCmd());
 * manager.register(new ReloadCmd());
 * }</pre>
 */
public final class CommandRegistrar {

    private CommandRegistrar() {
        // Utility class
    }

    // ==================== Standalone Commands ====================

    /**
     * Registers a standalone command with the plugin.
     * Uses the default admin permission pattern: "{commandName}.admin".
     *
     * @param plugin The plugin to register with
     * @param cmd    The command to register
     */
    public static void standalone(@Nonnull PluginBase plugin, @Nonnull CommandInterface cmd) {
        Objects.requireNonNull(plugin, "Plugin cannot be null");
        Objects.requireNonNull(cmd, "Command cannot be null");

        String adminPermission = cmd.getName() + ".admin";
        CommandSingleBase base = new CommandSingleBase(cmd, adminPermission);
        plugin.getCommandRegistry().registerCommand(base);

        HytaleLogger logger = plugin.getLogger();
        logger.atInfo().log("Registered command: /" + cmd.getName() +
            (cmd.getAliases().isEmpty() ? "" : " (aliases: " + String.join(", ", cmd.getAliases()) + ")"));
    }

    /**
     * Registers a standalone command with a custom admin permission.
     *
     * @param plugin          The plugin to register with
     * @param cmd             The command to register
     * @param adminPermission The admin permission node for OP-only access
     */
    public static void standalone(@Nonnull PluginBase plugin, @Nonnull CommandInterface cmd,
                                  @Nonnull String adminPermission) {
        Objects.requireNonNull(plugin, "Plugin cannot be null");
        Objects.requireNonNull(cmd, "Command cannot be null");
        Objects.requireNonNull(adminPermission, "Admin permission cannot be null");

        CommandSingleBase base = new CommandSingleBase(cmd, adminPermission);
        plugin.getCommandRegistry().registerCommand(base);

        HytaleLogger logger = plugin.getLogger();
        logger.atInfo().log("Registered command: /" + cmd.getName() +
            (cmd.getAliases().isEmpty() ? "" : " (aliases: " + String.join(", ", cmd.getAliases()) + ")"));
    }

    /**
     * Registers multiple standalone commands at once.
     * Uses the default admin permission pattern for each: "{commandName}.admin".
     *
     * @param plugin   The plugin to register with
     * @param commands The commands to register
     */
    public static void standaloneAll(@Nonnull PluginBase plugin, @Nonnull CommandInterface... commands) {
        Objects.requireNonNull(plugin, "Plugin cannot be null");
        Objects.requireNonNull(commands, "Commands cannot be null");

        for (CommandInterface cmd : commands) {
            standalone(plugin, cmd);
        }
    }

    // ==================== Tree Commands (Parent-Child) ====================

    /**
     * Creates and registers a tree command manager for parent-child command structures.
     * Uses the default admin permission pattern: "{name}.admin".
     *
     * <p>Example:</p>
     * <pre>{@code
     * TreeManager manager = CommandRegistrar.tree(plugin, "myplugin", "My plugin commands");
     * manager.register(new HelpCmd());
     * manager.register(new ReloadCmd());
     * }</pre>
     *
     * @param plugin      The plugin to register with
     * @param name        The parent command name (e.g., "myplugin")
     * @param description The command description
     * @return The TreeManager to register subcommands with
     */
    public static CommandTreeBase tree(@Nonnull PluginBase plugin, @Nonnull String name,
                                       @Nonnull String description) {
        return tree(plugin, name, description, name + ".admin", new String[0]);
    }

    /**
     * Creates and registers a tree command manager with aliases.
     * Uses the default admin permission pattern: "{name}.admin".
     *
     * @param plugin      The plugin to register with
     * @param name        The parent command name (e.g., "myplugin")
     * @param description The command description
     * @param aliases     Aliases for the parent command
     * @return The TreeManager to register subcommands with
     */
    public static CommandTreeBase treeWithAliases(@Nonnull PluginBase plugin, @Nonnull String name,
                                                  @Nonnull String description, @Nonnull String... aliases) {
        return tree(plugin, name, description, name + ".admin", aliases);
    }

    /**
     * Creates and registers a tree command manager with a custom admin permission and aliases.
     *
     * @param plugin          The plugin to register with
     * @param name            The parent command name (e.g., "myplugin")
     * @param description     The command description
     * @param adminPermission The admin permission node for OP-only subcommands
     * @param aliases         Aliases for the parent command
     * @return The TreeManager to register subcommands with
     */
    public static CommandTreeBase tree(@Nonnull PluginBase plugin, @Nonnull String name,
                                       @Nonnull String description, @Nonnull String adminPermission,
                                       @Nonnull String... aliases) {
        Objects.requireNonNull(plugin, "Plugin cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(adminPermission, "Admin permission cannot be null");

        CommandTreeBase manager = new CommandTreeBase(name, description, adminPermission, aliases);
        plugin.getCommandRegistry().registerCommand(manager);

        HytaleLogger logger = plugin.getLogger();
        logger.atInfo().log("Registered tree command: /" + name +
            (aliases != null && aliases.length > 0 ? " (aliases: " + String.join(", ", aliases) + ")" : ""));

        return manager;
    }
}
