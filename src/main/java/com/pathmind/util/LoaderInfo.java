package com.pathmind.util;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiPredicate;

public final class LoaderInfo {
    private LoaderInfo() {
    }

    public static Path getGameDir() {
        try {
            return FMLPaths.GAMEDIR.get();
        } catch (IllegalStateException ignored) {
            return Path.of(System.getProperty("user.home"), ".minecraft");
        }
    }

    public static boolean isModLoaded(String modId) {
        return modId != null && ModList.get().isLoaded(modId);
    }

    public static Optional<String> getModVersion(String modId) {
        if (modId == null) {
            return Optional.empty();
        }
        return ModList.get().getModContainerById(modId)
            .map(container -> container.getModInfo().getVersion().toString());
    }

    public static boolean anyLoadedModMatches(BiPredicate<String, String> predicate) {
        if (predicate == null) {
            return false;
        }
        return ModList.get().getMods().stream().anyMatch(modInfo -> {
            String id = modInfo.getModId();
            String name = modInfo.getDisplayName();
            return predicate.test(id, name);
        });
    }
}
