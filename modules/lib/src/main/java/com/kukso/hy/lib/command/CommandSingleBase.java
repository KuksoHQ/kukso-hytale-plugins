package com.kukso.hy.lib.command;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import java.util.Arrays;
import java.util.List;

/**
 * Base class that wraps a {@link CommandInterface} implementation for standalone commands.
 * Handles permission checking, argument parsing, and command execution.
 *
 * <p>This class is package-private. Use {@link CommandRegistrar#standalone} to register commands.</p>
 */
class CommandSingleBase extends CommandBase {

    private final CommandInterface cmd;
    private final String adminPermission;

    /**
     * Creates a new StandaloneBase wrapping the given command.
     *
     * @param cmd             The command implementation to wrap
     * @param adminPermission The admin permission node for OP-only access
     */
    CommandSingleBase(CommandInterface cmd, String adminPermission) {
        super(cmd.getName(), cmd.getDescription());
        this.cmd = cmd;
        this.adminPermission = adminPermission;

        // Register aliases
        List<String> aliases = cmd.getAliases();
        if (aliases != null && !aliases.isEmpty()) {
            addAliases(aliases.toArray(new String[0]));
        }

        // Allow arguments after the command name
        setAllowsExtraArguments(true);
    }

    @Override
    protected void executeSync(CommandContext context) {
        CommandSender sender = context.sender();
        String input = context.getInputString();

        // Parse args from input (skip the command name itself)
        String[] parts = input != null ? input.trim().split("\\s+") : new String[0];
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        // Check permission group (OP requirement)
        if (!hasPermissionGroup(sender)) {
            context.sendMessage(Message.raw("You don't have permission to use this command.").color("#FF5555"));
            return;
        }

        // Check specific permissions
        if (!hasAnyPermission(sender, cmd.getPermissions())) {
            context.sendMessage(Message.raw("You don't have permission to use this command.").color("#FF5555"));
            return;
        }

        // Execute the command
        cmd.execute(sender, args);
    }

    /**
     * Checks if the sender has the required permission group.
     */
    private boolean hasPermissionGroup(CommandSender sender) {
        GameMode permissionGroup = cmd.getPermissionGroup();

        // If permission group is null (default), require admin permission
        if (permissionGroup == null) {
            return sender.hasPermission(adminPermission) || sender.hasPermission("*");
        }

        // If permission group is Adventure, allow all players
        if (permissionGroup == GameMode.Adventure) {
            return true;
        }

        // For any other game mode, require admin permission (safe default)
        return sender.hasPermission(adminPermission) || sender.hasPermission("*");
    }

    /**
     * Checks if the sender has any of the required permissions.
     */
    private boolean hasAnyPermission(CommandSender sender, List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return true;
        }
        if (sender.hasPermission("*")) {
            return true;
        }
        for (String permission : permissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the wrapped command implementation.
     */
    CommandInterface getCommand() {
        return cmd;
    }
}
