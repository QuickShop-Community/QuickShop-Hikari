package com.ghostchu.quickshop.addon.discordsrv.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.discordsrv.Main;
import com.ghostchu.quickshop.addon.discordsrv.bean.NotificationFeature;
import com.ghostchu.quickshop.addon.discordsrv.database.DiscordDatabaseHelper;
import com.ghostchu.quickshop.addon.discordsrv.wrapper.JDAWrapper;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.details.ShopOwnershipTransferEvent;
import com.ghostchu.quickshop.api.event.details.ShopPlayerGroupSetEvent;
import com.ghostchu.quickshop.api.event.economy.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.event.modification.ShopDeleteEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopPriceEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;

public class QuickShopEventListener implements Listener {

  private final Main plugin;
  private final JDAWrapper jdaWrapper;
  private final DiscordDatabaseHelper databaseHelper;

  public QuickShopEventListener(final Main plugin) {

    this.plugin = plugin;
    this.jdaWrapper = plugin.getJdaWrapper();
    this.databaseHelper = plugin.getDatabaseHelper();
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPurchase(final ShopSuccessPurchaseEvent event) {

    if(event.getShop().isUnlimited() && plugin.getConfig().getBoolean("shop.ignore-unlimited-shop-messages")) {
      return;
    }
    notifyShopPurchase(event);
    notifyModShopPurchase(event);
    notifyShopOutOfStock(event);
    notifyShopOutOfSpace(event);
  }

  private void notifyShopPurchase(final ShopSuccessPurchaseEvent event) {

    Util.asyncThreadRun(()->{
      final MessageEmbed embed = plugin.getFactory().shopPurchasedSelf(event);
      //sendMessageIfEnabled(event.getShop().getOwner(), embed, NotificationFeature.USER_SHOP_PURCHASE);
      // Send to permission users
      for(final UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
        sendMessageIfEnabled(uuid, event.getShop(), embed, NotificationFeature.USER_SHOP_PURCHASE);
      }
    });
  }

  private void notifyModShopPurchase(final ShopSuccessPurchaseEvent event) {

    Util.asyncThreadRun(()->{
      final MessageEmbed embed = plugin.getFactory().modShopPurchase(event);
      sendModeratorChannelMessageIfEnabled(embed, NotificationFeature.MOD_SHOP_PURCHASE);
    });
  }

  private void notifyShopOutOfStock(final ShopSuccessPurchaseEvent event) {

    if(event.getShop().isSelling() && event.getShop().getRemainingStock() == 0) {
      Util.asyncThreadRun(()->{
        // Send to owner
        final MessageEmbed embed = plugin.getFactory().shopOutOfStock(event);
        //sendMessageIfEnabled(event.getShop().getOwner(), event.getShop(), embed, NotificationFeature.USER_SHOP_OUT_OF_STOCK);
        // Send to permission users
        for(final UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
          sendMessageIfEnabled(uuid, event.getShop(), embed, NotificationFeature.USER_SHOP_OUT_OF_STOCK);
        }
      });
    }
  }

  private void notifyShopOutOfSpace(final ShopSuccessPurchaseEvent event) {

    if(event.getShop().isBuying() && event.getShop().getRemainingSpace() == 0) {
      Util.asyncThreadRun(()->{
        // Send to owner
        final MessageEmbed embed = plugin.getFactory().shopOutOfSpace(event);
        //sendMessageIfEnabled(event.getShop().getOwner(), event.getShop(), embed, NotificationFeature.USER_SHOP_OUT_OF_SPACE);
        // Send to permission users
        for(final UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
          if(event.getShop().playerAuthorize(uuid, plugin, "discordalert")) {
            sendMessageIfEnabled(uuid, event.getShop(), embed, NotificationFeature.USER_SHOP_OUT_OF_SPACE);
          }
        }
      });
    }

  }

  private void sendMessageIfEnabled(@NotNull final QUser qUser, @NotNull final MessageEmbed embed, @NotNull final NotificationFeature feature) {

    final UUID uuid = qUser.getUniqueIdIfRealPlayer().orElse(null);
    if(uuid == null) {
      return;
    }
    Util.ensureThread(true);
    if(databaseHelper.isNotifactionFeatureEnabled(uuid, feature)) {
      jdaWrapper.sendMessage(uuid, embed);
    }
  }

  private void sendMessageIfEnabled(@NotNull final QUser qUser, @NotNull final Shop shop, @NotNull final MessageEmbed embed, @NotNull final NotificationFeature feature) {

    Util.ensureThread(true);
    final UUID uuid = qUser.getUniqueIdIfRealPlayer().orElse(null);
    if(uuid == null) {
      return;
    }
    if(shop.playerAuthorize(uuid, plugin, "discordalert")) {
      if(databaseHelper.isNotifactionFeatureEnabled(uuid, feature)) {
        jdaWrapper.sendMessage(uuid, embed);
      }
    }
  }


  private void sendMessageIfEnabled(@NotNull final UUID uuid, @NotNull final Shop shop, @NotNull final MessageEmbed embed, @NotNull final NotificationFeature feature) {

    Util.ensureThread(true);
    if(shop.playerAuthorize(uuid, plugin, "discordalert")) {
      if(databaseHelper.isNotifactionFeatureEnabled(uuid, feature)) {
        jdaWrapper.sendMessage(uuid, embed);
      }
    }
  }

  public void sendModeratorChannelMessageIfEnabled(@NotNull final MessageEmbed embed, @NotNull final NotificationFeature feature) {

    Util.ensureThread(true);
    if(plugin.isServerNotificationFeatureEnabled(feature)) {
      plugin.sendModeratorChannelMessage(embed);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onShopTransfer(final ShopOwnershipTransferEvent event) {

    notifyShopTransfer(event);
    notifyModShopTransfer(event);
  }

  private void notifyShopTransfer(final ShopOwnershipTransferEvent event) {

    Util.asyncThreadRun(()->sendMessageIfEnabled(event.getNewOwner(), plugin.getFactory().shopTransferToYou(event), NotificationFeature.USER_SHOP_TRANSFER));
  }

  private void notifyModShopTransfer(final ShopOwnershipTransferEvent event) {

    Util.asyncThreadRun(()->sendModeratorChannelMessageIfEnabled(plugin.getFactory().modShopTransfer(event), NotificationFeature.MOD_SHOP_TRANSFER));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onShopDelete(final ShopDeleteEvent event) {

    notifyModShopRemoved(event);
  }

  private void notifyModShopRemoved(final ShopDeleteEvent event) {

    Util.asyncThreadRun(()->sendModeratorChannelMessageIfEnabled(plugin.getFactory().modShopRemoved(event), NotificationFeature.MOD_SHOP_REMOVED));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onShopPriceChanged(final ShopPriceEvent event) {

    if(event.phase() != Phase.POST) {

      return;
    }

    notifyShopPriceChanged(event);
    notifyModShopPriceChanged(event);
  }

  private void notifyShopPriceChanged(final ShopPriceEvent event) {

    Util.asyncThreadRun(()->{
      final MessageEmbed embed = plugin.getFactory().priceChanged(event);
      //sendMessageIfEnabled(event.getShop().getOwner(), event.getShop(), embed, NotificationFeature.USER_SHOP_PRICE_CHANGED);
      // Send to permission users
      for(final UUID uuid : event.shop().getPermissionAudiences().keySet()) {
        if(event.shop().playerAuthorize(uuid, plugin, "discordalert")) {
          QUserImpl.createAsync(QuickShop.getInstance().getPlayerFinder(), uuid)
                  .thenAccept(qUser->sendMessageIfEnabled(qUser, event.shop(), embed, NotificationFeature.USER_SHOP_PRICE_CHANGED))
                  .exceptionally(e->{
                    plugin.getLogger().log(Level.WARNING, "Failed to find the player", e);
                    return null;
                  });
        }
      }
    });

  }

  private void notifyModShopPriceChanged(final ShopPriceEvent event) {

    Util.asyncThreadRun(()->sendModeratorChannelMessageIfEnabled(plugin.getFactory().modPriceChanged(event), NotificationFeature.MOD_SHOP_PRICE_CHANGED));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onShopPermissionChanged(final ShopPlayerGroupSetEvent event) {

    notifyShopPermissionChanged(event);
  }

  private void notifyShopPermissionChanged(final ShopPlayerGroupSetEvent event) {

    Util.asyncThreadRun(()->{
      final MessageEmbed embed = plugin.getFactory().shopPermissionChanged(event);
      sendMessageIfEnabled(event.getShop().getOwner(), event.getShop(), embed, NotificationFeature.USER_SHOP_PERMISSION_CHANGED);
      // Send to permission users
      for(final UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
        sendMessageIfEnabled(event.getPlayer(), event.getShop(), embed, NotificationFeature.USER_SHOP_PERMISSION_CHANGED);
      }
    });
  }
}
