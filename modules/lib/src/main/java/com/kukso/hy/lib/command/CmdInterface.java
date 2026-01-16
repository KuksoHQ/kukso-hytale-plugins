package com.kukso.hy.lib.command;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Represents a subcommand that can be registered with the command manager.
 */
public interface CmdInterface {
    String DEFAULT_DESCRIPTION = "No description provided.";
    String COMMAND_PREFIX = "/";

    /**
     * Gets the primary name of this command.
     * @return The command's name
     */
    String getName();

    /**
     * Gets alternative names (aliases) for this command.
     * @return List of command aliases
     */
    List<String> getAliases();

    /**
     * Gets the list of permissions required to use this command.
     * @return List of permission strings
     */
    List<String> getPermissions();

    /**
     * Gets the permission group for this command.
     * By default, returns null (OP-only).
     * Return GameMode.Adventure to allow all players to use this command.
     * @return The permission group, or null for OP-only
     */
    default GameMode getPermissionGroup() {
        return null; // OP-only by default
    }

    /**
     * Gets the command description.
     * @return Command description
     */
    default String getDescription() {
        return DEFAULT_DESCRIPTION;
    }

    /**
     * Gets the command usage syntax.
     * @return Command usage string
     */
    default String getUsage() {
        return COMMAND_PREFIX + "kuksolib " + getName() + " (" + String.join("/", getAliases()) + ")";
    }

    /**
     * Provides tab completion suggestions for this command.
     * Note: Permission checking is handled by CmdManager before this method is called.
     *
     * @param sender The command sender
     * @param args Current command arguments
     * @return List of suggestions
     */
    List<String> tabComplete(CommandSender sender, String[] args);

    /**
     * Executes the command logic.
     * Note: Permission checking is handled by CmdManager before this method is called.
     *
     * @param sender The command sender
     * @param args The command arguments
     * @return true if the command was executed successfully
     */
    boolean execute(CommandSender sender, String[] args);
}
