package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.Cache;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Info;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopAction;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BlockListener to listening events about block events
 *
 * @author KaiNoMood, Ghost_chu, sandtechnology
 */
public class BlockListener extends AbstractProtectionListener {
    private boolean updateSignWhenInventoryMoving;

    public BlockListener(@NotNull final QuickShop plugin, @Nullable final Cache cache) {
        super(plugin, cache);
        init();
    }

    private void init() {
        this.updateSignWhenInventoryMoving = super.getPlugin().getConfig().getBoolean("shop.update-sign-when-inventory-moving", true);
    }

    /*
     * Removes chests when they're destroyed.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        final Block b = e.getBlock();
        final Player p = e.getPlayer();
        // If the shop was a chest
        if (Util.canBeShop(b)) {
            final Shop shop = getShopPlayer(b.getLocation(), false);
            if (shop == null) {
                return;
            }
            // If they're either survival or the owner, they can break it
            if (p.getGameMode() == GameMode.CREATIVE
                    && (shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.DELETE) || plugin.perm().hasPermission(p, "quickshop.other.destory"))) {
                // Check SuperTool
                if (p.getInventory().getItemInMainHand().getType() == Material.GOLDEN_AXE) {
                    if (getPlugin().getConfig().getBoolean("shop.disable-super-tool")) {
                        e.setCancelled(true);
                        plugin.text().of(p, "supertool-is-disabled").send();
                        return;
                    }
                    plugin.text().of(p, "break-shop-use-supertool").send();
                    return;
                }
                e.setCancelled(true);
                Component component = Util.getItemStackName(new ItemStack(Material.GOLDEN_AXE, 1));
                plugin.text().of(p, "no-creative-break", component).send();
                return;
            }

            // Cancel their current menu... Doesnt cancel other's menu's.
            final Info action = super.getPlugin().getShopManager().getInteractiveManager().get(p.getUniqueId());

            if (action != null) {
                action.setAction(ShopAction.CANCELLED);
            }
            plugin.logEvent(new ShopRemoveLog(e.getPlayer().getUniqueId(), "BlockBreak(player)", shop.saveToInfoStorage()));
            shop.delete();
            plugin.text().of(p, "success-removed-shop").send();
        } else if (Util.isWallSign(b.getType())) {
            final Shop shop = getShopNextTo(b.getLocation());
            if (shop == null) {
                return;
            }
            // If they're in creative and not the owner, don't let them
            // (accidents happen)
            if (p.getGameMode() == GameMode.CREATIVE
                    && (shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.DELETE)
                    || plugin.perm().hasPermission(p, "quickshop.other.destory"))) {
                // Check SuperTool
                if (p.getInventory().getItemInMainHand().getType() == Material.GOLDEN_AXE) {
                    if (getPlugin().getConfig().getBoolean("shop.disable-super-tool")) {
                        e.setCancelled(true);
                        plugin.text().of(p, "supertool-is-disabled").send();
                        return;
                    }
                    plugin.text().of(p, "break-shop-use-supertool").send();
                    plugin.logEvent(new ShopRemoveLog(e.getPlayer().getUniqueId(), "BlockBreak(player)", shop.saveToInfoStorage()));
                    shop.delete();
                    return;
                }
                e.setCancelled(true);
                Component component = Util.getItemStackName(new ItemStack(Material.GOLDEN_AXE, 1));
                plugin.text().of(p, "no-creative-break", component).send();
                return;
            }
            //Allow Shop owner break the shop sign(for sign replacement)
            if (getPlugin().getConfig().getBoolean("shop.allow-owner-break-shop-sign") && p.getUniqueId().equals(shop.getOwner())) {
                return;
            }
            Log.debug("Player cannot break the shop information sign.");
            e.setCancelled(true);
        }
    }

    /**
     * Gets the shop a sign is attached to
     *
     * @param loc The location of the sign
     * @return The shop
     */
    @Nullable
    private Shop getShopNextTo(@NotNull Location loc) {
        final Block b = Util.getAttached(loc.getBlock());
        // Util.getAttached(b)
        if (b == null) {
            return null;
        }
        return getShopPlayer(b.getLocation(), false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (!this.updateSignWhenInventoryMoving) {
            return;
        }
        Location destination = event.getDestination().getLocation();
        Location source = event.getSource().getLocation();
        Shop destShop = null;
        Shop sourceShop = null;
        if (destination != null) {
            destination = Util.getBlockLocation(destination);
            destShop = getShopRedstone(destination, true);
        }
        if (source != null) {
            source = Util.getBlockLocation(source);
            sourceShop = getShopRedstone(source, true);
        }
        if (destShop != null) {
            super.getPlugin().getSignUpdateWatcher().scheduleSignUpdate(destShop);
        }
        if (sourceShop != null) {
            super.getPlugin().getSignUpdateWatcher().scheduleSignUpdate(sourceShop);
        }
    }

    /*
     * Listens for chest placement, so a doublechest shop can't be created.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent e) {

        final Material type = e.getBlock().getType();
        final Block placingBlock = e.getBlock();
        final Player player = e.getPlayer();

        if (type != Material.CHEST) {
            return;
        }
        Block chest = null;

        //Chest combine mechanic based checking
        if (player.isSneaking()) {
            Block blockAgainst = e.getBlockAgainst();
            if (blockAgainst.getType() == Material.CHEST && placingBlock.getFace(blockAgainst) != BlockFace.UP && placingBlock.getFace(blockAgainst) != BlockFace.DOWN && !Util.isDoubleChest(blockAgainst.getBlockData())) {
                chest = e.getBlockAgainst();
            } else {
                return;
            }
        } else {
            //Get all chest in vertical Location
            BlockFace placingChestFacing = ((Directional) (placingBlock.getBlockData())).getFacing();
            for (BlockFace face : Util.getVerticalFacing()) {
                //just check the right side and left side
                if (!face.equals(placingChestFacing) && !face.equals(placingChestFacing.getOppositeFace())) {
                    Block nearByBlock = placingBlock.getRelative(face);
                    BlockData nearByBlockData = nearByBlock.getBlockData();
                    if (nearByBlock.getType() == Material.CHEST
                            //non double chest
                            && !Util.isDoubleChest(nearByBlockData)
                            //same facing
                            && placingChestFacing == ((Directional) nearByBlockData).getFacing()) {
                        if (chest == null) {
                            chest = nearByBlock;
                        } else {
                            //when multiply chests competed, minecraft will always combine with right side
                            if (placingBlock.getFace(nearByBlock) == Util.getRightSide(placingChestFacing)) {
                                chest = nearByBlock;
                            }
                        }
                    }
                }
            }
        }
        if (chest == null) {
            return;
        }

        Shop shop = getShopPlayer(chest.getLocation(), false);
        if (shop != null) {
            if (!plugin.perm().hasPermission(player, "quickshop.create.double")) {
                e.setCancelled(true);
                plugin.text().of(player, "no-double-chests").send();
            } else if (!shop.playerAuthorize(player.getUniqueId(), BuiltInShopPermission.ACCESS_INVENTORY)
                    && !plugin.perm().hasPermission(player, "quickshop.other.open")) {
                e.setCancelled(true);
                plugin.text().of(player, "not-managed-shop").send();
            }
        }
    }

    /*
     * Listens for sign update to prevent other plugin or Purpur to edit the sign
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onSignUpdate(SignChangeEvent event) {
        Block posShopBlock = Util.getAttached(event.getBlock());
        if (posShopBlock == null) {
            return;
        }
        Shop shop = plugin.getShopManager().getShopIncludeAttached(posShopBlock.getLocation());
        if (shop == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!shop.playerAuthorize(player.getUniqueId(), BuiltInShopPermission.ACCESS_INVENTORY)
                && !plugin.perm().hasPermission(player, "quickshop.other.open")) {
            plugin.text().of(player, "not-managed-shop").send();
            event.setCancelled(true);
        }
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
