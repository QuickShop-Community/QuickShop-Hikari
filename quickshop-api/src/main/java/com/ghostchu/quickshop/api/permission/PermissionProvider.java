package com.ghostchu.quickshop.api.permission;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * The permission query service provider
 * QuickShop use that to perform a permission query request.
 */
public interface PermissionProvider {
    /**
     * Get permission provider name
     *
     * @return The name of permission provider
     */
    @NotNull
    String getName();

    /**
     * Test the sender has special permission
     *
     * @param sender     CommandSender
     * @param permission The permission want to check
     * @return hasPermission
     */
    boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission);

}
