package com.ghostchu.quickshop.addon.squaremap;

import com.ghostchu.quickshop.QuickShop;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.jpenilla.squaremap.api.BukkitAdapter;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.Squaremap;
import xyz.jpenilla.squaremap.api.SquaremapProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public final class Main extends JavaPlugin implements Listener {

  public static final String SQUAREMAP_KEY = "quickshop-hikari";
  public static final String SHOP_ICON_KEY = "quickshop_shop_icon";
  private static final long TICKS_IN_SECOND = 20L;

  private static Main instance;

  private ShopLayerProvider layerProvider = null;
  private Squaremap squaremapApi = null;
  private Key shopIconKey = null;

  //our configs
  private boolean layerEnabled = false;
  private String layerName = "QuickShop-Hikari Shops";
  private int layerPriority = 99;
  private int zIndex = 250;
  private boolean showControls = true;
  private boolean defaultHidden = false;
  private String markerLabel = "";
  private String markerTooltip = "";

  @Override
  public void onLoad() {

    instance = this;
  }

  @Override
  public void onDisable() {

    HandlerList.unregisterAll((Plugin)this);

    // Cancel all scheduled tasks
    QuickShop.folia().getScheduler().cancelAllTasks();

    if(squaremapApi != null && layerProvider != null) {
      unregisterAllWorlds();
    }
  }

  @Override
  public void onEnable() {

    saveDefaultConfig();
    getLogger().info("Loading Configurations...");
    this.layerEnabled = getConfig().getBoolean("layer-enabled");
    this.layerName = getConfig().getString("layer-name");
    this.layerPriority = getConfig().getInt("layer-priority");
    this.zIndex = getConfig().getInt("z-index");
    this.showControls = getConfig().getBoolean("show-controls");
    this.defaultHidden = getConfig().getBoolean("default-hidden");
    this.markerLabel = getConfig().getString("marker-label");
    this.markerTooltip = getConfig().getString("marker-tooltip");
    final int refreshPerSeconds = getConfig().getInt("refresh-per-seconds");

    if(!this.layerEnabled) {
      getLogger().info("Squaremap layer is disabled in config. Not loading.");
      return;
    }

    try {
      this.squaremapApi = SquaremapProvider.get();

      // Register custom icon
      registerCustomIcon();

      this.layerProvider = new ShopLayerProvider();

      getLogger().info("Squaremap API found! Registering layers...");
      registerAllWorlds();

      // Register event listeners
      final ShopEventListener shopEventListener = new ShopEventListener(layerProvider);
      getServer().getPluginManager().registerEvents(this, this);
      getServer().getPluginManager().registerEvents(shopEventListener, this);

      // Start marker update task
      QuickShop.folia().getScheduler().runTimerAsync(
          () -> {
            if(layerProvider != null) {
              layerProvider.updateMarkers();
            }
          },
          TICKS_IN_SECOND,
          refreshPerSeconds * TICKS_IN_SECOND
      );

      getLogger().info("Successfully integrated with Squaremap!");
    } catch(final Exception e) {
      getLogger().severe("Failed to hook into Squaremap: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @EventHandler
  public void onWorldLoad(@NotNull final WorldLoadEvent event) {
    if(!this.layerEnabled || squaremapApi == null || layerProvider == null) {
      return;
    }

    registerWorld(event.getWorld());
  }

  @EventHandler
  public void onWorldUnload(@NotNull final WorldUnloadEvent event) {
    if(!this.layerEnabled || squaremapApi == null || layerProvider == null) {
      return;
    }

    unregisterWorld(event.getWorld());
  }

  private void registerAllWorlds() {
    for(final World world : getServer().getWorlds()) {
      registerWorld(world);
    }
  }

  private void registerWorld(@NotNull final World world) {
    try {
      squaremapApi.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world))
          .ifPresent(mapWorld -> {
            final Key key = Key.of(SQUAREMAP_KEY);
            if(!mapWorld.layerRegistry().hasEntry(key)) {
              final SimpleLayerProvider provider = layerProvider.createWorldProvider(world);

              mapWorld.layerRegistry().register(key, provider);
              getLogger().info("Registered QuickShop layer for world: " + world.getName());
            }
          });
    } catch(final Exception e) {
      getLogger().warning("Failed to register world " + world.getName() + ": " + e.getMessage());
    }
  }

  private void unregisterWorld(@NotNull final World world) {
    try {
      squaremapApi.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world))
          .ifPresent(mapWorld -> {
            final Key key = Key.of(SQUAREMAP_KEY);
            if(mapWorld.layerRegistry().hasEntry(key)) {
              mapWorld.layerRegistry().unregister(key);
              layerProvider.clearWorld(world);
              getLogger().info("Unregistered QuickShop layer for world: " + world.getName());
            }
          });
    } catch(final Exception e) {
      getLogger().warning("Failed to unregister world " + world.getName() + ": " + e.getMessage());
    }
  }

  private void unregisterAllWorlds() {
    for(final World world : getServer().getWorlds()) {
      unregisterWorld(world);
    }
  }

  public static Main instance() {
    return instance;
  }

  public String layerName() {
    return layerName;
  }

  public int layerPriority() {
    return layerPriority;
  }

  public int zIndex() {
    return zIndex;
  }

  public boolean showControls() {
    return showControls;
  }

  public boolean defaultHidden() {
    return defaultHidden;
  }

  public String markerLabel() {
    return markerLabel;
  }

  public String markerTooltip() {
    return markerTooltip;
  }

  public Key shopIconKey() {
    return shopIconKey;
  }

  private void registerCustomIcon() {
    try {
      // Load icon from resources
      final InputStream iconStream = getResource("shop_icon.png");
      if(iconStream == null) {
        getLogger().severe("Could not load shop_icon.png from resources!");
        throw new RuntimeException("Custom shop icon not found in resources");
      }

      final BufferedImage iconImage = ImageIO.read(iconStream);
      iconStream.close();

      if(iconImage == null) {
        getLogger().severe("Failed to read shop_icon.png - ImageIO.read() returned null!");
        getLogger().severe("This usually means the image file is corrupted or not a valid PNG/image format.");
        throw new RuntimeException("Failed to parse custom shop icon - image data is invalid");
      }

      getLogger().info("Loaded shop icon: " + iconImage.getWidth() + "x" + iconImage.getHeight() + " pixels");

      // Register the icon with squaremap
      shopIconKey = Key.of(SHOP_ICON_KEY);
      squaremapApi.iconRegistry().register(shopIconKey, iconImage);

      getLogger().info("Registered custom shop icon with squaremap");
    } catch(final IOException e) {
      getLogger().severe("Failed to load custom icon: " + e.getMessage());
      throw new RuntimeException("Failed to load custom shop icon", e);
    }
  }

}
