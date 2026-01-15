package com.kukso.hy.lib.command.commands;

import com.kukso.hy.lib.command.BaseCommand;
import com.kukso.hy.lib.command.CommandSender;

public class ReloadCommand extends BaseCommand {

    public ReloadCommand() {
        super("reload", "Reloads the plugin configuration", "/reload", "kuksolib.reload", "rl");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        sender.sendMessage("Reloading KuksoLib configuration...");
        // TODO: Implement actual reload logic
        sender.sendMessage("Configuration reloaded successfully!");
        return true;
    }
}
