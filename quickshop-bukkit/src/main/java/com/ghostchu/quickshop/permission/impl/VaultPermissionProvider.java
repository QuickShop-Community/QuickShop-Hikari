package com.ghostchu.quickshop.permission.impl;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.permission.PermissionProvider;
import com.ghostchu.quickshop.api.permission.ProviderIsEmptyException;
import com.ghostchu.quickshop.permission.PermissionInformationContainer;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class VaultPermissionProvider implements PermissionProvider {

  private final Permission api;

  @Deprecated
  public VaultPermissionProvider() {

    final RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
    if(rsp == null) {
      throw new ProviderIsEmptyException(getName());
    }
    api = rsp.getProvider();
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
   * Test the sender has special permission
   *
   * @param sender     CommandSender
   * @param permission The permission want to check
   *
   * @return hasPermission
   */
  @Override
  public boolean hasPermission(@NotNull final CommandSender sender, @NotNull final String permission) {

    return api.has(sender, permission);
  }

  @Override
  public boolean hasPermission(@NotNull final QUser sender, @NotNull final String permission) {

    final Player player = sender.getBukkitPlayer().orElse(null);
    if(player == null) {
      return false;
    }
    return api.has(player, permission);
  }

  /**
   * Get the debug infos in provider
   *
   * @param sender     CommandSender
   * @param permission The permission want to check
   *
   * @return Debug Infos
   */
  public @NotNull PermissionInformationContainer getDebugInfo(
          @NotNull final CommandSender sender, @NotNull final String permission) {

    if(sender instanceof Server) {
      return new PermissionInformationContainer(sender, permission, null, "User is Console");
    }
    final OfflinePlayer offlinePlayer = (OfflinePlayer)sender;
    return new PermissionInformationContainer(
            sender, permission, api.getPrimaryGroup(null, offlinePlayer), null);
  }

}
