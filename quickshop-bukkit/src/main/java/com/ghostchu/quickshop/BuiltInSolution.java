package com.ghostchu.quickshop;

import org.bukkit.ChatColor;

/**
 * A class to check known issues that cause plugin boot failure.
 */
public class BuiltInSolution {

    private BuiltInSolution() {
    }

    /**
     * Call îf database failed to load. This checks the failure reason.
     *
     * @return The error reason.
     */
    public static BootError databaseError() {
        return new BootError(QuickShop.getInstance().getLogger(),
                "Error connecting to the database!",
                "Please make sure your database service is running.",
                "and check the configuration in your config.yml");
    }

    /**
     * Call îf economy system failed to load. This checks the failure reason.
     *
     * @return The error reason.
     */
    public static BootError econError() {
        // Check if Vault is installed
        if (QuickShop.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
            // Vault is not installed
            return new BootError(QuickShop.getInstance().getLogger(),
                    "Vault is not installed or loaded!",
                    "Please make sure Vault is installed.");
        }
        // Vault is installed
        if (QuickShop.getInstance().getServer().getPluginManager().getPlugin("CMI") != null) {
            // Found possible incompatible plugin
            return new BootError(QuickShop.getInstance().getLogger(),
                    "No Economy plugin detected! Please make sure that you have a compatible economy",
                    "plugin installed that is hooked into Vault and loads before QuickShop.",
                    ChatColor.YELLOW + "Incompatibility detected: CMI Installed",
                    "The use of the CMI Edition of Vault might fix this.");
        }

        return new BootError(QuickShop.getInstance().getLogger(),
                "No Economy plugin detected! Please make sure that you have a",
                "compatible economy plugin installed to get Vault working.");
    }

}
