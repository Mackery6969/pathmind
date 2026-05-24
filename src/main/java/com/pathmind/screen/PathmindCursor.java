package com.pathmind.screen;

import com.pathmind.PathmindMod;
import com.pathmind.util.DrawContextBridge;
import com.pathmind.util.MatrixStackBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

final class PathmindCursor {
    static final ResourceLocation DEFAULT_TEXTURE = PathmindMod.id("textures/cursor/cursor_default.png");
    static final ResourceLocation GRAB_TEXTURE = PathmindMod.id("textures/cursor/cursor_grab.png");
    static final ResourceLocation GRABBING_TEXTURE = PathmindMod.id("textures/cursor/cursor_grabbing.png");
    static final ResourceLocation CUT_TEXTURE = PathmindMod.id("textures/cursor/cursor_cut.png");
    static final ResourceLocation POINTER_TEXTURE = PathmindMod.id("textures/cursor/cursor_pointer.png");
    static final ResourceLocation SCALE_TEXTURE = PathmindMod.id("textures/cursor/cursor_scale.png");
    static final ResourceLocation SCALE_BOTTOM_LEFT_TEXTURE = PathmindMod.id("textures/cursor/cursor_scale_bottom_left.png");
    static final ResourceLocation SCALE_TOP_RIGHT_TEXTURE = PathmindMod.id("textures/cursor/cursor_scale_top_right.png");
    static final ResourceLocation SCALE_TOP_LEFT_TEXTURE = PathmindMod.id("textures/cursor/cursor_scale_top_left.png");
    static final ResourceLocation DISABLED_TEXTURE = PathmindMod.id("textures/cursor/cursor_disabled.png");
    private static final int SOURCE_SIZE = 16;
    static final int SIZE = 8;
    static final int HOTSPOT_X = Math.round(3f * SIZE / SOURCE_SIZE);
    static final int HOTSPOT_Y = Math.round(1f * SIZE / SOURCE_SIZE);
    private static final int CURSOR_TINT = 0xFFFFFFFF;
    private static final boolean USE_SYSTEM_CURSOR = true;

    private PathmindCursor() {
    }

    static void hideSystemCursor(Minecraft client) {
        if (USE_SYSTEM_CURSOR) {
            showSystemCursor(client);
            return;
        }
        if (client == null) {
            return;
        }
        GLFW.glfwSetInputMode(client.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
    }

    static void showSystemCursor(Minecraft client) {
        if (client == null) {
            return;
        }
        GLFW.glfwSetInputMode(client.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
    }

    static void renderDefault(GuiGraphics context, int mouseX, int mouseY) {
        render(context, DEFAULT_TEXTURE, mouseX, mouseY);
    }

    static void render(GuiGraphics context, ResourceLocation texture, int mouseX, int mouseY) {
        if (USE_SYSTEM_CURSOR) {
            return;
        }
        DrawContextBridge.startNewRootLayer(context);
        Object matrices = context.pose();
        MatrixStackBridge.push(matrices);
        try {
            MatrixStackBridge.translateZ(matrices, 1000.0f);
            GuiTextureRenderer.drawIcon(context, texture, mouseX - HOTSPOT_X, mouseY - HOTSPOT_Y, SIZE, CURSOR_TINT);
            DrawContextBridge.flush(context);
        } finally {
            MatrixStackBridge.pop(matrices);
        }
    }
}
