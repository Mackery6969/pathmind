package com.pathmind.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.SharedConstants;

public final class GameVersionCompatibilityBridge {
    private GameVersionCompatibilityBridge() {
    }

    public static String currentMinecraftVersion() {
        Object version = SharedConstants.getCurrentVersion();
        String name = invokeString(version, "getName");
        if (name != null && !name.isBlank()) {
            return name;
        }
        name = invokeString(version, "name");
        if (name != null && !name.isBlank()) {
            return name;
        }
        name = invokeString(version, "getId");
        if (name != null && !name.isBlank()) {
            return name;
        }
        name = invokeString(version, "id");
        return name != null && !name.isBlank() ? name : "unknown";
    }

    private static String invokeString(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            method.setAccessible(true);
            Object result = method.invoke(target);
            return result instanceof String string ? string : null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }
}
