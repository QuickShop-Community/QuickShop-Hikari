package com.ghostchu.quickshop.compatibility.fabledskyblock;


import com.craftaro.skyblock.SkyBlock;
import com.craftaro.skyblock.api.SkyBlockAPI;
import com.craftaro.skyblock.api.event.island.IslandBanEvent;
import com.craftaro.skyblock.api.event.island.IslandDeleteEvent;
import com.craftaro.skyblock.api.event.island.IslandKickEvent;
import com.craftaro.skyblock.api.event.island.IslandOwnershipTransferEvent;
import com.craftaro.skyblock.api.event.player.PlayerIslandLeaveEvent;
import com.craftaro.skyblock.api.island.Island;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.management.ShopPermissionCheckEvent;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public final class Main extends CompatibilityModule {

  private boolean onlyOwnerCanCreateShop;
  private boolean deleteShopOnMemberLeave;
  private boolean deleteShopOnOwnerTransfer;

  @Override
  public void init() {

    onlyOwnerCanCreateShop = getConfig().getBoolean("owner-create-only");
    deleteShopOnMemberLeave = getConfig().getBoolean("delete-shop-on-member-leave");
    deleteShopOnOwnerTransfer = getConfig().getBoolean("delete-shop-on-owner-transfer");
  }

  @EventHandler
  public void onTransfer(final IslandOwnershipTransferEvent event) {

    if(deleteShopOnOwnerTransfer) {
      deleteShops(event.getIsland(), event.getPreviousOwnerId(), event.getPreviousOwnerId(), "IslandOwnershipTransferEvent");
    }

  }

  @EventHandler
  public void onLeave(final PlayerIslandLeaveEvent event) {

    if(deleteShopOnMemberLeave) {
      deleteShops(event.getIsland(), event.getPlayer().getUniqueId(), event.getPlayer().getUniqueId(), "PlayerIslandLeaveEvent");
    }

  }

  @EventHandler
  public void onKick(final IslandKickEvent event) {

    if(deleteShopOnMemberLeave) {
      deleteShops(event.getIsland(), event.getKicked().getUniqueId(), event.getIsland().getOwnerUUID(), "IslandKickEvent");
    }
  }

  @EventHandler
  public void onBan(final IslandBanEvent event) {

    if(deleteShopOnMemberLeave) {
      deleteShops(event.getIsland(), event.getBanned().getUniqueId(), event.getIsland().getOwnerUUID(), "IslandBanEvent");
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onDelete(final IslandDeleteEvent event) {

    deleteShops(event.getIsland(), event.getIsland().getOwnerUUID(), event.getIsland().getOwnerUUID(), "IslandDeleteEvent");
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopCreateEvent event) {

    if(!event.phase().cancellable()) {
      return;
    }

    final Island island = SkyBlockAPI.getIslandManager().getIslandAtLocation(event.location());
    event.user().getBukkitPlayer().ifPresent(player->{
      if(island == null) {
        return;
      }
      final UUID uuid = player.getUniqueId();
      if(onlyOwnerCanCreateShop) {
        if(!island.getOwnerUUID().equals(uuid)) {

          event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.fabledskyblock.owner-create-only").forLocale());
        }
      } else {
        if(!island.getOwnerUUID().equals(uuid) && !island.isCoopPlayer(uuid)) {

            event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.fabledskyblock.owner-member-create-only").forLocale());
        }
      }
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void permissionOverride(final ShopPermissionCheckEvent event) {

    if(event.shop().isEmpty()) {
      return;
    }

    final Location loc = event.shop().get().getLocation();
    final Island island = SkyBlockAPI.getIslandManager().getIslandAtLocation(loc);
    if(island == null) {

      return;
    }

    if(island.getOwnerUUID().equals(event.playerUUID())) {
      if(event.pluginNamespace().equals(QuickShop.getInstance().getJavaPlugin().getName())
         && event.permissionNode().equals(BuiltInShopPermission.DELETE.getRawNode())) {

        event.hasPermission(true);
      }
    }
  }



  private void deleteShops(@NotNull final Island island, @Nullable final UUID shopOwnerToDelete, @NotNull final UUID deleteOperator, @NotNull final String deleteReason) {
    //TODO: Shop lookup through API?
  }
}
