package com.kukso.hy.lib.command;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.kukso.hy.lib.economy.Currency;
import com.kukso.hy.lib.economy.CurrencyManager;
import com.kukso.hy.lib.economy.Economy;
import com.kukso.hy.lib.service.ServiceManager;
import com.kukso.hy.lib.util.ColorMan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Admin command for querying player wallet information.
 * Shows ECS component data for debugging.
 */
class WalletCmd implements CommandInterface {

    @Override
    public String getName() {
        return "wallet";
    }

    @Override
    public List<String> getAliases() {
        return List.of("w", "bal");
    }

    @Override
    public List<String> getPermissions() {
        return List.of("kuksolib.wallet");
    }

    @Override
    public String getDescription() {
        return "Query player wallet information (admin)";
    }

    @Override
    public String getUsage() {
        return "/kuksolib wallet <player>";
    }

    @SuppressWarnings("removal")
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ColorMan.translate("&cUsage: " + getUsage()));
            return true;
        }

        String playerName = args[0];
        Player target = resolvePlayer(sender, playerName);

        if (target == null) {
            sender.sendMessage(ColorMan.translate("&cPlayer not found: " + playerName));
            return true;
        }

        PlayerRef playerRef = target.getPlayerRef();

        sender.sendMessage(ColorMan.translate("&e&l=== Wallet Info: " + playerRef.getUsername() + " ==="));
        sender.sendMessage(ColorMan.translate("&7UUID: &f" + playerRef.getUuid()));

        Economy economy = ServiceManager.getProvider(Economy.class);
        if (economy == null) {
            sender.sendMessage(ColorMan.translate("&cNo economy provider registered"));
            return true;
        }

        sender.sendMessage(ColorMan.translate("&7Provider: &f" + economy.getName()));

        if (!CurrencyManager.hasCurrencies()) {
            sender.sendMessage(ColorMan.translate("&cNo currencies registered"));
            return true;
        }

        sender.sendMessage(ColorMan.translate("&e&lBalances:"));

        EntityStore entityStore = null;
        try {
            entityStore = target.getWorld().getEntityStore();
        } catch (Exception e) {
            // EntityStore might not be accessible
        }

        for (String currencyId : CurrencyManager.getCurrencyIds()) {
            Currency currency = CurrencyManager.getCurrency(currencyId);
            if (currency == null) continue;

            String balanceStatus = "&c[N/A]";
            if (entityStore != null) {
                try {
                    double balance = CurrencyManager.getBalance(playerRef, currencyId, entityStore);
                    balanceStatus = "&a" + currency.formatAmount(balance);
                } catch (Exception e) {
                    balanceStatus = "&c[Error]";
                }
            }

            String defaultMarker = currencyId.equals(CurrencyManager.getDefaultCurrencyId()) ? " &6(default)" : "";
            sender.sendMessage(ColorMan.translate(
                "  &f" + currency.displayName() + defaultMarker + ": " + balanceStatus
            ));
        }

        sender.sendMessage(ColorMan.translate("&e&lAccount Status:"));
        boolean hasAccount = economy.hasAccount(playerRef);
        sender.sendMessage(ColorMan.translate("  &7Has Account: " + (hasAccount ? "&aYes" : "&cNo")));

        return true;
    }

    @SuppressWarnings("removal")
    private Player resolvePlayer(CommandSender sender, String name) {
        if (sender instanceof Player senderPlayer) {
            for (Player p : senderPlayer.getWorld().getPlayers()) {
                String playerName = p.getPlayerRef().getUsername();
                if (playerName.equalsIgnoreCase(name)) {
                    return p;
                }
            }
            for (Player p : senderPlayer.getWorld().getPlayers()) {
                String playerName = p.getPlayerRef().getUsername();
                if (playerName.toLowerCase().contains(name.toLowerCase())) {
                    return p;
                }
            }
        }
        return null;
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
}
