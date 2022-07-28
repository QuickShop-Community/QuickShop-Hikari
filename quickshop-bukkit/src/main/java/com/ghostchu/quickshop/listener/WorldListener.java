package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class WorldListener extends AbstractQSListener {

    public WorldListener(QuickShop plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent e) {
        /* *************************************
         * This listener fixes any broken world references. Such as hashmap
         * lookups will fail, because the World reference is different, but the
         * world value is the same.
         *  ************************************
         */
        final World world = e.getWorld();

        plugin.getShopLoader().loadShops(world.getName());
        // New world data
        final Map<ShopChunk, Map<Location, Shop>> inWorld = new ConcurrentHashMap<>(1);
        // Old world data
        final Map<ShopChunk, Map<Location, Shop>> oldInWorld =
                plugin.getShopManager().getShops(world.getName());
        // Nothing in the old world, therefore we don't care. No locations to
        // update.
        if (oldInWorld == null) {
            return;
        }

        for (Entry<ShopChunk, Map<Location, Shop>> oldInChunk : oldInWorld.entrySet()) {
            final Map<Location, Shop> inChunk = new ConcurrentHashMap<>(1);
            // Put the new chunk were the old chunk was
            inWorld.put(oldInChunk.getKey(), inChunk);

            for (Entry<Location, Shop> entry : oldInChunk.getValue().entrySet()) {
                final Shop shop = entry.getValue();

                shop.getLocation().setWorld(world);
                inChunk.put(shop.getLocation(), shop);
            }
        }
        // Done - Now we can store the new world dataz!

        plugin.getShopManager().getShops().put(world.getName(), inWorld);
        // This is a workaround, because I don't get parsed chunk events when a
        // world first loads....
        // So manually tell all of these shops they're loaded.
        for (final Chunk chunk : world.getLoadedChunks()) {
            final Map<Location, Shop> inChunk = plugin.getShopManager().getShops(chunk);

            if (inChunk == null) {
                continue;
            }

            for (final Shop shop : inChunk.values()) {
                shop.onLoad();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent e) {
        // This is a workaround, because I don't get parsed chunk events when a
        // world unloads, I think...
        // So manually tell all of these shops they're unloaded.
        for (final Chunk chunk : e.getWorld().getLoadedChunks()) {
            final Map<Location, Shop> inChunk = plugin.getShopManager().getShops(chunk);
            if (inChunk == null) {
                continue;
            }
            for (final Shop shop : inChunk.values()) {
                if (shop.isLoaded()) { //Don't unload already unloaded shops.
                    shop.onUnload();
                }
            }
        }
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
