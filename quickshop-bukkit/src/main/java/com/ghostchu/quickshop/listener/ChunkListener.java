package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class ChunkListener extends AbstractQSListener {

  public ChunkListener(final QuickShop plugin) {

    super(plugin);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onChunkLoad(final ChunkLoadEvent e) {

    if(e.isNewChunk()) {
      return;
    }
    final Map<Location, Shop> inChunk = plugin.getShopManager().getShops(e.getChunk());
    if(inChunk == null) {
      return;
    }
    cleanDisplayItems(e.getChunk());
    final String chunkName = e.getChunk().getWorld().getName() + ", X=" + e.getChunk().getX() + ", Z=" + e.getChunk().getZ();
    try(PerfMonitor ignored = new PerfMonitor("Load shops in chunk [" + chunkName + "]", Duration.of(500, ChronoUnit.MILLIS))) {
      for(final Shop shop : inChunk.values()) {
        plugin.getShopManager().loadShop(shop);
      }
    }
  }

  private void cleanDisplayItems(final Chunk chunk) {

    for(final Entity entity : chunk.getEntities()) {
      if(entity instanceof Item itemEntity) {
        if(AbstractDisplayItem.checkIsGuardItemStack(itemEntity.getItemStack())) {
          itemEntity.remove();
          Log.debug("Removed shop display item at " + itemEntity.getLocation() + " while chunk loading, pending for regenerate.");
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onChunkUnload(final ChunkUnloadEvent e) {

    final Map<Location, Shop> inChunk = plugin.getShopManager().getShops(e.getChunk());
    if(inChunk == null) {
      return;
    }
    for(final Shop shop : inChunk.values()) {
      try(PerfMonitor ignored = new PerfMonitor("Unload shops in chunk " + e.getChunk(), Duration.of(500, ChronoUnit.MILLIS))) {
        if(shop.isLoaded()) {
          plugin.getShopManager().unloadShop(shop);
        }
      }
    }

  }

  /**
   * Callback for reloading
   *
   * @return Reloading success
   */
  @Override
  public ReloadResult reloadModule() {

    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }
}
