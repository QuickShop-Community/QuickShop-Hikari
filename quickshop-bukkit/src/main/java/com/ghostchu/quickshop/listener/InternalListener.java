package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.economy.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.event.inventory.ShopInventoryCalculateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopDeleteEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopPriceEvent;
import com.ghostchu.quickshop.api.serialize.BlockPos;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.logging.container.PlayerEconomyPreCheckLog;
import com.ghostchu.quickshop.util.logging.container.ShopCreationLog;
import com.ghostchu.quickshop.util.logging.container.ShopPriceChangedLog;
import com.ghostchu.quickshop.util.logging.container.ShopPurchaseLog;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class InternalListener extends AbstractQSListener {

  private final QuickShop plugin;
  private final Cache<Shop, SpaceCache> countUpdateCache = CacheBuilder
          .newBuilder()
          .weakKeys()
          .expireAfterAccess(5, TimeUnit.MINUTES)
          .maximumSize(100)
          .build();
  private boolean loggingBalance;
  private boolean loggingAction;

  public InternalListener(final QuickShop plugin) {

    super(plugin);
    this.plugin = plugin;
    plugin.getReloadManager().register(this);
    readConfig();
  }

  private void readConfig() {

    this.loggingBalance = plugin.getConfig().getBoolean("logging.log-balance");
    this.loggingAction = plugin.getConfig().getBoolean("logging.log-actions");
  }

  /**
   * Callback for reloading
   *
   * @return Reloading success
   */
  @Override
  public ReloadResult reloadModule() {

    readConfig();
    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void shopCreate(final ShopCreateEvent event) {

    if(isForbidden(event.getShop().getLocation().getBlock().getType(), event.getShop().getItem().getType())) {
      event.setCancelled(true, plugin.text().of(event.getCreator(), "forbidden-vanilla-behavior").forLocale());
      return;
    }
    if(loggingAction) {
      plugin.logEvent(new ShopCreationLog(event.getCreator(), event.getShop().saveToInfoStorage(), new BlockPos(event.getShop().getLocation())));
    }
  }

  public boolean isForbidden(@NotNull final Material shopMaterial, @NotNull final Material itemMaterial) {

    if(!Objects.equals(shopMaterial, itemMaterial)) {
      return false;
    }
    return shopMaterial.isBlock() && shopMaterial.name().toUpperCase().endsWith("SHULKER_BOX");
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void shopDelete(final ShopDeleteEvent event) {

    if(loggingAction) {
      plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SYSTEM", false), "Shop removed", event.getShop().saveToInfoStorage()));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void shopInventoryCalc(final ShopInventoryCalculateEvent event) {

    if(event.getShop().getShopId() < 1) {
      return;
    }
    final SpaceCache count = countUpdateCache.getIfPresent(event.getShop());
    if(count != null && count.getSpace() == event.getSpace() && count.getStock() == event.getStock()) {
      return;
    }
    countUpdateCache.put(event.getShop(), new SpaceCache(event.getSpace(), event.getStock()));
    plugin.getDatabaseHelper().updateExternalInventoryProfileCache(event.getShop().getShopId(), event.getSpace(), event.getStock())
            .exceptionally(err->{
              Log.debug("Error updating external inventory profile cache for shop " + event.getShop().getShopId() + ": " + err.getMessage());
              return 0;
            });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void shopPrePurchase(final ShopPurchaseEvent event) {

    if(isForbidden(event.getShop().getLocation().getBlock().getType(), event.getShop().getItem().getType())) {
      event.setCancelled(true, plugin.text().of(event.getPurchaser(), "forbidden-vanilla-behavior").forLocale());
      return;
    }
    if(loggingBalance) {
      plugin.logEvent(new PlayerEconomyPreCheckLog(true, event.getPurchaser(), plugin.getEconomy().getBalance(event.getPurchaser(), event.getShop().getLocation().getWorld(), event.getShop().getCurrency())));
      plugin.logEvent(new PlayerEconomyPreCheckLog(true, event.getShop().getOwner(), plugin.getEconomy().getBalance(event.getShop().getOwner(), event.getShop().getLocation().getWorld(), event.getShop().getCurrency())));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void shopPriceChanges(final ShopPriceEvent event) {

    if(!event.isPhase(Phase.POST)) {
      return;
    }

    if(loggingAction) {
      plugin.logEvent(new ShopPriceChangedLog(event.shop().saveToInfoStorage(), event.old(), event.updated()));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void shopPurchase(final ShopSuccessPurchaseEvent event) {

    if(loggingAction) {
      plugin.logEvent(new ShopPurchaseLog(event.getShop().saveToInfoStorage(),
                                          event.getShop().getShopType(),
                                          event.getPurchaser(),
                                          LegacyComponentSerializer.legacySection().serialize(Util.getItemStackName(event.getShop().getItem())),
                                          Util.serialize(event.getShop().getItem()),
                                          event.getAmount(),
                                          event.getBalance(),
                                          event.getTax()));
    }
    if(loggingBalance) {
      plugin.logEvent(new PlayerEconomyPreCheckLog(false, event.getPurchaser(), plugin.getEconomy().getBalance(event.getPurchaser(), event.getShop().getLocation().getWorld(), event.getShop().getCurrency())));
      plugin.logEvent(new PlayerEconomyPreCheckLog(false, event.getShop().getOwner(), plugin.getEconomy().getBalance(event.getShop().getOwner(), event.getShop().getLocation().getWorld(), event.getShop().getCurrency())));
    }
    if(event.getPurchaser().equals(event.getShop().getOwner())) {
      plugin.text().of(event.getPurchaser(), "shop-owner-self-trade").send();
    }
  }

  static class SpaceCache {

    private final int stock;
    private final int space;

    public SpaceCache(final int stock, final int space) {

      this.stock = stock;
      this.space = space;
    }

    public int getSpace() {

      return space;
    }

    public int getStock() {

      return stock;
    }
  }
}
