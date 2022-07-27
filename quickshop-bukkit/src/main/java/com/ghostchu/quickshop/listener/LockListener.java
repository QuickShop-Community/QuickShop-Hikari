package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.Cache;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LockListener extends AbstractProtectionListener {
    public LockListener(@NotNull final QuickShop plugin, @Nullable final Cache cache) {
        super(plugin, cache);
    }
    private final com.google.common.cache.Cache<UUID, Object> lockCoolDown = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.SECONDS)
            .build();
    private static final Object emptyObject=  new Object();
    @Override
    public void register() {
        if (plugin.getConfig().getBoolean("shop.lock")) {
            super.register();
        } else {
            super.unregister();
        }
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        register();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }

    /*
     * Removes chests when they're destroyed.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        final Player p = e.getPlayer();
        // If the chest was a chest
        if (Util.canBeShop(b)) {
            final Shop shop = getShopPlayer(b.getLocation(), true);

            if (shop == null) {
                return; // Wasn't a shop
            }
            // If they owned it or have bypass perms, they can destroy it
            if (!shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.DELETE)
                    && !plugin.perm().hasPermission(p, "quickshop.other.destroy")) {
                e.setCancelled(true);
                plugin.text().of(p, "no-permission").send();
            }
        } else if (Util.isWallSign(b.getType())) {
            b = Util.getAttached(b);

            if (b == null) {
                return;
            }

            final Shop shop = getShopPlayer(b.getLocation(), false);

            if (shop == null) {
                return;
            }
            // If they're the shop owner or have bypass perms, they can destroy
            // it.
            if (!shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.DELETE)
                    && !plugin.perm().hasPermission(p, "quickshop.other.destroy")) {
                e.setCancelled(true);
                plugin.text().of(p, "no-permission").send();
            }
        }
    }

    /*
     * Listens for sign placement to prevent placing sign for creating protection
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onSignPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        if (!(placedBlock.getState() instanceof Sign)) {
            return;
        }
        Block posShopBlock = Util.getAttached(placedBlock);
        if (posShopBlock == null) {
            return;
        }
        Shop shop = plugin.getShopManager().getShopIncludeAttached(posShopBlock.getLocation());
        if (shop == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!shop.playerAuthorize(player.getUniqueId(), BuiltInShopPermission.DELETE)
                && !plugin.perm().hasPermission(player, "quickshop.other.open")) {
            plugin.text().of(player, "that-is-locked").send();
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(PlayerInteractEvent e) {

        final Block b = e.getClickedBlock();

        if (b == null) {
            return;
        }

        if (!Util.canBeShop(b)) {
            return;
        }

        final Player p = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return; // Didn't right click it, we dont care.
        }

        final Shop shop = getShopPlayer(b.getLocation(), true);
        // Make sure they're not using the non-shop half of a double chest.
        if (shop == null) {
            return;
        }

        if (!shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.ACCESS_INVENTORY)) {
            if (plugin.perm().hasPermission(p, "quickshop.other.open")) {
                if(lockCoolDown.getIfPresent(p.getUniqueId()) == null){
                    plugin.text().of(p, "bypassing-lock").send();
                    lockCoolDown.put(p.getUniqueId(), emptyObject);
                }
                return;
            }
            plugin.text().of(p, "that-is-locked").send();
            e.setCancelled(true);
        }
    }

    /*
     * Handles hopper placement
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHopperPlace(BlockPlaceEvent e) {

        final Block b = e.getBlock();

        if (b.getType() != Material.HOPPER) {
            return;
        }

        final Player p = e.getPlayer();

        if (!Util.isOtherShopWithinHopperReach(b, p)) {
            return;
        }

        if (plugin.perm().hasPermission(p, "quickshop.other.open")) {
            if(lockCoolDown.getIfPresent(p.getUniqueId()) == null){
                plugin.text().of(p, "bypassing-lock").send();
                lockCoolDown.put(p.getUniqueId(), emptyObject);
            }
            return;
        }

        plugin.text().of(p, "that-is-locked").send();
        e.setCancelled(true);
    }

}
