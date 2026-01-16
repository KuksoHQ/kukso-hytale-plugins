package com.kukso.hy.lib.command.sub;

import com.kukso.hy.lib.command.CmdInterface;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.plugin.PluginBase;

import java.util.Collections;
import java.util.List;

/**
 * Version subcommand - displays plugin version information.
 */
public class VersionCmd implements CmdInterface {

    private static final String CMD_NAME = "version";
    private final PluginBase plugin;

    public VersionCmd(PluginBase plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public List<String> getAliases() {
        return List.of("ver", "v");
    }

    @Override
    public List<String> getPermissions() {
        return Collections.emptyList();
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

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
