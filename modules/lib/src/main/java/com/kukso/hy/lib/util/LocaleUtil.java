package com.kukso.hy.lib.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.kukso.hy.lib.locale.LocaleMan;

import java.util.Map;
import java.util.Set;

/**
 * Public utility class for localization.
 * This is the main entry point for developers using the localization system.
 */
public class LocaleUtil {

    public static final String DEFAULT_LOCALE = LocaleMan.DEFAULT_LOCALE;

    private LocaleUtil() {
        // Utility class
    }

    /**
     * Gets a localized message for a player.
     *
     * @param player The player
     * @param key    The translation key (dot-notation)
     * @return Localized Message with color codes translated
     */
    public static Message get(PlayerRef player, String key) {
        return LocaleMan.get(player, key);
    }

    /**
     * Gets a localized message for a player with placeholder substitution.
     *
     * @param player       The player
     * @param key          The translation key (dot-notation)
     * @param placeholders Map of placeholder names to values
     * @return Localized Message with placeholders and color codes resolved
     */
    public static Message get(PlayerRef player, String key, Map<String, String> placeholders) {
        return LocaleMan.get(player, key, placeholders);
    }

    /**
     * Gets a raw localized string for a specific locale.
     *
     * @param locale The locale code (e.g., "en_US")
     * @param key    The translation key (dot-notation)
     * @return Raw string value, or the key itself if not found
     */
    public static String getRaw(String locale, String key) {
        return LocaleMan.getRaw(locale, key);
    }

    /**
     * Gets the cached locale for a player.
     *
     * @param player The player
     * @return The player's locale code
     */
    public static String getPlayerLocale(PlayerRef player) {
        return LocaleMan.getPlayerLocale(player);
    }

    /**
     * Checks if a locale is loaded.
     *
     * @param locale The locale code
     * @return true if the locale is loaded
     */
    public static boolean hasLocale(String locale) {
        return LocaleMan.hasLocale(locale);
    }

    /**
     * Gets all loaded locale codes.
     *
     * @return Set of loaded locale codes
     */
    public static Set<String> getLoadedLocales() {
        return LocaleMan.getLoadedLocales();
    }

    /**
     * Reloads all locale files from disk.
     */
    public static void reload() {
        LocaleMan.reload();
    }
}
