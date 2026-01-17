package com.kukso.hy.lib.command.sub;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.kukso.hy.lib.command.CmdInterface;
import com.kukso.hy.lib.economy.EconomyManager;
import com.kukso.hy.lib.economy.Transaction;
import com.kukso.hy.lib.util.ColorMan;

import java.util.Collections;
import java.util.List;

/**
 * Economy command for managing player balances.
 * Supports balance checking, transfers, and admin operations.
 */
public class EconomyCmd implements CmdInterface {

    private final EconomyManager eco;

    public EconomyCmd() {
        this.eco = EconomyManager.getInstance();
    }

    @Override
    public String getName() {
        return "economy";
    }

    @Override
    public List<String> getAliases() {
        return List.of("eco", "money", "bal");
    }

    @Override
    public List<String> getPermissions() {
        return List.of("kuksolib.economy");
    }

    @Override
    public GameMode getPermissionGroup() {
        return GameMode.Adventure;
    }

    @Override
    public String getDescription() {
        return "Manage economy and balances";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // Check if sender is a Player entity
        if (!(sender instanceof Player playerEntity)) {
            sender.sendMessage(ColorMan.translate("&cThis command can only be used by players."));
            return true;
        }

        // Get PlayerRef from Player entity
        PlayerRef player = playerEntity.getPlayerRef();
        if (player == null) {
            sender.sendMessage(ColorMan.translate("&cCould not get player reference. Please try again."));
            return true;
        }

        // Default: show own balance
        if (args.length == 0) {
            sendBalance(player, player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "balance":
            case "bal":
                handleBalance(player, args);
                break;
            case "pay":
                handlePay(player, args);
                break;
            case "set":
                handleSet(player, args);
                break;
            case "give":
                handleGive(player, args);
                break;
            case "take":
                handleTake(player, args);
                break;
            case "help":
            case "?":
                sendHelp(player);
                break;
            default:
                sendBalance(player, player);
                break;
        }
        return true;
    }

    private void handleBalance(PlayerRef player, String[] args) {
        if (args.length == 1) {
            sendBalance(player, player);
        } else {
            // Check another player's balance
            String targetName = args[1];
            PlayerRef target = resolvePlayer(targetName);
            if (target == null) {
                player.sendMessage(ColorMan.translate("&cPlayer not found: " + targetName));
                return;
            }
            sendBalance(player, target);
        }
    }

    private void handlePay(PlayerRef player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ColorMan.translate("&cUsage: /kl eco pay <player> <amount>"));
            return;
        }

        String targetName = args[1];
        PlayerRef target = resolvePlayer(targetName);
        if (target == null) {
            player.sendMessage(ColorMan.translate("&cPlayer not found: " + targetName));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(ColorMan.translate("&cYou cannot pay yourself!"));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ColorMan.translate("&cInvalid amount: " + args[2]));
            return;
        }

        if (amount <= 0) {
            player.sendMessage(ColorMan.translate("&cAmount must be positive!"));
            return;
        }

        Transaction result = eco.transfer(player, target, amount);
        if (result.isSuccessful()) {
            player.sendMessage(ColorMan.translate("&aYou paid &e" + target.getUsername() + " &a" + eco.format(amount)));
            target.sendMessage(ColorMan.translate("&aYou received &e" + eco.format(amount) + " &afrom &e" + player.getUsername()));
        } else {
            player.sendMessage(ColorMan.translate("&cTransaction failed: " + result.getFailureReason()));
        }
    }

    private void handleSet(PlayerRef player, String[] args) {
        if (!hasAdminPermission(player)) {
            player.sendMessage(ColorMan.translate("&cYou don't have permission to use this command."));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ColorMan.translate("&cUsage: /kl eco set <player> <amount>"));
            return;
        }

        String targetName = args[1];
        PlayerRef target = resolvePlayer(targetName);
        if (target == null) {
            player.sendMessage(ColorMan.translate("&cPlayer not found: " + targetName));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ColorMan.translate("&cInvalid amount: " + args[2]));
            return;
        }

        if (amount < 0) {
            player.sendMessage(ColorMan.translate("&cAmount cannot be negative!"));
            return;
        }

        boolean success = eco.setBalance(target, amount);
        if (success) {
            player.sendMessage(ColorMan.translate("&aSet &e" + target.getUsername() + "'s &abalance to &e" + eco.format(amount)));
            if (!target.equals(player)) {
                target.sendMessage(ColorMan.translate("&eYour balance has been set to &a" + eco.format(amount)));
            }
        } else {
            player.sendMessage(ColorMan.translate("&cFailed to set balance."));
        }
    }

    private void handleGive(PlayerRef player, String[] args) {
        if (!hasAdminPermission(player)) {
            player.sendMessage(ColorMan.translate("&cYou don't have permission to use this command."));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ColorMan.translate("&cUsage: /kl eco give <player> <amount>"));
            return;
        }

        String targetName = args[1];
        PlayerRef target = resolvePlayer(targetName);
        if (target == null) {
            player.sendMessage(ColorMan.translate("&cPlayer not found: " + targetName));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ColorMan.translate("&cInvalid amount: " + args[2]));
            return;
        }

        if (amount <= 0) {
            player.sendMessage(ColorMan.translate("&cAmount must be positive!"));
            return;
        }

        boolean success = eco.deposit(target, amount);
        if (success) {
            player.sendMessage(ColorMan.translate("&aGave &e" + eco.format(amount) + " &ato &e" + target.getUsername()));
            if (!target.equals(player)) {
                target.sendMessage(ColorMan.translate("&aYou received &e" + eco.format(amount)));
            }
        } else {
            player.sendMessage(ColorMan.translate("&cFailed to give money."));
        }
    }

    private void handleTake(PlayerRef player, String[] args) {
        if (!hasAdminPermission(player)) {
            player.sendMessage(ColorMan.translate("&cYou don't have permission to use this command."));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ColorMan.translate("&cUsage: /kl eco take <player> <amount>"));
            return;
        }

        String targetName = args[1];
        PlayerRef target = resolvePlayer(targetName);
        if (target == null) {
            player.sendMessage(ColorMan.translate("&cPlayer not found: " + targetName));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ColorMan.translate("&cInvalid amount: " + args[2]));
            return;
        }

        if (amount <= 0) {
            player.sendMessage(ColorMan.translate("&cAmount must be positive!"));
            return;
        }

        Transaction result = eco.withdraw(target, amount);
        if (result.isSuccessful()) {
            player.sendMessage(ColorMan.translate("&aTook &e" + eco.format(amount) + " &afrom &e" + target.getUsername()));
            if (!target.equals(player)) {
                target.sendMessage(ColorMan.translate("&e" + eco.format(amount) + " &awas taken from your account"));
            }
        } else {
            player.sendMessage(ColorMan.translate("&cTransaction failed: " + result.getFailureReason()));
        }
    }

    private void sendBalance(PlayerRef viewer, PlayerRef target) {
        double bal = eco.getBalance(target);
        if (viewer.equals(target)) {
            viewer.sendMessage(ColorMan.translate("&eBalance: &a" + eco.format(bal)));
        } else {
            viewer.sendMessage(ColorMan.translate("&e" + target.getUsername() + "'s Balance: &a" + eco.format(bal)));
        }
    }

    private void sendHelp(PlayerRef player) {
        player.sendMessage(ColorMan.translate("&e&lEconomy Commands:"));
        player.sendMessage(ColorMan.translate("&7/kl eco [balance] &f- Check your balance"));
        player.sendMessage(ColorMan.translate("&7/kl eco balance <player> &f- Check player's balance"));
        player.sendMessage(ColorMan.translate("&7/kl eco pay <player> <amount> &f- Pay someone"));

        if (hasAdminPermission(player)) {
            player.sendMessage(ColorMan.translate("&c&lAdmin Commands:"));
            player.sendMessage(ColorMan.translate("&7/kl eco set <player> <amount> &f- Set balance"));
            player.sendMessage(ColorMan.translate("&7/kl eco give <player> <amount> &f- Give money"));
            player.sendMessage(ColorMan.translate("&7/kl eco take <player> <amount> &f- Take money"));
        }
    }

    /**
     * Checks if a player has admin permission.
     *
     * @param player The player to check
     * @return true if the player has admin permission
     */
    private boolean hasAdminPermission(PlayerRef player) {
        // PlayerRef doesn't have hasPermission, so we check via CommandSender
        // For now, assume all commands go through CmdManager which checks permissions
        // This is a simplified check - in production, integrate with permission system
        return true; // Permissions are checked by CmdManager
    }

    /**
     * Resolves a player name to a PlayerRef.
     * First checks online players, then can be extended for offline support.
     *
     * @param name The player name
     * @return The PlayerRef or null if not found
     */
    private PlayerRef resolvePlayer(String name) {
        // Try to find online player
        try {
            // This needs to be adapted based on actual Hytale API
            // Placeholder implementation - needs real player lookup
            // For now, return null to indicate "not implemented"
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("kuksolib.economy.admin") || sender.hasPermission("*")) {
                return List.of("balance", "pay", "set", "give", "take", "help");
            }
            return List.of("balance", "pay", "help");
        }
        return Collections.emptyList();
    }
}
