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
        return List.of("eco");
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

    @SuppressWarnings("removal")
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // Check if sender is a Player entity
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorMan.translate("&cThis command can only be used by players."));
            return true;
        }

        PlayerRef playerRef = player.getPlayerRef();

        // Default: show own balance
        if (args.length == 0) {
            sendBalance(player, playerRef);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "balance":
            case "bal":
                handleBalance(player, playerRef, args);
                break;
            case "pay":
                handlePay(player, playerRef, args);
                break;
            case "set":
                handleSet(player, playerRef, args);
                break;
            case "give":
                handleGive(player, playerRef, args);
                break;
            case "take":
                handleTake(player, playerRef, args);
                break;
            case "help":
            case "?":
                sendHelp(player);
                break;
            default:
                sendBalance(player, playerRef);
                break;
        }
        return true;
    }

    @SuppressWarnings("removal")
    private void handleBalance(Player player, PlayerRef playerRef, String[] args) {
        if (args.length == 1) {
            sendBalance(player, playerRef);
        } else {
            // Check another player's balance
            String targetName = args[1];
            Player target = resolvePlayer(player, targetName);
            if (target == null) {
                player.sendMessage(ColorMan.translate("&cPlayer not found: " + targetName));
                return;
            }
            sendBalance(player, target.getPlayerRef());
        }
    }

    @SuppressWarnings("removal")
    private void handlePay(Player player, PlayerRef playerRef, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ColorMan.translate("&cUsage: /kl eco pay <player> <amount>"));
            return;
        }

        String targetName = args[1];
        Player target = resolvePlayer(player, targetName);
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

        PlayerRef targetRef = target.getPlayerRef();
        Transaction result = eco.transfer(playerRef, targetRef, amount);
        if (result.isSuccessful()) {
            player.sendMessage(ColorMan.translate("&aYou paid &e" + targetRef.getUsername() + " &a" + eco.format(amount)));
            target.sendMessage(ColorMan.translate("&aYou received &e" + eco.format(amount) + " &afrom &e" + playerRef.getUsername()));
        } else {
            player.sendMessage(ColorMan.translate("&cTransaction failed: " + result.getFailureReason()));
        }
    }

    @SuppressWarnings("removal")
    private void handleSet(Player player, PlayerRef playerRef, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ColorMan.translate("&cUsage: /kl eco set <player> <amount>"));
            return;
        }

        String targetName = args[1];
        Player target = resolvePlayer(player, targetName);
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

        PlayerRef targetRef = target.getPlayerRef();
        boolean success = eco.setBalance(targetRef, amount);
        if (success) {
            player.sendMessage(ColorMan.translate("&aSet &e" + targetRef.getUsername() + "'s &abalance to &e" + eco.format(amount)));
            if (!target.equals(player)) {
                target.sendMessage(ColorMan.translate("&eYour balance has been set to &a" + eco.format(amount)));
            }
        } else {
            player.sendMessage(ColorMan.translate("&cFailed to set balance."));
        }
    }

    @SuppressWarnings("removal")
    private void handleGive(Player player, PlayerRef playerRef, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ColorMan.translate("&cUsage: /kl eco give <player> <amount>"));
            return;
        }

        String targetName = args[1];
        Player target = resolvePlayer(player, targetName);
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

        PlayerRef targetRef = target.getPlayerRef();
        boolean success = eco.deposit(targetRef, amount);
        if (success) {
            player.sendMessage(ColorMan.translate("&aGave &e" + eco.format(amount) + " &ato &e" + targetRef.getUsername()));
            if (!target.equals(player)) {
                target.sendMessage(ColorMan.translate("&aYou received &e" + eco.format(amount)));
            }
        } else {
            player.sendMessage(ColorMan.translate("&cFailed to give money."));
        }
    }

    @SuppressWarnings("removal")
    private void handleTake(Player player, PlayerRef playerRef, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ColorMan.translate("&cUsage: /kl eco take <player> <amount>"));
            return;
        }

        String targetName = args[1];
        Player target = resolvePlayer(player, targetName);
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

        PlayerRef targetRef = target.getPlayerRef();
        Transaction result = eco.withdraw(targetRef, amount);
        if (result.isSuccessful()) {
            player.sendMessage(ColorMan.translate("&aTook &e" + eco.format(amount) + " &afrom &e" + targetRef.getUsername()));
            if (!target.equals(player)) {
                target.sendMessage(ColorMan.translate("&e" + eco.format(amount) + " &awas taken from your account"));
            }
        } else {
            player.sendMessage(ColorMan.translate("&cTransaction failed: " + result.getFailureReason()));
        }
    }

    private void sendBalance(Player viewer, PlayerRef target) {
        double bal = eco.getBalance(target);
        @SuppressWarnings("removal")
        PlayerRef viewerRef = viewer.getPlayerRef();
        if (viewerRef.equals(target)) {
            viewer.sendMessage(ColorMan.translate("&eBalance: &a" + eco.format(bal)));
        } else {
            viewer.sendMessage(ColorMan.translate("&e" + target.getUsername() + "'s Balance: &a" + eco.format(bal)));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ColorMan.translate("&e&lEconomy Commands:"));
        player.sendMessage(ColorMan.translate("&7/kl eco [balance] &f- Check your balance"));
        player.sendMessage(ColorMan.translate("&7/kl eco balance <player> &f- Check player's balance"));
        player.sendMessage(ColorMan.translate("&7/kl eco pay <player> <amount> &f- Pay someone"));
        player.sendMessage(ColorMan.translate("&c&lAdmin Commands:"));
        player.sendMessage(ColorMan.translate("&7/kl eco set <player> <amount> &f- Set balance"));
        player.sendMessage(ColorMan.translate("&7/kl eco give <player> <amount> &f- Give money"));
        player.sendMessage(ColorMan.translate("&7/kl eco take <player> <amount> &f- Take money"));
    }

    /**
     * Resolves a player name to a Player entity.
     * Searches online players in the sender's world using case-insensitive substring matching.
     *
     * @param sender The player executing the command (used to get world)
     * @param name The player name to search for
     * @return The Player or null if not found
     */
    @SuppressWarnings("removal")
    private Player resolvePlayer(Player sender, String name) {
        for (Player p : sender.getWorld().getPlayers()) {
            if (p.toString().toLowerCase().contains(name.toLowerCase())) {
                return p;
            }
        }
        return null;
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
