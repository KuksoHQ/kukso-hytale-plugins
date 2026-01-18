package com.kukso.hy.lib.service;

/**
 * Priority levels for service registration.
 * Higher priority providers are preferred when multiple providers are registered.
 *
 * <p>When multiple plugins register the same service type (Economy, Permission, Chat),
 * the provider with the highest priority is used as the active provider.</p>
 */
public enum ServicePriority {
    /**
     * Lowest priority - used for fallback/default implementations.
     * Will only be used if no other providers are registered.
     */
    LOWEST,

    /**
     * Low priority - below normal priority.
     */
    LOW,

    /**
     * Normal/standard priority - the default choice for most implementations.
     */
    NORMAL,

    /**
     * High priority - preferred over normal implementations.
     */
    HIGH,

    /**
     * Highest priority - override all other implementations.
     * Use sparingly; typically for server-specific customizations.
     */
    HIGHEST
}
