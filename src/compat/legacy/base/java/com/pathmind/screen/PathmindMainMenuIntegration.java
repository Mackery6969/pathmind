package com.pathmind.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

/**
 * Hooks into the main menu to add the Pathmind visual editor button and key handling.
 */
public final class PathmindMainMenuIntegration {
    private static final int BUTTON_SIZE = 20;
    private static final int BUTTON_MARGIN = 8;

    private PathmindMainMenuIntegration() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PathmindMainMenuIntegration::afterScreenInit);
        NeoForge.EVENT_BUS.addListener(PathmindMainMenuIntegration::afterKeyPressed);
    }

    private static void afterScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            addButton(event);
        }
    }

    private static void addButton(ScreenEvent.Init.Post event) {
        int x = BUTTON_MARGIN;
        int y = BUTTON_MARGIN;

        event.addListener(new PathmindMainMenuButton(x, y, BUTTON_SIZE, button -> {
            Minecraft client = Minecraft.getInstance();
            Screen screen = event.getScreen();
            PathmindScreens.openVisualEditorOrWarn(client, screen);
        }));
    }

    private static void afterKeyPressed(ScreenEvent.KeyPressed.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof TitleScreen)) {
            return;
        }

        if (event.getKeyCode() == GLFW.GLFW_KEY_RIGHT_ALT) {
            PathmindScreens.openVisualEditorOrWarn(Minecraft.getInstance(), screen);
            event.setCanceled(true);
        }
    }
}
