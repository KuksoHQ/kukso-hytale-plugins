package com.kukso.hy.lib.command;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.kukso.hy.lib.util.ColorMan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Example standalone command that displays player information.
 */
class PlayerInfoCmd implements CommandInterface {

    @Override
    public String getName() {
        return "playerinfo";
    }

    @Override
    public List<String> getAliases() {
        return List.of("pinfo", "pi");
    }

    @Override
    public GameMode getPermissionGroup() {
        return GameMode.Adventure;
    }

    @Override
    public String getDescription() {
        return "Display information about a player";
    }

    @Override
    public String getUsage() {
        return "/playerinfo [player]";
    }

    @SuppressWarnings("removal")
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage(ColorMan.translate("&cThis command can only be used by players."));
            return true;
        }

        Player target;

        if (args.length == 0) {
            target = senderPlayer;
        } else {
            String targetName = args[0];
            target = resolvePlayer(senderPlayer, targetName);

            if (target == null) {
                sender.sendMessage(ColorMan.translate("&cPlayer not found: " + targetName));
                return true;
            }
        }

        displayPlayerInfo(sender, target);
        return true;
    }

    @SuppressWarnings("removal")
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            List<String> suggestions = new ArrayList<>();
            String partial = args[0].toLowerCase();

            for (Player p : player.getWorld().getPlayers()) {
                String name = p.getPlayerRef().getUsername();
                if (name.toLowerCase().startsWith(partial)) {
                    suggestions.add(name);
                }
            }
            return suggestions;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("removal")
    private Player resolvePlayer(Player senderPlayer, String name) {
        for (Player p : senderPlayer.getWorld().getPlayers()) {
            String playerName = p.getPlayerRef().getUsername();
            if (playerName.equalsIgnoreCase(name)) {
                return p;
            }
        }
        for (Player p : senderPlayer.getWorld().getPlayers()) {
            String playerName = p.getPlayerRef().getUsername();
            if (playerName.toLowerCase().startsWith(name.toLowerCase())) {
                return p;
            }
        }
        return null;
    }

    @SuppressWarnings("removal")
    private void displayPlayerInfo(CommandSender sender, Player target) {
        PlayerRef ref = target.getPlayerRef();

        sender.sendMessage(ColorMan.translate("&e&l=== Player Info: " + ref.getUsername() + " ==="));
        sender.sendMessage(ColorMan.translate("&7UUID: &f" + ref.getUuid()));
        sender.sendMessage(ColorMan.translate("&7World: &f" + target.getWorld().getName()));
        sender.sendMessage(ColorMan.translate("&7Language: &f" + ref.getLanguage()));
    }
}
