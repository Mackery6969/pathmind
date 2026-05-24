package com.pathmind.util;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;

/**
 * Bridges input helpers that shifted across 1.21.x.
 */
public final class InputCompatibilityBridge {
    private static final Method SCREEN_HAS_CONTROL_DOWN = resolveScreenMethod("hasControlDown");
    private static final Method SCREEN_HAS_SHIFT_DOWN = resolveScreenMethod("hasShiftDown");
    private static final Method IS_KEY_PRESSED_WINDOW = resolveIsKeyPressed(Window.class);
    private static final Method IS_KEY_PRESSED_HANDLE = resolveIsKeyPressed(long.class);
    private static final Method SCREEN_MOUSE_CLICKED = resolveScreenMouseMethod("mouseClicked");
    private static final Method SCREEN_MOUSE_RELEASED = resolveScreenMouseMethod("mouseReleased");
    private static final Method MOUSE_CURSOR_POS_CALLBACK = resolveMouseCursorPosCallback();
    private static final Method MOUSE_BUTTON_CALLBACK = resolveMouseButtonCallback();
    private static final Constructor<?> SCREEN_CLICK_CONSTRUCTOR = resolveScreenClickConstructor();
    private static final Constructor<?> MOUSE_INPUT_CONSTRUCTOR = resolveMouseInputConstructor();

    private InputCompatibilityBridge() {
    }

    public static boolean hasControlDown() {
        Boolean screenValue = invokeScreenBoolean(SCREEN_HAS_CONTROL_DOWN);
        if (screenValue != null) {
            return screenValue;
        }
        Minecraft client = Minecraft.getInstance();
        return isKeyPressed(client, InputConstants.KEY_LCONTROL)
            || isKeyPressed(client, InputConstants.KEY_RCONTROL);
    }

    public static boolean hasShiftDown() {
        Boolean screenValue = invokeScreenBoolean(SCREEN_HAS_SHIFT_DOWN);
        if (screenValue != null) {
            return screenValue;
        }
        Minecraft client = Minecraft.getInstance();
        return isKeyPressed(client, InputConstants.KEY_LSHIFT)
            || isKeyPressed(client, InputConstants.KEY_RSHIFT);
    }

    public static boolean isKeyPressed(Minecraft client, int keyCode) {
        if (client == null) {
            return false;
        }
        Window window = client.getWindow();
        if (window == null) {
            return false;
        }
        if (IS_KEY_PRESSED_WINDOW != null) {
            try {
                Object result = IS_KEY_PRESSED_WINDOW.invoke(null, window, keyCode);
                return result instanceof Boolean value && value;
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                return false;
            }
        }
        if (IS_KEY_PRESSED_HANDLE != null) {
            try {
                Object result = IS_KEY_PRESSED_HANDLE.invoke(null, window.getWindow(), keyCode);
                return result instanceof Boolean value && value;
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                return false;
            }
        }
        return GLFW.glfwGetKey(window.getWindow(), keyCode) == GLFW.GLFW_PRESS;
    }

    public static boolean isMouseButtonPressed(Minecraft client, int buttonCode) {
        if (client == null) {
            return false;
        }
        Window window = client.getWindow();
        if (window == null) {
            return false;
        }
        return GLFW.glfwGetMouseButton(window.getWindow(), buttonCode) == GLFW.GLFW_PRESS;
    }

    public static boolean dispatchMouseButton(Minecraft client, int buttonCode, int action, int mods) {
        if (client == null) {
            return false;
        }
        Window window = client.getWindow();
        if (window == null) {
            return false;
        }
        MouseHandler mouse = client.mouseHandler;
        if (mouse == null || MOUSE_BUTTON_CALLBACK == null) {
            return false;
        }
        try {
            Class<?>[] parameterTypes = MOUSE_BUTTON_CALLBACK.getParameterTypes();
            if (parameterTypes.length == 4 && parameterTypes[1] == int.class) {
                MOUSE_BUTTON_CALLBACK.invoke(mouse, window.getWindow(), buttonCode, action, mods);
                return true;
            }
            if (parameterTypes.length == 3
                && parameterTypes[0] == long.class
                && parameterTypes[2] == int.class
                && MOUSE_INPUT_CONSTRUCTOR != null) {
                Object mouseInput = MOUSE_INPUT_CONSTRUCTOR.newInstance(buttonCode, action);
                MOUSE_BUTTON_CALLBACK.invoke(mouse, window.getWindow(), mouseInput, mods);
                return true;
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }
        return false;
    }

    public static boolean dispatchCursorPos(Minecraft client, double x, double y) {
        if (client == null) {
            return false;
        }
        Window window = client.getWindow();
        if (window == null) {
            return false;
        }
        MouseHandler mouse = client.mouseHandler;
        if (mouse == null || MOUSE_CURSOR_POS_CALLBACK == null) {
            return false;
        }
        GLFW.glfwSetCursorPos(window.getWindow(), x, y);
        try {
            MOUSE_CURSOR_POS_CALLBACK.invoke(mouse, window.getWindow(), x, y);
            return true;
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }
    }

    public static boolean dispatchScreenMouseClicked(Screen screen, double x, double y, int button) {
        return dispatchScreenMouseEvent(screen, SCREEN_MOUSE_CLICKED, x, y, button, true);
    }

    public static boolean dispatchScreenMouseReleased(Screen screen, double x, double y, int button) {
        return dispatchScreenMouseEvent(screen, SCREEN_MOUSE_RELEASED, x, y, button, false);
    }

    public static boolean mouseClicked(Object target, double x, double y, int button) {
        return invokeMouseEvent(target, "mouseClicked", x, y, button, true, 0.0D, 0.0D);
    }

    public static boolean mouseReleased(Object target, double x, double y, int button) {
        return invokeMouseEvent(target, "mouseReleased", x, y, button, true, 0.0D, 0.0D);
    }

    public static boolean mouseDragged(Object target, double x, double y, int button, double deltaX, double deltaY) {
        return invokeMouseEvent(target, "mouseDragged", x, y, button, true, deltaX, deltaY);
    }

    public static boolean keyPressed(Object target, int keyCode, int scanCode, int modifiers) {
        if (target == null) {
            return false;
        }
        try {
            Method legacy = target.getClass().getMethod("keyPressed", int.class, int.class, int.class);
            Object result = legacy.invoke(target, keyCode, scanCode, modifiers);
            return result instanceof Boolean value && value;
        } catch (NoSuchMethodException ignored) {
            // Try event-based input below.
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }
        Object event = createKeyEvent(keyCode, scanCode, modifiers);
        if (event == null) {
            return false;
        }
        Method method = findMethod(target.getClass(), "keyPressed", event.getClass());
        if (method == null) {
            return false;
        }
        try {
            Object result = method.invoke(target, event);
            return result instanceof Boolean value && value;
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }
    }

    public static boolean charTyped(Object target, char chr, int modifiers) {
        if (target == null) {
            return false;
        }
        try {
            Method legacy = target.getClass().getMethod("charTyped", char.class, int.class);
            Object result = legacy.invoke(target, chr, modifiers);
            return result instanceof Boolean value && value;
        } catch (NoSuchMethodException ignored) {
            // Try event-based input below.
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }
        Object event = createCharacterEvent(chr, modifiers);
        if (event == null) {
            return false;
        }
        Method method = findMethod(target.getClass(), "charTyped", event.getClass());
        if (method == null) {
            return false;
        }
        try {
            Object result = method.invoke(target, event);
            return result instanceof Boolean value && value;
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }
    }

    private static Method resolveScreenMethod(String name) {
        try {
            Method method = Screen.class.getMethod(name);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static boolean invokeMouseEvent(Object target, String methodName, double x, double y, int button,
                                            boolean inBounds, double deltaX, double deltaY) {
        if (target == null) {
            return false;
        }
        try {
            Method legacy;
            if ("mouseDragged".equals(methodName)) {
                legacy = target.getClass().getMethod(methodName, double.class, double.class, int.class, double.class, double.class);
                Object result = legacy.invoke(target, x, y, button, deltaX, deltaY);
                return result instanceof Boolean value && value;
            }
            legacy = target.getClass().getMethod(methodName, double.class, double.class, int.class);
            Object result = legacy.invoke(target, x, y, button);
            return result instanceof Boolean value && value;
        } catch (NoSuchMethodException ignored) {
            // Try event-based input below.
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }

        Object event = createMouseButtonEvent(x, y, button, 0);
        if (event == null) {
            return false;
        }
        Method method = findMouseEventMethod(target.getClass(), methodName, event.getClass());
        if (method == null) {
            return false;
        }
        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object result;
            if (parameterTypes.length == 3) {
                result = method.invoke(target, event, deltaX, deltaY);
            } else if (parameterTypes.length == 2) {
                result = method.invoke(target, event, inBounds);
            } else {
                result = method.invoke(target, event);
            }
            return result instanceof Boolean value && value;
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }
    }

    private static Method findMouseEventMethod(Class<?> targetClass, String methodName, Class<?> eventClass) {
        for (Method method : targetClass.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(eventClass)) {
                return method;
            }
            if (parameterTypes.length == 2
                && parameterTypes[0].isAssignableFrom(eventClass)
                && parameterTypes[1] == boolean.class) {
                return method;
            }
            if (parameterTypes.length == 3
                && parameterTypes[0].isAssignableFrom(eventClass)
                && parameterTypes[1] == double.class
                && parameterTypes[2] == double.class) {
                return method;
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> targetClass, String methodName, Class<?> eventClass) {
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(methodName)
                && method.getParameterCount() == 1
                && method.getParameterTypes()[0].isAssignableFrom(eventClass)) {
                return method;
            }
        }
        return null;
    }

    private static Object createMouseButtonEvent(double x, double y, int button, int modifiers) {
        try {
            Class<?> infoClass = Class.forName("net.minecraft.client.input.MouseButtonInfo");
            Constructor<?> infoConstructor = infoClass.getConstructor(int.class, int.class);
            Object info = infoConstructor.newInstance(button, modifiers);
            Class<?> eventClass = Class.forName("net.minecraft.client.input.MouseButtonEvent");
            Constructor<?> eventConstructor = eventClass.getConstructor(double.class, double.class, infoClass);
            return eventConstructor.newInstance(x, y, info);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static Object createKeyEvent(int keyCode, int scanCode, int modifiers) {
        try {
            Class<?> eventClass = Class.forName("net.minecraft.client.input.KeyEvent");
            Constructor<?> constructor = eventClass.getConstructor(int.class, int.class, int.class);
            return constructor.newInstance(keyCode, scanCode, modifiers);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static Object createCharacterEvent(char chr, int modifiers) {
        try {
            Class<?> eventClass = Class.forName("net.minecraft.client.input.CharacterEvent");
            Constructor<?> constructor = eventClass.getConstructor(int.class, int.class);
            return constructor.newInstance((int) chr, modifiers);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static Boolean invokeScreenBoolean(Method method) {
        if (method == null) {
            return null;
        }
        try {
            Object result = method.invoke(null);
            return result instanceof Boolean value ? value : null;
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static Method resolveIsKeyPressed(Class<?> firstParam) {
        try {
            Method method = InputConstants.class.getMethod("isKeyPressed", firstParam, int.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static Method resolveScreenMouseMethod(String name) {
        for (Method method : Screen.class.getMethods()) {
            if (!method.getName().equals(name)) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 3
                && parameterTypes[0] == double.class
                && parameterTypes[1] == double.class
                && parameterTypes[2] == int.class) {
                method.setAccessible(true);
                return method;
            }
            if (name.equals("mouseClicked")
                && parameterTypes.length == 2
                && parameterTypes[1] == boolean.class
                && isScreenClickClass(parameterTypes[0])) {
                method.setAccessible(true);
                return method;
            }
            if (name.equals("mouseReleased")
                && parameterTypes.length == 1
                && isScreenClickClass(parameterTypes[0])) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    private static boolean dispatchScreenMouseEvent(Screen screen, Method method, double x, double y, int button, boolean inBounds) {
        if (screen == null || method == null) {
            return false;
        }
        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 3
                && parameterTypes[0] == double.class
                && parameterTypes[1] == double.class
                && parameterTypes[2] == int.class) {
                Object result = method.invoke(screen, x, y, button);
                return !(result instanceof Boolean value) || value;
            }
            Object click = createScreenClick(x, y, button);
            if (click == null) {
                return false;
            }
            Object result;
            if (parameterTypes.length == 2) {
                result = method.invoke(screen, click, inBounds);
            } else {
                result = method.invoke(screen, click);
            }
            return !(result instanceof Boolean value) || value;
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }
    }

    private static Constructor<?> resolveScreenClickConstructor() {
        try {
            Class<?> clickClass = Class.forName("net.minecraft.client.gui.Click");
            for (Constructor<?> constructor : clickClass.getDeclaredConstructors()) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length != 3) {
                    continue;
                }
                int doubleCount = 0;
                int intCount = 0;
                for (Class<?> parameterType : parameterTypes) {
                    if (parameterType == double.class) {
                        doubleCount++;
                    } else if (parameterType == int.class) {
                        intCount++;
                    }
                }
                if (doubleCount == 2 && intCount == 1) {
                    constructor.setAccessible(true);
                    return constructor;
                }
            }
        } catch (ClassNotFoundException ignored) {
            return null;
        }
        return null;
    }

    private static boolean isScreenClickClass(Class<?> type) {
        return type != null && "net.minecraft.client.gui.Click".equals(type.getName());
    }

    private static Object createScreenClick(double x, double y, int button) {
        if (SCREEN_CLICK_CONSTRUCTOR == null) {
            return null;
        }
        Class<?>[] parameterTypes = SCREEN_CLICK_CONSTRUCTOR.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        boolean xAssigned = false;
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == double.class) {
                arguments[i] = xAssigned ? y : x;
                xAssigned = true;
            } else if (parameterType == int.class) {
                arguments[i] = button;
            } else {
                return null;
            }
        }
        try {
            return SCREEN_CLICK_CONSTRUCTOR.newInstance(arguments);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static Method resolveMouseButtonCallback() {
        for (Method method : MouseHandler.class.getDeclaredMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (method.getReturnType() != void.class) {
                continue;
            }
            if (parameterTypes.length == 4
                && parameterTypes[0] == long.class
                && parameterTypes[1] == int.class
                && parameterTypes[2] == int.class
                && parameterTypes[3] == int.class) {
                method.setAccessible(true);
                return method;
            }
            if (parameterTypes.length == 3
                && parameterTypes[0] == long.class
                && parameterTypes[2] == int.class) {
                Constructor<?> constructor = resolveTwoIntConstructor(parameterTypes[1]);
                if (constructor != null) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;
    }

    private static Method resolveMouseCursorPosCallback() {
        for (Method method : MouseHandler.class.getDeclaredMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (method.getReturnType() != void.class) {
                continue;
            }
            if (parameterTypes.length == 3
                && parameterTypes[0] == long.class
                && parameterTypes[1] == double.class
                && parameterTypes[2] == double.class) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    private static Constructor<?> resolveMouseInputConstructor() {
        if (MOUSE_BUTTON_CALLBACK == null) {
            return null;
        }
        Class<?>[] parameterTypes = MOUSE_BUTTON_CALLBACK.getParameterTypes();
        if (parameterTypes.length != 3) {
            return null;
        }
        return resolveTwoIntConstructor(parameterTypes[1]);
    }

    private static Constructor<?> resolveTwoIntConstructor(Class<?> type) {
        if (type == null || type.isPrimitive()) {
            return null;
        }
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(int.class, int.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
