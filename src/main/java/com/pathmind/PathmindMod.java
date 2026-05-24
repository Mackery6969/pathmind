package com.pathmind;

import com.pathmind.util.VersionSupport;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main mod class for Pathmind.
 * This class initializes the mod and sets up event handlers.
 */
@Mod(value = PathmindMod.MOD_ID, dist = Dist.CLIENT)
public class PathmindMod {
    public static final String MOD_ID = "pathmind";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public PathmindMod(IEventBus modBus) {
        LOGGER.info("Initializing Pathmind mod");

        String minecraftVersion = SharedConstants.getCurrentVersion().getName();
        if (!VersionSupport.isSupported(minecraftVersion)) {
            LOGGER.warn("Pathmind targets Minecraft {} but detected {}", VersionSupport.SUPPORTED_RANGE, minecraftVersion);
        }

        new PathmindClientMod().initialize(modBus);
        LOGGER.info("Pathmind mod initialized successfully");
    }
}
