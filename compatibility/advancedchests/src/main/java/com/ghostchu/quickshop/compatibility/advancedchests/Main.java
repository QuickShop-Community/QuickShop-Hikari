package com.ghostchu.quickshop.compatibility.advancedchests;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.Nullable;
import us.lynuxcraft.deadsilenceiv.advancedchests.AdvancedChestsAPI;
import us.lynuxcraft.deadsilenceiv.advancedchests.chest.AdvancedChest;
import us.lynuxcraft.deadsilenceiv.advancedchests.events.ChestRemoveEvent;
import us.lynuxcraft.deadsilenceiv.advancedchests.utils.ChunkLocation;

import java.util.Map;

public final class Main extends CompatibilityModule implements Listener {
    public AdvancedChestsInventoryManager manager;

    public AdvancedChestsInventoryManager getManager() {
        return manager;
    }


    @Override
    public void onLoad() {
        super.onLoad();
        manager = new AdvancedChestsInventoryManager(this);
        getApi().getInventoryWrapperRegistry().register(this, manager);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void init() {
        // There no init stuffs need to do
    }

    @Nullable
    public AdvancedChest<?, ?> locateAdvancedChest(Location location) {
        AdvancedChest<?, ?> result = AdvancedChestsAPI.getChestManager().getAdvancedChest(location);
        if (result == null)
            result = AdvancedChestsAPI.getChestManager().getNonLoadableChest(location);
        return result;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopCreated(ShopCreateEvent event) {
        Location createdPos = event.getShop().getLocation();
        AdvancedChest<?, ?> advancedChests = locateAdvancedChest(createdPos);
        if (advancedChests == null) {
            return;
        }

        if (!QuickShop.getPermissionManager().hasPermission(event.getPlayer(), "quickshop.create.advancedchests")) {
            event.setCancelled(true, getApi().getTextManager().of(event.getPlayer(), "compat.advancedchests.permission-denied").forLocale());
            return;
        }
        Shop shop = event.getShop();
        shop.setInventory(new AdvancedChestsWrapper(advancedChests, this), manager);
        getApi().getTextManager().of(event.getPlayer(), "compat.advancedchests.created").send();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAdvancedChestRemoved(ChestRemoveEvent event) {
        AdvancedChest<?, ?> advancedChests = event.getChest();
        ChunkLocation chunkLocation = advancedChests.getChunkLocation();
        Map<Location, Shop> shops = getApi().getShopManager().getShops(chunkLocation.getWorld().getName(), chunkLocation.getX(), chunkLocation.getZ());
        if (shops == null) return;
        for (Shop shop : shops.values()) {
            InventoryWrapper inventory = shop.getInventory();
            if (inventory == null) continue;
            if (inventory.getHolder() instanceof AdvancedChestsWrapper advancedChestsWrapper) {
                if (advancedChestsWrapper.getAdvancedChest().getUniqueId().equals(advancedChests.getUniqueId())) {
                    recordDeletion(CommonUtil.getNilUniqueId(), shop, "AdvancedChest Removed");
                    shop.delete();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent e) {
        AdvancedChest<?, ?> advancedChests = AdvancedChestsAPI.getInventoryManager().getAdvancedChest(e.getInventory());
        for (Shop shop : getApi().getShopManager().getLoadedShops()) {
            InventoryWrapper inventory = shop.getInventory();
            if (inventory == null) continue;
            if (inventory instanceof AdvancedChestsWrapper advancedChestsWrapper) {
                if (advancedChestsWrapper.getAdvancedChest().getUniqueId().equals(advancedChests.getUniqueId())) {
                    shop.setSignText(getApi().getTextManager().findRelativeLanguages(e.getPlayer()));
                }
            }
        }
    }
}
