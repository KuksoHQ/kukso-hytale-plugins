package com.kukso.hy.lib.commands.kuksolib.test;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.kukso.hy.lib.util.ColorUtil;
import com.kukso.hy.lib.util.LocaleUtil;
import java.util.Map;

public class TestLocaleCommand extends AbstractPlayerCommand {

    public TestLocaleCommand() {
        super("locale", "Test localization system");
        // Porting switch cases: "lang", "language"
        addAliases("lang", "language");
    }

    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {

        send(context, "&e&l=== LocaleMan Test ===");
        send(context, "");
        send(context, "&7Loaded locales: &e" + String.join(", ", LocaleUtil.getLoadedLocales()));

        String welcome = LocaleUtil.getRaw(LocaleUtil.DEFAULT_LOCALE, "messages.welcome");
        send(context, "&7Raw welcome key: &f" + welcome);

        String goodbye = LocaleUtil.getRaw(LocaleUtil.DEFAULT_LOCALE, "messages.goodbye");
        send(context, "&7Raw goodbye key: &f" + goodbye);

        String prefix = LocaleUtil.getRaw(LocaleUtil.DEFAULT_LOCALE, "prefix");
        send(context, "&7Prefix: &f" + prefix);

        // AbstractPlayerCommand guarantees we have a player, so no instanceof check needed
        String playerLocale = LocaleUtil.getPlayerLocale(playerRef);
        send(context, "&7Your locale: &e" + playerLocale);
        send(context, "&7Personalized welcome:");

        // Assuming LocaleUtil.get returns a String
        Message personalMsg = LocaleUtil.get(playerRef, "messages.welcome", Map.of("player", playerRef.getUsername()));
        context.sendMessage(personalMsg); // Send directly if it's already formatted

        String missing = LocaleUtil.getRaw(LocaleUtil.DEFAULT_LOCALE, "this.key.does.not.exist");
        send(context, "&7Missing key test: &c" + missing);

        send(context, "");
        send(context, "&aAll locale tests completed!");
    }

    private void send(CommandContext ctx, String text) {
        ctx.sendMessage(ColorUtil.translation(text));
    }
}