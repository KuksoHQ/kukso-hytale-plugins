package com.kukso.hytale.lib.commands.kuksolib.test;

import com.kukso.hytale.lib.util.ColorUtil;
import com.kukso.hytale.lib.util.LocaleUtil;
import com.kukso.hytale.lib.util.MessageUtil;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;

class TestLocaleCommand extends AbstractPlayerCommand {

    MessageUtil messageUtil = new MessageUtil();

    TestLocaleCommand() {
        super("locale", "Test localization system");
        addAliases("lang", "language");
    }

    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {

        messageUtil.sendStringFormatted(context, "&e&l=== LocaleMan Test ===");
        messageUtil.sendStringFormatted(context, "");
        messageUtil.sendStringFormatted(context, "&7Loaded locales: &e" + String.join(", ", LocaleUtil.getLoadedLocales()));

        String welcome = LocaleUtil.getRaw(LocaleUtil.DEFAULT_LOCALE, "messages.welcome");
        messageUtil.sendStringFormatted(context, "&7Raw welcome key: &f" + welcome);

        String goodbye = LocaleUtil.getRaw(LocaleUtil.DEFAULT_LOCALE, "messages.goodbye");
        messageUtil.sendStringFormatted(context, "&7Raw goodbye key: &f" + goodbye);

        String prefix = LocaleUtil.getRaw(LocaleUtil.DEFAULT_LOCALE, "prefix");
        messageUtil.sendStringFormatted(context, "&7Prefix: &f" + prefix);

        // AbstractPlayerCommand guarantees we have a player, so no instanceof check needed
        String playerLocale = LocaleUtil.getPlayerLocale(playerRef);
        messageUtil.sendStringFormatted(context, "&7Your locale: &e" + playerLocale);
        messageUtil.sendStringFormatted(context, "&7Personalized welcome:");

        messageUtil.sendTranslationFormatted(context, "messages.welcome", Map.of("player", playerRef.getUsername()));
        messageUtil.sendTranslationFormatted(context, "");

        String missing = LocaleUtil.getRaw(LocaleUtil.DEFAULT_LOCALE, "this.key.does.not.exist");
        messageUtil.sendStringFormatted(context, "&7Missing key test: &c" + missing);

        messageUtil.sendStringFormatted(context, "");
        messageUtil.sendStringFormatted(context, "&aAll locale tests completed!");
    }

    private void send(CommandContext ctx, String text) {
        ctx.sendMessage(ColorUtil.format(text));
    }
}