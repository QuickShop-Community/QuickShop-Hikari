package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.*;
import com.ghostchu.quickshop.api.serialize.BlockPos;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.logging.container.*;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

    public InternalListener(QuickShop plugin) {
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
    public void shopCreate(ShopCreateEvent event) {
        if (isForbidden(event.getShop().getLocation().getBlock().getType(), event.getShop().getItem().getType())) {
            event.setCancelled(true, plugin.text().of(event.getCreator(), "forbidden-vanilla-behavior").forLocale());
            return;
        }
        if (loggingAction) {
            plugin.logEvent(new ShopCreationLog(event.getCreator(), event.getShop().saveToInfoStorage(), new BlockPos(event.getShop().getLocation())));
        }
    }

    public boolean isForbidden(@NotNull Material shopMaterial, @NotNull Material itemMaterial) {
        if (!Objects.equals(shopMaterial, itemMaterial)) {
            return false;
        }
        return shopMaterial.isBlock() && shopMaterial.name().toUpperCase().endsWith("SHULKER_BOX");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopDelete(ShopDeleteEvent event) {
        if (loggingAction) {
            plugin.logEvent(new ShopRemoveLog(CommonUtil.getNilUniqueId(), "Shop removed", event.getShop().saveToInfoStorage()));
        }
        if (plugin.getShopCache() != null) {
            plugin.getShopCache().invalidate(event.getShop().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopInventoryCalc(ShopInventoryCalculateEvent event) {
        if (event.getShop().getShopId() < 1) {
            return;
        }
        SpaceCache count = countUpdateCache.getIfPresent(event.getShop());
        if (count != null && count.getSpace() == event.getSpace() && count.getStock() == event.getStock()) {
            return;
        }
        countUpdateCache.put(event.getShop(), new SpaceCache(event.getSpace(), event.getStock()));
        plugin.getDatabaseHelper().updateExternalInventoryProfileCache(event.getShop().getShopId(), event.getSpace(), event.getStock())
                .whenComplete((lines, err) -> {
                    if (err != null) {
                        Log.debug("Error updating external inventory profile cache for shop " + event.getShop().getShopId() + ": " + err.getMessage());
                    }
                });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopPrePurchase(ShopPurchaseEvent event) {
        if (isForbidden(event.getShop().getLocation().getBlock().getType(), event.getShop().getItem().getType())) {
            event.setCancelled(true, plugin.text().of(event.getPurchaser(), "forbidden-vanilla-behavior").forLocale());
            return;
        }
        if (loggingBalance) {
            plugin.logEvent(new PlayerEconomyPreCheckLog(true, event.getPurchaser(), plugin.getEconomy().getBalance(event.getPurchaser(), event.getShop().getLocation().getWorld(), event.getShop().getCurrency())));
            plugin.logEvent(new PlayerEconomyPreCheckLog(true, event.getShop().getOwner(), plugin.getEconomy().getBalance(event.getShop().getOwner(), event.getShop().getLocation().getWorld(), event.getShop().getCurrency())));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopPriceChanges(ShopPriceChangeEvent event) {
        if (loggingAction) {
            plugin.logEvent(new ShopPriceChangedLog(event.getShop().saveToInfoStorage(), event.getOldPrice(), event.getNewPrice()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopPurchase(ShopSuccessPurchaseEvent event) {
        if (loggingAction) {
            plugin.logEvent(new ShopPurchaseLog(event.getShop().saveToInfoStorage(),
                    event.getShop().getShopType(),
                    event.getPurchaser(),
                    LegacyComponentSerializer.legacySection().serialize(Util.getItemStackName(event.getShop().getItem())),
                    Util.serialize(event.getShop().getItem()),
                    event.getAmount(),
                    event.getBalance(),
                    event.getTax()));
        }
        if (loggingBalance) {
            plugin.logEvent(new PlayerEconomyPreCheckLog(false, event.getPurchaser(), plugin.getEconomy().getBalance(event.getPurchaser(), event.getShop().getLocation().getWorld(), event.getShop().getCurrency())));
            plugin.logEvent(new PlayerEconomyPreCheckLog(false, event.getShop().getOwner(), plugin.getEconomy().getBalance(event.getShop().getOwner(), event.getShop().getLocation().getWorld(), event.getShop().getCurrency())));
        }
        if (event.getPurchaser().equals(event.getShop().getOwner())) {
            Player player = Bukkit.getPlayer(event.getPurchaser());
            if (player != null) {
                plugin.text().of(player, "shop-owner-self-trade").send();
            }
        }
    }

    static class SpaceCache {
        private final int stock;
        private final int space;

        public SpaceCache(int stock, int space) {
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
