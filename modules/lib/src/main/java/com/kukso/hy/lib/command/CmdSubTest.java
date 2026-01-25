package com.kukso.hy.lib.command;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.kukso.hy.lib.locale.LocaleMan;
import com.kukso.hy.lib.util.ColorUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test subcommand - provides testing utilities for KuksoLib features.
 */
class CmdSubTest implements CommandInterface {

    @Override
    public String getName() {
        return "test";
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
        return GameMode.Adventure;
    }

    @Override
    public String getDescription() {
        return "Test KuksoLib features (chatcolor, locale, etc.)";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.colorThis("&e&lKuksoLib Test Commands"));
            sender.sendMessage(ColorUtil.colorThis("&7/kuksolib test chatcolor &f- Test color formatting"));
            sender.sendMessage(ColorUtil.colorThis("&7/kuksolib test locale &f- Test localization system"));
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
                sender.sendMessage(ColorUtil.colorThis("&cUnknown test: &f" + args[0]));
                sender.sendMessage(ColorUtil.colorThis("&7Available tests: &echatcolor&7, &elocale"));
                break;
        }

        return true;
    }

    private void testChatColor(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorThis("&e&l=== ColorMan Test ==="));
        sender.sendMessage(ColorUtil.colorThis(""));
        sender.sendMessage(ColorUtil.colorThis("&7Rainbow: &4R&ca&6i&en&ab&bo&9w&5!"));
        sender.sendMessage(ColorUtil.colorThis("&7Mixed: &l&cERROR: &r&7Something went &4&lwrong&r&7! &a(no it didn't)"));
        sender.sendMessage(ColorUtil.colorThis("&7Hex: &#FF0000Red &#00FF00Green &#0000FFBlue"));
        sender.sendMessage(ColorUtil.colorThis("&7Complex: &e&lKuksoLib &r&8» &aReady &7to &broll&7!"));
        sender.sendMessage(ColorUtil.colorThis("&7Standard: &00 &11 &22 &33 &44 &55 &66 &77 &88 &99 &aa &bb &cc &dd &ee &ff"));
        sender.sendMessage(ColorUtil.colorThis("&7Format: &lBold &r&oItalic &r&7Normal"));
        sender.sendMessage(ColorUtil.colorThis(""));
        sender.sendMessage(ColorUtil.colorThis("&aAll color tests completed!"));
    }

    private void testLocale(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorThis("&e&l=== LocaleMan Test ==="));
        sender.sendMessage(ColorUtil.colorThis(""));
        sender.sendMessage(ColorUtil.colorThis("&7Loaded locales: &e" + String.join(", ", LocaleMan.getLoadedLocales())));

        String welcome = LocaleMan.getRaw(LocaleMan.DEFAULT_LOCALE, "messages.welcome");
        sender.sendMessage(ColorUtil.colorThis("&7Raw welcome key: &f" + welcome));

        String goodbye = LocaleMan.getRaw(LocaleMan.DEFAULT_LOCALE, "messages.goodbye");
        sender.sendMessage(ColorUtil.colorThis("&7Raw goodbye key: &f" + goodbye));

        String prefix = LocaleMan.getRaw(LocaleMan.DEFAULT_LOCALE, "prefix");
        sender.sendMessage(ColorUtil.colorThis("&7Prefix: &f" + prefix));

        if (sender instanceof PlayerRef player) {
            String playerLocale = LocaleMan.getPlayerLocale(player);
            sender.sendMessage(ColorUtil.colorThis("&7Your locale: &e" + playerLocale));
            sender.sendMessage(ColorUtil.colorThis("&7Personalized welcome:"));
            sender.sendMessage(LocaleMan.get(player, "messages.welcome", Map.of("player", player.getUsername())));
        }

        String missing = LocaleMan.getRaw(LocaleMan.DEFAULT_LOCALE, "this.key.does.not.exist");
        sender.sendMessage(ColorUtil.colorThis("&7Missing key test: &c" + missing));

        sender.sendMessage(ColorUtil.colorThis(""));
        sender.sendMessage(ColorUtil.colorThis("&aAll locale tests completed!"));
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
