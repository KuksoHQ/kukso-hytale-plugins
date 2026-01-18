package com.kukso.hy.lib.command.sub;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.kukso.hy.lib.command.CmdInterface;
import com.kukso.hy.lib.economy.Currency;
import com.kukso.hy.lib.economy.CurrencyManager;
import com.kukso.hy.lib.economy.Economy;
import com.kukso.hy.lib.economy.KuksoEconomy;
import com.kukso.hy.lib.service.ServiceManager;
import com.kukso.hy.lib.util.ColorMan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Admin command for querying player wallet information.
 * Shows both ECS component data and database backend data for debugging.
 *
 * <p>Usage: /kuksolib wallet &lt;player&gt;</p>
 *
 * <p>This helps admins verify that ECS components (in-memory, fast access)
 * are in sync with the database backend (persistent storage).</p>
 */
public class WalletCmd implements CmdInterface {

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

        // Get economy provider
        Economy economy = ServiceManager.getProvider(Economy.class);
        if (economy == null) {
            sender.sendMessage(ColorMan.translate("&cNo economy provider registered"));
            return true;
        }

        sender.sendMessage(ColorMan.translate("&7Provider: &f" + economy.getName()));

        // Show registered currencies
        if (!CurrencyManager.hasCurrencies()) {
            sender.sendMessage(ColorMan.translate("&cNo currencies registered"));
            return true;
        }

        sender.sendMessage(ColorMan.translate("&e&lBalances:"));

        // Get EntityStore if available
        EntityStore entityStore = null;
        try {
            entityStore = target.getWorld().getEntityStore();
        } catch (Exception e) {
            // EntityStore might not be accessible
        }

        // Show balances for each currency
        for (String currencyId : CurrencyManager.getCurrencyIds()) {
            Currency currency = CurrencyManager.getCurrency(currencyId);
            if (currency == null) continue;

            // Get ECS balance
            double ecsBalance = 0.0;
            String ecsStatus = "&c[N/A]";
            if (entityStore != null) {
                try {
                    ecsBalance = CurrencyManager.getBalance(playerRef, currencyId, entityStore);
                    ecsStatus = "&a[ECS: " + currency.formatAmount(ecsBalance) + "]";
                } catch (Exception e) {
                    ecsStatus = "&c[ECS Error]";
                }
            }

            // Get DB balance (if KuksoEconomy)
            String dbStatus = "&7[DB: N/A]";
            if (economy instanceof KuksoEconomy kuksoEcon) {
                try {
                    double dbBalance = economy.getBalance(playerRef.getUuid(), currencyId);
                    dbStatus = "&b[DB: " + currency.formatAmount(dbBalance) + "]";

                    // Check for sync issues
                    if (entityStore != null && Math.abs(ecsBalance - dbBalance) > 0.01) {
                        dbStatus += " &c(OUT OF SYNC!)";
                    }
                } catch (Exception e) {
                    dbStatus = "&c[DB Error]";
                }
            }

            String defaultMarker = currencyId.equals(CurrencyManager.getDefaultCurrencyId()) ? " &6(default)" : "";
            sender.sendMessage(ColorMan.translate(
                "  &f" + currency.displayName() + defaultMarker + ": " + ecsStatus + " " + dbStatus
            ));
        }

        // Show account status
        sender.sendMessage(ColorMan.translate("&e&lAccount Status:"));
        boolean hasAccount = economy.hasAccount(playerRef);
        sender.sendMessage(ColorMan.translate("  &7Has Account: " + (hasAccount ? "&aYes" : "&cNo")));

        return true;
    }

    /**
     * Resolves a player name to a Player entity.
     */
    @SuppressWarnings("removal")
    private Player resolvePlayer(CommandSender sender, String name) {
        // First try exact match
        if (sender instanceof Player senderPlayer) {
            for (Player p : senderPlayer.getWorld().getPlayers()) {
                @SuppressWarnings("removal")
                String playerName = p.getPlayerRef().getUsername();
                if (playerName.equalsIgnoreCase(name)) {
                    return p;
                }
            }
            // Then try substring match
            for (Player p : senderPlayer.getWorld().getPlayers()) {
                @SuppressWarnings("removal")
                String playerName = p.getPlayerRef().getUsername();
                if (playerName.toLowerCase().contains(name.toLowerCase())) {
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            List<String> suggestions = new ArrayList<>();
            String partial = args[0].toLowerCase();
            for (Player p : player.getWorld().getPlayers()) {
                @SuppressWarnings("removal")
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
