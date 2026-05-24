package com.pathmind.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class LevelCompatibilityBridge {
    private LevelCompatibilityBridge() {
    }

    public static int minBuildHeight(Object level) {
        Integer minBuildHeight = invokeInt(level, "getMinBuildHeight");
        if (minBuildHeight != null) {
            return minBuildHeight;
        }
        Integer minY = invokeInt(level, "getMinY");
        return minY != null ? minY : -64;
    }

    public static Object recipeManager(Object level) {
        Object manager = invoke(level, "getRecipeManager");
        if (manager != null) {
            return manager;
        }
        return invoke(level, "recipeAccess");
    }

    private static Integer invokeInt(Object target, String methodName) {
        Object value = invoke(target, methodName);
        return value instanceof Integer integer ? integer : null;
    }

    private static Object invoke(Object target, String methodName) {
        if (target == null || methodName == null || methodName.isEmpty()) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }
}
