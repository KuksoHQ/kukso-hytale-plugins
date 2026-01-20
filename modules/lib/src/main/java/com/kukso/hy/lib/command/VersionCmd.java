package com.kukso.hy.lib.command;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.plugin.PluginBase;

import java.util.List;

/**
 * Version subcommand - displays plugin version information.
 */
class VersionCmd implements CommandInterface {

    private final PluginBase plugin;

    VersionCmd(PluginBase plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public List<String> getAliases() {
        return List.of("ver", "v");
    }

    @Override
    public GameMode getPermissionGroup() {
        return GameMode.Adventure; // Allow all players to view version info
    }

    @Override
    public String getDescription() {
        return "Shows the plugin version.";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        String version = String.valueOf(plugin.getManifest().getVersion());
        String name = plugin.getName();

        sender.sendMessage(Message.raw("=== " + name + " ===").bold(true).color("#FFD700"));
        sender.sendMessage(Message.raw("Version: " + version));
        sender.sendMessage(Message.raw("Author: Kukso"));
        sender.sendMessage(Message.raw("Java: " + System.getProperty("java.version")));

        return true;
    }
}
