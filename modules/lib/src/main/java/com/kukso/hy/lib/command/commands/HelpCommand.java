package com.kukso.hy.lib.command.commands;

import com.kukso.hy.lib.command.BaseCommand;
import com.kukso.hy.lib.command.CmdInterface;
import com.kukso.hy.lib.command.CmdRegistrar;
import com.kukso.hy.lib.command.CommandSender;

import java.util.List;

public class HelpCommand extends BaseCommand {

    public HelpCommand() {
        super("help", "Shows available commands", "/help [command]", null, "?");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        CmdRegistrar registrar = CmdRegistrar.getInstance();

        if (args.length == 0) {
            sender.sendMessage("=== KuksoLib Commands ===");
            for (CmdInterface cmd : registrar.getCommands()) {
                sender.sendMessage("/" + cmd.getName() + " - " + cmd.getDescription());
            }
            return true;
        }

        CmdInterface command = registrar.getCommand(args[0]);
        if (command == null) {
            sender.sendMessage("Unknown command: " + args[0]);
            return false;
        }

        sender.sendMessage("=== " + command.getName() + " ===");
        sender.sendMessage("Description: " + command.getDescription());
        sender.sendMessage("Usage: " + command.getUsage());

        List<String> aliases = command.getAliases();
        if (!aliases.isEmpty()) {
            sender.sendMessage("Aliases: " + String.join(", ", aliases));
        }

        String permission = command.getPermission();
        if (permission != null && !permission.isEmpty()) {
            sender.sendMessage("Permission: " + permission);
        }

        return true;
    }
}
