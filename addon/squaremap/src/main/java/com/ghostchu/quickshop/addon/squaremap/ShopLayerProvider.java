package com.ghostchu.quickshop.addon.squaremap;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.menu.browse.MarketUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.marker.Icon;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShopLayerProvider {

  @Getter
  private final SimpleLayerProvider provider;
  private final Map<String, Boolean> registeredWorlds = new ConcurrentHashMap<>();

  public ShopLayerProvider() {
    this.provider = SimpleLayerProvider.builder(Main.instance().layerName())
        .showControls(Main.instance().showControls())
        .defaultHidden(Main.instance().defaultHidden())
        .layerPriority(Main.instance().layerPriority())
        .zIndex(Main.instance().zIndex())
        .build();
  }

  public void addWorld(@NotNull final String worldName) {
    if(registeredWorlds.containsKey(worldName)) {
      return;
    }
    registeredWorlds.put(worldName, true);
  }

  public void clearWorld(@NotNull final String worldName) {
    registeredWorlds.remove(worldName);
    // Clear all markers for this world by removing them individually
    provider.registeredMarkers().keySet().stream()
        .filter(markerKey -> {
          final String key = markerKey.getKey();
          return key.startsWith(Main.SQUAREMAP_KEY + "_" + worldName + "_");
        })
        .forEach(provider::removeMarker);
  }

  public void updateMarkers() {
    // Get all shops and update their markers
    QuickShop.folia().getScheduler().runLater(() -> {
      for(final String worldName : registeredWorlds.keySet()) {
        final org.bukkit.World world = Bukkit.getWorld(worldName);
        if(world == null) {
          continue;
        }

        final Collection<Shop> shops = QuickShop.getInstance().getShopManager().getShopsInWorld(world);
        for(final Shop shop : shops) {
          updateShopMarker(shop);
        }
      }
    }, 1L);
  }

  public void updateShopMarker(@NotNull final Shop shop) {
    final String worldName = shop.bukkitLocation().getWorld().getName();
    if(!registeredWorlds.containsKey(worldName)) {
      return;
    }

    final Key markerKey = Key.of(String.format("%s_%s_%s", Main.SQUAREMAP_KEY, worldName, shop.getShopId()));

    // Remove old marker if exists
    provider.removeMarker(markerKey);

    // Create new marker
    final Icon icon = createShopIcon(shop);
    provider.addMarker(markerKey, icon);
  }

  public void removeShopMarker(@NotNull final Shop shop) {
    final String worldName = shop.bukkitLocation().getWorld().getName();
    final Key markerKey = Key.of(String.format("%s_%s_%s", Main.SQUAREMAP_KEY, worldName, shop.getShopId()));
    provider.removeMarker(markerKey);
  }

  @NotNull
  private Icon createShopIcon(@NotNull final Shop shop) {
    final Location loc = shop.bukkitLocation();
    final Point point = Point.of(loc.getBlockX(), loc.getBlockZ());

    final String tooltip = fillPlaceholders(Main.instance().markerTooltip(), shop);

    // Use custom icon registered by the plugin
    final Icon icon = Marker.icon(point, Main.instance().shopIconKey(), 16);

    final MarkerOptions options = MarkerOptions.builder()
        .hoverTooltip(tooltip)
        .build();

    icon.markerOptions(options);

    return icon;
  }

  @NotNull
  private String plain(@NotNull final Component component) {
    return PlainTextComponentSerializer.plainText().serialize(component);
  }

  @NotNull
  private String fillPlaceholders(@NotNull final String text, @NotNull final Shop shop) {
    final Location loc = shop.bukkitLocation();
    final String x = String.valueOf(loc.getBlockX());
    final String y = String.valueOf(loc.getBlockY());
    final String z = String.valueOf(loc.getBlockZ());

    final int stock = shop.shopType().isBuying() ? MarketUtils.getSpaceFromCache(shop) : MarketUtils.getStockFromCache(shop);

    return text.replace("%owner%", plain(shop.ownerName()))
        .replace("%item%", shop.getItem().getType().name())
        .replace("%price%", String.valueOf(shop.getPrice()))
        .replace("%stock%", String.valueOf(stock == -1 ? "Unlimited" : stock))
        .replace("%type%", shop.shopType().identifier())
        .replace("%location%", x + "," + y + "," + z)
        .replace("%x%", x)
        .replace("%y%", y)
        .replace("%z%", z)
        .replace("%world%", loc.getWorld().getName());
  }
}
