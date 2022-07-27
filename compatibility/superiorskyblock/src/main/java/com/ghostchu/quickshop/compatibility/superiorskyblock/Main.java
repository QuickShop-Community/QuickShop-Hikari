package com.ghostchu.quickshop.compatibility.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.IslandChunkResetEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;


public final class Main extends CompatibilityModule implements Listener {
    private boolean onlyOwnerCanCreateShop;
    private boolean deleteShopOnMemberLeave;

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void init() {
        onlyOwnerCanCreateShop = getConfig().getBoolean("owner-create-only");
        deleteShopOnMemberLeave = getConfig().getBoolean("delete-shop-on-member-leave");
    }

    @EventHandler(ignoreCancelled = true)
    public void permissionOverride(ShopAuthorizeCalculateEvent event) {
        Location shopLoc = event.getShop().getLocation();
        Island island = SuperiorSkyblockAPI.getIslandAt(shopLoc);
        if (island == null) {
            return;
        }
        if (island.getOwner().getUniqueId().equals(event.getAuthorizer())) {
            if (event.getNamespace().equals(QuickShop.getInstance()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
                event.setResult(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreCreation(ShopPreCreateEvent event) {
        Island island = SuperiorSkyblockAPI.getIslandAt(event.getLocation());
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(event.getPlayer());
        if (island == null) {
            return;
        }
        if (onlyOwnerCanCreateShop) {
            if (!island.getOwner().equals(superiorPlayer)) {
                event.setCancelled(true, "Only owner can create shop there.");
            }
        } else {
            if (!island.getOwner().equals(superiorPlayer)) {
                if (!island.isMember(superiorPlayer)) {
                    event.setCancelled(true, "Only owner or member can create shop there.");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreation(ShopCreateEvent event) {
        Island island = SuperiorSkyblockAPI.getIslandAt(event.getShop().getLocation());
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(event.getCreator());
        if (island == null) {
            return;
        }
        if (onlyOwnerCanCreateShop) {
            if (!island.getOwner().equals(superiorPlayer)) {
                event.setCancelled(true, "Only owner can create shop there.");
            }
        } else {
            if (!island.getOwner().equals(superiorPlayer)) {
                if (!island.isMember(superiorPlayer)) {
                    event.setCancelled(true, "Only owner or member can create shop there.");
                }
            }
        }
    }

    @EventHandler
    public void deleteShops(IslandQuitEvent event) {
        if (deleteShopOnMemberLeave) {
            deleteShops(event.getIsland(), event.getPlayer().getUniqueId());
        }

    }

    private void deleteShops(@NotNull Island island, @Nullable UUID uuid) {
        island.getAllChunks().forEach((chunk) -> {
            Map<Location, Shop> shops = QuickShop.getInstance().getShopManager().getShops(chunk);
            if (shops != null && !shops.isEmpty()) {
                shops.forEach((location, shop) -> {
                    if (uuid != null) {
                        if (!shop.getOwner().equals(uuid)) {
                            return;
                        }
                        recordDeletion(uuid, shop, "Shop deleting");
                        shop.delete();
                    }
                });
            }
        });
    }

    @EventHandler
    public void deleteShops(IslandKickEvent event) {
        if (deleteShopOnMemberLeave) {
            deleteShops(event.getIsland(), event.getTarget().getUniqueId());
        }
    }

    @EventHandler
    public void deleteShops(IslandUncoopPlayerEvent event) {
        deleteShops(event.getIsland(), event.getTarget().getUniqueId());
    }

    @EventHandler
    public void deleteShopsOnChunkReset(IslandChunkResetEvent event) {
        deleteShops(event.getIsland(), null);
    }
}
