package com.ghostchu.quickshop.addon.discordsrv.listener;

import com.ghostchu.quickshop.addon.discordsrv.Main;
import com.ghostchu.quickshop.addon.discordsrv.bean.NotificationFeature;
import com.ghostchu.quickshop.addon.discordsrv.database.DiscordDatabaseHelper;
import com.ghostchu.quickshop.addon.discordsrv.wrapper.JDAWrapper;
import com.ghostchu.quickshop.api.event.*;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class QuickShopEventListener implements Listener {
    private final Main plugin;
    private final JDAWrapper jdaWrapper;
    private final DiscordDatabaseHelper databaseHelper;

    public QuickShopEventListener(Main plugin) {
        this.plugin = plugin;
        this.jdaWrapper = plugin.getJdaWrapper();
        this.databaseHelper = plugin.getDatabaseHelper();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPurchase(ShopSuccessPurchaseEvent event) {
        if (event.getShop().isUnlimited() && plugin.getConfig().getBoolean("shop.ignore-unlimited-shop-messages")) {
            return;
        }
        notifyShopPurchase(event);
        notifyModShopPurchase(event);
        notifyShopOutOfStock(event);
        notifyShopOutOfSpace(event);
    }

    private void notifyShopPurchase(ShopSuccessPurchaseEvent event) {
        Util.asyncThreadRun(() -> {
            MessageEmbed embed = plugin.getFactory().shopPurchasedSelf(event);
            sendMessageIfEnabled(event.getShop().getOwner(), embed, NotificationFeature.USER_SHOP_PURCHASE);
            // Send to permission users
            for (UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
                sendMessageIfEnabled(uuid, event.getShop(), embed, NotificationFeature.USER_SHOP_PURCHASE);
            }
        });
    }

    private void notifyModShopPurchase(ShopSuccessPurchaseEvent event) {
        Util.asyncThreadRun(() -> {
            MessageEmbed embed = plugin.getFactory().modShopPurchase(event);
            sendModeratorChannelMessageIfEnabled(embed, NotificationFeature.MOD_SHOP_PURCHASE);
        });
    }

    private void notifyShopOutOfStock(ShopSuccessPurchaseEvent event) {
        if (event.getShop().isSelling() && event.getShop().getRemainingStock() == 0) {
            Util.asyncThreadRun(() -> {
                // Send to owner
                MessageEmbed embed = plugin.getFactory().shopOutOfStock(event);
                sendMessageIfEnabled(event.getShop().getOwner(), event.getShop(), embed, NotificationFeature.USER_SHOP_OUT_OF_STOCK);
                // Send to permission users
                for (UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
                    sendMessageIfEnabled(uuid, event.getShop(), embed, NotificationFeature.USER_SHOP_OUT_OF_STOCK);
                }
            });
        }
    }

    private void notifyShopOutOfSpace(ShopSuccessPurchaseEvent event) {
        if (event.getShop().isBuying() && event.getShop().getRemainingSpace() == 0) {
            Util.asyncThreadRun(() -> {
                // Send to owner
                MessageEmbed embed = plugin.getFactory().shopOutOfSpace(event);
                sendMessageIfEnabled(event.getShop().getOwner(), event.getShop(), embed, NotificationFeature.USER_SHOP_OUT_OF_SPACE);
                // Send to permission users
                for (UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
                    if (event.getShop().playerAuthorize(uuid, plugin, "discordalert")) {
                        sendMessageIfEnabled(uuid, event.getShop(), embed, NotificationFeature.USER_SHOP_OUT_OF_SPACE);
                    }
                }
            });
        }

    }

    private void sendMessageIfEnabled(@NotNull UUID uuid, @NotNull MessageEmbed embed, @NotNull NotificationFeature feature) {
        Util.ensureThread(true);
        if (databaseHelper.isNotifactionFeatureEnabled(uuid, feature)) {
            jdaWrapper.sendMessage(uuid, embed);
        }
    }

    private void sendMessageIfEnabled(@NotNull UUID uuid, @NotNull Shop shop, @NotNull MessageEmbed embed, @NotNull NotificationFeature feature) {
        Util.ensureThread(true);
        if (shop.playerAuthorize(uuid, plugin, "discordalert")) {
            if (databaseHelper.isNotifactionFeatureEnabled(uuid, feature)) {
                jdaWrapper.sendMessage(uuid, embed);
            }
        }
    }

    public void sendModeratorChannelMessageIfEnabled(@NotNull MessageEmbed embed, @NotNull NotificationFeature feature) {
        Util.ensureThread(true);
        if (plugin.isServerNotificationFeatureEnabled(feature)) {
            plugin.sendModeratorChannelMessage(embed);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShopTransfer(ShopOwnershipTransferEvent event) {
        notifyShopTransfer(event);
        notifyModShopTransfer(event);
    }

    private void notifyShopTransfer(ShopOwnershipTransferEvent event) {
        Util.asyncThreadRun(() -> sendMessageIfEnabled(event.getNewOwner(), plugin.getFactory().shopTransferToYou(event), NotificationFeature.USER_SHOP_TRANSFER));
    }

    private void notifyModShopTransfer(ShopOwnershipTransferEvent event) {
        Util.asyncThreadRun(() -> sendModeratorChannelMessageIfEnabled(plugin.getFactory().modShopTransfer(event), NotificationFeature.MOD_SHOP_TRANSFER));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShopDelete(ShopDeleteEvent event) {
        notifyModShopRemoved(event);
    }

    private void notifyModShopRemoved(ShopDeleteEvent event) {
        Util.asyncThreadRun(() -> sendModeratorChannelMessageIfEnabled(plugin.getFactory().modShopRemoved(event), NotificationFeature.MOD_SHOP_REMOVED));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShopPriceChanged(ShopPriceChangeEvent event) {
        notifyShopPriceChanged(event);
        notifyModShopPriceChanged(event);
    }

    private void notifyShopPriceChanged(ShopPriceChangeEvent event) {
        Util.asyncThreadRun(() -> {
            MessageEmbed embed = plugin.getFactory().priceChanged(event);
            sendMessageIfEnabled(event.getShop().getOwner(), event.getShop(), embed, NotificationFeature.USER_SHOP_PRICE_CHANGED);
            // Send to permission users
            for (UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
                sendMessageIfEnabled(uuid, event.getShop(), embed, NotificationFeature.USER_SHOP_PRICE_CHANGED);
            }
        });

    }

    private void notifyModShopPriceChanged(ShopPriceChangeEvent event) {
        Util.asyncThreadRun(() -> sendModeratorChannelMessageIfEnabled(plugin.getFactory().modPriceChanged(event), NotificationFeature.MOD_SHOP_PRICE_CHANGED));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShopPermissionChanged(ShopPlayerGroupSetEvent event) {
        notifyShopPermissionChanged(event);
    }

    private void notifyShopPermissionChanged(ShopPlayerGroupSetEvent event) {
        Util.asyncThreadRun(() -> {
            MessageEmbed embed = plugin.getFactory().shopPermissionChanged(event);
            sendMessageIfEnabled(event.getShop().getOwner(), event.getShop(), embed, NotificationFeature.USER_SHOP_PERMISSION_CHANGED);
            // Send to permission users
            for (UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
                sendMessageIfEnabled(event.getPlayer(), event.getShop(), embed, NotificationFeature.USER_SHOP_PERMISSION_CHANGED);
            }
        });
    }
}
