package com.ghostchu.quickshop.compatibility.advancedregionmarket;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import net.alex9849.arm.events.RemoveRegionEvent;
import net.alex9849.arm.events.RestoreRegionEvent;
import net.alex9849.arm.regions.Region;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public final class Main extends CompatibilityModule implements Listener {

  @Override
  public void init() {
    // There no init stuffs need to do
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
