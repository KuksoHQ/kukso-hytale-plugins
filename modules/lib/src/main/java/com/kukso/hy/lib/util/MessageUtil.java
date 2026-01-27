package com.kukso.hy.lib.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;

/**
 * Utility class for sending formatted, unformatted, translated
 * messages to the CommandContext or a PlayerRef.
 */
public class MessageUtil {

    /**
     * Formats and sends a translation message to the CommandContext.
     */
    public void sendTranslationFormat(CommandContext ctx, String text) {
        ctx.sendMessage(ColorUtil.translation(text));
    }

    /**
     * Formats and sends a translation message to the PlayerRef.
     */
    public void sendTranslationFormat(PlayerRef player, String text) {
        player.sendMessage(ColorUtil.translation(text));
    }

    /**
     * Sends an unformatted translation message to the CommandContext.
     */
    public void sendTranslationUnformat(CommandContext ctx, String text) {

        ctx.sendMessage(message);
    }

    /**
     * Sends an unformatted translation message to the PlayerRef.
     */
    public void sendTranslationUnformat(PlayerRef player, String text) {

        player.sendMessage(message);
    }

    /**
     * Sends an formatted String message to the CommandContext.
     */
    public void sendStringformat(CommandContext ctx, String text) {

    }

    /**
     * Sends an formatted String message to the PlayerRef.
     */
    public void sendStringformat(PlayerRef player, String text) {

    }

    /**
     * Sends an unformatted String message to the CommandContext.
     */
    public void sendStringUnformat(CommandContext ctx, String text) {

    }

    /**
     * Sends an unformatted String message to the PlayerRef.
     */
    public void sendStringUnformat(PlayerRef player, String text) {

    }
}
