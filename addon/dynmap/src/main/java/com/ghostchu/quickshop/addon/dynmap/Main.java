package com.ghostchu.quickshop.addon.dynmap;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.QSConfigurationReloadEvent;
import com.ghostchu.quickshop.api.event.details.ShopOwnershipTransferEvent;
import com.ghostchu.quickshop.api.event.details.ShopPriceChangeEvent;
import com.ghostchu.quickshop.api.event.details.ShopTypeChangeEvent;
import com.ghostchu.quickshop.api.event.economy.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.event.modification.ShopCreateSuccessEvent;
import com.ghostchu.quickshop.api.event.modification.ShopDeleteEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopItemEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopNameEvent;
import com.ghostchu.quickshop.api.localization.text.TextManager;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.jetbrains.annotations.NotNull;

public final class Main extends JavaPlugin implements Listener {

  static Main instance;
  private QuickShop plugin;
  private DynmapCommonAPI dynmapAPI;
  private MarkerAPI markerAPI;

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
    this.dynmapAPI = (DynmapCommonAPI)Bukkit.getPluginManager().getPlugin("dynmap");

    if(dynmapAPI == null) {
      return;
    }

    this.markerAPI = this.dynmapAPI.getMarkerAPI();
    if(markerAPI == null) {
      return;
    }

    Bukkit.getPluginManager().registerEvents(this, this);
    QuickShop.folia().getImpl().runLater(this::updateAllMarkers, 80);
  }

  @EventHandler(ignoreCancelled = true)
  public void onEvent(final WorldLoadEvent event) {

    Util.mainThreadRun(this::updateAllMarkers);
  }

  @EventHandler(ignoreCancelled = true)
  public void onEvent(final WorldUnloadEvent event) {

    Util.mainThreadRun(this::updateAllMarkers);
  }

  @EventHandler(ignoreCancelled = true)
  public void onEvent(final QSConfigurationReloadEvent event) {

    Util.mainThreadRun(this::updateAllMarkers);
  }

  @EventHandler(ignoreCancelled = true)
  public void onEvent(final ShopCreateSuccessEvent event) {

    updateShopMarker(event.getShop());
  }

  @EventHandler(ignoreCancelled = true)
  public void onEvent(final ShopDeleteEvent event) {

    Util.mainThreadRun(()->updateShopMarker(event.getShop()));
  }

  @EventHandler(ignoreCancelled = true)
  public void onEvent(final ShopPriceChangeEvent event) {

    Util.mainThreadRun(()->updateShopMarker(event.getShop()));
  }

  @EventHandler(ignoreCancelled = true)
  public void onEvent(final ShopItemEvent event) {

    if(event.phase() != Phase.POST) {
      return;
    }

    Util.mainThreadRun(()->updateShopMarker(event.shop()));
  }

  @EventHandler(ignoreCancelled = true)
  public void onEvent(final ShopOwnershipTransferEvent event) {

    Util.mainThreadRun(()->updateShopMarker(event.getShop()));
  }

  @EventHandler(ignoreCancelled = true)
  public void onEvent(final ShopSuccessPurchaseEvent event) {

    Util.mainThreadRun(()->updateShopMarker(event.getShop()));
  }

  @EventHandler(ignoreCancelled = true)
  public void onEvent(final ShopTypeChangeEvent event) {

    Util.mainThreadRun(()->updateShopMarker(event.getShop()));
  }

  @EventHandler(ignoreCancelled = true)
  public void onEvent(final ShopNameEvent event) {

    if(event.phase() != Phase.POST) {

      return;
    }

    Util.mainThreadRun(()->updateShopMarker(event.shop()));
  }

  @NotNull
  public String plain(@NotNull final Component component) {

    return PlainTextComponentSerializer.plainText().serialize(component);
  }

  @NotNull
  public TextManager text() {

    return plugin.getTextManager();
  }

  public MarkerSet getMarkerSet() {

    final String id = PackageUtil.parsePackageProperly("marker-id").asString("quickshop-hikari-shops");
    MarkerSet markerSet = markerAPI.getMarkerSet(id);
    if(markerSet == null) {
      markerSet = markerAPI.createMarkerSet(id, plain(text().of("addon.dynmap.markerset-title").forLocale()), null, false);
    }
    markerSet.setHideByDefault(getConfig().getBoolean("display-by-default", true));
    return markerSet;
  }

  public MarkerIcon getShopMarkerIcon() {

    return markerAPI.getMarkerIcon(PackageUtil.parsePackageProperly("marker-icon").asString("chest"));
  }

  public void updateAllMarkers() {

    final MarkerSet markerSet = getMarkerSet();
    markerSet.getMarkers().forEach(GenericMarker::deleteMarker);
    for(final Shop shop : plugin.getShopManager().getAllShops()) {
      updateShopMarker(shop);
    }
  }

  public void updateShopMarker(final Shop shop) {

    final MarkerSet markerSet = getMarkerSet();
    String shopName = shop.getShopName();
    final String posStr = String.format("%s %s, %s, %s", shop.getLocation().getWorld().getName(), shop.getLocation().getBlockX(), shop.getLocation().getBlockY(), shop.getLocation().getBlockZ());
    if(shopName == null) {
      shopName = posStr;
    }
    Marker marker = markerSet.findMarker("quickshop-hikari-shop-" + shop.getShopId());
    final String type = switch(shop.getShopType()) {
      case SELLING -> plain(text().of("shop-type.selling").forLocale());
      case BUYING -> plain(text().of("shop-type.buying").forLocale());
      case FROZEN -> plain(text().of("shop-type.frozen").forLocale());
    };

    final String markerName = plain(text().of("addon.dynmap.marker-name",
                                              shopName,
                                              plain(shop.ownerName()),
                                              plain(Util.getItemStackName(shop.getItem())),
                                              plugin.getShopManager().format(shop.getPrice(), shop),
                                              shop.getShopStackingAmount(),
                                              type,
                                              shop.isUnlimited(),
                                              posStr
                                             ).forLocale());
    if(marker == null) {
      marker = markerSet.createMarker("quickshop-hikari-shop-" + shop.getShopId()
              , markerName
              , shop.getLocation().getWorld().getName(),
                                      shop.getLocation().getX(),
                                      shop.getLocation().getY(),
                                      shop.getLocation().getZ(),
                                      getShopMarkerIcon(), false);
    } else {
      marker.setLocation(shop.getLocation().getWorld().getName(),
                         shop.getLocation().getX(),
                         shop.getLocation().getY(),
                         shop.getLocation().getZ());
    }
    final String desc = plain(text().of("addon.dynmap.marker-description",
                                        shopName,
                                        plain(shop.ownerName()),
                                        plain(Util.getItemStackName(shop.getItem())),
                                        plugin.getShopManager().format(shop.getPrice(), shop),
                                        shop.getShopStackingAmount(),
                                  shop.getShopType() == ShopType.SELLING? plain(text().of("shop-type.selling").forLocale()) : plain(text().of("shop-type.buying").forLocale()),
                                        shop.isUnlimited(),
                                        posStr
                                       ).forLocale());
    marker.setDescription(desc.replace("\n", "<br/>"));
  }

}
