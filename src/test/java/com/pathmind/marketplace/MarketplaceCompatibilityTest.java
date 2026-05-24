package com.pathmind.marketplace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarketplaceCompatibilityTest {

    @Test
    void pathmindVersionsMatchAcrossLoaderSuffixes() {
        assertTrue(MarketplaceCompatibility.isPathmindVersionCompatible(
            "1.1.4+mc1.21.1-fabric",
            "1.1.4+mc1.21.1-neoforge"
        ));
        assertTrue(MarketplaceCompatibility.isPathmindVersionCompatible(
            "1.1.4+mc1.21.1-neoforge",
            "1.1.4+mc1.21.1-fabric"
        ));
    }

    @Test
    void pathmindVersionsStillRejectDifferentModVersions() {
        assertFalse(MarketplaceCompatibility.isPathmindVersionCompatible(
            "1.1.4+mc1.21.1-fabric",
            "1.1.5+mc1.21.1-neoforge"
        ));
    }

    @Test
    void loaderNeutralPathmindVersionOnlyStripsKnownTrailingLoaderSuffixes() {
        assertEquals("1.1.4+mc1.21.1", MarketplaceCompatibility.toLoaderNeutralPathmindVersion("1.1.4+mc1.21.1-fabric"));
        assertEquals("1.1.4+mc1.21.1", MarketplaceCompatibility.toLoaderNeutralPathmindVersion("1.1.4+mc1.21.1-neoforge"));
        assertEquals("1.1.4+fabric-helper", MarketplaceCompatibility.toLoaderNeutralPathmindVersion("1.1.4+fabric-helper"));
    }
}
