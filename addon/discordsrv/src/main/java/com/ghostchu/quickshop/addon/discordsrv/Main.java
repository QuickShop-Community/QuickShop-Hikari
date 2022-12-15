package com.ghostchu.quickshop.addon.discordsrv;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.discordsrv.message.MessageFactory;
import com.ghostchu.quickshop.addon.discordsrv.message.MessageManager;
import com.ghostchu.quickshop.addon.discordsrv.message.MessageRepository;
import com.ghostchu.quickshop.addon.discordsrv.wrapper.JDAWrapper;
import com.ghostchu.quickshop.addon.discordsrv.wrapper.discordsrv.DiscordSRVWrapper;
import com.ghostchu.quickshop.api.event.*;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.util.Util;
import github.scarsz.discordsrv.api.commands.PluginSlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommandProvider;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener, SlashCommandProvider {
    private QuickShop plugin;
    private MessageFactory factory;
    private MessageManager manager;
    private JDAWrapper jdaWrapper;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        plugin = QuickShop.getInstance();
        manager = new MessageManager(this);
        manager.register(new MessageRepository(plugin));
        factory = new MessageFactory(plugin, manager);
        this.jdaWrapper = new DiscordSRVWrapper();
        Bukkit.getPluginManager().registerEvents(this, this);
        plugin.getShopPermissionManager().registerPermission(BuiltInShopPermissionGroup.STAFF.getNamespacedNode(), this, "discordalert");
        plugin.getShopPermissionManager().registerPermission(BuiltInShopPermissionGroup.ADMINISTRATOR.getNamespacedNode(), this, "discordalert");

    }

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        // perform configuration upgrade
        if (getConfig().getInt("config-version") != 2) {
            getConfig().set("moderator-channel", "000000000000000000");
            getConfig().set("config-version", 2);
            saveConfig();
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPurchase(ShopSuccessPurchaseEvent event) {
        if (event.getShop().isUnlimited() && plugin.getConfig().getBoolean("shop.ignore-unlimited-shop-messages")) {
            return;
        }
        Util.asyncThreadRun(() -> {
            notifyShopPurchase(event);
            notifyModShopPurchase(event);
            notifyShopOutOfStock(event);
            notifyShopOutOfSpace(event);
        });
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShopTransfer(ShopOwnershipTransferEvent event) {
        Util.asyncThreadRun(() -> {
            notifyShopTransfer(event);
            notifyModShopTransfer(event);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShopDelete(ShopDeleteEvent event) {
        Util.asyncThreadRun(() -> notifyModShopRemoved(event));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShopPriceChanged(ShopPriceChangeEvent event) {
        Util.asyncThreadRun(() -> {
            notifyShopPriceChanged(event);
            notifyModShopPriceChanged(event);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShopCreated(ShopCreateEvent event) {
        Util.asyncThreadRun(() -> notifyModShopCreated(event));
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShopPermissionChanged(ShopPlayerGroupSetEvent event) {
        Util.asyncThreadRun(() -> notifyShopPermissionChanged(event));
    }

    private void notifyShopPermissionChanged(ShopPlayerGroupSetEvent event) {
        if (!isFeatureEnabled("notify-shop-permission-changed"))
            return;
        jdaWrapper.sendMessage(event.getShop().getOwner(), factory.shopPermissionChanged(event));
        // Send to permission users
        for (UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
            if (event.getShop().playerAuthorize(uuid, this, "discordalert")) {
                jdaWrapper.sendMessage(uuid, factory.shopPermissionChanged(event));
            }
        }
    }

    private void notifyShopPriceChanged(ShopPriceChangeEvent event) {
        if (!isFeatureEnabled("notify-shop-price-changed"))
            return;
        jdaWrapper.sendMessage(event.getShop().getOwner(), factory.priceChanged(event));
        // Send to permission users
        for (UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
            if (event.getShop().playerAuthorize(uuid, this, "discordalert")) {
                jdaWrapper.sendMessage(uuid, factory.priceChanged(event));
            }
        }
    }

    private void notifyModShopPriceChanged(ShopPriceChangeEvent event) {
        if (!isFeatureEnabled("mod-notify-shop-price-changed"))
            return;
        sendModeratorChannelMessage(factory.modPriceChanged(event));
    }


    private void notifyModShopRemoved(ShopDeleteEvent event) {
        if (!isFeatureEnabled("mod-notify-shop-removed"))
            return;
        sendModeratorChannelMessage(factory.modShopRemoved(event));
    }

    private void notifyShopTransfer(ShopOwnershipTransferEvent event) {
        if (!isFeatureEnabled("notify-shop-transfer"))
            return;
        jdaWrapper.sendMessage(event.getShop().getOwner(), factory.shopTransferToYou(event));
    }

    private void notifyModShopTransfer(ShopOwnershipTransferEvent event) {
        if (!isFeatureEnabled("mod-notify-shop-transfer"))
            return;
        sendModeratorChannelMessage(factory.modShopTransfer(event));
    }

    private void notifyModShopCreated(ShopCreateEvent event) {
        if (!isFeatureEnabled("mod-notify-shop-created"))
            return;
        sendModeratorChannelMessage(factory.modShopCreated(event));
    }


    private void notifyShopOutOfStock(ShopSuccessPurchaseEvent event) {
        if (!isFeatureEnabled("notify-shop-out-of-stock"))
            return;
        Util.mainThreadRun(() -> {
            if (event.getShop().isSelling() && event.getShop().getRemainingStock() == 0) {
                Util.asyncThreadRun(() -> {
                    // Send to owner
                    jdaWrapper.sendMessage(event.getShop().getOwner(), factory.shopOutOfStock(event));
                    // Send to permission users
                    for (UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
                        if (event.getShop().playerAuthorize(uuid, this, "discordalert")) {
                            jdaWrapper.sendMessage(uuid, factory.shopOutOfStock(event));
                        }
                    }
                });
            }
        });

    }

    private void notifyShopOutOfSpace(ShopSuccessPurchaseEvent event) {
        if (!isFeatureEnabled("notify-shop-out-of-space"))
            return;
        Util.mainThreadRun(() -> {
            if (event.getShop().isBuying() && event.getShop().getRemainingSpace() == 0) {
                Util.asyncThreadRun(() -> {
                    // Send to owner
                    jdaWrapper.sendMessage(event.getShop().getOwner(), factory.shopOutOfSpace(event));
                    // Send to permission users
                    for (UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
                        if (event.getShop().playerAuthorize(uuid, this, "discordalert")) {
                            jdaWrapper.sendMessage(uuid, factory.shopOutOfSpace(event));
                        }
                    }
                });
            }
        });

    }

    private void notifyShopPurchase(ShopSuccessPurchaseEvent event) {
        if (!isFeatureEnabled("notify-shop-purchase")) {
            getLogger().info("Feature not enabled!!");
            return;
        }
        // Send to owner
        MessageEmbed embed = factory.shopPurchasedSelf(event);
        jdaWrapper.sendMessage(event.getShop().getOwner(), embed);
        // Send to permission users
        for (UUID uuid : event.getShop().getPermissionAudiences().keySet()) {
            if (event.getShop().playerAuthorize(uuid, this, "discordalert")) {
                jdaWrapper.sendMessage(uuid, factory.shopPurchasedSelf(event));
            }
        }
    }


    private void notifyModShopPurchase(ShopSuccessPurchaseEvent event) {
        if (!isFeatureEnabled("mod-notify-shop-purchase"))
            return;
        sendModeratorChannelMessage(factory.modShopPurchase(event));
    }


    public boolean isFeatureEnabled(@NotNull String featureName) {
        return getConfig().getBoolean("features." + featureName, true);
    }

    private void sendModeratorChannelMessage(@NotNull MessageEmbed embed) {
        String channelId = getConfig().getString("moderator-channel", "000000000000000000");
        if (channelId.equals("000000000000000000")) return;
        jdaWrapper.sendChannelMessage(channelId, embed);
    }

    private void sendModeratorChannelMessage(@NotNull String msg) {
        String channelId = getConfig().getString("moderator-channel", "000000000000000000");
        if (channelId.equals("000000000000000000")) return;
        jdaWrapper.sendChannelMessage(channelId, msg);
    }

    @Override
    public Set<PluginSlashCommand> getSlashCommands() {
        return Collections.emptySet();
    }

    public MessageManager getManager() {
        return manager;
    }

    public MessageFactory getFactory() {
        return factory;
    }

    public JDAWrapper getJdaWrapper() {
        return jdaWrapper;
    }
}
