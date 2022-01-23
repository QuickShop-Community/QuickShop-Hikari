/*
 * This file is a part of project QuickShop, the name is PlayerListener.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.listener;

import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.economy.AbstractEconomy;
import org.maxgamer.quickshop.api.shop.Info;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.api.shop.ShopAction;
import org.maxgamer.quickshop.shop.SimpleInfo;
import org.maxgamer.quickshop.shop.inventory.BukkitInventoryWrapper;
import org.maxgamer.quickshop.util.InteractUtil;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener extends AbstractQSListener {
    private final CooldownMap<Player> cooldownMap = CooldownMap.create(Cooldown.of(1, TimeUnit.SECONDS));
    private final boolean swapBehavior;

    public PlayerListener(QuickShop plugin) {
        super(plugin);
        swapBehavior = plugin.getConfig().getBoolean("shop.interact.swap-click-behavior");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onClick(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        final Block b = e.getClickedBlock();

        // ----Adventure dupe click workaround start----
        if (e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            cooldownMap.test(e.getPlayer());
        }
        // ----Adventure dupe click workaround end----
        if (!e.getAction().equals(Action.LEFT_CLICK_BLOCK) && b != null) {
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (!swapBehavior) {
                    postControlPanel(e);
                } else {
                    postTrade(e);
                }
                return;
            }
        }
        if (!swapBehavior) {
            postTrade(e);
        } else {
            postControlPanel(e);
        }

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAdventureClick(PlayerAnimationEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.ADVENTURE) {
            return;
        }
        // ----Adventure dupe click workaround start----
        if (!cooldownMap.test(event.getPlayer())) {
            return;
        }
        // ----Adventure dupe click workaround end----
        Block focused = event.getPlayer().getTargetBlock(null, 5);
        PlayerInteractEvent interactEvent
                = new PlayerInteractEvent(event.getPlayer(),
                focused.getType() == Material.AIR ? Action.LEFT_CLICK_AIR : Action.LEFT_CLICK_BLOCK,
                event.getPlayer().getInventory().getItemInMainHand(),
                focused,
                event.getPlayer().getFacing().getOppositeFace());

        if (!swapBehavior) {
            postTrade(interactEvent);
        } else {
            postControlPanel(interactEvent);
        }

    }

    private void playClickSound(@NotNull Player player) {
        if (plugin.getConfig().getBoolean("effect.sound.onclick")) {
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.f, 1.0f);
        }
    }

    private void postControlPanel(PlayerInteractEvent e) {
        final Block b = e.getClickedBlock();
        final Player p = e.getPlayer();
        if (Util.isWallSign(b.getType())) {
            final Block block;
            if (Util.isWallSign(b.getType())) {
                block = Util.getAttached(b);
            } else {
                block = e.getClickedBlock();
            }
            Shop controlPanelShop = plugin.getShopManager().getShop(Objects.requireNonNull(block).getLocation());
            if (controlPanelShop != null && (controlPanelShop.getOwner().equals(p.getUniqueId()) || QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.control"))) {
                MsgUtil.sendControlPanelInfo(p, Objects.requireNonNull(plugin.getShopManager().getShop(block.getLocation())));
                this.playClickSound(e.getPlayer());
                Objects.requireNonNull(plugin.getShopManager().getShop(block.getLocation())).setSignText();
                //Prevent use item by ancient
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    e.setUseItemInHand(Event.Result.DENY);
                }
            }
        }
    }

    private void postTrade(PlayerInteractEvent e) {
        final Block b = e.getClickedBlock();
        final Player p = e.getPlayer();
        if (b == null) {
            return;
        }
        if (!Util.canBeShop(b) && !Util.isWallSign(b.getType())) {
            return;
        }

        final Location loc = b.getLocation();
        final ItemStack item = e.getItem();
        // Get the shop
        Shop shop = plugin.getShopManager().getShop(loc);
        // If that wasn't a shop, search nearby shops
        if (shop == null) {
            final Block attached;
            if (Util.isWallSign(b.getType())) {
                attached = Util.getAttached(b);
                if (attached != null) {
                    shop = plugin.getShopManager().getShop(attached.getLocation());
                }
            } else if (Util.isDoubleChest(b.getBlockData())) {
                attached = Util.getSecondHalf(b);
                if (attached != null) {
                    Shop secondHalfShop = plugin.getShopManager().getShop(attached.getLocation());
                    if (secondHalfShop != null && !p.getUniqueId().equals(secondHalfShop.getOwner())) {
                        // If player not the owner of the shop, make him select the second half of the
                        // shop
                        // Otherwise owner will be able to create new double chest shop
                        shop = secondHalfShop;
                    }
                }
            }
        }
        // Purchase handling
        if (shop != null && QuickShop.getPermissionManager().hasPermission(p, "quickshop.use")) {
            if (!InteractUtil.check(InteractUtil.Action.TRADE, p.isSneaking())) {
                return;
            }
            //Prevent use item by ancient
            e.setUseItemInHand(Event.Result.DENY);
            shop.onClick();
            this.playClickSound(e.getPlayer());
            // Text menu
            plugin.getShopManager().sendShopInfo(p, shop);
            shop.setSignText();

            final AbstractEconomy eco = plugin.getEconomy();
            final double price = shop.getPrice();
            final Inventory playerInventory = p.getInventory();
            final String tradeAllWord = plugin.getConfig().getString("shop.word-for-trade-all-items", "all");
            if (shop.isSelling()) {
                if (shop.getRemainingStock() == 0) {
                    plugin.text().of(p, "purchase-out-of-stock", shop.ownerName()).send();
                    return;
                }
                final double traderBalance = eco.getBalance(p.getUniqueId(), shop.getLocation().getWorld(), shop.getCurrency());
                int itemAmount = getPlayerCanBuy(shop, traderBalance, price, playerInventory);
                if (shop.isStackingShop()) {
                    plugin.text().of(p, "how-many-buy-stack", Integer.toString(shop.getItem().getAmount()), Integer.toString(itemAmount), tradeAllWord).send();
                } else {
                    plugin.text().of(p, "how-many-buy", Integer.toString(itemAmount), tradeAllWord).send();
                }
            } else {
                if (shop.getRemainingSpace() == 0) {
                    plugin.text().of(p, "purchase-out-of-space", shop.ownerName()).send();
                    return;
                }
                final double ownerBalance = eco.getBalance(shop.getOwner(), shop.getLocation().getWorld(), shop.getCurrency());
                int items = getPlayerCanSell(shop, ownerBalance, price, playerInventory);
                if (shop.isStackingShop()) {
                    plugin.text().of(p, "how-many-sell-stack", Integer.toString(shop.getItem().getAmount()), Integer.toString(items), tradeAllWord).send();
                } else {
                    plugin.text().of(p, "how-many-sell", Integer.toString(items), tradeAllWord).send();
                }
            }
            // Add the new action
            Map<UUID, Info> actions = plugin.getShopManager().getActions();
            Info info = new SimpleInfo(shop.getLocation(), ShopAction.BUY, null, null, shop, false);
            actions.put(p.getUniqueId(), info);
        }
        // Handles creating shops
        else if (e.useInteractedBlock() == Event.Result.ALLOW
                && shop == null
                && item != null
                && item.getType() != Material.AIR
                && QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.sell")
                && p.getGameMode() != GameMode.CREATIVE) {
            if (e.useInteractedBlock() == Event.Result.DENY
                    || !InteractUtil.check(InteractUtil.Action.CREATE, p.isSneaking())
                    || plugin.getConfig().getBoolean("shop.disable-quick-create")
                    || !plugin.getShopManager().canBuildShop(p, b, e.getBlockFace())) {
                // As of the new checking system, most plugins will tell the
                // player why they can't create a shop there.
                // So telling them a message would cause spam etc.
                return;
            }
            if (Util.isDoubleChest(b.getBlockData())
                    && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.double")) {
                plugin.text().of(p, "no-double-chests").send();
                return;
            }
            if (Util.isBlacklisted(item)
                    && !QuickShop.getPermissionManager()
                    .hasPermission(p, "quickshop.bypass." + item.getType().name())) {
                plugin.text().of(p, "blacklisted-item").send();
                return;
            }
            if (b.getType() == Material.ENDER_CHEST //FIXME: Need a better impl
                    && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.enderchest")) {
                return;
            }
            if (Util.isWallSign(b.getType())) {
                return;
            }
            // Finds out where the sign should be placed for the shop
            Block last = null;
            final Location from = p.getLocation().clone();

            from.setY(b.getY());
            from.setPitch(0);
            final BlockIterator bIt = new BlockIterator(from, 0, 7);

            while (bIt.hasNext()) {
                final Block n = bIt.next();
                if (n.equals(b)) {
                    break;
                }
                last = n;
            }
            // Send creation menu.
            final SimpleInfo info = new SimpleInfo(b.getLocation(), ShopAction.CREATE, e.getItem(), last, false);
            plugin.getShopManager().getActions().put(p.getUniqueId(), info);
            plugin.text().of(p, "how-much-to-trade-for", MsgUtil.getTranslateText(Objects.requireNonNull(e.getItem())), Integer.toString(plugin.isAllowStack() && QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.stacks") ? item.getAmount() : 1)).send();
            //Prevent use item by ancient
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                e.setUseItemInHand(Event.Result.DENY);
            }
        }
    }

    private int getPlayerCanBuy(Shop shop, double traderBalance, double price, Inventory playerInventory) {
        boolean isContainerCountingNeeded = shop.isUnlimited() && !shop.isAlwaysCountingContainer();
        if (shop.isFreeShop()) { // Free shop
            return isContainerCountingNeeded ? new BukkitInventoryWrapper(playerInventory).countSpace(shop) : Math.min(shop.getRemainingStock(), new BukkitInventoryWrapper(playerInventory).countSpace(shop));
        }
        int itemAmount = Math.min(new BukkitInventoryWrapper(playerInventory).countSpace(shop), (int) Math.floor(traderBalance / price));
        if (!isContainerCountingNeeded) {
            itemAmount = Math.min(itemAmount, shop.getRemainingStock());
        }
        if (itemAmount < 0) {
            itemAmount = 0;
        }
        return itemAmount;
    }

    private int getPlayerCanSell(Shop shop, double ownerBalance, double price, Inventory playerInventory) {
        boolean isContainerCountingNeeded = shop.isUnlimited() && !shop.isAlwaysCountingContainer();
        if (shop.isFreeShop()) {
            return isContainerCountingNeeded ? new BukkitInventoryWrapper(playerInventory).countItems(shop) : Math.min(shop.getRemainingSpace(), new BukkitInventoryWrapper(playerInventory).countItems(shop));
        }

        int items = new BukkitInventoryWrapper(playerInventory).countItems(shop);
        final int ownerCanAfford = (int) (ownerBalance / price);
        if (!isContainerCountingNeeded) {
            // Amount check player amount and shop empty slot
            items = Math.min(items, shop.getRemainingSpace());
            // Amount check player selling item total cost and the shop owner's balance
            items = Math.min(items, ownerCanAfford);
        } else if (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
            // even if the shop is unlimited, the config option pay-unlimited-shop-owners is set to
            // true,
            // the unlimited shop owner should have enough money.
            items = Math.min(items, ownerCanAfford);
        }
        if (items < 0) {
            items = 0;
        }
        return items;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent e) {
        try {
            Location location;
            //for strange NPE from spigot API fix
            //noinspection ConstantConditions
            if (e.getInventory() == null) {
                return;
            }
            location = e.getInventory().getLocation();
            if (location == null) {
                return; /// ignored as workaround, GH-303
            }
            //Cannot tick distance manager while unloading playerchunks
            //https://github.com/pl3xgaming/Purpur/issues/631
            if (!Util.isLoaded(location)) {
                return;
            }
            final Shop shop = plugin.getShopManager().getShopIncludeAttached(location);
            if (shop != null) {
                shop.setSignText();
            }
        } catch (NullPointerException ignored) {
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Util.debugLog("Player " + e.getPlayer().getName() + " using locale " + e.getPlayer().getLocale() + ": " + plugin.text().of(e.getPlayer(), "file-test").forLocale());
        // Notify the player any messages they were sent
        if (plugin.getConfig().getBoolean("shop.auto-fetch-shop-messages")) {
            MsgUtil.flush(e.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onJoin(PlayerLocaleChangeEvent e) {
        Util.debugLog("Player " + e.getPlayer().getName() + " using new locale " + e.getLocale() + ": " + plugin.text().of(e.getPlayer(), "file-test").forLocale(e.getLocale()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Remove them from the menu
        plugin.getShopManager().getActions().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        onMove(new PlayerMoveEvent(e.getPlayer(), e.getFrom(), e.getTo()));
    }

    /*
     * Cancels the menu for broken shop block
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBreakShopCreationChest(BlockBreakEvent event) {
        @Nullable Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        Map<UUID, Info> actionMap = plugin.getShopManager().getActions();
        final Info info = actionMap.get(player.getUniqueId());
        if (info != null && info.getLocation().equals(event.getBlock().getLocation())) {
            actionMap.remove(player.getUniqueId());
            if (info.getAction() == ShopAction.BUY) {
                plugin.text().of(player, "shop-purchase-cancelled").send();
            } else if (info.getAction() == ShopAction.CREATE) {
                plugin.text().of(player, "shop-creation-cancelled").send();
            }
        }
    }

    /*
     * Waits for a player to move too far from a shop, then cancels the menu.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        final Info info = plugin.getShopManager().getActions().get(e.getPlayer().getUniqueId());
        if (info == null) {
            return;
        }
        final Player p = e.getPlayer();
        final Location loc1 = info.getLocation();
        final Location loc2 = p.getLocation();
        if (loc1.getWorld() != loc2.getWorld() || loc1.distanceSquared(loc2) > 25) {
            if (info.getAction() == ShopAction.BUY) {
                plugin.text().of(p, "shop-purchase-cancelled").send();
            } else if (info.getAction() == ShopAction.CREATE) {
                plugin.text().of(p, "shop-creation-cancelled").send();
            }
            Util.debugLog(p.getName() + " too far with the shop location.");
            plugin.getShopManager().getActions().remove(p.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDyeing(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null || !Util.isDyes(e.getItem().getType())) {
            return;
        }
        final Block block = e.getClickedBlock();
        if (block == null || !Util.isWallSign(block.getType())) {
            return;
        }
        final Block attachedBlock = Util.getAttached(block);
        if (attachedBlock == null || plugin.getShopManager().getShopIncludeAttached(attachedBlock.getLocation()) == null) {
            return;
        }
        e.setCancelled(true);
        Util.debugLog("Disallow " + e.getPlayer().getName() + " dye the shop sign.");
    }

//    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//    public void onLocaleChanges(PlayerLocaleChangeEvent event) {
//        MsgUtil.sendDirectMessage(event.getPlayer(), plugin.text().of(event.getPlayer(), "client-language-changed", event.getLocale()).forLocale(event.getLocale()));
//    }

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
