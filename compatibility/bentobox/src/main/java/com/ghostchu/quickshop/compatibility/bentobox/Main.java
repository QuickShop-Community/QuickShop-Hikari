package com.ghostchu.quickshop.compatibility.bentobox;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.events.island.IslandDeletedEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.database.objects.IslandDeletion;

import java.util.List;

public final class Main extends CompatibilityModule implements Listener {
    private boolean deleteShopOnLeave;
    private boolean deleteShopOnReset;

    @Override
    public void init() {
        deleteShopOnLeave = getConfig().getBoolean("delete-shop-on-member-leave");
        deleteShopOnReset = getConfig().getBoolean("delete-shop-on-island-reset");
    }

    @EventHandler(ignoreCancelled = true)
    public void permissionOverride(ShopAuthorizeCalculateEvent event) {
        Location shopLoc = event.getShop().getLocation();
        BentoBox.getInstance().getIslandsManager().getIslandAt(shopLoc).ifPresent(island -> {
            if (event.getAuthorizer().equals(island.getOwner())) {
                if (event.getNamespace().equals(QuickShop.getInstance()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
                    event.setResult(true);
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onIslandResetted(IslandResettedEvent event) {
        if (!deleteShopOnReset) {
            return;
        }
        getShops(event.getOldIsland()).forEach(shop -> {
            recordDeletion(event.getPlayerUUID(), shop, "Island " + event.getIsland().getName() + " was resetted");
            shop.delete();
        });
    }

    private List<Shop> getShops(Island island) {
        return getShops(island.getWorld().getName(), island.getMinX(), island.getMinZ(), island.getMaxX(), island.getMaxZ());
    }

    @EventHandler(ignoreCancelled = true)
    public void onIslandDeleted(IslandDeletedEvent event) {
        if (!deleteShopOnReset) {
            return;
        }
        getShops(event.getDeletedIslandInfo()).forEach(shop -> {
            recordDeletion(event.getPlayerUUID(), shop, "Island " + event.getIsland().getName() + " was deleted");
            shop.delete();
        });
    }

    private List<Shop> getShops(IslandDeletion island) {
        return getShops(island.getWorld().getName(), island.getMinX(), island.getMinZ(), island.getMaxX(), island.getMaxZ());
    }

    @EventHandler(ignoreCancelled = true)
    public void onIslandKick(world.bentobox.bentobox.api.events.team.TeamKickEvent event) {
        if (!deleteShopOnLeave) {
            return;
        }
        getShops(event.getIsland()).forEach((shop) -> {
            if (shop.getOwner().equals(event.getPlayerUUID())) {
                recordDeletion(event.getOwner(), shop, "Player " + event.getPlayerUUID() + " was kicked from the island");
                shop.delete();
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onIslandLeave(world.bentobox.bentobox.api.events.team.TeamLeaveEvent event) {
        if (!deleteShopOnLeave) {
            return;
        }
        getShops(event.getIsland()).forEach((shop) -> {
            if (shop.getOwner().equals(event.getPlayerUUID())) {
                recordDeletion(null, shop, "Player " + event.getPlayerUUID() + " was leaved from the island");
                shop.delete();
            }
        });
    }
}
