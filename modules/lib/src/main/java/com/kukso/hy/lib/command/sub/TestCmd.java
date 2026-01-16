package com.kukso.hy.lib.command.sub;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.kukso.hy.lib.command.CmdInterface;
import com.kukso.hy.lib.util.ColorMan;

import java.util.Collections;
import java.util.List;

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
        return "Test KuksoLib features (chatcolor, etc.)";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorMan.translate("&e&lKuksoLib Test Commands"));
            sender.sendMessage(ColorMan.translate("&7/kuksolib test chatcolor &f- Test color formatting"));
            return true;
        }

        String testType = args[0].toLowerCase();

        switch (testType) {
            case "chatcolor":
            case "color":
            case "colors":
                testChatColor(sender);
                break;
            default:
                sender.sendMessage(ColorMan.translate("&cUnknown test: &f" + args[0]));
                sender.sendMessage(ColorMan.translate("&7Available tests: &echatcolor"));
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
        sender.sendMessage(ColorMan.translate("&7Mixed: &l&cERROR: &r&7Something went &4&lwrong&r&7!"));

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

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return List.of("chatcolor");
        }
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return List.of("chatcolor", "color", "colors").stream()
                    .filter(s -> s.startsWith(partial))
                    .toList();
        }
        return Collections.emptyList();
    }
}
