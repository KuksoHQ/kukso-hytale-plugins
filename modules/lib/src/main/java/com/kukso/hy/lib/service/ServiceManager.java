package com.kukso.hy.lib.service;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.PluginBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central registry for all KuksoLib services.
 * Manages registration, priority, and retrieval of service providers.
 *
 * <p>Uses LAZY ACTIVATION pattern:</p>
 * <ul>
 *   <li>Economy, Permission, and Chat modules are dormant by default</li>
 *   <li>Modules only activate when a plugin registers to use that service</li>
 *   <li>No event listeners or ECS components are loaded until needed</li>
 *   <li>Servers that don't need all features have zero overhead from unused modules</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Registering a provider
 * ServiceManager.register(Economy.class, this, myEconomyImpl, ServicePriority.NORMAL);
 *
 * // Retrieving a provider
 * Economy econ = ServiceManager.getProvider(Economy.class);
 * if (econ != null) {
 *     econ.deposit(player, 100.0);
 * }
 * }</pre>
 */
public final class ServiceManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    /**
     * Map of service class to sorted set of providers (sorted by priority, highest first).
     */
    private static final Map<Class<?>, TreeSet<ServiceProvider<?>>> providers = new ConcurrentHashMap<>();

    /**
     * Comparator for sorting providers by priority (highest first), then by plugin name.
     */
    private static final Comparator<ServiceProvider<?>> PROVIDER_COMPARATOR = (a, b) -> {
        // Higher priority first
        int priorityCompare = Integer.compare(b.getPriority().ordinal(), a.getPriority().ordinal());
        if (priorityCompare != 0) return priorityCompare;
        // Then by plugin name for consistency
        return a.getPluginName().compareTo(b.getPluginName());
    };

    // ==================== Lazy Activation State ====================

    private static final AtomicBoolean economyActive = new AtomicBoolean(false);
    private static final AtomicBoolean permissionActive = new AtomicBoolean(false);
    private static final AtomicBoolean chatActive = new AtomicBoolean(false);

    // Activation callbacks
    private static Runnable onEconomyActivate;
    private static Runnable onPermissionActivate;
    private static Runnable onChatActivate;

    private ServiceManager() {
        // Static utility class
    }

    // ==================== Registration ====================

    /**
     * Registers a service provider.
     *
     * @param serviceClass The service interface class (e.g., Economy.class)
     * @param plugin       The plugin registering the provider
     * @param provider     The service implementation
     * @param priority     Registration priority
     * @param <T>          Service type
     */
    public static <T> void register(
            @Nonnull Class<T> serviceClass,
            @Nonnull PluginBase plugin,
            @Nonnull T provider,
            @Nonnull ServicePriority priority) {

        Objects.requireNonNull(serviceClass, "Service class cannot be null");
        Objects.requireNonNull(plugin, "Plugin cannot be null");
        Objects.requireNonNull(provider, "Provider cannot be null");
        Objects.requireNonNull(priority, "Priority cannot be null");

        ServiceProvider<T> serviceProvider = new ServiceProvider<>(plugin, provider, priority);

        providers.computeIfAbsent(serviceClass, k -> new TreeSet<>(PROVIDER_COMPARATOR))
                .add(serviceProvider);

        LOGGER.atInfo().log("Registered " + serviceClass.getSimpleName() + " provider: " +
            provider.getClass().getSimpleName() + " from " + plugin.getIdentifier().getName() +
            " (priority: " + priority + ")");

        // Check for module activation
        checkModuleActivation(serviceClass, plugin);
    }

    /**
     * Checks if a module should be activated on first provider registration.
     */
    private static <T> void checkModuleActivation(Class<T> serviceClass, PluginBase plugin) {
        String serviceName = serviceClass.getSimpleName();

        // Check Economy activation
        if (serviceName.equals("Economy") && economyActive.compareAndSet(false, true)) {
            LOGGER.atInfo().log("Economy module activated by " + plugin.getIdentifier().getName());
            if (onEconomyActivate != null) {
                onEconomyActivate.run();
            }
        }

        // Check Permission activation
        if (serviceName.equals("Permission") && permissionActive.compareAndSet(false, true)) {
            LOGGER.atInfo().log("Permission module activated by " + plugin.getIdentifier().getName());
            if (onPermissionActivate != null) {
                onPermissionActivate.run();
            }
        }

        // Check Chat activation
        if (serviceName.equals("Chat") && chatActive.compareAndSet(false, true)) {
            LOGGER.atInfo().log("Chat module activated by " + plugin.getIdentifier().getName());
            if (onChatActivate != null) {
                onChatActivate.run();
            }
        }
    }

    // ==================== Retrieval ====================

    /**
     * Gets the highest priority provider for a service.
     *
     * @param serviceClass The service interface class
     * @param <T>          Service type
     * @return The highest priority provider, or null if none registered
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getProvider(@Nonnull Class<T> serviceClass) {
        TreeSet<ServiceProvider<?>> set = providers.get(serviceClass);
        if (set == null || set.isEmpty()) {
            return null;
        }
        return (T) set.first().getProvider();
    }

    /**
     * Gets all registered providers for a service type, sorted by priority.
     *
     * @param serviceClass The service interface class
     * @param <T>          Service type
     * @return List of all providers (highest priority first), or empty list if none
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T> List<T> getProviders(@Nonnull Class<T> serviceClass) {
        TreeSet<ServiceProvider<?>> set = providers.get(serviceClass);
        if (set == null) {
            return List.of();
        }
        List<T> result = new ArrayList<>();
        for (ServiceProvider<?> sp : set) {
            result.add((T) sp.getProvider());
        }
        return result;
    }

    /**
     * Gets all registered ServiceProvider wrappers for a service type.
     *
     * @param serviceClass The service interface class
     * @param <T>          Service type
     * @return List of all ServiceProvider wrappers
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T> List<ServiceProvider<T>> getServiceProviders(@Nonnull Class<T> serviceClass) {
        TreeSet<ServiceProvider<?>> set = providers.get(serviceClass);
        if (set == null) {
            return List.of();
        }
        List<ServiceProvider<T>> result = new ArrayList<>();
        for (ServiceProvider<?> sp : set) {
            result.add((ServiceProvider<T>) sp);
        }
        return result;
    }

    /**
     * Checks if any provider is registered for a service.
     *
     * @param serviceClass The service interface class
     * @return true if at least one provider is registered
     */
    public static boolean hasProvider(@Nonnull Class<?> serviceClass) {
        TreeSet<ServiceProvider<?>> set = providers.get(serviceClass);
        return set != null && !set.isEmpty();
    }

    // ==================== Unregistration ====================

    /**
     * Unregisters all services from a specific plugin.
     *
     * @param plugin The plugin to unregister
     */
    public static void unregisterAll(@Nonnull PluginBase plugin) {
        Objects.requireNonNull(plugin, "Plugin cannot be null");

        for (TreeSet<ServiceProvider<?>> set : providers.values()) {
            set.removeIf(sp -> sp.getPlugin().equals(plugin));
        }

        LOGGER.atInfo().log("Unregistered all services from " + plugin.getIdentifier().getName());
    }

    /**
     * Unregisters a specific provider from a service.
     *
     * @param serviceClass The service interface class
     * @param plugin       The plugin that registered the provider
     * @param <T>          Service type
     */
    public static <T> void unregister(@Nonnull Class<T> serviceClass, @Nonnull PluginBase plugin) {
        TreeSet<ServiceProvider<?>> set = providers.get(serviceClass);
        if (set != null) {
            set.removeIf(sp -> sp.getPlugin().equals(plugin));
        }
    }

    // ==================== Module Activation Callbacks ====================

    /**
     * Sets a callback to run when the Economy module is first activated.
     * This allows lazy initialization of ECS components, etc.
     *
     * @param callback The callback to run on activation
     */
    public static void onEconomyActivate(@Nullable Runnable callback) {
        onEconomyActivate = callback;
    }

    /**
     * Sets a callback to run when the Permission module is first activated.
     *
     * @param callback The callback to run on activation
     */
    public static void onPermissionActivate(@Nullable Runnable callback) {
        onPermissionActivate = callback;
    }

    /**
     * Sets a callback to run when the Chat module is first activated.
     *
     * @param callback The callback to run on activation
     */
    public static void onChatActivate(@Nullable Runnable callback) {
        onChatActivate = callback;
    }

    // ==================== Status ====================

    /**
     * Checks if the Economy module has been activated.
     *
     * @return true if an economy provider has been registered
     */
    public static boolean isEconomyActive() {
        return economyActive.get();
    }

    /**
     * Checks if the Permission module has been activated.
     *
     * @return true if a permission provider has been registered
     */
    public static boolean isPermissionActive() {
        return permissionActive.get();
    }

    /**
     * Checks if the Chat module has been activated.
     *
     * @return true if a chat provider has been registered
     */
    public static boolean isChatActive() {
        return chatActive.get();
    }

    /**
     * Gets the total number of registered providers across all service types.
     *
     * @return Total provider count
     */
    public static int getTotalProviderCount() {
        return providers.values().stream()
            .mapToInt(Set::size)
            .sum();
    }

    /**
     * Gets all registered service types.
     *
     * @return Set of service classes with registered providers
     */
    @Nonnull
    public static Set<Class<?>> getRegisteredServiceTypes() {
        return Collections.unmodifiableSet(providers.keySet());
    }

    // ==================== Shutdown ====================

    /**
     * Clears all registered services and resets module activation state.
     * Called during plugin shutdown.
     */
    public static void shutdown() {
        providers.clear();

        economyActive.set(false);
        permissionActive.set(false);
        chatActive.set(false);

        onEconomyActivate = null;
        onPermissionActivate = null;
        onChatActivate = null;

        LOGGER.atInfo().log("ServiceManager shutdown complete");
    }
}
