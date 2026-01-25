package com.kukso.hy.lib.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.kukso.hy.lib.util.LocaleUtil;

import java.util.List;
import java.util.logging.Level;

/**
 * Reload subcommand - reloads the plugin configuration.
 */
class CmdSubReload implements CommandInterface {

    private final PluginBase plugin;

    CmdSubReload(PluginBase plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
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

        sender.sendMessage(Message.raw("Reloading KuksoLib..."));

        // Reload localization
        LocaleUtil.reload();

        sender.sendMessage(Message.raw("KuksoLib reloaded successfully!").color("#55FF55"));

        plugin.getLogger().at(Level.INFO).log("Reloaded by " + sender.getDisplayName());
        return true;
    }
}
