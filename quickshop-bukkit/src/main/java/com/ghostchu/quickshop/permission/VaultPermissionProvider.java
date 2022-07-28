package com.ghostchu.quickshop.permission;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.permission.PermissionProvider;
import com.ghostchu.quickshop.api.permission.ProviderIsEmptyException;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class VaultPermissionProvider implements PermissionProvider {
    private final Permission api;

    @Deprecated
    public VaultPermissionProvider() {
        RegisteredServiceProvider<Permission> rsp =
                QuickShop.getInstance().getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            throw new ProviderIsEmptyException(getName());
        }
        api = rsp.getProvider();
    }

    /**
     * Test the sender has special permission
     *
     * @param sender     CommandSender
     * @param permission The permission want to check
     * @return hasPermission
     */
    @Override
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        return api.has(sender, permission);
    }

    /**
     * Get permission provider name
     *
     * @return The name of permission provider
     */
    @Override
    public @NotNull String getName() {
        return "Vault";
    }

    /**
     * Get the debug infos in provider
     *
     * @param sender     CommandSender
     * @param permission The permission want to check
     * @return Debug Infos
     */
    public @NotNull PermissionInformationContainer getDebugInfo(
            @NotNull CommandSender sender, @NotNull String permission) {
        if (sender instanceof Server) {
            return new PermissionInformationContainer(sender, permission, null, "User is Console");
        }
        OfflinePlayer offlinePlayer = (OfflinePlayer) sender;
        return new PermissionInformationContainer(
                sender, permission, api.getPrimaryGroup(null, offlinePlayer), null);
    }

}
