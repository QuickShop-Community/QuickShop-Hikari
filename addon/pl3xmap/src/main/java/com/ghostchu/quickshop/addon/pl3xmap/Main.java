package com.ghostchu.quickshop.addon.pl3xmap;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.ServerLoadedEvent;
import net.pl3x.map.core.event.world.WorldLoadedEvent;
import net.pl3x.map.core.event.world.WorldUnloadedEvent;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.world.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.File;

public final class Main extends JavaPlugin implements EventListener, Listener {

  public static final String PL3X_KEY = "quickshop-hikari";
  public static final String PL3X_ICON_KEY = "quickshop-icon";

  private static Main instance;

  private ShopMarkerManager manager = null;

  //our configs
  private boolean layerEnabled = false;
  private String layerName = "QuickShop-Hikari Shops";
  private int layerPriority = 99;
  private boolean showControls = true;
  private boolean useCustomIcon = true;
  private String icon = "players";
  private int distance = 1000;
  private String markerLabel = "";
  private String markerDetail = "";
  private int refreshPerSeconds = 5;

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
    getLogger().info("Loading Configurations...");
    this.layerEnabled = getConfig().getBoolean("display-by-default");
    this.layerName = getConfig().getString("layer-name");
    this.layerPriority = getConfig().getInt("layer-priority");
    this.showControls = getConfig().getBoolean("layer-controls");
    this.useCustomIcon = getConfig().getBoolean("icon-custom");
    this.icon = getConfig().getString("icon-file-location");
    this.distance = getConfig().getInt("max-distance");
    this.markerLabel = getConfig().getString("marker-label");
    this.markerDetail = getConfig().getString("marker-detail");
    this.refreshPerSeconds = getConfig().getInt("refresh-per-seconds");

    this.manager = new ShopMarkerManager();

    if(this.layerEnabled) {

      if(this.useCustomIcon) {
        try {

          final File file = new File(this.getDataFolder(), this.icon);
          Pl3xMap.api().getIconRegistry().register(new IconImage(PL3X_ICON_KEY, ImageIO.read(file), "png"));
        } catch(final Exception ignore) {

          getLogger().warning("Failed to load icon file: " + this.icon);
        }
      }

      Pl3xMap.api().getEventRegistry().register(this);
    }

    getLogger().info("Registering the per shop permissions...");
  }

  @EventHandler
  public void onServerLoaded(@NotNull final ServerLoadedEvent event) {
    if(!this.layerEnabled) {
      return;
    }

    Pl3xMap.api().getWorldRegistry().forEach(this::registerWorld);
  }

  @EventHandler
  public void onWorldLoaded(@NotNull final WorldLoadedEvent event) {
    if(!this.layerEnabled) {
      return;
    }

    registerWorld(event.getWorld());
  }

  @EventHandler
  public void onWorldUnloaded(@NotNull final WorldUnloadedEvent event) {
    if(!this.layerEnabled || manager == null) {
      return;
    }

    try {

      this.manager.clearWorld(event.getWorld().getName());

      event.getWorld().getLayerRegistry().unregister(PL3X_KEY);
    } catch (final Throwable ignore) {}
  }

  private void registerWorld(@NotNull final World world) {
    if(manager == null) {
      return;
    }

    this.manager.addWorld(world.getName());

    world.getLayerRegistry().register(new QuickShopLayer(world));
  }

  public static Main instance() {
    return instance;
  }

  public boolean layerEnabled() {
    return layerEnabled;
  }

  public String layerName() {
    return layerName;
  }

  public int layerPriority() {
    return layerPriority;
  }

  public boolean showControls() {
    return showControls;
  }

  public String icon() {
    return icon;
  }

  public int distance() {
    return distance;
  }

  public String markerLabel() {
    return markerLabel;
  }

  public String markerDetail() {
    return markerDetail;
  }

  public int refreshPerSeconds() {
    return refreshPerSeconds;
  }

  public ShopMarkerManager manager() {
    return manager;
  }
}
