package org.maxgamer.quickshop;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Use for testing related to bukkit api
 * <p>
 * If you are writing test which using runtime stuff, just extend it
 */
public abstract class TestBukkitBase {
    @BeforeAll
    public static void setUp() {
        MockBukkit.mock();
        System.getProperties().setProperty("org.maxgamer.quickshop.util.envcheck.skip.SIGNATURE_VERIFY", "true");
        System.getProperties().setProperty("org.maxgamer.quickshop.util.envcheck.skip.POTENTIAL_INFECTION_CHARACTERISTICS_CHECK", "true");
        MockBukkit.load(QuickShop.class);
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }
}
