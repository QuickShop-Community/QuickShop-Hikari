package com.ghostchu.quickshop.addon.bluemap;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.localization.text.TextManager;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.menu.browse.MarketUtils;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class Main extends JavaPlugin implements Listener {

  static Main instance;
  private QuickShop plugin;
  private BlueMapAPI blueMapAPI;

  @Override
  public void onLoad() {

    instance = this;
  }

  @Override
  public void onDisable() {

    HandlerList.unregisterAll((Plugin)this);
  }

  @Override
  public void onEnable() {

    saveDefaultConfig();
    plugin = QuickShop.getInstance();
    getLogger().info("Registering the per shop permissions...");
    BlueMapAPI.onEnable(blueMapAPI->{
      getLogger().info("Found BlueMap loaded! Hooking!");
      this.blueMapAPI = blueMapAPI;
      createMarkerSet();
      QuickShop.folia().getScheduler().runTimerAsync(this::updateAllMarkers, 1, getConfig().getInt("refresh-per-seconds") * 20L);
    });
    BlueMapAPI.onDisable(api->QuickShop.folia().getScheduler().cancelAllTasks());
  }

  @NotNull
  public String plain(@NotNull final Component component) {

    return PlainTextComponentSerializer.plainText().serialize(component);
  }

  @NotNull
  public TextManager text() {

    return plugin.getTextManager();
  }

  public MarkerSet createMarkerSet() {

    return MarkerSet.builder()
            .defaultHidden(true)
            .label(plain(text().of("addon.bluemap.markerset-title").forLocale()))
            .defaultHidden(getConfig().getBoolean("display-by-default"))
            .toggleable(true)
            .build();
  }

  private void updateAllMarkers() {

    blueMapAPI.getWorlds().forEach(bWorld->bWorld.getMaps().forEach(bMap->bMap.getMarkerSets().remove("quickshop-hikari-shops")));
    plugin.getShopManager().getAllShops().forEach(this::updateShopMarker);
  }

  public void updateShopMarker(final Shop shop) {

    final Optional<BlueMapWorld> bWorld = blueMapAPI.getWorld(shop.bukkitLocation().getWorld());
    if(bWorld.isEmpty()) {
      return;
    }
    for(final BlueMapMap map : bWorld.get().getMaps()) {
      final MarkerSet markerSet = map.getMarkerSets().computeIfAbsent("quickshop-hikari-shops", (key)->createMarkerSet());
      final String markerName = fillPlaceholders(getConfig().getString("marker-label"), shop);
      final String desc = fillPlaceholders(getConfig().getString("marker-detail"), shop);
      
      final POIMarker.Builder markerBuilder = POIMarker.builder()
              .label(markerName)
              .position(shop.bukkitLocation().getX(),
                        shop.bukkitLocation().getY() + 1,
                        shop.bukkitLocation().getZ())
              .maxDistance(getConfig().getDouble("max-distance"))
              .detail(desc);
      
      // Use custom icon or colored circle based on config
      if(getConfig().getBoolean("use-custom-icon", false)) {
        final String iconPath = getConfig().getString("icon-file-location", "/assets/chest.png");
        markerBuilder.icon(iconPath, 0, 0);
      } else {
        final String color = getShopColor(shop);
        final String svgIcon = "data:image/svg+xml," + createColoredMarkerSvg(color);
        markerBuilder.icon(svgIcon, 12, 24);  // Anchor at bottom-center of 24x24 icon
      }
      
      markerSet.getMarkers().put("quickshop-hikari-shop" + shop.getShopId(), markerBuilder.build());
    }
  }

  /**
   * Create a colored circle SVG marker
   *
   * @param color The fill color
   * @return SVG string (URL encoded)
   */
  @NotNull
  private String createColoredMarkerSvg(final String color) {

    final String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24'>" +
            "<circle cx='12' cy='12' r='10' fill='" + color + "' stroke='white' stroke-width='2'/>" +
            "</svg>";
    // URL encode for data URI
    return svg.replace("#", "%23").replace("'", "%27");
  }

  /**
   * Get the color for a shop based on its state
   *
   * @param shop The shop to get color for
   * @return The color (CSS format)
   */
  @NotNull
  private String getShopColor(final Shop shop) {

    if(shop.isFrozen()) {
      return getConfig().getString("colors.frozen", "#888888");
    }
    if(shop.isBuying()) {
      return getConfig().getString("colors.buying", "#3498db");
    }
    // Default to selling
    return getConfig().getString("colors.selling", "#2ecc71");
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
