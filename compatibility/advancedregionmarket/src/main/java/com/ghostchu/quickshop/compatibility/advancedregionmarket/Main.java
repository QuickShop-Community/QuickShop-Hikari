package com.ghostchu.quickshop.compatibility.advancedregionmarket;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.management.ShopPermissionCheckEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import net.alex9849.arm.AdvancedRegionMarket;
import net.alex9849.arm.events.RemoveRegionEvent;
import net.alex9849.arm.events.RestoreRegionEvent;
import net.alex9849.arm.regions.Region;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public final class Main extends CompatibilityModule implements Listener {

  boolean ownerCreate = true;

  @Override
  public void init() {
    // There no init stuffs need to do
    this.ownerCreate = getConfig().getBoolean("owner-create", true);
  }

  @EventHandler(ignoreCancelled = true)
  public void canCreateShopHere(final ShopCreateEvent event) {

    if(!ownerCreate) {
      return;
    }

    if(!event.phase().cancellable()) {
      return;
    }

    final Location shopLoc = event.location();
    if(shopLoc.getWorld() == null) {
      return;
    }

    final List<Region> regions = AdvancedRegionMarket.getInstance().getRegionManager().getRegionsByLocation(shopLoc);
    for(final Region region : regions) {

      if(region.getOwner() == null) {
        continue;
      }

      if(!region.getOwner().equals(event.user().getUniqueId())) {

        event.setCancelled(true, "owner-create is enabled and player is not a region owner");
      } else {

        event.setCancelled(false, "owner-create is enabled and player is a region owner");
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void permissionOverride(final ShopPermissionCheckEvent event) {

    if(event.shop().isEmpty()) {
      return;
    }

    final Location shopLoc = event.shop().get().getLocation();
    if(shopLoc.getWorld() == null) {
      return;
    }


    if(!event.pluginNamespace().equals(QuickShop.getInstance().getJavaPlugin().getName())) {
      return;
    }

    if(!event.permissionNode().equals(BuiltInShopPermission.DELETE.getRawNode())) {
      return;
    }

    final List<Region> regions = AdvancedRegionMarket.getInstance().getRegionManager().getRegionsByLocation(shopLoc);
    for(final Region region : regions) {

      if(region.getOwner() == null) {
        continue;
      }

      if(region.getOwner().equals(event.playerUUID())) {
        event.hasPermission(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onShopNeedDeletion(final RemoveRegionEvent event) {

    handleDeletion(event.getRegion());
  }

  private void handleDeletion(final Region region) {

    final List<Shop> shops = getApi().getShopManager().getAllShops();
    for(final Shop shop : shops) {

      final Location shopLocation = shop.getLocation();
      if(region.getRegion().contains(shopLocation.getBlockX(), shopLocation.getBlockY(), shopLocation.getBlockZ())) {

        getApi().getShopManager().deleteShop(shop);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onShopNeedDeletion(final RestoreRegionEvent event) {

    handleDeletion(event.getRegion());
  }
}
