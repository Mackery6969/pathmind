package com.pathmind.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

public final class ItemCompatibilityBridge {
    private ItemCompatibilityBridge() {
    }

    public static String displayName(Item item) {
        Component component = component(item, "getDescription");
        if (component == null) {
            component = component(item, "getName");
        }
        return component != null ? component.getString() : "";
    }

    private static Component component(Item item, String methodName) {
        if (item == null) {
            return null;
        }
        try {
            Method method = item.getClass().getMethod(methodName);
            method.setAccessible(true);
            Object result = method.invoke(item);
            return result instanceof Component component ? component : null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }
}
