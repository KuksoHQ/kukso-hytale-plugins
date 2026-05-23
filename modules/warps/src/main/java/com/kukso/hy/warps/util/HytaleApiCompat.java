package com.kukso.hy.warps.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class HytaleApiCompat {

    private HytaleApiCompat() {
    }

    public static Position getPosition(Store<EntityStore> store, Ref<EntityStore> ref) {
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent == null) {
            return null;
        }

        Object position = invokeNoArg(transformComponent, "getPosition");
        return new Position(readDouble(position, "x"), readDouble(position, "y"), readDouble(position, "z"));
    }

    public static Rotation getHeadRotation(Store<EntityStore> store, Ref<EntityStore> ref) {
        HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
        if (headRotation == null) {
            return null;
        }

        Object rotation = invokeNoArg(headRotation, "getRotation");
        return new Rotation(readFloat(rotation, "getYaw", "y"), readFloat(rotation, "getPitch", "x"));
    }

    public static Teleport createTeleport(World world, double x, double y, double z, float yaw, float pitch) {
        for (Constructor<?> constructor : Teleport.class.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 3 && parameterTypes[0].isInstance(world)) {
                Object position = createVector3(parameterTypes[1], x, y, z);
                Object rotation = createVector3(parameterTypes[2], pitch, yaw, 0.0F);
                try {
                    return (Teleport) constructor.newInstance(world, position, rotation);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to create Teleport", e);
                }
            }
        }
        throw new IllegalStateException("No compatible Teleport constructor found");
    }

    private static Object createVector3(Class<?> vectorType, double x, double y, double z) {
        for (Constructor<?> constructor : vectorType.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 3
                    && parameterTypes[0] == double.class
                    && parameterTypes[1] == double.class
                    && parameterTypes[2] == double.class) {
                try {
                    return constructor.newInstance(x, y, z);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to create " + vectorType.getName(), e);
                }
            }
            if (parameterTypes.length == 3
                    && parameterTypes[0] == float.class
                    && parameterTypes[1] == float.class
                    && parameterTypes[2] == float.class) {
                try {
                    return constructor.newInstance((float) x, (float) y, (float) z);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to create " + vectorType.getName(), e);
                }
            }
        }
        throw new IllegalStateException("No compatible vector constructor found for " + vectorType.getName());
    }

    private static Object invokeNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to call " + methodName + " on " + target.getClass().getName(), e);
        }
    }

    private static double readDouble(Object target, String fieldName) {
        if (target == null) {
            throw new IllegalArgumentException("Cannot read " + fieldName + " from null vector");
        }

        String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        try {
            Field field = target.getClass().getField(fieldName);
            return ((Number) field.get(target)).doubleValue();
        } catch (ReflectiveOperationException ignored) {
            try {
                Method method = target.getClass().getMethod(getterName);
                return ((Number) method.invoke(target)).doubleValue();
            } catch (ReflectiveOperationException getterIgnored) {
                try {
                    Method method = target.getClass().getMethod(fieldName);
                    return ((Number) method.invoke(target)).doubleValue();
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to read " + fieldName + " from " + target.getClass().getName(), e);
                }
            }
        }
    }

    private static float readFloat(Object target, String semanticGetterName, String fieldName) {
        if (target == null) {
            throw new IllegalArgumentException("Cannot read " + fieldName + " from null vector");
        }

        try {
            Method method = target.getClass().getMethod(semanticGetterName);
            return ((Number) method.invoke(target)).floatValue();
        } catch (ReflectiveOperationException ignored) {
            return (float) readDouble(target, fieldName);
        }
    }

    public record Position(double x, double y, double z) {
    }

    public record Rotation(float yaw, float pitch) {
    }
}
