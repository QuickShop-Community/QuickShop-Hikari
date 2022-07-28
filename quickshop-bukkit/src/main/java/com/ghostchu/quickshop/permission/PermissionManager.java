package com.ghostchu.quickshop.permission;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.permission.PermissionProvider;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

@Getter
public class PermissionManager {
    private final QuickShop plugin;

    private final PermissionProvider provider;

    /**
     * The manager to call permission providers
     *
     * @param plugin Instance
     */
    public PermissionManager(QuickShop plugin) {
        this.plugin = plugin;
        provider = new BukkitPermsProvider();
        plugin.getLogger().info("Selected permission provider: " + provider.getName());
    }

    /**
     * Check the permission for sender
     *
     * @param sender     The CommandSender you want check
     * @param permission The permission node wait to check
     * @return The result of check
     */
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        try {
            boolean result = provider.hasPermission(sender, permission);
            if (Util.isDevMode()) {
                try {
                    Log.permission(sender.getName() + " : " + permission + " -> " + result);
                } catch (Exception th) {
                    Log.debug("Exception threw when getting debug messages.");
                    MsgUtil.debugStackTrace(th.getStackTrace());
                }
            }
            return result;
        } catch (Exception th) {
            plugin.getLogger().log(Level.WARNING, "Failed to processing permission response, This might or not a bug, we not sure, but you can report to both permission provider plugin author or QuickShop devs about this error", th);
            return false;
        }
    }

}
