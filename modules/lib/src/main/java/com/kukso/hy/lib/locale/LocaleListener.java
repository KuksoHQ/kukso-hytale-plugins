package com.kukso.hy.lib.locale;

import com.hypixel.hytale.event.IEventRegistry;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;

/**
 * Event listener for player locale management.
 * Handles caching player language on join and clearing on disconnect.
 */
public class LocaleListener {

    private final PluginBase plugin;

    public LocaleListener(PluginBase plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player connects.
     * Caches the player's locale for future lookups.
     *
     * @param event The player connect event
     */
    public void onPlayerConnect(PlayerConnectEvent event) {
        PlayerRef player = event.getPlayerRef();
        if (player == null) {
            return;
        }

        String locale = player.getLanguage();
        if (locale == null || locale.isEmpty()) {
            locale = LocaleMan.DEFAULT_LOCALE;
        }

        LocaleCache.set(player.getUuid(), locale);
    }

    /**
     * Called when a player disconnects.
     * Removes the player from the locale cache.
     *
     * @param event The player disconnected event
     */
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef player = event.getPlayerRef();
        if (player == null) {
            return;
        }

        LocaleCache.remove(player.getUuid());
    }

    /**
     * Registers this listener with the plugin.
     */
    public void register() {
        IEventRegistry eventRegistry = plugin.getEventRegistry();
        eventRegistry.register(PlayerConnectEvent.class, this::onPlayerConnect);
        eventRegistry.register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
    }

    /**
     * Unregisters this listener from the plugin.
     */
    public void unregister() {
        // Event unregistration handled by plugin shutdown
    }
}
