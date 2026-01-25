package com.kukso.hy.lib.util;

import com.hypixel.hytale.server.core.permissions.PermissionsModule;

import java.util.Collection;
import java.util.UUID;

/**
 * Utility class for permission checks using PermissionsModule.get().
 */
public class PermissionUtil {
    private PermissionUtil() {
    }

    /**
     * Checks if the player has the specified permission.
     *
     * @param uuid       the player's UUID
     * @param permission the permission to check
     * @return true if the player has the permission
     */
    public static boolean hasPermission(UUID uuid, String permission) {
        return PermissionsModule.get().hasPermission(uuid, permission);
    }

    /**
     * Checks if the player has any of the specified permissions.
     *
     * @param uuid        the player's UUID
     * @param permissions the permissions to check
     * @return true if the player has at least one of the permissions
     */
    public static boolean hasAnyPermission(UUID uuid, String... permissions) {
        PermissionsModule module = PermissionsModule.get();
        for (String perm : permissions) {
            if (module.hasPermission(uuid, perm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the player has any of the specified permissions.
     *
     * @param uuid        the player's UUID
     * @param permissions the permissions to check
     * @return true if the player has at least one of the permissions
     */
    public static boolean hasAnyPermission(UUID uuid, Collection<String> permissions) {
        PermissionsModule module = PermissionsModule.get();
        for (String perm : permissions) {
            if (module.hasPermission(uuid, perm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the player has all of the specified permissions.
     *
     * @param uuid        the player's UUID
     * @param permissions the permissions to check
     * @return true if the player has all of the permissions
     */
    public static boolean hasAllPermissions(UUID uuid, String... permissions) {
        PermissionsModule module = PermissionsModule.get();
        for (String perm : permissions) {
            if (!module.hasPermission(uuid, perm)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the player has all of the specified permissions.
     *
     * @param uuid        the player's UUID
     * @param permissions the permissions to check
     * @return true if the player has all of the permissions
     */
    public static boolean hasAllPermissions(UUID uuid, Collection<String> permissions) {
        PermissionsModule module = PermissionsModule.get();
        for (String perm : permissions) {
            if (!module.hasPermission(uuid, perm)) {
                return false;
            }
        }
        return true;
    }
}
