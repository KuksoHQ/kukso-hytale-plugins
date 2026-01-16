package com.kukso.hy.lib.command.sub;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.kukso.hy.lib.command.CmdInterface;
import com.kukso.hy.lib.command.CmdManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Help subcommand - displays available commands.
 */
public class HelpCmd implements CmdInterface {

    private static final String CMD_NAME = "help";
    private final CmdManager manager;

    public HelpCmd(CmdManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public List<String> getAliases() {
        return List.of("?");
    }

    @Override
    public List<String> getPermissions() {
        return Collections.emptyList();
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
        sender.sendMessage(Message.raw("=== KuksoLib Help ===").bold(true).color("#FFD700"));

        if (manager == null) return true;

        Collection<CmdInterface> commands = manager.getCommands();
        for (CmdInterface cmd : commands) {
            List<String> permissions = cmd.getPermissions();
            boolean hasPermission = permissions.isEmpty() ||
                    permissions.stream().anyMatch(sender::hasPermission);

            if (!hasPermission) continue;

            sender.sendMessage(Message.raw(cmd.getUsage() + " - " + cmd.getDescription()).color("#FFFF55"));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
