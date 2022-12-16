package com.ghostchu.quickshop.addon.plan;

import com.djrapitops.plan.capability.CapabilityService;
import com.djrapitops.plan.extension.ExtensionService;
import org.bukkit.Bukkit;

public class PlanHook {
    private final Main main;
    private final CapabilityService capabilities = CapabilityService.getInstance();

    public PlanHook(Main main) {
        this.main = main;
    }

    public void hookIntoPlan() {
        registerDataExtension();
        listenForPlanReloads();
    }

    private void registerDataExtension() {
        try {
            if (capabilities.hasCapability("DATA_EXTENSION_TABLES")
                    && capabilities.hasCapability("DATA_EXTENSION_VALUES")) {
                ExtensionService.getInstance().register(new HikariDataExtension(main));
            } else {
                main.getLogger().severe("Your Plan build doesn't support DATA_EXTENSION_TABLES or DATA_EXTENSION_VALUES capability!");
                Bukkit.getPluginManager().disablePlugin(main);
            }
        } catch (IllegalStateException planIsNotEnabled) {
            // Plan is not enabled, handle exception
        } catch (IllegalArgumentException dataExtensionImplementationIsInvalid) {
            // The DataExtension implementation has an implementation error, handle exception
        }
    }

    private void listenForPlanReloads() {
        CapabilityService.getInstance().registerEnableListener(
                isPlanEnabled -> {
                    // Register DataExtension again
                    if (isPlanEnabled) registerDataExtension();
                }
        );
    }
}
