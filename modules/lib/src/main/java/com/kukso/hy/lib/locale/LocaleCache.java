package com.kukso.hy.lib.locale;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe cache for player language preferences.
 * Maps player UUIDs to their locale codes (e.g., "en_US", "tr_TR").
 */
public final class LocaleCache {

    private static final ConcurrentHashMap<UUID, String> cache = new ConcurrentHashMap<>();

    private LocaleCache() {
        // Utility class
    }

    /**
     * Sets the locale for a player.
     *
     * @param uuid   The player's UUID
     * @param locale The player's locale code (e.g., "en_US")
     */
    public static void set(UUID uuid, String locale) {
        if (uuid == null || locale == null) {
            return;
        }
        cache.put(uuid, locale);
    }

    /**
     * Gets the locale for a player.
     *
     * @param uuid The player's UUID
     * @return The player's locale, or null if not cached
     */
    public static String get(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return cache.get(uuid);
    }

    /**
     * Removes a player from the cache (called on disconnect).
     *
     * @param uuid The player's UUID
     */
    public static void remove(UUID uuid) {
        if (uuid == null) {
            return;
        }
        cache.remove(uuid);
    }

    /**
     * Checks if a player is in the cache.
     *
     * @param uuid The player's UUID
     * @return true if the player is cached, false otherwise
     */
    public static boolean contains(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return cache.containsKey(uuid);
    }

    /**
     * Clears all cached locales.
     * Used during reload to force refresh.
     */
    public static void clear() {
        cache.clear();
    }

    /**
     * Gets the number of cached players.
     *
     * @return The cache size
     */
    public static int size() {
        return cache.size();
    }
}
