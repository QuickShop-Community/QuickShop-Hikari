package com.ghostchu.quickshop.compatibility.fabledskyblock;


import com.craftaro.skyblock.SkyBlock;
import com.craftaro.skyblock.api.SkyBlockAPI;
import com.craftaro.skyblock.api.event.island.IslandBanEvent;
import com.craftaro.skyblock.api.event.island.IslandDeleteEvent;
import com.craftaro.skyblock.api.event.island.IslandKickEvent;
import com.craftaro.skyblock.api.event.island.IslandOwnershipTransferEvent;
import com.craftaro.skyblock.api.event.player.PlayerIslandLeaveEvent;
import com.craftaro.skyblock.api.island.Island;
import com.craftaro.skyblock.api.island.IslandEnvironment;
import com.craftaro.skyblock.api.island.IslandWorld;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.management.ShopPermissionCheckEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.obj.QUserImpl;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    //Delete overworld shops
    delete(island, IslandWorld.OVERWORLD, IslandEnvironment.ISLAND, shopOwnerToDelete, deleteOperator, deleteReason);

    //delete end shops
    delete(island, IslandWorld.END, IslandEnvironment.ISLAND, shopOwnerToDelete, deleteOperator, deleteReason);

    //delete nether shops
    delete(island, IslandWorld.NETHER, IslandEnvironment.ISLAND, shopOwnerToDelete, deleteOperator, deleteReason);
  }

  public void delete(final Island island, final IslandWorld iWorld, final IslandEnvironment environment,
                     @Nullable final UUID shopOwnerToDelete, @NotNull final UUID deleteOperator,
                     @NotNull final String deleteReason) {

    if(shopOwnerToDelete == null) {
      return;
    }

    final Location min = island.getIslandMin(iWorld);
    final Location max = island.getIslandMax(iWorld);
    final World world = island.getLocation(iWorld, environment).getWorld();
    if(world != null && min != null && max != null) {
      getShops(world.getName(), min.getBlockX(), min.getBlockZ(), max.getBlockX(), max.getBlockZ()).forEach(shop->{
        if(shopOwnerToDelete.equals(shop.getOwner().getUniqueId())) {

          recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "FabledSkyblock", false), shop, deleteReason);
          getApi().getShopManager().deleteShop(shop);
        }
      });;
    }
  }

  public List<Shop> getShops(@NotNull final String worldName, final int minX, final int minZ, final int maxX, final int maxZ) {

    final List<Shop> shopsList = new ArrayList<>();
    for(int x = minX >> 4; x <= maxX >> 4; x++) {
      for(int z = minZ >> 4; z <= maxZ >> 4; z++) {

        final Map<Location, Shop> shops = getApi().getShopManager().getShops(worldName, x, z);
        if(shops != null) {
          shopsList.addAll(shops.values());
        }
      }
    }
    final BoundingBox boundingBox = new BoundingBox(minX, Integer.MIN_VALUE, minZ, maxX, Integer.MAX_VALUE, maxZ);
    return shopsList.stream().filter(s->boundingBox.contains(s.getLocation().toVector())).toList();
  }
}
