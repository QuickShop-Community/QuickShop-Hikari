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
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;


public final class Main extends CompatibilityModule implements Listener {
    private boolean onlyOwnerCanCreateShop;
    private boolean deleteShopOnMemberLeave;

    @EventHandler
    public void deleteShops(IslandQuitEvent event) {
        if (deleteShopOnMemberLeave) {
            deleteShops(event.getIsland(), event.getPlayer().getUniqueId(), event.getPlayer().getUniqueId(), "IslandQuitEvent");
        }

    }

    private void deleteShops(@NotNull Island island, @Nullable UUID shopOwnerToDelete, @NotNull UUID deleteOperator, @NotNull String deleteReason) {
        List<CompletableFuture<Chunk>> allFutures = this.getAllChunksAsync(island);
        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).thenAccept(v -> {
            List<Shop> pendingForDeletion = new ArrayList<>();
            allFutures.forEach(future -> {
                Chunk chunk = future.getNow(null);
                for (Shop shop : getShops(chunk.getWorld().getName(), chunk.getX(), chunk.getZ())) {
                    if (shopOwnerToDelete == null || shopOwnerToDelete.equals(shop.getOwner().getUniqueId())) {
                        pendingForDeletion.add(shop);
                    }
                }
            });
            Util.mainThreadRun(() -> pendingForDeletion.forEach(s -> {
                getApi().getShopManager().deleteShop(s);
                recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SuperiorSkyblock", false), s, deleteReason);
            }));
        }).exceptionally(throwable -> {
            getLogger().log(Level.WARNING, "Failed to handle SuperiorSkyblock2 island shops deletion", throwable);
            return null;
        });
    }

    private void deleteShops(@NotNull World world, int chunkX, int chunkZ, @Nullable UUID shopOwnerToDelete, @NotNull UUID deleteOperator, @NotNull String deleteReason) {
        List<Shop> pendingForDeletion = new ArrayList<>();
        for (Shop shop : getShops(world.getName(), chunkX, chunkZ)) {
            if (shopOwnerToDelete == null || shopOwnerToDelete.equals(shop.getOwner().getUniqueId())) {
                pendingForDeletion.add(shop);
            }
        }
        Util.mainThreadRun(() -> pendingForDeletion.forEach(s -> {
            getApi().getShopManager().deleteShop(s);
            recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SuperiorSkyblock", false), s, deleteReason);
        }));
    }

    @EventHandler
    public void deleteShops(IslandKickEvent event) {
        if (deleteShopOnMemberLeave) {
            deleteShops(event.getIsland(), event.getTarget().getUniqueId(), event.getIsland().getOwner().getUniqueId(), "IslandKickEvent");
        }
    }

    @EventHandler
    public void deleteShops(IslandUncoopPlayerEvent event) {
        deleteShops(event.getIsland(), event.getTarget().getUniqueId(), event.getIsland().getOwner().getUniqueId(), "IslandUncoopPlayerEvent");
    }

    @EventHandler
    public void deleteShopsOnChunkReset(IslandChunkResetEvent event) {
        deleteShops(event.getWorld(), event.getChunkX(), event.getChunkZ(), null, CommonUtil.getNilUniqueId(), "IslandChunkResetEvent");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreation(ShopCreateEvent event) {
        Island island = SuperiorSkyblockAPI.getIslandAt(event.getShop().getLocation());
        event.getCreator().getBukkitPlayer().ifPresent(player -> {
            SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
            if (island == null) {
                return;
            }
            if (onlyOwnerCanCreateShop) {
                if (!island.getOwner().equals(superiorPlayer)) {
                    event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.superiorskyblock.owner-create-only").forLocale());
                }
            } else {
                if (!island.getOwner().equals(superiorPlayer)) {
                    if (!island.isMember(superiorPlayer)) {
                        event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.superiorskyblock.owner-member-create-only").forLocale());
                    }
                }
            }
        });

    }

    @Override
    public void init() {
        onlyOwnerCanCreateShop = getConfig().getBoolean("owner-create-only");
        deleteShopOnMemberLeave = getConfig().getBoolean("delete-shop-on-member-leave");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreCreation(ShopPreCreateEvent event) {
        Island island = SuperiorSkyblockAPI.getIslandAt(event.getLocation());
        event.getCreator().getBukkitPlayer().ifPresent(player -> {
            SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
            if (island == null) {
                return;
            }
            if (onlyOwnerCanCreateShop) {
                if (!island.getOwner().equals(superiorPlayer)) {
                    event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.superiorskyblock.owner-create-only").forLocale());
                }
            } else {
                if (!island.getOwner().equals(superiorPlayer)) {
                    if (!island.isMember(superiorPlayer)) {
                        event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.superiorskyblock.owner-member-create-only").forLocale());
                    }
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void permissionOverride(ShopAuthorizeCalculateEvent event) {
        Location shopLoc = event.getShop().getLocation();
        Island island = SuperiorSkyblockAPI.getIslandAt(shopLoc);
        if (island == null) {
            return;
        }
        if (island.getOwner().getUniqueId().equals(event.getAuthorizer())) {
            if (event.getNamespace().equals(QuickShop.getInstance().getJavaPlugin()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
                event.setResult(true);
            }
        }
    }

    private List<CompletableFuture<Chunk>> getAllChunksAsync(Island island) {
        List<CompletableFuture<Chunk>> chunkFutures = new ArrayList<>();
        for (World.Environment environment : World.Environment.values()) {
            try {
                chunkFutures.addAll(island.getAllChunksAsync(environment, false, chunk -> {
                }));
            } catch (NullPointerException ignored) {
            }
        }
        return chunkFutures;
    }
}
