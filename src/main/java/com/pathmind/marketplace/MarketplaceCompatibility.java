package com.pathmind.marketplace;

import java.util.Locale;
import java.util.regex.Pattern;

public final class MarketplaceCompatibility {
    private static final Pattern LOADER_SUFFIX = Pattern.compile("-(fabric|neoforge)$", Pattern.CASE_INSENSITIVE);

    private MarketplaceCompatibility() {
    }

    public static boolean isPathmindVersionCompatible(String expected, String actual) {
        return isVersionLooseMatch(expected, actual)
            || isVersionLooseMatch(toLoaderNeutralPathmindVersion(expected), toLoaderNeutralPathmindVersion(actual));
    }

    public static String toLoaderNeutralPathmindVersion(String version) {
        if (version == null) {
            return "";
        }
        String normalized = version.trim();
        if (normalized.isEmpty()) {
            return "";
        }
        return LOADER_SUFFIX.matcher(normalized).replaceFirst("");
    }

    private static boolean isVersionLooseMatch(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        String normalizedExpected = expected.trim().toLowerCase(Locale.ROOT);
        String normalizedActual = actual.trim().toLowerCase(Locale.ROOT);
        if (normalizedExpected.isEmpty() || normalizedActual.isEmpty()) {
            return false;
        }
        return normalizedActual.equals(normalizedExpected)
            || normalizedActual.startsWith(normalizedExpected + "+")
            || normalizedExpected.startsWith(normalizedActual + "+")
            || normalizedActual.contains(normalizedExpected)
            || normalizedExpected.contains(normalizedActual);
    }
}
