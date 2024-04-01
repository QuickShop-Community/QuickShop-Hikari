package com.ghostchu.quickshop.permission.impl;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.permission.PermissionProvider;
import com.ghostchu.quickshop.permission.PermissionInformationContainer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

    @Override
    public boolean hasPermission(@NotNull QUser sender, @NotNull String permission) {
        Player player = sender.getBukkitPlayer().orElse(null);
        if (player == null) {
            return false;
        }
        return player.hasPermission(permission);
    }

}
