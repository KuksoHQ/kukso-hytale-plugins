package com.kukso.hy.lib.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.kukso.hy.lib.locale.PlaceholderResolver;

import java.util.Map;

/**
 * Public utility class for sending formatted, unformatted and
 * translated messages to the CommandContext or a PlayerRef.
 */
public class MessageUtil {

    /**
     * Formats and sends a translation message to the CommandContext with placeholders.
     */
    public void sendTranslationFormatted(CommandContext ctx, String text, Map<String, String> placeholders) {
        PlayerRef player = new HytaleUtil().getPlayerRef(ctx);
        if (player != null) {
            ctx.sendMessage(LocaleUtil.get(player, text, placeholders));
        } else {
            String translation = LocaleUtil.getRaw(LocaleUtil.DEFAULT_LOCALE, text);
            translation = PlaceholderResolver.resolve(translation, placeholders);
            ctx.sendMessage(ColorUtil.format(translation));
        }
    }

    /**
     * Formats and sends a translation message to the PlayerRef with placeholders.
     */
    public void sendTranslationFormatted(PlayerRef player, String text, Map<String, String> placeholders) {
        player.sendMessage(LocaleUtil.get(player, text, placeholders));
    }

    /**
     * Formats and sends a translation message to the CommandContext.
     */
    public void sendTranslationFormatted(CommandContext ctx, String text) {
        PlayerRef player = new HytaleUtil().getPlayerRef(ctx);
        if (player != null) {
            ctx.sendMessage(LocaleUtil.get(player, text));
        } else {
            String translation = LocaleUtil.getRaw(LocaleUtil.DEFAULT_LOCALE, text);
            ctx.sendMessage(ColorUtil.format(translation));
        }
    }

    /**
     * Formats and sends a translation message to the PlayerRef.
     */
    public void sendTranslationFormatted(PlayerRef player, String text) {
        player.sendMessage(LocaleUtil.get(player, text));
    }

    /**
     * Sends an unformatted translation message to the CommandContext.
     */
    public void sendTranslationUnformatted(CommandContext ctx, String text) {
        PlayerRef player = new HytaleUtil().getPlayerRef(ctx);
        String locale = player != null ? LocaleUtil.getPlayerLocale(player) : LocaleUtil.DEFAULT_LOCALE;
        String translation = LocaleUtil.getRaw(locale, text);
        ctx.sendMessage(Message.raw(translation));
    }

    /**
     * Sends an unformatted translation message to the CommandContext with placeholders.
     */
    public void sendTranslationUnformatted(CommandContext ctx, String text, java.util.Map<String, String> placeholders) {
        PlayerRef player = new HytaleUtil().getPlayerRef(ctx);
        String locale = player != null ? LocaleUtil.getPlayerLocale(player) : LocaleUtil.DEFAULT_LOCALE;
        String translation = LocaleUtil.getRaw(locale, text);
        translation = PlaceholderResolver.resolve(translation, placeholders);
        ctx.sendMessage(Message.raw(translation));
    }

    /**
     * Sends an unformatted translation message to the PlayerRef.
     */
    public void sendTranslationUnformatted(PlayerRef player, String text) {
        String locale = LocaleUtil.getPlayerLocale(player);
        String translation = LocaleUtil.getRaw(locale, text);
        player.sendMessage(Message.raw(translation));
    }

    /**
     * Sends an unformatted translation message to the PlayerRef with placeholders.
     */
    public void sendTranslationUnformatted(PlayerRef player, String text, java.util.Map<String, String> placeholders) {
        String locale = LocaleUtil.getPlayerLocale(player);
        String translation = LocaleUtil.getRaw(locale, text);
        translation = PlaceholderResolver.resolve(translation, placeholders);
        player.sendMessage(Message.raw(translation));
    }

    /**
     * Sends an formatted String message to the CommandContext.
     */
    public void sendStringFormatted(CommandContext ctx, String text) {
        ctx.sendMessage(ColorUtil.format(text));
    }

    /**
     * Sends an formatted String message to the PlayerRef.
     */
    public void sendStringFormatted(PlayerRef player, String text) {
        player.sendMessage(ColorUtil.format(text));
    }

    /**
     * Sends an unformatted String message to the CommandContext.
     */
    public void sendStringUnformatted(CommandContext ctx, String text) {
        ctx.sendMessage(Message.raw(text));
    }

    /**
     * Sends an unformatted String message to the PlayerRef.
     */
    public void sendStringUnformatted(PlayerRef player, String text) {
        player.sendMessage(Message.raw(text));
    }
}
