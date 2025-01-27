package com.ghostchu.quickshop.compatibility.plotsquared;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.modification.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.CaptionMap;
import com.plotsquared.core.configuration.caption.LocalizedCaptionMap;
import com.plotsquared.core.configuration.caption.PerUserLocaleCaptionMap;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import com.sk89q.worldedit.regions.CuboidRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Main extends CompatibilityModule implements Listener {

  private boolean whiteList;
  private boolean deleteUntrusted;
  private QuickshopCreateFlag createFlag;
  private QuickshopTradeFlag tradeFlag;

  @EventHandler(ignoreCancelled = true)
  public void canCreateShopHere(final ShopPreCreateEvent event) {

    final Location location = event.getLocation();
    final com.plotsquared.core.location.Location pLocation = com.plotsquared.core.location.Location.at(
            location.getWorld().getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ());
    final Plot plot = pLocation.getPlot();
    if(plot == null) {
      if(!whiteList) {
        event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.plotsqured.no-plot-whitelist-creation").forLocale());
      }
      return;
    }
    if(!plot.getFlag(tradeFlag)) {
      event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.plotsqured.trade-denied").forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void canTradeShopHere(final ShopPurchaseEvent event) {

    final Location location = event.getShop().getLocation();
    final com.plotsquared.core.location.Location pLocation = com.plotsquared.core.location.Location.at(
            location.getWorld().getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ());
    final Plot plot = pLocation.getPlot();
    if(plot == null) {
      if(!whiteList) {
        event.setCancelled(true, getApi().getTextManager().of(event.getPurchaser(), "addon.plotsqured.no-plot-whitelist-creation").forLocale());
      }
      return;
    }
    if(!plot.getFlag(tradeFlag)) {
      event.setCancelled(true, getApi().getTextManager().of(event.getPurchaser(), "addon.plotsqured.trade-denied").forLocale());
    }
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    super.onDisable();
    PlotSquared.get().getEventDispatcher().unregisterListener(this);
  }

  @Override
  public void onEnable() {
    // Plugin startup logic
    super.onEnable();
    getLogger().info("Mapping localized captions...");
    final Map<Locale, CaptionMap> finalRegisterMap = new HashMap<>();
    for(final String availableLanguage : getApi().getTextManager().getAvailableLanguages()) {
      final Component flagCreate = getApi().getTextManager().of("addon.plotsquared.flag.create").forLocale(availableLanguage);
      final Component flagPurchase = getApi().getTextManager().of("addon.plotsquared.flag.trade").forLocale(availableLanguage);
      final Map<TranslatableCaption, String> stringMapping = new HashMap<>();
      stringMapping.put(TranslatableCaption.of("quickshop-hikari", "quickshop-create"), LegacyComponentSerializer.legacySection().serialize(flagCreate));
      stringMapping.put(TranslatableCaption.of("quickshop-hikari", "quickshop-trade"), LegacyComponentSerializer.legacySection().serialize(flagPurchase));
      finalRegisterMap.put(Locale.forLanguageTag(availableLanguage.substring(0, 2)), new LocalizedCaptionMap(Locale.forLanguageTag(availableLanguage.substring(0, 2)), stringMapping));
    }
    PlotSquared.get().registerCaptionMap("quickshop-hikari", new PerUserLocaleCaptionMap(finalRegisterMap));
    this.createFlag = new QuickshopCreateFlag();
    this.tradeFlag = new QuickshopTradeFlag();
    GlobalFlagContainer.getInstance().addAll(Arrays.asList(createFlag, tradeFlag));
    getLogger().info("Flags register successfully.");
    PlotSquared.get().getEventDispatcher().registerListener(this);

  }

  @Override
  public void init() {

    this.whiteList = getConfig().getBoolean("whitelist-mode");
    this.deleteUntrusted = getConfig().getBoolean("delete-when-user-untrusted");
  }

  @Subscribe
  public void onPlotDelete(final PlotDeleteEvent event) {

    getShops(event.getPlot()).forEach(shop->{
      recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "PlotSquared", false), shop, "Plot deleted");
      getApi().getShopManager().deleteShop(shop);
    });
  }

  private List<Shop> getShops(final Plot plot) {

    final List<Shop> shopsList = new ArrayList<>();
    for(final CuboidRegion region : plot.getRegions()) {
      String worldName = plot.getWorldName();
      if(region.getWorld() != null) {
        worldName = region.getWorld().getName();
      }
      if(worldName == null) {
        getLogger().warning("Failed to handle CuboidRegion " + region + " in plot " + plot.getId() + " because world is null, does the world exist? Skipping...");
      } else {
        shopsList.addAll(getShops(worldName, region.getMinimumPoint().getX(), region.getMinimumPoint().getZ(), region.getMaximumPoint().getX(), region.getMaximumPoint().getZ()));
      }
    }
    return shopsList;
  }

  @Subscribe
  public void onPlotPlayerUntrusted(final com.plotsquared.core.events.PlayerPlotTrustedEvent event) {

    if(!deleteUntrusted) {
      return;
    }
    if(event.wasAdded()) {
      return; // We only check untrusted
    }
    getShops(event.getPlot()).stream().filter(shop->event.getPlayer().equals(shop.getOwner().getUniqueId())).forEach(shop->{
      recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "PlotSquared", false), shop, "Untrusted -> " + event.getPlayer());
      getApi().getShopManager().deleteShop(shop);
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void onShopCreation(final ShopCreateEvent event) {

    final Location location = event.getShop().getLocation();
    final com.plotsquared.core.location.Location pLocation = com.plotsquared.core.location.Location.at(
            location.getWorld().getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ());
    final Plot plot = pLocation.getPlot();
    if(plot == null) {
      if(!whiteList) {
        event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.plotsquared.no-plot-whitelist-creation").forLocale());
      }
      return;
    }
    if(!plot.getFlag(createFlag)) {
      event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.plotsquared.create-denied").forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onShopTrading(final ShopPurchaseEvent event) {

    final Location location = event.getShop().getLocation();
    final com.plotsquared.core.location.Location pLocation = com.plotsquared.core.location.Location.at(
            location.getWorld().getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ());
    final Plot plot = pLocation.getPlot();
    if(plot == null) {
      if(!whiteList) {
        event.setCancelled(true, getApi().getTextManager().of(event.getPurchaser(), "addon.plotsquared.no-plot-whitelist-creation").forLocale());
      }
      return;
    }
    if(!plot.getFlag(tradeFlag)) {
      event.setCancelled(true, getApi().getTextManager().of(event.getPurchaser(), "addon.plotsquared.trade-denied").forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void permissionOverride(final ShopAuthorizeCalculateEvent event) {

    final Location shopLoc = event.getShop().getLocation();
    final com.plotsquared.core.location.Location pLocation = com.plotsquared.core.location.Location.at(shopLoc.getWorld().getName(), shopLoc.getBlockX(), shopLoc.getBlockY(), shopLoc.getBlockZ());
    final Plot plot = pLocation.getPlot();
    if(plot == null) {
      return;
    }
    if(plot.getOwners().contains(event.getAuthorizer())) {
      if(event.getNamespace().equals(QuickShop.getInstance().getJavaPlugin()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
        event.setResult(true);
      }
    }
  }

  static class QuickshopCreateFlag extends BooleanFlag<QuickshopCreateFlag> {

    protected QuickshopCreateFlag(final boolean value, final Caption description) {

      super(value, description);
    }

    public QuickshopCreateFlag() {

      super(true, TranslatableCaption.of("quickshop-hikari", "quickshop-create"));
    }

    @Override
    protected QuickshopCreateFlag flagOf(@NotNull final Boolean aBoolean) {

      return new QuickshopCreateFlag(aBoolean, TranslatableCaption.of("quickshop-hikari", "quickshop-create"));
    }
  }

  static class QuickshopTradeFlag extends BooleanFlag<QuickshopTradeFlag> {

    protected QuickshopTradeFlag(final boolean value, final Caption description) {

      super(value, description);
    }

    public QuickshopTradeFlag() {

      super(true, TranslatableCaption.of("quickshop-hikari", "quickshop-trade"));
    }

    @Override
    protected QuickshopTradeFlag flagOf(@NotNull final Boolean aBoolean) {

      return new QuickshopTradeFlag(aBoolean, TranslatableCaption.of("quickshop-hikari", "quickshop-trade"));
    }
  }

}
