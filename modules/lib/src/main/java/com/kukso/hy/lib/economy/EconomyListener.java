package com.kukso.hy.lib.economy;

import com.hypixel.hytale.event.IEventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;

/**
 * Event listener for economy-related events.
 * Initializes wallet components when players join.
 */
public class EconomyListener {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final PluginBase plugin;
    private final EconomyManager economyManager;

    public EconomyListener(PluginBase plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    /**
     * Ensures a player has a wallet component when they connect.
     * Creates a new wallet with starting balance if one doesn't exist.
     *
     * @param event The player connect event
     */
    public void onPlayerConnect(PlayerConnectEvent event) {
        PlayerRef player = event.getPlayerRef();
        if (player != null) {
            try {
                economyManager.ensureWallet(player);
                LOGGER.atInfo().log("Initialized wallet for player: " + player.getUsername());
            } catch (Exception e) {
                LOGGER.atSevere().log("Failed to initialize wallet for " + player.getUsername() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Registers this listener with the plugin.
     */
    public void register() {
        IEventRegistry eventRegistry = plugin.getEventRegistry();
        eventRegistry.register(PlayerConnectEvent.class, this::onPlayerConnect);
    }

    /**
     * Unregisters this listener from the plugin.
     */
    public void unregister() {
        // Event unregistration handled by plugin shutdown
    }
}
