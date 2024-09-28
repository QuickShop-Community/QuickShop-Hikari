package com.ghostchu.quickshop.compatibility.advancedregionmarket;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
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

    final Vector minPoint = region.getRegion().getMinPoint();
    final Vector maxPoint = region.getRegion().getMaxPoint();
    final World world = region.getRegionworld();
    final Set<Chunk> chuckLocations = new HashSet<>();

    for(int x = minPoint.getBlockX(); x <= maxPoint.getBlockX() + 16; x += 16) {
      for(int z = minPoint.getBlockZ(); z <= maxPoint.getBlockZ() + 16; z += 16) {
        chuckLocations.add(world.getChunkAt(x >> 4, z >> 4));
      }
    }


    final HashMap<Location, Shop> shopMap = new HashMap<>();

    for(final Chunk chunk : chuckLocations) {
      final Map<Location, Shop> shopsInChunk = getApi().getShopManager().getShops(chunk);
      if(shopsInChunk != null) {
        shopMap.putAll(shopsInChunk);
      }
    }
    for(final Map.Entry<Location, Shop> shopEntry : shopMap.entrySet()) {
      final Location shopLocation = shopEntry.getKey();
      if(region.getRegion().contains(shopLocation.getBlockX(), shopLocation.getBlockY(), shopLocation.getBlockZ())) {
        final Shop shop = shopEntry.getValue();
        if(shop != null) {
          getApi().getShopManager().deleteShop(shop);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onShopNeedDeletion(final RestoreRegionEvent event) {

    handleDeletion(event.getRegion());
  }
}
