package org.maxgamer.quickshop;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Field;

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
        //Prevent network flow to OTA
        try {
            Field CROWDIN_OTA_HOST = Class.forName("org.maxgamer.quickshop.localization.text.distributions.crowdin.CrowdinOTA").getDeclaredField("CROWDIN_OTA_HOST");
            CROWDIN_OTA_HOST.setAccessible(true);
            CROWDIN_OTA_HOST.set(null, "https://0.0.0.0");
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        MockBukkit.load(QuickShop.class);
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }
}
