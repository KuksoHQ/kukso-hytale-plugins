package com.kukso.hy.lib.util;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Method;

/**
 * Utility class for handling Hytale's Entity Component System (ECS).
 * Uses reflection to work around potential API changes and generic type issues.
 */
public class HytaleUtils {

    /**
     * Retrieves a component from an entity in the EntityStore.
     * Uses reflection to handle potential API variations.
     *
     * @param store The EntityStore containing the entity
     * @param entityRef The entity reference (typically PlayerRef)
     * @param type The ComponentType to retrieve
     * @param <T> The component type
     * @return The component instance, or null if not found
     */
    public static <T extends Component<EntityStore>> T getComponent(
            EntityStore store,
            Object entityRef,
            ComponentType<EntityStore, T> type) {
        try {
            // Try: getComponent(Ref, ComponentType)
            Method m = store.getClass().getMethod("getComponent", entityRef.getClass(), ComponentType.class);
            return (T) m.invoke(store, entityRef, type);
        } catch (Exception e) {
            try {
                // Try: get(Ref, ComponentType)
                Method m = store.getClass().getMethod("get", entityRef.getClass(), ComponentType.class);
                return (T) m.invoke(store, entityRef, type);
            } catch (Exception ex) {
                System.err.println("Error getting component: " + ex.getMessage());
            }
        }
        return null;
    }

    /**
     * Adds or updates a component on an entity in the EntityStore.
     * Uses reflection to handle potential API variations.
     *
     * @param store The EntityStore containing the entity
     * @param entityRef The entity reference (typically PlayerRef)
     * @param type The ComponentType to add
     * @param component The component instance
     * @param <T> The component type
     */
    public static <T extends Component<EntityStore>> void addComponent(
            EntityStore store,
            Object entityRef,
            ComponentType<EntityStore, T> type,
            T component) {
        try {
            // Try: addComponent(Ref, ComponentType, Component)
            Method m = store.getClass().getMethod("addComponent",
                entityRef.getClass(), ComponentType.class, Component.class);
            m.invoke(store, entityRef, type, component);
        } catch (Exception e) {
            try {
                // Try: setComponent(Ref, ComponentType, Component)
                Method m = store.getClass().getMethod("setComponent",
                    entityRef.getClass(), ComponentType.class, Component.class);
                m.invoke(store, entityRef, type, component);
            } catch (Exception ex) {
                try {
                    // Try: set(Ref, ComponentType, Component)
                    Method m = store.getClass().getMethod("set",
                        entityRef.getClass(), ComponentType.class, Component.class);
                    m.invoke(store, entityRef, type, component);
                } catch (Exception exc) {
                    System.err.println("Error adding component: " + exc.getMessage());
                }
            }
        }
    }

    /**
     * Checks if an entity has a specific component.
     *
     * @param store The EntityStore containing the entity
     * @param entityRef The entity reference
     * @param type The ComponentType to check
     * @param <T> The component type
     * @return true if the component exists
     */
    public static <T extends Component<EntityStore>> boolean hasComponent(
            EntityStore store,
            Object entityRef,
            ComponentType<EntityStore, T> type) {
        return getComponent(store, entityRef, type) != null;
    }
}
