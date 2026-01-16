package com.kukso.hy.lib.command.sub;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.kukso.hy.lib.command.CmdInterface;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Reload subcommand - reloads the plugin configuration.
 */
public class ReloadCmd implements CmdInterface {

    private static final String CMD_NAME = "reload";
    private final PluginBase plugin;

    public ReloadCmd(PluginBase plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public List<String> getAliases() {
        return List.of("rl");
    }

    @Override
    public List<String> getPermissions() {
        return List.of("kuksolib.reload");
    }

    @Override
    public String getDescription() {
        return "Reloads the KuksoLib configuration.";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.getLogger().at(Level.INFO).log("Reload command used by " + sender.getDisplayName());

        // TODO: Implement actual reload logic
        sender.sendMessage(Message.raw("Reloading KuksoLib..."));
        sender.sendMessage(Message.raw("KuksoLib reloaded successfully!").color("#55FF55"));

        plugin.getLogger().at(Level.INFO).log("Reloaded by " + sender.getDisplayName());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
