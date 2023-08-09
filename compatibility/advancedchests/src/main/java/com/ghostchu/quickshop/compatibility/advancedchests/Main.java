package com.ghostchu.quickshop.compatibility.advancedchests;

import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;
import us.lynuxcraft.deadsilenceiv.advancedchests.AdvancedChestsAPI;
import us.lynuxcraft.deadsilenceiv.advancedchests.chest.AdvancedChest;
import us.lynuxcraft.deadsilenceiv.advancedchests.events.ChestRemoveEvent;
import us.lynuxcraft.deadsilenceiv.advancedchests.utils.ChunkLocation;

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
        Shop shop = event.getShop();
        shop.setInventory(new AdvancedChestsWrapper(advancedChests, this), manager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAdvancedChestRemoved(ChestRemoveEvent event) {
        AdvancedChest<?, ?> advancedChests = event.getChest();
        ChunkLocation chunkLocation = advancedChests.getChunkLocation();
        for (Shop shop : getApi().getShopManager().getShops(chunkLocation.getWorld().getName(), chunkLocation.getX(), chunkLocation.getZ()).values()) {
            if (!shop.getInventoryWrapperProvider().equals(getDescription().getName())) return;
            recordDeletion(CommonUtil.getNilUniqueId(), shop, "AdvancedChest Removed");
            shop.delete();
        }
    }
}
