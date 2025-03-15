package com.ghostchu.quickshop.compatibility.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.IslandBanEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChunkResetEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.management.ShopPermissionCheckEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;


public final class Main extends CompatibilityModule implements Listener {

  private boolean onlyOwnerCanCreateShop;
  private boolean deleteShopOnMemberLeave;

  @EventHandler
  public void deleteShops(final IslandQuitEvent event) {

    if(deleteShopOnMemberLeave) {
      deleteShops(event.getIsland(), event.getPlayer().getUniqueId(), event.getPlayer().getUniqueId(), "IslandQuitEvent");
    }

  }

  private void deleteShops(@NotNull final Island island, @Nullable final UUID shopOwnerToDelete, @NotNull final UUID deleteOperator, @NotNull final String deleteReason) {

    final List<CompletableFuture<Chunk>> allFutures = this.getAllChunksAsync(island);
    CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).thenAccept(v->{
      final List<Shop> pendingForDeletion = new ArrayList<>();
      allFutures.forEach(future->{
        final Chunk chunk = future.getNow(null);
        for(final Shop shop : getShops(chunk.getWorld().getName(), chunk.getX(), chunk.getZ())) {
          if(shopOwnerToDelete == null || shopOwnerToDelete.equals(shop.getOwner().getUniqueId())) {
            pendingForDeletion.add(shop);
          }
        }
      });
      Util.mainThreadRun(()->pendingForDeletion.forEach(s->{
        getApi().getShopManager().deleteShop(s);
        recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SuperiorSkyblock", false), s, deleteReason);
      }));
    }).exceptionally(throwable->{
      getLogger().log(Level.WARNING, "Failed to handle SuperiorSkyblock2 island shops deletion", throwable);
      return null;
    });
  }

  private void deleteShops(@NotNull final World world, final int chunkX, final int chunkZ, @Nullable final UUID shopOwnerToDelete, @NotNull final UUID deleteOperator, @NotNull final String deleteReason) {

    final List<Shop> pendingForDeletion = new ArrayList<>();
    for(final Shop shop : getShops(world.getName(), chunkX, chunkZ)) {
      if(shopOwnerToDelete == null || shopOwnerToDelete.equals(shop.getOwner().getUniqueId())) {
        pendingForDeletion.add(shop);
      }
    }
    Util.mainThreadRun(()->pendingForDeletion.forEach(s->{
      getApi().getShopManager().deleteShop(s);
      recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SuperiorSkyblock", false), s, deleteReason);
    }));
  }

  @EventHandler
  public void deleteShops(final IslandKickEvent event) {

    if(deleteShopOnMemberLeave) {
      deleteShops(event.getIsland(), event.getTarget().getUniqueId(), event.getIsland().getOwner().getUniqueId(), "IslandKickEvent");
    }
  }

  @EventHandler
  public void deleteShops(final IslandBanEvent event) {

    if(deleteShopOnMemberLeave) {
      deleteShops(event.getIsland(), event.getTarget().getUniqueId(), event.getIsland().getOwner().getUniqueId(), "IslandKickEvent");
    }
  }

  @EventHandler
  public void deleteShops(final IslandUncoopPlayerEvent event) {

    deleteShops(event.getIsland(), event.getTarget().getUniqueId(), event.getIsland().getOwner().getUniqueId(), "IslandUncoopPlayerEvent");
  }

  @EventHandler
  public void deleteShopsOnChunkReset(final IslandChunkResetEvent event) {

    deleteShops(event.getWorld(), event.getChunkX(), event.getChunkZ(), null, CommonUtil.getNilUniqueId(), "IslandChunkResetEvent");
  }

  @Override
  public void init() {

    onlyOwnerCanCreateShop = getConfig().getBoolean("owner-create-only");
    deleteShopOnMemberLeave = getConfig().getBoolean("delete-shop-on-member-leave");
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopCreateEvent event) {

    if(!event.phase().cancellable()) {
      return;
    }

    final Island island = SuperiorSkyblockAPI.getIslandAt(event.location());
    event.user().getBukkitPlayer().ifPresent(player->{
      final SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
      if(island == null) {
        return;
      }
      if(onlyOwnerCanCreateShop) {
        if(!island.getOwner().equals(superiorPlayer)) {
          event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.superiorskyblock.owner-create-only").forLocale());
        }
      } else {
        if(!island.getOwner().equals(superiorPlayer)) {
          if(!island.isMember(superiorPlayer)) {
            event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.superiorskyblock.owner-member-create-only").forLocale());
          }
        }
      }
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void permissionOverride(final ShopPermissionCheckEvent event) {

    if(event.shop().isEmpty()) {
      return;
    }

    final Location shopLoc = event.shop().get().getLocation();
    final Island island = SuperiorSkyblockAPI.getIslandAt(shopLoc);
    if(island == null) {
      return;
    }
    if(island.getOwner().getUniqueId().equals(event.playerUUID())) {
      if(event.pluginNamespace().equals(QuickShop.getInstance().getJavaPlugin().getName()) && event.permissionNode().equals(BuiltInShopPermission.DELETE.getRawNode())) {
        event.hasPermission(true);
      }
    }
  }

  private List<CompletableFuture<Chunk>> getAllChunksAsync(final Island island) {

    final List<CompletableFuture<Chunk>> chunkFutures = new ArrayList<>();
    for(final World.Environment environment : World.Environment.values()) {
      try {
        chunkFutures.addAll(island.getAllChunksAsync(environment, false, chunk->{
        }));
      } catch(final NullPointerException ignored) {
      }
    }
    return chunkFutures;
  }
}
