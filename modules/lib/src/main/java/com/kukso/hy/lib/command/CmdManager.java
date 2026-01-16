package com.kukso.hy.lib.command;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages subcommand execution and routing for the /kuksolib command.
 */
public class CmdManager extends CommandBase {

    private final Map<String, CmdInterface> commands = new HashMap<>();
    private final Map<String, String> aliasToCommand = new HashMap<>();

    public CmdManager() {
        super("kuksolib", "KuksoLib main command");
        addAliases("klib");

        // Allow extra arguments for subcommands
        setAllowsExtraArguments(true);
    }

    /**
     * Registers a subcommand with the manager.
     * @param cmd The subcommand to register
     */
    public void register(CmdInterface cmd) {
        String name = cmd.getName().toLowerCase();
        commands.put(name, cmd);

        // Register aliases
        for (String alias : cmd.getAliases()) {
            String lowerAlias = alias.toLowerCase();
            if (aliasToCommand.containsKey(lowerAlias)) {
                System.out.println("[" + this.getName() + "] Warning: Alias '" + alias + "' is already registered, overwriting...");
            }
            aliasToCommand.put(lowerAlias, name);
        }
    }

    /**
     * Gets all registered commands.
     * @return Collection of registered commands
     */
    public Collection<CmdInterface> getCommands() {
        return commands.values();
    }

    @Override
    protected void executeSync(CommandContext context) {
        CommandSender sender = context.sender();
        String input = context.getInputString();

        // Parse args from input (skip the command name itself)
        String[] parts = input != null ? input.trim().split("\\s+") : new String[0];
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        if (args.length == 0) {
            // No subcommand provided - show usage
            context.sendMessage(Message.raw("Use /kuksolib <subcommand>").color("#AAAAAA"));
            context.sendMessage(Message.raw("Type /kuksolib help for available commands.").italic(true));
            return;
        }

        String subCmdName = args[0].toLowerCase();

        // Check if it's an alias
        if (aliasToCommand.containsKey(subCmdName)) {
            subCmdName = aliasToCommand.get(subCmdName);
        }

        CmdInterface sub = commands.get(subCmdName);
        if (sub == null) {
            context.sendMessage(Message.raw("Unknown subcommand: " + args[0]).color("#FF5555"));
            return;
        }

        // Check permission group (OP requirement)
        if (!hasPermissionGroup(sender, sub)) {
            context.sendMessage(Message.raw("You don't have permission to use this command.").color("#FF5555"));
            return;
        }

        // Check permissions
        if (!hasAnyPermission(sender, sub.getPermissions())) {
            context.sendMessage(Message.raw("You don't have permission to use this command.").color("#FF5555"));
            return;
        }

        // Execute the subcommand with remaining args
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        sub.execute(sender, subArgs);
    }

    /**
     * Checks if the sender has the required permission group.
     * @param sender The command sender
     * @param cmd The command to check
     * @return true if sender has permission group access
     */
    private boolean hasPermissionGroup(CommandSender sender, CmdInterface cmd) {
        GameMode permissionGroup = cmd.getPermissionGroup();

        // If permission group is null (default), require operator permission
        if (permissionGroup == null) {
            return sender.hasPermission("kuksolib.admin") || sender.hasPermission("*");
        }

        // If permission group is Adventure, allow all players
        if (permissionGroup == GameMode.Adventure) {
            return true;
        }

        // For any other game mode, require operator permission (safe default)
        return sender.hasPermission("kuksolib.admin") || sender.hasPermission("*");
    }

    /**
     * Checks if the sender has any of the required permissions.
     * @param sender The command sender
     * @param permissions List of permissions (any one grants access)
     * @return true if sender has permission or no permissions required
     */
    private boolean hasAnyPermission(CommandSender sender, List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return true;
        }
        for (String permission : permissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }
}
