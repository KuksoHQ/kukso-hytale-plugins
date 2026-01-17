package com.kukso.hy.lib.command.sub;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.kukso.hy.lib.command.CmdInterface;
import com.kukso.hy.lib.locale.LocaleMan;
import com.kukso.hy.lib.util.ColorMan;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test subcommand - provides testing utilities for KuksoLib features.
 */
public class TestCmd implements CmdInterface {

    private static final String CMD_NAME = "test";

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public List<String> getAliases() {
        return List.of("t");
    }

    @Override
    public List<String> getPermissions() {
        return List.of("kuksolib.test");
    }

    @Override
    public GameMode getPermissionGroup() {
        return GameMode.Adventure; // Allow all players to test
    }

    @Override
    public String getDescription() {
        return "Test KuksoLib features (chatcolor, locale, etc.)";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorMan.translate("&e&lKuksoLib Test Commands"));
            sender.sendMessage(ColorMan.translate("&7/kuksolib test chatcolor &f- Test color formatting"));
            sender.sendMessage(ColorMan.translate("&7/kuksolib test locale &f- Test localization system"));
            return true;
        }

        String testType = args[0].toLowerCase();

        switch (testType) {
            case "chatcolor":
            case "color":
            case "colors":
                testChatColor(sender);
                break;
            case "locale":
            case "lang":
            case "language":
                testLocale(sender);
                break;
            default:
                sender.sendMessage(ColorMan.translate("&cUnknown test: &f" + args[0]));
                sender.sendMessage(ColorMan.translate("&7Available tests: &echatcolor&7, &elocale"));
                break;
        }

        return true;
    }

    /**
     * Demonstrates ColorMan's color formatting capabilities.
     */
    private void testChatColor(CommandSender sender) {
        sender.sendMessage(ColorMan.translate("&e&l=== ColorMan Test ==="));
        sender.sendMessage(ColorMan.translate(""));

        // Rainbow-like text
        sender.sendMessage(ColorMan.translate("&7Rainbow: &4R&ca&6i&en&ab&bo&9w&5!"));

        // Mixed formatting
        sender.sendMessage(ColorMan.translate("&7Mixed: &l&cERROR: &r&7Something went &4&lwrong&r&7! &a(no it didn't)"));

        // Hex colors
        sender.sendMessage(ColorMan.translate("&7Hex: &#FF0000Red &#00FF00Green &#0000FFBlue"));

        // Complex example
        sender.sendMessage(ColorMan.translate("&7Complex: &e&lKuksoLib &r&8» &aReady &7to &broll&7!"));

        // All standard colors
        sender.sendMessage(ColorMan.translate("&7Standard: &00 &11 &22 &33 &44 &55 &66 &77 &88 &99 &aa &bb &cc &dd &ee &ff"));

        // Format codes
        sender.sendMessage(ColorMan.translate("&7Format: &lBold &r&oItalic &r&7Normal"));

        sender.sendMessage(ColorMan.translate(""));
        sender.sendMessage(ColorMan.translate("&aAll color tests completed!"));
    }

    /**
     * Demonstrates the localization system capabilities.
     */
    private void testLocale(CommandSender sender) {
        sender.sendMessage(ColorMan.translate("&e&l=== LocaleMan Test ==="));
        sender.sendMessage(ColorMan.translate(""));

        // Show loaded locales
        sender.sendMessage(ColorMan.translate("&7Loaded locales: &e" + String.join(", ", LocaleMan.getLoadedLocales())));

        // Test raw string retrieval
        String welcome = LocaleMan.getRaw(LocaleMan.DEFAULT_LOCALE, "messages.welcome");
        sender.sendMessage(ColorMan.translate("&7Raw welcome key: &f" + welcome));

        // Test with placeholder
        String goodbye = LocaleMan.getRaw(LocaleMan.DEFAULT_LOCALE, "messages.goodbye");
        sender.sendMessage(ColorMan.translate("&7Raw goodbye key: &f" + goodbye));

        // Test prefix
        String prefix = LocaleMan.getRaw(LocaleMan.DEFAULT_LOCALE, "prefix");
        sender.sendMessage(ColorMan.translate("&7Prefix: &f" + prefix));

        // If sender is a player, test player-specific lookup
        if (sender instanceof PlayerRef player) {
            String playerLocale = LocaleMan.getPlayerLocale(player);
            sender.sendMessage(ColorMan.translate("&7Your locale: &e" + playerLocale));

            // Test getting message for player with placeholder
            sender.sendMessage(ColorMan.translate("&7Personalized welcome:"));
            sender.sendMessage(LocaleMan.get(player, "messages.welcome", Map.of("player", player.getUsername())));
        }

        // Test fallback (missing key)
        String missing = LocaleMan.getRaw(LocaleMan.DEFAULT_LOCALE, "this.key.does.not.exist");
        sender.sendMessage(ColorMan.translate("&7Missing key test: &c" + missing));

        sender.sendMessage(ColorMan.translate(""));
        sender.sendMessage(ColorMan.translate("&aAll locale tests completed!"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return List.of("chatcolor", "locale");
        }
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return List.of("chatcolor", "color", "colors", "locale", "lang", "language").stream()
                    .filter(s -> s.startsWith(partial))
                    .toList();
        }
        return Collections.emptyList();
    }
}
