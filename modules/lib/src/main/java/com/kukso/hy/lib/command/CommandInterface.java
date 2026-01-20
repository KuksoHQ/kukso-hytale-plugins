package com.kukso.hy.lib.command;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Interface for all command types - both standalone commands (e.g., /warps)
 * and subcommands under a parent (e.g., /plugin help).
 *
 * <p>Implement this interface to create commands that can be registered with
 * {@link CommandRegistrar}.</p>
 *
 * <p>Example standalone command:</p>
 * <pre>{@code
 * public class WarpsCmd implements CommandInterface {
 *     @Override
 *     public String getName() { return "warps"; }
 *
 *     @Override
 *     public List<String> getAliases() { return List.of("warp", "w"); }
 *
 *     @Override
 *     public boolean execute(CommandSender sender, String[] args) {
 *         // Handle /warps command
 *         return true;
 *     }
 * }
 *
 * // Register as standalone:
 * CommandRegistrar.standalone(plugin, new WarpsCmd());
 * }</pre>
 *
 * <p>Example subcommand:</p>
 * <pre>{@code
 * public class HelpCmd implements CommandInterface {
 *     @Override
 *     public String getName() { return "help"; }
 *
 *     @Override
 *     public boolean execute(CommandSender sender, String[] args) {
 *         // Handle /parent help command
 *         return true;
 *     }
 * }
 *
 * // Register under a parent:
 * TreeManager manager = CommandRegistrar.tree(plugin, "myplugin", "My plugin commands");
 * manager.register(new HelpCmd());
 * }</pre>
 */
public interface CommandInterface {

    String DEFAULT_DESCRIPTION = "No description provided.";

    /**
     * Gets the primary name of this command (without the leading /).
     *
     * @return The command's name (e.g., "warps" for /warps, or "help" for a subcommand)
     */
    String getName();

    /**
     * Gets alternative names (aliases) for this command.
     * Players can use any of these instead of the primary name.
     *
     * @return List of command aliases, or empty list if none
     */
    default List<String> getAliases() {
        return Collections.emptyList();
    }

    /**
     * Gets the list of permissions required to use this command.
     * If multiple permissions are listed, having ANY one grants access.
     *
     * @return List of permission strings, or empty list if no specific permission required
     */
    default List<String> getPermissions() {
        return Collections.emptyList();
    }

    /**
     * Gets the permission group for this command.
     * Controls the base access level before checking specific permissions.
     *
     * <ul>
     *   <li>{@code null} (default) - Requires admin permission (OP-only)</li>
     *   <li>{@code GameMode.Adventure} - All players can access</li>
     * </ul>
     *
     * @return The permission group, or null for OP-only
     */
    default GameMode getPermissionGroup() {
        return null;
    }

    /**
     * Gets the command description.
     * Used in help messages and command listings.
     *
     * @return Command description
     */
    default String getDescription() {
        return DEFAULT_DESCRIPTION;
    }

    /**
     * Gets the command usage syntax.
     * Shown when the command is used incorrectly.
     *
     * @return Command usage string (e.g., "/warps [name]")
     */
    default String getUsage() {
        return "/" + getName();
    }

    /**
     * Provides tab completion suggestions for this command.
     * Called when the player presses tab while typing this command.
     *
     * <p>Note: Permission checking is handled before this method is called.</p>
     *
     * @param sender The command sender
     * @param args   Current command arguments (after the command name)
     * @return List of suggestions, or empty list for none
     */
    default List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    /**
     * Executes the command logic.
     * Called when a player or console runs this command.
     *
     * <p>Note: Permission checking is handled before this method is called.</p>
     *
     * @param sender The command sender (player or console)
     * @param args   The command arguments (everything after the command name)
     * @return true if the command was executed successfully
     */
    boolean execute(CommandSender sender, String[] args);
}
