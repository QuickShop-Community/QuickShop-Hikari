package com.ghostchu.quickshop.addon.pl3xmap;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.world.World;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class QuickShopLayer extends WorldLayer {

  final String worldName;

  public QuickShopLayer(final World world) {
    super(Main.PL3X_KEY, world, ()->Main.instance().layerName());

    this.worldName = world.getName();

    setShowControls(Main.instance().showControls());
    setDefaultHidden(Main.instance().layerEnabled());
    setUpdateInterval(Main.instance().refreshPerSeconds());
    setPriority(Main.instance().layerPriority());
  }

  @Override
  public @NotNull Collection<Marker<?>> getMarkers() {
    retrieveMarkers();

    if(Main.instance().manager() == null) {
      return Collections.emptyList();
    }

    return Main.instance().manager().worldMarkers(getWorld().getName());
  }

  private void retrieveMarkers() {
    if(Main.instance().manager() == null) {
      return;
    }

    final org.bukkit.World worldInstance = Bukkit.getServer().getWorld(worldName);

    if(worldInstance == null) {
      return;
    }

    QuickShop.folia().getScheduler().runLater(()->{

      for(final Shop shop : QuickShop.getInstance().getShopManager().getShopsInWorld(worldInstance)) {

        final String key = String.format("%s_%s_%s", Main.PL3X_KEY, worldName, shop.getShopId());
        Main.instance().manager().addMarker(key, shop);
      }
    }, 1L);
  }
}