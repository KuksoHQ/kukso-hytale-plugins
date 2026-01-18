package com.kukso.hy.lib.service;

import com.hypixel.hytale.server.core.plugin.PluginBase;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Wrapper class for a registered service provider.
 * Contains the provider implementation along with metadata about its registration.
 *
 * @param <T> The service interface type (Economy, Permission, or Chat)
 */
public class ServiceProvider<T> {

    private final PluginBase plugin;
    private final T provider;
    private final ServicePriority priority;

    /**
     * Creates a new ServiceProvider wrapper.
     *
     * @param plugin   The plugin that registered this provider
     * @param provider The actual service implementation
     * @param priority The priority of this registration
     */
    public ServiceProvider(@Nonnull PluginBase plugin, @Nonnull T provider, @Nonnull ServicePriority priority) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.provider = Objects.requireNonNull(provider, "Provider cannot be null");
        this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
    }

    /**
     * Gets the plugin that registered this provider.
     *
     * @return The registering plugin
     */
    @Nonnull
    public PluginBase getPlugin() {
        return plugin;
    }

    /**
     * Gets the service provider implementation.
     *
     * @return The provider implementation
     */
    @Nonnull
    public T getProvider() {
        return provider;
    }

    /**
     * Gets the priority of this registration.
     *
     * @return The registration priority
     */
    @Nonnull
    public ServicePriority getPriority() {
        return priority;
    }

    /**
     * Gets the name of the registering plugin.
     *
     * @return Plugin name
     */
    public String getPluginName() {
        return plugin.getIdentifier().getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceProvider<?> that = (ServiceProvider<?>) o;
        return Objects.equals(plugin, that.plugin) &&
               Objects.equals(provider, that.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plugin, provider);
    }

    @Override
    public String toString() {
        return "ServiceProvider{" +
            "plugin=" + getPluginName() +
            ", provider=" + provider.getClass().getSimpleName() +
            ", priority=" + priority +
            '}';
    }
}
