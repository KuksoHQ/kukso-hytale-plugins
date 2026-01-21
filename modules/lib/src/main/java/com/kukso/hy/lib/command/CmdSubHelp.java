package com.kukso.hy.lib.command;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.kukso.hy.lib.util.ColorMan;

import java.util.Collection;
import java.util.List;

/**
 * Help subcommand - displays available commands.
 */
class CmdSubHelp implements CommandInterface {

    private final CommandTreeBase manager;

    CmdSubHelp(CommandTreeBase manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public List<String> getAliases() {
        return List.of("?");
    }

    @Override
    public GameMode getPermissionGroup() {
        return GameMode.Adventure; // Allow all players to view help
    }

    @Override
    public String getDescription() {
        return "Shows this help menu.";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(ColorMan.translate("&e&l=== KuksoLib Help ==="));

        if (manager == null) return true;

        Collection<CommandInterface> commands = manager.getCommands();
        for (CommandInterface cmd : commands) {
            List<String> permissions = cmd.getPermissions();
            boolean hasPermission = permissions.isEmpty() ||
                    permissions.stream().anyMatch(sender::hasPermission);

            if (!hasPermission) continue;

            sender.sendMessage(ColorMan.translate("&e" + cmd.getUsage() + " &7- " + cmd.getDescription()));
        }
        return true;
    }
}
