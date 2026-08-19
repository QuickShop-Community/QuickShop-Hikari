package com.ghostchu.quickshop.addon.squaremap;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.menu.browse.MarketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

  private final Map<String, SimpleLayerProvider> providers = new ConcurrentHashMap<>();

  @NotNull
  public SimpleLayerProvider createWorldProvider(@NotNull final World world) {
    return providers.computeIfAbsent(world.getName(), ignored ->
                                             SimpleLayerProvider.builder(Main.instance().layerName())
                                                     .showControls(Main.instance().showControls())
                                                     .defaultHidden(Main.instance().defaultHidden())
                                                     .layerPriority(Main.instance().layerPriority())
                                                     .zIndex(Main.instance().zIndex())
                                                     .build()
                                    );
  }

  @Nullable
  public SimpleLayerProvider getWorldProvider(@NotNull final World world) {

    return providers.get(world.getName());
  }

  public void clearWorld(@NotNull final World world) {

    final SimpleLayerProvider provider = providers.remove(world.getName());

    if(provider != null) {
      provider.clearMarkers();
    }
  }

  public void updateMarkers() {
    QuickShop.folia().getScheduler().runLater(() -> {
      for(final String worldName : providers.keySet()) {
        final World world = Bukkit.getWorld(worldName);

        if(world == null) {
          continue;
        }

        final Collection<Shop> shops = QuickShop.getInstance().getShopManager().getShopsInWorld(world);
        //final Collection<Shop> shops = new ShopQuery().filterBy(Filters.WORLD_UUID).execute();

        for(final Shop shop : shops) {
          updateShopMarker(shop);
        }
      }
    }, 1L);
  }

  public void updateShopMarker(@NotNull final Shop shop) {

    final Location location = shop.bukkitLocation();
    final World world = location.getWorld();

    if(world == null) {
      return;
    }

    final SimpleLayerProvider provider = getWorldProvider(world);

    if(provider == null) {
      return;
    }

    final Key markerKey = markerKey(shop);

    provider.removeMarker(markerKey);
    provider.addMarker(markerKey, createShopIcon(shop));
  }

  public void removeShopMarker(@NotNull final Shop shop) {

    final World world = shop.bukkitLocation().getWorld();

    if(world == null) {
      return;
    }

    final SimpleLayerProvider provider = getWorldProvider(world);

    if(provider != null) {

      provider.removeMarker(markerKey(shop));
    }
  }

  @NotNull
  private Key markerKey(@NotNull final Shop shop) {

    return Key.of(Main.SQUAREMAP_KEY + "_" + shop.bukkitLocation().getWorld().getName() + "_" + shop.getShopId());
  }

  @NotNull
  private Icon createShopIcon(@NotNull final Shop shop) {

    final Location loc = shop.bukkitLocation();
    final Point point = Point.of(loc.getBlockX(), loc.getBlockZ());

    final String tooltip = fillPlaceholders(Main.instance().markerTooltip(), shop);

    final Icon icon = Marker.icon(point, Main.instance().shopIconKey(), 16);

    final MarkerOptions options = MarkerOptions.builder().hoverTooltip(tooltip).build();

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

    final int stock = (shop.shopType().isBuying())? MarketUtils.getSpaceFromCache(shop) : MarketUtils.getStockFromCache(shop);

    return text.replace("%owner%", plain(shop.ownerName()))
            .replace("%item%", shop.getItem().getType().name())
            .replace("%price%", String.valueOf(shop.getPrice()))
            .replace("%stock%", stock == -1 ? "Unlimited" : String.valueOf(stock))
            .replace("%type%", shop.shopType().identifier())
            .replace("%location%", x + "," + y + "," + z)
            .replace("%x%", x)
            .replace("%y%", y)
            .replace("%z%", z)
            .replace("%world%", loc.getWorld().getName());
  }
}
