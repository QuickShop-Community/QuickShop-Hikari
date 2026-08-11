package com.ghostchu.quickshop.addon.pl3xmap;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.menu.browse.MarketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.marker.Icon;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Tooltip;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShopMarkerManager {

  private final Map<String, Collection<Marker<?>>> shopMarkers = new ConcurrentHashMap<>();

  public Collection<Marker<?>> worldMarkers(@NotNull final String world) {

    if(!shopMarkers.containsKey(world)) {
      return Collections.emptySet();
    }

    return shopMarkers.get(world);
  }

  public void clearWorld(@NotNull final String world) {

    this.shopMarkers.remove(world);
  }

  public void clearAll() {

    this.shopMarkers.clear();
  }

  public void addWorld(@NotNull final String world) {

    if(shopMarkers.containsKey(world) || world.isBlank()) {
      return;
    }

    this.shopMarkers.put(world, new HashSet<>());
  }

  public void addMarker(@NotNull final String key, @NotNull final Shop shop) {
    final Icon icon = getIcon(key, shop);

    removeMarker(shop);

    this.shopMarkers.get(shop.bukkitLocation().getWorld().getName()).add(icon);
  }

  public void removeMarker(@NotNull final Shop shop) {
    final String worldName = shop.bukkitLocation().getWorld().getName();

    final String key = String.format("%s_%s_%s", Main.PL3X_KEY, worldName, shop.getShopId());

    if(!this.shopMarkers.containsKey(worldName)) {
      return;
    }

    this.shopMarkers.get(worldName).removeIf(marker->marker.getKey().equals(key));
  }

  private @NotNull Point point(@NotNull final Location loc) {
    return Point.of(loc.getBlockX(), loc.getBlockZ());
  }

  public Icon getIcon(@NotNull final String key, @NotNull final Shop shop) {
    return Marker.icon(key, point(shop.bukkitLocation()), Main.PL3X_ICON_KEY)
        .setOptions(Options.builder()
            .tooltipDirection(Tooltip.Direction.TOP)
            .tooltipContent(fillPlaceholders(Main.instance().markerDetail(), shop))
            .build());
  }

  @NotNull
  public String plain(@NotNull final Component component) {

    return PlainTextComponentSerializer.plainText().serialize(component);
  }

  private String fillPlaceholders(String s, final Shop shop) {

    final Location loc = shop.bukkitLocation();
    final String x = String.valueOf(loc.getX());
    final String y = String.valueOf(loc.getY());
    final String z = String.valueOf(loc.getZ());
    s = s.replace("%owner%", plain(shop.ownerName()));
    s = s.replace("%item%", shop.getItem().getType().name());
    s = s.replace("%price%", String.valueOf(shop.getPrice()));
    final int stock = shop.shopType().isBuying() ? MarketUtils.getSpaceFromCache(shop) : MarketUtils.getStockFromCache(shop);
    s = s.replace("%stock%", String.valueOf(stock == -1 ? "Unlimited" : stock));
    s = s.replace("%type%", shop.shopType().identifier());
    s = s.replace("%location%", x + "," + y + "," + z);
    return s;
  }
}
