package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.ContainerShop;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class DisplayAutoDespawnWatcher extends BukkitRunnable implements Reloadable {
    private final QuickShop plugin;
    private int range;

    public DisplayAutoDespawnWatcher(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        plugin.getReloadManager().register(this);
        init();
    }

    private void init() {
        this.range = plugin.getConfig().getInt("shop.display-despawn-range");
    }

    public DisplayAutoDespawnWatcher(QuickShop plugin, int range) {
        this.plugin = plugin;
        this.range = range;
    }

    @Override
    public ReloadResult reloadModule() {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }

    @Override
    public void run() {
        for (Shop shop : plugin.getShopManager().getLoadedShops()) {
            //Shop may be deleted or unloaded when iterating
            if (shop.isDeleted() || !shop.isLoaded()) {
                continue;
            }
            Location location = shop.getLocation();
            World world = shop.getLocation().getWorld(); //Cache this, because it will took some time.
            AbstractDisplayItem displayItem = ((ContainerShop) shop).getDisplayItem();
            if (displayItem != null) {
                // Check the range has player?
                boolean anyPlayerInRegion = false;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if ((player.getWorld() == world) && (player.getLocation().distance(location) <= range)) {
                        anyPlayerInRegion = true;
                        break;
                    }
                }
                if (anyPlayerInRegion) {
                    if (!displayItem.isSpawned()) {
                        Log.debug("Respawning the shop " + shop + " the display, cause it was despawned and a player close to it");
                        displayItem.spawn();
                    }
                } else if (displayItem.isSpawned()) {
                    displayItem.remove();
                }
            }
        }
    }

}
