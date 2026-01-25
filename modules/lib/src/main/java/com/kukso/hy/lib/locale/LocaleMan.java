package com.kukso.hy.lib.locale;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.kukso.hy.lib.util.ColorUtil;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main localization manager for KuksoLib.
 * Provides a fluent API for getting localized messages with color support.
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Basic usage
 * Message msg = LocaleMan.get(playerRef, "messages.welcome");
 *
 * // With placeholders
 * Message msg = LocaleMan.get(playerRef, "messages.welcome", Map.of("player", playerName));
 *
 * // Raw string for specific locale
 * String text = LocaleMan.getRaw("en_US", "messages.welcome");
 * }</pre>
 *
 * <h2>Fallback Chain:</h2>
 * <ol>
 *     <li>Player's locale (e.g., "tr_TR")</li>
 *     <li>Default locale ("en_US")</li>
 *     <li>Key itself (with warning logged)</li>
 * </ol>
 */
public final class LocaleMan {

    public static final String DEFAULT_LOCALE = "en_US";
    private static final String LOCALES_DIR = "locales";
    private static final String RESOURCE_PREFIX = "locales/";

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static PluginBase plugin;
    private static Path localesPath;
    private static LocaleListener listener;

    // Locale code -> (Key -> Value)
    private static volatile Map<String, Map<String, String>> translations = new ConcurrentHashMap<>();

    private LocaleMan() {
        // Utility class
    }

    /**
     * Initializes the localization system.
     * Should be called from Main.setup().
     *
     * @param pluginInstance The plugin instance
     * @param dataDirectory  The base directory for locale files
     */
    public static void init(PluginBase pluginInstance, Path dataDirectory) {
        plugin = pluginInstance;
        localesPath = dataDirectory.resolve(LOCALES_DIR);

        // Extract default locale if not present
        extractDefaultLocales();

        // Load all locales
        reload();

        // Register event listener
        listener = new LocaleListener(plugin);
        listener.register();

        LOGGER.atInfo().log("LocaleMan initialized with " + translations.size() + " locale(s)");
    }

    /**
     * Shuts down the localization system.
     * Should be called from Main.shutdown().
     */
    public static void shutdown() {
        if (listener != null) {
            listener.unregister();
            listener = null;
        }
        LocaleCache.clear();
        translations = new ConcurrentHashMap<>();
        plugin = null;

        LOGGER.atInfo().log("LocaleMan shutdown complete");
    }

    /**
     * Reloads all locale files from disk.
     * Thread-safe: builds new map then swaps reference.
     */
    public static void reload() {
        Map<String, Map<String, String>> newTranslations = LocaleLoader.loadAll(localesPath);

        // Ensure default locale exists
        if (!newTranslations.containsKey(DEFAULT_LOCALE)) {
            Map<String, String> defaultFromResource = LocaleLoader.loadFromResource(RESOURCE_PREFIX + DEFAULT_LOCALE + ".json");
            if (!defaultFromResource.isEmpty()) {
                newTranslations.put(DEFAULT_LOCALE, defaultFromResource);
            }
        }

        // Atomic swap
        translations = new ConcurrentHashMap<>(newTranslations);

        // Clear cache to force refresh on next access
        LocaleCache.clear();

        LOGGER.atInfo().log("Reloaded " + translations.size() + " locale(s)");
    }

    /**
     * Gets a localized message for a player.
     *
     * @param player The player
     * @param key    The translation key (dot-notation)
     * @return Localized Message with color codes translated
     */
    public static Message get(PlayerRef player, String key) {
        return get(player, key, Collections.emptyMap());
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
        String locale = getPlayerLocale(player);
        String raw = getRaw(locale, key);

        // Resolve placeholders
        if (!placeholders.isEmpty()) {
            raw = PlaceholderResolver.resolve(raw, placeholders);
        }

        // Translate color codes and return Message
        return ColorUtil.colorThis(raw);
    }

    /**
     * Gets a raw localized string for a specific locale.
     *
     * @param locale The locale code (e.g., "en_US")
     * @param key    The translation key (dot-notation)
     * @return Raw string value, or the key itself if not found
     */
    public static String getRaw(String locale, String key) {
        // Try requested locale
        Map<String, String> localeMap = translations.get(locale);
        if (localeMap != null) {
            String value = localeMap.get(key);
            if (value != null) {
                return value;
            }
        }

        // Fallback to default locale
        if (!DEFAULT_LOCALE.equals(locale)) {
            Map<String, String> defaultMap = translations.get(DEFAULT_LOCALE);
            if (defaultMap != null) {
                String value = defaultMap.get(key);
                if (value != null) {
                    return value;
                }
            }
        }

        // Return key itself as last resort
        LOGGER.atWarning().log("Missing translation key '" + key + "' for locale '" + locale + "'");
        return key;
    }

    /**
     * Gets the cached locale for a player, with lazy refresh.
     *
     * @param player The player
     * @return The player's locale code
     */
    public static String getPlayerLocale(PlayerRef player) {
        if (player == null) {
            return DEFAULT_LOCALE;
        }

        // Get current language from player (lazy refresh)
        String currentLocale = player.getLanguage();
        if (currentLocale == null || currentLocale.isEmpty()) {
            currentLocale = DEFAULT_LOCALE;
        }

        // Update cache if changed
        String cachedLocale = LocaleCache.get(player.getUuid());
        if (!currentLocale.equals(cachedLocale)) {
            LocaleCache.set(player.getUuid(), currentLocale);
        }

        return currentLocale;
    }

    /**
     * Checks if a locale is loaded.
     *
     * @param locale The locale code
     * @return true if the locale is loaded
     */
    public static boolean hasLocale(String locale) {
        return translations.containsKey(locale);
    }

    /**
     * Gets all loaded locale codes.
     *
     * @return Set of loaded locale codes
     */
    public static java.util.Set<String> getLoadedLocales() {
        return Collections.unmodifiableSet(translations.keySet());
    }

    /**
     * Extracts default locale files from resources to the plugin data folder.
     */
    private static void extractDefaultLocales() {
        LocaleLoader.extractResource(RESOURCE_PREFIX + DEFAULT_LOCALE + ".json",
                localesPath.resolve(DEFAULT_LOCALE + ".json"));
    }
}
