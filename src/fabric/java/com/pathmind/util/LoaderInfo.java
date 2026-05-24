package com.pathmind.util;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiPredicate;

public final class LoaderInfo {
    private LoaderInfo() {
    }

    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static boolean isModLoaded(String modId) {
        return modId != null && FabricLoader.getInstance().isModLoaded(modId);
    }

    public static Optional<String> getModVersion(String modId) {
        if (modId == null) {
            return Optional.empty();
        }
        return FabricLoader.getInstance()
            .getModContainer(modId)
            .map(container -> container.getMetadata().getVersion().getFriendlyString());
    }

    public static String getLoaderName() {
        return "Fabric Loader";
    }

    public static Optional<String> getLoaderVersion() {
        return getModVersion("fabricloader");
    }

    public static boolean anyLoadedModMatches(BiPredicate<String, String> predicate) {
        if (predicate == null) {
            return false;
        }
        return FabricLoader.getInstance().getAllMods().stream().anyMatch(container -> {
            String id = container.getMetadata().getId();
            String name = container.getMetadata().getName();
            return predicate.test(id, name);
        });
    }
}
