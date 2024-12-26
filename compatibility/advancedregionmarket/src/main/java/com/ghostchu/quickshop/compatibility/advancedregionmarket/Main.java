package com.ghostchu.quickshop.compatibility.advancedregionmarket;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import net.alex9849.arm.events.RemoveRegionEvent;
import net.alex9849.arm.events.RestoreRegionEvent;
import net.alex9849.arm.regions.Region;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
