package com.ghostchu.quickshop.permission;

import com.ghostchu.quickshop.api.permission.PermissionProvider;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * A simple impl for PermissionProvider
 *
 * @author Ghost_chu
 */
public class BukkitPermsProvider implements PermissionProvider {

    /**
     * Get the debug infos in provider
     *
     * @param sender     CommandSender
     * @param permission The permission want to check
     * @return Debug Infos
     */
    public @NotNull PermissionInformationContainer getDebugInfo(
            @NotNull CommandSender sender, @NotNull String permission) {
        return new PermissionInformationContainer(sender, permission, null, null);
    }

    @Override
    public @NotNull String getName() {
        return "Bukkit";
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        return sender.hasPermission(permission);
    }

}
