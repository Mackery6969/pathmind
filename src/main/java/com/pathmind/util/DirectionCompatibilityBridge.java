package com.pathmind.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public final class DirectionCompatibilityBridge {
    private static final Method APPROXIMATE_NEAREST = resolve("getApproximateNearest");
    private static final Method NEAREST = resolve("getNearest");

    private DirectionCompatibilityBridge() {
    }

    public static Direction nearest(double x, double y, double z) {
        Direction direction = invoke(APPROXIMATE_NEAREST, x, y, z);
        if (direction != null) {
            return direction;
        }
        direction = invoke(NEAREST, x, y, z);
        return direction != null ? direction : Direction.UP;
    }

    public static Vec3i normal(Direction direction) {
        if (direction == null) {
            return Vec3i.ZERO;
        }
        Vec3i normal = invokeNoArg(direction, "getNormal");
        if (normal != null) {
            return normal;
        }
        normal = invokeNoArg(direction, "getUnitVec3i");
        return normal != null ? normal : Vec3i.ZERO;
    }

    private static Method resolve(String name) {
        try {
            Method method = Direction.class.getMethod(name, double.class, double.class, double.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static Direction invoke(Method method, double x, double y, double z) {
        if (method == null) {
            return null;
        }
        try {
            Object result = method.invoke(null, x, y, z);
            return result instanceof Direction direction ? direction : null;
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static Vec3i invokeNoArg(Direction direction, String methodName) {
        try {
            Method method = Direction.class.getMethod(methodName);
            method.setAccessible(true);
            Object result = method.invoke(direction);
            return result instanceof Vec3i vec ? vec : null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }
}
