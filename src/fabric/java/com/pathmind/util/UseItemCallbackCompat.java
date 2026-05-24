package com.pathmind.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public final class UseItemCallbackCompat {
    private UseItemCallbackCompat() {
    }

    public static void register(Consumer<String> eventSink, String eventName) {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            eventSink.accept(eventName);
            return pass(player, hand);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T pass(Player player, InteractionHand hand) {
        Object typedResult = createTypedPassResult(player, hand);
        if (typedResult != null) {
            return (T) typedResult;
        }
        Object actionResult = resolveEnumValue("net.minecraft.world.InteractionResult", "PASS");
        if (actionResult != null) {
            return (T) actionResult;
        }
        return null;
    }

    private static Object createTypedPassResult(Player player, InteractionHand hand) {
        try {
            Class<?> holderClass = Class.forName("net.minecraft.world.InteractionResultHolder");
            Method passMethod = findSingleArgumentMethod(holderClass, "pass");
            if (passMethod == null) {
                return null;
            }
            passMethod.setAccessible(true);
            return passMethod.invoke(null, player != null && hand != null ? player.getItemInHand(hand) : null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static Method findSingleArgumentMethod(Class<?> targetClass, String methodName) throws NoSuchMethodException {
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                return method;
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    private static Object resolveEnumValue(String className, String valueName) {
        try {
            Class<?> enumClass = Class.forName(className);
            Object[] constants = enumClass.getEnumConstants();
            if (constants == null) {
                return null;
            }
            for (Object constant : constants) {
                if (constant instanceof Enum<?> enumValue && enumValue.name().equals(valueName)) {
                    return enumValue;
                }
            }
        } catch (ClassNotFoundException ignored) {
            return null;
        }
        return null;
    }
}
