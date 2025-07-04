package com.ghostchu.quickshop.compatibility.iridiumskyblock;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.management.ShopPermissionCheckEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.api.IslandDeleteEvent;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public final class Main extends CompatibilityModule implements Listener {

  private boolean onlyOwnerCanCreateShop;

  private void deleteShops(@NotNull final World world, @NotNull final String deleteReason) {

    final List<Shop> pendingForDeletion = new ArrayList<>(getApi().getShopManager().getShopsInWorld(world));

    Util.mainThreadRun(()->pendingForDeletion.forEach(s->{
      getApi().getShopManager().deleteShop(s);
      recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "IridiumSkyblock", false), s, deleteReason);
    }));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDelete(final IslandDeleteEvent event) {
    if(event.getIsland().getHome() == null || event.getIsland().getHome().getWorld() == null) return;

    deleteShops(event.getIsland().getHome().getWorld(), "IslandDeleteEvent");
  }

  @Override
  public void init() {

    onlyOwnerCanCreateShop = getConfig().getBoolean("owner-create-only");
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopCreateEvent event) {

    if(!event.phase().cancellable()) {
      return;
    }

    final Optional<Island> island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(event.location());

    event.user().getBukkitPlayer().ifPresent(player->{
      if(island.isEmpty()) {
        return;
      }

      if(player.hasPermission("quickshop.iridium.override")) {
       event.setCancelled(false, "admin override using quickshop.iridium.override");
       return;
      }


      final UUID uuid = player.getUniqueId();
      if(onlyOwnerCanCreateShop) {
        if(!isOwner(island.get(), uuid)) {

          event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.iridiumskyblock.owner-create-only").forLocale());
        }
      } else {

        if(!isMember(island.get(), uuid)) {

            event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.iridiumskyblock.owner-member-create-only").forLocale());
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
    final Optional<Island> island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(shopLoc);
    if(island.isEmpty()) {
      return;
    }

    final Player player = Bukkit.getPlayer(event.playerUUID());
    if(player != null && player.hasPermission("quickshop.iridium.override")) {
      event.hasPermission(true);
    }

    if(isOwner(island.get(), event.playerUUID())) {

      if(event.pluginNamespace().equals(QuickShop.getInstance().getJavaPlugin().getName()) && event.permissionNode().equals(BuiltInShopPermission.DELETE.getRawNode())) {
        event.hasPermission(true);
      }
    }
  }

  private boolean isOwner(final Island island, final UUID uuid) {

    return island.getOwner().isPresent() && island.getOwner().get().getUuid().equals(uuid);
  }

  private boolean isMember(final Island island, final UUID uuid) {

    if(island.getOwner().isPresent() && island.getOwner().get().getUuid().equals(uuid)) {
      return true;
    }

    for(final User user : island.getMembers()) {

      if(user.getUuid().equals(uuid)) {
        return true;
      }
    }
    return false;
  }
}
