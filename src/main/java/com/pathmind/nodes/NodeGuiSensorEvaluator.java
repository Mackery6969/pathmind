package com.pathmind.nodes;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class NodeGuiSensorEvaluator {
    @SuppressWarnings("unused")
    private final Node owner;

    NodeGuiSensorEvaluator(Node owner) {
        this.owner = owner;
    }

    boolean isOpenGuiFilled() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) {
            return false;
        }
        AbstractContainerMenu handler = client.player.containerMenu;
        if (handler == null) {
            return false;
        }
        boolean hasContainerSlots = false;
        for (Slot slot : handler.slots) {
            if (slot == null) {
                continue;
            }
            if (slot.container instanceof Inventory) {
                continue;
            }
            hasContainerSlots = true;
            ItemStack stack = slot.getItem();
            if (stack == null || stack.isEmpty()) {
                return false;
            }
        }
        return hasContainerSlots;
    }
}
