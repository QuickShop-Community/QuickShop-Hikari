package com.ghostchu.quickshop.addon.squaremap;

import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.management.ShopDeleteEvent;
import com.ghostchu.quickshop.api.event.management.ShopLoadEvent;
import com.ghostchu.quickshop.api.event.management.ShopUnloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ShopEventListener implements Listener {

  private final ShopLayerProvider layerProvider;

  public ShopEventListener(@NotNull final ShopLayerProvider layerProvider) {
    this.layerProvider = layerProvider;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onShopCreate(@NotNull final ShopCreateEvent event) {
    event.shop().ifPresent(layerProvider::updateShopMarker);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onShopDelete(@NotNull final ShopDeleteEvent event) {
    event.shop().ifPresent(layerProvider::removeShopMarker);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onShopLoad(@NotNull final ShopLoadEvent event) {
    event.shop().ifPresent(layerProvider::updateShopMarker);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onShopUnload(@NotNull final ShopUnloadEvent event) {
    event.shop().ifPresent(layerProvider::removeShopMarker);
  }
}
