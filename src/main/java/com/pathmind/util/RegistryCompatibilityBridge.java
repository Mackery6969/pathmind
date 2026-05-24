package com.pathmind.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

public final class RegistryCompatibilityBridge {
    private RegistryCompatibilityBridge() {
    }

    public static Holder<VillagerProfession> villagerProfessionHolder(ResourceLocation id) {
        return holder(BuiltInRegistries.VILLAGER_PROFESSION, id, VillagerProfession.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> Holder<T> holder(Object registry, ResourceLocation id, Class<T> valueType) {
        if (registry == null || id == null || valueType == null) {
            return null;
        }
        Object result = invoke(registry, "getHolder", id);
        if (result == null) {
            result = invoke(registry, "get", id);
        }
        Object unwrapped = unwrap(result);
        if (unwrapped instanceof Holder<?> holder && valueType.isInstance(holder.value())) {
            return (Holder<T>) holder;
        }
        return null;
    }

    private static Object unwrap(Object value) {
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        return value;
    }

    private static Object invoke(Object target, String methodName, Object argument) {
        Method method = findMethod(target.getClass(), methodName, argument.getClass());
        if (method == null) {
            return null;
        }
        try {
            method.setAccessible(true);
            return method.invoke(target, argument);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static Method findMethod(Class<?> targetClass, String methodName, Class<?> argumentClass) {
        for (Method method : targetClass.getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                continue;
            }
            Class<?> parameterType = method.getParameterTypes()[0];
            if (parameterType.isAssignableFrom(argumentClass)) {
                return method;
            }
        }
        return null;
    }
}
