/*
 * This file is a part of project QuickShop, the name is ContainerShop.java
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

package org.maxgamer.quickshop.shop;

import com.lishid.openinv.OpenInv;
import de.tr7zw.nbtapi.NBTTileEntity;
import io.papermc.lib.PaperLib;
import lombok.EqualsAndHashCode;
import me.lucko.helper.serialize.BlockPosition;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.chat.ComponentPackage;
import org.maxgamer.quickshop.api.event.*;
import org.maxgamer.quickshop.api.shop.*;
import org.maxgamer.quickshop.chat.platform.minedown.BungeeQuickChat;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.logging.container.ShopRemoveLog;

import java.util.*;
import java.util.logging.Level;

import static org.maxgamer.quickshop.chat.platform.minedown.BungeeQuickChat.fromLegacyText;
import static org.maxgamer.quickshop.chat.platform.minedown.BungeeQuickChat.toLegacyText;

/**
 * ChestShop core
 */
@EqualsAndHashCode
public class ContainerShop implements Shop {
    @EqualsAndHashCode.Exclude
    @Deprecated
    private static final String SHOP_SIGN_PREFIX = "§d§o §r";
    @NotNull
    private final Location location;
    private final YamlConfiguration extra;
    @EqualsAndHashCode.Exclude
    private final QuickShop plugin;
    @EqualsAndHashCode.Exclude
    private final UUID runtimeRandomUniqueId = UUID.randomUUID();
    private ShopModerator moderator;
    private double price;
    private ShopType shopType;
    private boolean unlimited;
    @NotNull
    private ItemStack item;
    @Nullable
    @EqualsAndHashCode.Exclude
    private AbstractDisplayItem displayItem;
    @EqualsAndHashCode.Exclude
    private volatile boolean isLoaded = false;
    @EqualsAndHashCode.Exclude
    private volatile boolean isDeleted = false;
    @EqualsAndHashCode.Exclude
    private volatile boolean isLeftShop = false;
    @EqualsAndHashCode.Exclude
    private volatile boolean createBackup = false;
    @EqualsAndHashCode.Exclude
    private InventoryPreview inventoryPreview = null;
    @EqualsAndHashCode.Exclude
    private volatile ContainerShop attachedShop;
    @EqualsAndHashCode.Exclude
    private volatile boolean isDisplayItemChanged = false;
    @EqualsAndHashCode.Exclude
    private volatile boolean dirty;
    @EqualsAndHashCode.Exclude
    private volatile boolean updating = false;
    @Nullable
    private String currency;
    private boolean disableDisplay;
    private UUID taxAccount;

    @SuppressWarnings("CopyConstructorMissesField")
    private ContainerShop(@NotNull ContainerShop s) {
        Util.ensureThread(false);
        this.shopType = s.shopType;
        this.item = s.item.clone();
        this.location = s.location.clone();
        this.plugin = s.plugin;
        this.unlimited = s.unlimited;
        this.moderator = s.moderator.clone();
        this.price = s.price;
        this.isLoaded = s.isLoaded;
        this.isDeleted = s.isDeleted;
        this.createBackup = s.createBackup;
        this.extra = s.extra;
        this.dirty = true;
        this.inventoryPreview = null;
        this.currency = s.currency;
        this.disableDisplay = s.disableDisplay;
        this.taxAccount = s.taxAccount;
        initDisplayItem();
    }

    /**
     * Adds a new shop. You need call ShopManager#loadShop if you create from outside of
     * ShopLoader.
     *
     * @param location  The location of the chest block
     * @param price     The cost per item
     * @param item      The itemstack with the properties we want. This is .cloned, no need to worry
     *                  about references
     * @param moderator The modertators
     * @param type      The shop type
     * @param unlimited The unlimited
     * @param plugin    The plugin instance
     * @param extra     The extra data saved by addon
     */
    public ContainerShop(
            @NotNull QuickShop plugin,
            @NotNull Location location,
            double price,
            @NotNull ItemStack item,
            @NotNull ShopModerator moderator,
            boolean unlimited,
            @NotNull ShopType type,
            @NotNull YamlConfiguration extra,
            @Nullable String currency,
            boolean disableDisplay,
            @Nullable UUID taxAccount) {
        Util.ensureThread(false);
        this.location = location;
        this.price = price;
        this.moderator = moderator;
        this.item = item.clone();
        this.plugin = plugin;
        if (!plugin.isAllowStack()) {
            this.item.setAmount(1);
        }
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && meta.getDisplayName().matches("\\{.*}")) {
                //https://hub.spigotmc.org/jira/browse/SPIGOT-5964
                meta.setDisplayName(meta.getDisplayName());
                //Correct both items
                item.setItemMeta(meta);
                this.item.setItemMeta(meta);
            }
        }
        this.shopType = type;
        this.unlimited = unlimited;
        this.extra = extra;
        this.currency = currency;
        this.disableDisplay = disableDisplay;
        this.taxAccount = taxAccount;
        initDisplayItem();
        this.dirty = false;
        updateShopData();
    }

    private void updateShopData() {
        ConfigurationSection section = getExtra(plugin);
        if (section.getString("currency") != null) {
            this.currency = section.getString("currency");
            section.set("currency", null);
            Util.debugLog("Shop " + this + " currency data upgrade successful.");
        }
        setExtra(plugin, section);
        setDirty();
        this.update();
    }

    @Override
    public boolean isDisableDisplay() {
        return disableDisplay;
    }

    @Override
    public void setDisableDisplay(boolean disabled) {
        this.disableDisplay = disabled;
        setDirty();
        update();
        checkDisplay();
    }

    @Override
    @Nullable
    public UUID getTaxAccount() {
        UUID uuid = null;
        if (taxAccount != null) {
            uuid = taxAccount;
        } else {
            if (((SimpleShopManager) plugin.getShopManager()).getCacheTaxAccount() != null) {
                uuid = ((SimpleShopManager) plugin.getShopManager()).getCacheTaxAccount().getUniqueId();
            }
        }
        ShopTaxAccountGettingEvent event = new ShopTaxAccountGettingEvent(this, uuid);
        event.callEvent();
        return event.getTaxAccount();

    }

    @Override
    public void setTaxAccount(@Nullable UUID taxAccount) {
        this.taxAccount = taxAccount;
        setDirty();
        update();
    }

    @Override
    @Nullable
    public UUID getTaxAccountActual() {
        return taxAccount;
    }

    private void initDisplayItem() {
        Util.ensureThread(false);
        if (plugin.isDisplayEnabled() && !isDisableDisplay()) {
            switch (AbstractDisplayItem.getNowUsing()) {
                case REALITEM:
                    this.displayItem = new RealDisplayItem(this);
                    break;
                case VIRTUALITEM:
                    this.displayItem = new VirtualDisplayItem(this);
                    break;
                default:
                    Util.debugLog("Warning: Failed to create a ContainerShop displayItem, the type we didn't know, fallback to VirualDisplayItem");
                    this.displayItem = new VirtualDisplayItem(this);
                    break;
            }
        }
    }

    /**
     * Add an item to shops chest.
     *
     * @param item   The itemstack. The amount does not matter, just everything else
     * @param amount The amount to add to the shop.
     */
    @Override
    public void add(@NotNull ItemStack item, int amount) {
        Util.ensureThread(false);
        if (this.unlimited) {
            return;
        }
        item = item.clone();
        int itemMaxStackSize = Util.getItemMaxStackSize(item.getType());
        Inventory inv = this.getInventory();
        int remains = amount;
        while (remains > 0) {
            int stackSize = Math.min(remains, itemMaxStackSize);
            item.setAmount(stackSize);
            Objects.requireNonNull(inv).addItem(item);
            remains -= stackSize;
        }
        this.setSignText();
    }

    @Override
    public boolean addStaff(@NotNull UUID player) {
        Util.ensureThread(false);
        setDirty();
        boolean result = this.moderator.addStaff(player);
        update();
        if (result) {
            Util.mainThreadRun(() -> plugin.getServer().getPluginManager()
                    .callEvent(new ShopModeratorChangedEvent(this, this.moderator)));
        }
        return result;
    }

    /**
     * Buys amount of item from Player p. Does NOT check our inventory, or balances
     *
     * @param buyer          The player to buy from
     * @param buyerInventory The buyer's inventory
     * @param loc2Drop       The location to drop items if inventory are full
     * @param amount         The amount to buy
     */
    @Override
    public void buy(@NotNull UUID buyer, @NotNull Inventory buyerInventory,
                    @NotNull Location loc2Drop, int amount) {
        Util.ensureThread(false);
        amount = amount * item.getAmount();
        if (amount < 0) {
            this.sell(buyer, buyerInventory, loc2Drop, -amount);
            return;
        }
        ItemStack[] contents = buyerInventory.getContents();
        if (this.isUnlimited()) {
            for (int i = 0; amount > 0 && i < contents.length; i++) {
                ItemStack stack = contents[i];
                if (stack == null || stack.getType() == Material.AIR) {
                    continue; // No item
                }
                if (matches(stack, true)) {
                    int stackSize = Math.min(amount, stack.getAmount());
                    stack.setAmount(stack.getAmount() - stackSize);
                    amount -= stackSize;
                }
            }
            // Send the players new inventory to them
            buyerInventory.setContents(contents);
            this.setSignText();
            // This should not happen.
            if (amount > 0) {
                plugin.getLogger().log(
                        Level.WARNING,
                        "Could not take all items from a players inventory on purchase! "
                                + buyer
                                + ", missing: "
                                + amount
                                + ", item: "
                                + Util.getItemStackName(this.getItem())
                                + "!");
            }
        } else {
            Inventory chestInv = this.getInventory();
            for (int i = 0; amount > 0 && i < contents.length; i++) {
                ItemStack item = contents[i];
                if (item != null && this.matches(item, true)) {
                    // Copy it, we don't want to interfere
                    item = item.clone();
                    // Amount = total, item.getAmount() = how many items in the
                    // stack
                    int stackSize = Math.min(amount, item.getAmount());
                    // If Amount is item.getAmount(), then this sets the amount
                    // to 0
                    // Else it sets it to the remainder
                    contents[i].setAmount(contents[i].getAmount() - stackSize);
                    // We can modify this, it is a copy.
                    item.setAmount(stackSize);
                    // Add the items to the players inventory
                    Objects.requireNonNull(chestInv).addItem(item);
                    amount -= stackSize;
                }
            }
            // Now update the players inventory.
            buyerInventory.setContents(contents);

            //Update sign
            this.setSignText();
            if (attachedShop != null) {
                attachedShop.setSignText();
            }
        }
    }

    @Override
    public void checkDisplay() {
        Util.ensureThread(false);
        if (!plugin.isDisplayEnabled() || this.disableDisplay || !this.isLoaded || this.isDeleted()) { // FIXME: Reinit scheduler on reloading config
            if (this.displayItem != null && this.displayItem.isSpawned()) {
                this.displayItem.remove();
            }
            return;
        }

        //FIXME: This may affect the performance
        updateAttachedShop();

        if (isLeftShop) {
            if (displayItem != null) {
                displayItem.remove();
            }
            if (attachedShop != null) {
                attachedShop.refresh();
            }
            return;
        }

        if (this.displayItem == null) {
            Util.debugLog("Warning: DisplayItem is null, this shouldn't happened...");
            StackTraceElement traceElements = Thread.currentThread().getStackTrace()[2];
            Util.debugLog("Call from: " + traceElements.getClassName() + "#" + traceElements.getMethodName() + "%" + traceElements.getLineNumber());
            return;
        }

        if (!this.displayItem.isSpawned()) {
            /* Not spawned yet. */
            displayItem.spawn();
        } else {
            /* If not spawned, we didn't need check these, only check them when we need. */
            if (this.displayItem.checkDisplayNeedRegen()) {
                this.displayItem.fixDisplayNeedRegen();
            } else {
                /* If display was regened, we didn't need check it moved, performance! */
                if (this.displayItem.checkDisplayIsMoved()) {
                    this.displayItem.fixDisplayMoved();
                }
            }
        }
        /* Dupe is always need check, if enabled display */
        this.displayItem.removeDupe();
    }

    @Override
    public void clearStaffs() {
        setDirty();
        this.moderator.clearStaffs();
        Util.mainThreadRun(() -> plugin.getServer().getPluginManager()
                .callEvent(new ShopModeratorChangedEvent(this, this.moderator)));
        update();
    }

    @Override
    public boolean delStaff(@NotNull UUID player) {
        Util.ensureThread(false);
        setDirty();
        boolean result = this.moderator.delStaff(player);
        update();
        if (result) {
            Util.mainThreadRun(() -> plugin.getServer().getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator)));
        }
        return result;
    }

    /**
     * Deletes the shop from the list of shops and queues it for database
     */
    @Override
    public void delete() {
        Util.ensureThread(false);
        delete(false);
    }

    /**
     * Deletes the shop from the list of shops and queues it for database deletion
     *
     * @param memoryOnly whether to delete from database
     */
    @Override
    public void delete(boolean memoryOnly) {
        Util.ensureThread(false);
        // Get a copy of the attached shop to save it from deletion
        ContainerShop neighbor = getAttachedShop();
        setDirty();
        ShopDeleteEvent shopDeleteEvent = new ShopDeleteEvent(this, memoryOnly);
        if (Util.fireCancellableEvent(shopDeleteEvent)) {
            Util.debugLog("Shop deletion was canceled because a plugin canceled it.");
            return;
        }
        isDeleted = true;
        // Unload the shop
        if (isLoaded) {
            this.onUnload();
        }
        if (memoryOnly) {
            // Delete it from memory
            plugin.getShopManager().removeShop(this);
        } else {
            // Delete the signs around it
            for (Sign s : this.getSigns()) {
                s.getBlock().setType(Material.AIR);
            }
            // Delete it from the database
            // Refund if necessary
            if (plugin.getConfig().getBoolean("shop.refund")) {
                plugin.getEconomy().deposit(this.getOwner(), plugin.getConfig().getDouble("shop.cost"),
                        Objects.requireNonNull(getLocation().getWorld()), getCurrency());
            }
            plugin.getShopManager().removeShop(this);
            plugin.getDatabaseHelper().removeShop(this);
        }
        // Use that copy we saved earlier (which is now deleted) to refresh it's now alone neighbor
        if (neighbor != null) {
            neighbor.refresh();
        }
    }

    @Override
    public boolean isAttached(@NotNull Block b) {
        Util.ensureThread(false);
        return this.getLocation().getBlock().equals(Util.getAttached(b));
    }

    /**
     * Returns true if the ItemStack matches what this shop is selling/buying
     *
     * @param item The ItemStack
     * @return True if the ItemStack is the same (Excludes amounts)
     */
    @Override
    public boolean matches(@Nullable ItemStack item) {
        return this.matches(item, false);
    }

    /**
     * Returns true if the ItemStack matches what this shop is selling/buying
     *
     * @param item The ItemStack
     * @param buying Some matching functions are not symmetric, this decides the order it will check
     * @return True if the ItemStack is the same (Excludes amounts)
     */
    public boolean matches(@Nullable ItemStack item, boolean buying) {
        if (item == null) {
            return false;
        }
        ItemStack guest = item.clone();
        guest.setAmount(1);
        ItemStack owner = this.item.clone();
        owner.setAmount(1);
        return buying ? plugin.getItemMatcher().matches(guest, owner) : 
            plugin.getItemMatcher().matches(owner, guest);
    }

    @Override
    public void onClick() {
        Util.ensureThread(false);
        ShopClickEvent event = new ShopClickEvent(this);
        if (Util.fireCancellableEvent(event)) {
            Util.debugLog("Ignore shop click, because some plugin cancel it.");
            return;
        }
        refresh();
        setSignText();
    }

    /**
     * @return The ItemStack type of this shop
     */
    public @NotNull Material getMaterial() {
        return this.item.getType();
    }

    /**
     * Unload ContainerShop.
     */
    @Override
    public void onUnload() {
        Util.ensureThread(false);
        if (!this.isLoaded) {
            Util.debugLog("Dupe unload request, canceled.");
            return;
        }
        if (inventoryPreview != null) {
            inventoryPreview.close();
        }
        if (this.displayItem != null) {
            this.displayItem.remove();
        }
        update();
        this.isLoaded = false;
        plugin.getShopManager().getLoadedShops().remove(this);
        ShopUnloadEvent shopUnloadEvent = new ShopUnloadEvent(this);
        plugin.getServer().getPluginManager().callEvent(shopUnloadEvent);
    }

    @Override
    public @NotNull String ownerName(boolean forceUsername) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(this.getOwner());
        String name = player.getName();
        if (name == null || name.isEmpty()) {
            name = plugin.text().of("unknown-owner").forLocale();
        }
        if (!forceUsername && isUnlimited()) {
            name = plugin.text().of("admin-shop").forLocale();
        }
        ShopOwnerNameGettingEvent event = new ShopOwnerNameGettingEvent(this, getOwner(), name);
        event.callEvent();
        name = event.getName();
        return name;
    }

    @Override
    public boolean isFreeShop() {
        return this.price == 0.0d;
    }

    @Override
    public @NotNull String ownerName() {
        return ownerName(false);
    }

    /**
     * Removes an item from the shop.
     *
     * @param item   The itemstack. The amount does not matter, just everything else
     * @param amount The amount to remove from the shop.
     */
    @Override
    public void remove(@NotNull ItemStack item, int amount) {
        Util.ensureThread(false);
        if (this.unlimited) {
            return;
        }
        item = item.clone();
        int itemMaxStackSize = Util.getItemMaxStackSize(item.getType());
        Inventory inv = this.getInventory();
        int remains = amount;
        while (remains > 0) {
            int stackSize = Math.min(remains, itemMaxStackSize);
            item.setAmount(stackSize);
            Objects.requireNonNull(inv).removeItem(item);
            remains -= stackSize;
        }
        this.setSignText();
    }

    /**
     * Sells amount of item to Player p. Does NOT check our inventory, or balances
     *
     * @param seller          The seller
     * @param sellerInventory The seller's inventory
     * @param loc2Drop        Location to drop items if inventory are full
     * @param amount          The amount to sell
     */
    @Override
    public void sell(@NotNull UUID seller, @NotNull Inventory sellerInventory,
                     @NotNull Location loc2Drop, int amount) {
        Util.ensureThread(false);
        amount = item.getAmount() * amount;
        if (amount < 0) {
            this.buy(seller, sellerInventory, loc2Drop, -amount);
            return;
        }
        // Items to drop on floor
        ArrayList<ItemStack> floor = new ArrayList<>(5);
        int itemMaxStackSize = Util.getItemMaxStackSize(this.item.getType());
        if (this.isUnlimited()) {
            ItemStack item = this.item.clone();
            while (amount > 0) {
                int stackSize = Math.min(amount, itemMaxStackSize);
                item.setAmount(stackSize);
                sellerInventory.addItem(item);
                amount -= stackSize;
            }
        } else {
            ItemStack[] chestContents = Objects.requireNonNull(this.getInventory()).getContents();
            for (int i = 0; amount > 0 && i < chestContents.length; i++) {
                // Can't clone it here, it could be null
                ItemStack item = chestContents[i];
                if (item != null && item.getType() != Material.AIR && this.matches(item, false)) {
                    // Copy it, we don't want to interfere
                    item = item.clone();
                    // Amount = total, item.getAmount() = how many items in the
                    // stack
                    int stackSize = Math.min(amount, item.getAmount());
                    // If Amount is item.getAmount(), then this sets the amount
                    // to 0
                    // Else it sets it to the remainder
                    chestContents[i].setAmount(chestContents[i].getAmount() - stackSize);
                    // We can modify this, it is a copy.
                    item.setAmount(stackSize);
                    // Add the items to the players inventory
                    floor.addAll(sellerInventory.addItem(item).values());
                    amount -= stackSize;
                }
            }
            // We now have to update the chests inventory manually.
            this.getInventory().setContents(chestContents);
            //Update sign
            this.setSignText();
            if (attachedShop != null) {
                attachedShop.setSignText();
            }
        }
        if (loc2Drop != null) {
            for (ItemStack stack : floor) {
                loc2Drop.getWorld().dropItem(loc2Drop, stack);
            }
        }
    }

    public boolean inventoryAvailable() {
        if (isUnlimited()) {
            return true;
        }
        if (isSelling()) {
            return getRemainingStock() > 0;
        }
        if (isBuying()) {
            return getRemainingSpace() > 0;
        }
        return true;
    }

    @Override
    public List<ComponentPackage> getSignText(@NotNull String locale) {
        Util.ensureThread(false);
        List<ComponentPackage> lines = new ArrayList<>();
        //Line 1
        String statusStringKey = inventoryAvailable() ? "signs.status-available" : "signs.status-unavailable";
        lines.add(new ComponentPackage(fromLegacyText(plugin.text().of("signs.header", this.ownerName(false), plugin.text().of(statusStringKey).forLocale(locale)).forLocale(locale))));

        //Line 2
        String tradingStringKey;
        String noRemainingStringKey;
        int shopRemaining;

        switch (shopType) {
            case BUYING:
                shopRemaining = getRemainingSpace();
                tradingStringKey = isStackingShop() ? "signs.stack-buying" : "signs.buying";
                noRemainingStringKey = "signs.out-of-space";
                break;
            case SELLING:
                shopRemaining = getRemainingStock();
                tradingStringKey = isStackingShop() ? "signs.stack-selling" : "signs.selling";
                noRemainingStringKey = "signs.out-of-stock";
                break;
            default:
                shopRemaining = 0;
                tradingStringKey = "MissingKey for shop type:" + shopType;
                noRemainingStringKey = "MissingKey for shop type:" + shopType;
        }
        String line2;
        switch (shopRemaining) {
            //Unlimited
            case -1:
                line2 = plugin.text().of(tradingStringKey, plugin.text().of("signs.unlimited").forLocale(locale)).forLocale(locale);
                break;
            //No remaining
            case 0:
                line2 = plugin.text().of(noRemainingStringKey).forLocale(locale);
                break;
            //Has remaining
            default:
                line2 = plugin.text().of(tradingStringKey, Integer.toString(shopRemaining)).forLocale(locale);
        }

        // TODO No-longer use SHOP_SIGN_PREFIX since we use modern storage method. Pending for deletion.
        lines.add(new ComponentPackage(new BungeeQuickChat.BungeeComponentBuilder().appendLegacy(SHOP_SIGN_PREFIX, line2).create()));

        //line 3
        if (plugin.getConfig().getBoolean("shop.force-use-item-original-name") || !this.getItem().hasItemMeta() || !this.getItem().getItemMeta().hasDisplayName()) {
            String left = plugin.text().of("signs.item-left").forLocale();
            String right = plugin.text().of("signs.item-right").forLocale();
            if (plugin.getNbtapi() == null) {
                // NBTAPI not installed
                lines.add(new ComponentPackage(new BungeeQuickChat.BungeeComponentBuilder()
                        .appendLegacy(left, Util.getItemStackName(getItem()), right)
                        .create()));
            } else {
                // NBTAPI installed
                String itemName = Util.getItemCustomName(getItem());
                BaseComponent[] itemComponents = itemName == null ? Util.getTranslateComponentForItem(getItem()) : fromLegacyText(itemName);
                lines.add(new ComponentPackage(new BungeeQuickChat.BungeeComponentBuilder()
                        .appendLegacyAndItem(left,
                                itemComponents, right)
                        .create()));
            }
        } else {
            lines.add(new ComponentPackage(new BungeeQuickChat.BungeeComponentBuilder().appendLegacy(plugin.text().of("signs.item-left").forLocale()
                    , Util.getItemStackName(getItem()), plugin.text().of("signs.item-right").forLocale()).create()));
        }

        //line 4
        String line4;
        if (this.isStackingShop()) {
            line4 = plugin.text().of("signs.stack-price",
                    plugin.getShopManager().format(this.getPrice(), this), Integer.toString(item.getAmount()),
                    Util.getItemStackName(item)).forLocale();
        } else {
            line4 = plugin.text().of("signs.price", plugin.getShopManager().format(this.getPrice(), this)).forLocale();
        }
        lines.add(new ComponentPackage(new BungeeQuickChat.BungeeComponentBuilder().appendLegacy(line4).create()));

        return lines;
    }

    /**
     * Changes all lines of text on a sign near the shop
     *
     * @param lines The array of lines to change. Index is line number.
     */
    @Override
    public void setSignText(@NotNull List<ComponentPackage> lines) {
        Util.ensureThread(false);
        List<Sign> signs = this.getSigns();
        for (Sign sign : signs) {
            if (this.plugin.getNbtapi() != null) {
                NBTTileEntity tileSign = new NBTTileEntity(sign);
                for (int i = 0; i < lines.size(); i++) {
                    try {
                        tileSign.setString("Text" + (i + 1), Util.componentsToJson(lines.get(i).getComponents()));
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "NBTAPI support is broken, dsiable and fallback... (You can safely ignore this)", e);
                        plugin.disableNBTAPI();
                        Util.debugLog("NBTAPI is broken, error: " + e.getMessage() + "\n stacktrace:  \n" + Arrays.toString(e.getStackTrace()));
                        //Reset it since we disable nbt api, text need to change
                        setSignText();
                        return;
                    }
                }
            } else {
                for (int i = 0; i < lines.size(); i++) {
                    sign.setLine(i, toLegacyText(lines.get(i).getComponents()));
                }
            }
            if (plugin.getGameVersion().isSignTextDyeSupport()) {
                DyeColor dyeColor = Util.getDyeColor();
                if (dyeColor != null) {
                    sign.setColor(dyeColor);
                }
            }
            if (plugin.getGameVersion().isSignGlowingSupport()) {
                boolean isGlowing = plugin.getConfig().getBoolean("shop.sign-glowing");
                sign.setGlowingText(isGlowing);
            }
            sign.update(true);
            plugin.getServer().getPluginManager().callEvent(new ShopSignUpdateEvent(this, sign));
        }
    }

    /**
     * Updates signs attached to the shop
     */
    @Override
    public void setSignText() {
        Util.ensureThread(false);
        if (!Util.isLoaded(this.location)) {
            return;
        }
        this.setSignText(getSignText(MsgUtil.getDefaultGameLanguageCode()));
        // this.setSignText(getSignText("en_us"));
    }

    /**
     * Updates the shop into the database.
     */
    @Override
    public void update() {
        //TODO: check isDirty()
        Util.ensureThread(false);
        if (updating) {
            return;
        }
        ShopUpdateEvent shopUpdateEvent = new ShopUpdateEvent(this);
        if (Util.fireCancellableEvent(shopUpdateEvent)) {
            Util.debugLog("The Shop update action was canceled by a plugin.");
            return;
        }
        updating = true;
        int x = this.getLocation().getBlockX();
        int y = this.getLocation().getBlockY();
        int z = this.getLocation().getBlockZ();
        String world = Objects.requireNonNull(this.getLocation().getWorld()).getName();
        int unlimited = this.isUnlimited() ? 1 : 0;
        try {
            plugin.getDatabaseHelper()
                    .updateShop(SimpleShopModerator.serialize(this.moderator), this.getItem(),
                            unlimited, shopType.toID(), this.getPrice(), x, y, z, world,
                            this.saveExtraToYaml(), this.currency, this.disableDisplay, this.taxAccount == null ? null : this.taxAccount.toString());
            this.dirty = false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING,
                    "Could not update a shop in the database! Changes will revert after a reboot!", e);
        } finally {
            updating = false;
        }
    }

    private void notifyDisplayItemChange() {
        isDisplayItemChanged = true;
        if (attachedShop != null && !attachedShop.isDisplayItemChanged) {
            attachedShop.notifyDisplayItemChange();
        }
    }

    /**
     * @return The durability of the item
     */
    @Override
    public short getDurability() {
        return (short) ((Damageable) this.item.getItemMeta()).getDamage();
    }

    /**
     * @return Returns a dummy itemstack of the item this shop is selling.
     */
    @Override
    public @NotNull ItemStack getItem() {
        return item;
    }

    @Override
    public void setItem(@NotNull ItemStack item) {
        Util.ensureThread(false);
        ShopItemChangeEvent event = new ShopItemChangeEvent(this, this.item, item);
        if (Util.fireCancellableEvent(event)) {
            Util.debugLog("A plugin cancelled the item change event.");
            return;
        }
        this.item = item;
        notifyDisplayItemChange();
        update();
        refresh();
    }

    @Override
    public void refresh() {
        Util.ensureThread(false);
        if (inventoryPreview != null) {
            inventoryPreview.close();
            inventoryPreview = null;
        }
        if (displayItem != null) {
            displayItem.remove();
        }

        if (plugin.isDisplayEnabled() && !isDisableDisplay()) {
            if (displayItem != null) {
                displayItem.remove();
            }
            // Update double shop status, is left status, and the attachedShop
            updateAttachedShop();
            // Update displayItem
            if (isDisplayItemChanged && !isDisableDisplay()) {
                initDisplayItem();
                isDisplayItemChanged = false;
            }
            //Update attachedShop DisplayItem
            if (attachedShop != null && attachedShop.isDisplayItemChanged) {
                attachedShop.refresh();
            }
            // Don't make an item for this chest if it's a left shop.
            if (!isLeftShop && !isDisableDisplay() && displayItem != null) {
                displayItem.spawn();
            }
        }
        setSignText();
    }

    /**
     * Load ContainerShop.
     */
    @Override
    public void onLoad() {
        Util.ensureThread(false);
        if (this.isLoaded) {
            Util.debugLog("Dupe load request, canceled.");
            return;
        }
        Map<Location, Shop> shopsInChunk = plugin.getShopManager().getShops(getLocation().getChunk());

        if (shopsInChunk == null || !shopsInChunk.containsValue(this)) {
            throw new IllegalStateException("Shop must register into ShopManager before loading.");
        }

        ShopLoadEvent shopLoadEvent = new ShopLoadEvent(this);
        if (Util.fireCancellableEvent(shopLoadEvent)) {
            return;
        }
        this.isLoaded = true;
        //Shop manger done this already
        //plugin.getShopManager().loadShop(this.getLocation().getWorld().getName(), this);
        plugin.getShopManager().getLoadedShops().add(this);
        plugin.getShopContainerWatcher().scheduleCheck(this);

        // check price restriction
        PriceLimiterCheckResult priceRestriction = plugin.getShopManager().getPriceLimiter().check(item, price);
        boolean markUpdate = false;
        if (priceRestriction.getStatus() != PriceLimiterStatus.PASS) {
            if (priceRestriction.getStatus() == PriceLimiterStatus.NOT_A_WHOLE_NUMBER) {
                setDirty();
                price = Math.floor(price);
                markUpdate = true;
            } else if (priceRestriction.getStatus() == PriceLimiterStatus.NOT_VALID) {
                setDirty();
                price = priceRestriction.getMin();
                markUpdate = true;
            }
            if (price < priceRestriction.getMin()) {
                setDirty();
                price = priceRestriction.getMin();
                markUpdate = true;
            } else if (price > priceRestriction.getMax()) {
                setDirty();
                price = priceRestriction.getMax();
                markUpdate = true;
            }
            if (markUpdate) {
                update();
            }
        }
        checkDisplay();
    }

    /**
     * @return The location of the shops chest
     */
    @Override
    public @NotNull Location getLocation() {
        return this.location;
    }

    @Override
    public @NotNull ShopModerator getModerator() {
        return this.moderator;
    }

    @Override
    public void setModerator(@NotNull ShopModerator shopModerator) {
        Util.ensureThread(false);
        setDirty();
        this.moderator = shopModerator;
        update();
        plugin.getServer().getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
    }

    /**
     * @return The name of the player who owns the shop.
     */
    @Override
    public @NotNull UUID getOwner() {
        return this.moderator.getOwner();
    }

    /**
     * Changes the owner of this shop to the given player.
     *
     * @param owner the new owner
     */
    @Override
    public void setOwner(@NotNull UUID owner) {
        Util.ensureThread(false);
        this.moderator.setOwner(owner);
        setSignText();
        update();
        plugin.getServer().getPluginManager().callEvent(new ShopModeratorChangedEvent(this, this.moderator));
    }

    /**
     * @return The price per item this shop is selling
     */
    @Override
    public double getPrice() {
        return this.price;
    }

    /**
     * Sets the price of the shop.
     *
     * @param price The new price of the shop.
     */
    @Override
    public void setPrice(double price) {
        Util.ensureThread(false);
        ShopPriceChangeEvent event = new ShopPriceChangeEvent(this, this.price, price);
        if (Util.fireCancellableEvent(event)) {
            Util.debugLog("A plugin cancelled the price change event.");
            return;
        }
        setDirty();
        this.price = price;
        setSignText();
        update();
    }

    /**
     * Returns the number of free spots in the chest for the particular item.
     *
     * @return remaining space
     */
    @Override
    public int getRemainingSpace() {
        Util.ensureThread(false);
        if (this.unlimited) {
            return -1;
        }
        int space = Util.countSpace(this.getInventory(), this.getItem());
        new ShopInventoryCalculateEvent(this, space, -1).callEvent();
        return space;
    }

    /**
     * Returns the number of items this shop has in stock.
     *
     * @return The number of items available for purchase.
     */
    @Override
    public int getRemainingStock() {
        Util.ensureThread(false);
        if (this.unlimited) {
            return -1;
        }
        int stock = Util.countItems(this.getInventory(), this.getItem());
        new ShopInventoryCalculateEvent(this, -1, stock).callEvent();
        return stock;
    }

    @Override
    public @NotNull ShopType getShopType() {
        return this.shopType;
    }

    /**
     * Changes a shop type to Bu ying or Selling. Also updates the signs nearby.
     *
     * @param newShopType The new type (ShopType.BUYING or ShopType.SELLING)
     */
    @Override
    public void setShopType(@NotNull ShopType newShopType) {
        Util.ensureThread(false);
        if (this.shopType == newShopType) {
            return; //Ignore if there actually no changes
        }
        setDirty();
        if (Util.fireCancellableEvent(new ShopTypeChangeEvent(this, this.shopType, newShopType))) {
            Util.debugLog(
                    "Some addon cancelled shop type changes, target shop: " + this);
            return;
        }
        this.shopType = newShopType;
        this.setSignText();
        update();
    }

    /**
     * Returns a list of signs that are attached to this shop (QuickShop and blank signs only)
     *
     * @return a list of signs that are attached to this shop (QuickShop and blank signs only)
     */
    @Override
    public @NotNull List<Sign> getSigns() {
        Util.ensureThread(false);
        List<Sign> signs = new ArrayList<>(4);
        if (this.getLocation().getWorld() == null) {
            return Collections.emptyList();
        }
        Block[] blocks = new Block[4];
        blocks[0] = location.getBlock().getRelative(BlockFace.EAST);
        blocks[1] = location.getBlock().getRelative(BlockFace.NORTH);
        blocks[2] = location.getBlock().getRelative(BlockFace.SOUTH);
        blocks[3] = location.getBlock().getRelative(BlockFace.WEST);
        for (Block b : blocks) {
            if (b == null) {
                continue;
            }
            BlockState state = PaperLib.getBlockState(b, false).getState();
            if (!(state instanceof Sign)) {
                continue;
            }
            Sign sign = (Sign) state;
            if (isShopSign(sign)) {
                claimShopSign(sign);
                signs.add(sign);
            }
        }

        return signs;
    }

    private ShopSignStorage saveToShopSignStorage() {
        return new ShopSignStorage(getLocation().getWorld().getName(), getLocation().getBlockX(), getLocation().getBlockY(), getLocation().getBlockZ());
    }

    @Override
    public void claimShopSign(@NotNull Sign sign) {
        sign.getPersistentDataContainer().set(SHOP_NAMESPACED_KEY, ShopSignPersistentDataType.INSTANCE, saveToShopSignStorage());
    }

    /**
     * @return The list of players who can manage the shop.
     */
    @NotNull
    @Override
    public List<UUID> getStaffs() {
        return new ArrayList<>(this.moderator.getStaffs()); //Clone only, so make sure external calling will use addStaff
    }

    @Override
    public boolean isBuying() {
        return this.shopType == ShopType.BUYING;
    }

    @Override
    public boolean isLoaded() {
        return this.isLoaded;
    }

    @Override
    public boolean isSelling() {
        return this.shopType == ShopType.SELLING;
    }

    @Override
    public boolean isUnlimited() {
        return this.unlimited;
    }

    @Override
    public void setUnlimited(boolean unlimited) {
        Util.ensureThread(false);
        this.unlimited = unlimited;
        this.setSignText();
        update();
    }

    /**
     * Check shop is or not still Valid.
     *
     * @return isValid
     */
    @Override
    public boolean isValid() {
        Util.ensureThread(false);
        if (this.isDeleted) {
            return false;
        }
        return Util.canBeShop(this.getLocation().getBlock());
    }

    @Override
    public boolean isDeleted() {
        return this.isDeleted;
    }

    @Override
    public @Nullable AbstractDisplayItem getDisplay() {
        return this.displayItem;
    }

    @Override
    public void setDirty() {
        this.dirty = true;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void setDirty(boolean isDirty) {
        this.dirty = isDirty;
    }

    /**
     * Returns a clone of this shop. References to the same display item, itemstack, location and
     * owner as this shop does. Do not modify them or you will modify this shop.
     *
     * <p>**NOT A DEEP CLONE**
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public @NotNull ContainerShop clone() {
        return new ContainerShop(this);
    }

    @Override
    public String toString() {

        return "Shop " +
                (location.getWorld() == null ? "unloaded world" : location.getWorld().getName()) +
                "(" +
                location.getBlockX() +
                ", " +
                location.getBlockY() +
                ", " +
                location.getBlockZ() +
                ")" +
                " Owner: " + this.ownerName(false) + " - " + getOwner() +
                ", Unlimited: " + isUnlimited() +
                " Price: " + getPrice();
    }

    /**
     * Returns the display item associated with this shop.
     *
     * @return The display item associated with this shop.
     */
    @Nullable
    public AbstractDisplayItem getDisplayItem() {
        return this.displayItem;
    }

    /**
     * @return The enchantments the shop has on its items.
     */
    public @NotNull Map<Enchantment, Integer> getEnchants() {
        if (this.item.hasItemMeta() && this.item.getItemMeta().hasEnchants()) {
            return Objects.requireNonNull(this.item.getItemMeta()).getEnchants();
        }
        return Collections.emptyMap();
    }

    /**
     * @return The chest this shop is based on.
     */
    public @Nullable Inventory getInventory() {
        Util.ensureThread(false);
        BlockState state = PaperLib.getBlockState(location.getBlock(), false).getState();
        Inventory inv = null;
        try {
            if (state.getType() == Material.ENDER_CHEST
                    && plugin.getOpenInvPlugin() != null) { //FIXME: Need better impl
                OpenInv openInv = ((OpenInv) plugin.getOpenInvPlugin());
                inv = openInv.getSpecialEnderChest(
                        Objects.requireNonNull(
                                openInv.loadPlayer(
                                        plugin.getServer().getOfflinePlayer(this.moderator.getOwner()))),
                        plugin.getServer().getOfflinePlayer((this.moderator.getOwner())).isOnline())
                        .getBukkitInventory();
            }
        } catch (Exception e) {
            Util.debugLog(e.getMessage());
            return null;
        }
        InventoryHolder container;
        try {
            container = (InventoryHolder) state;
            inv = container.getInventory();
        } catch (Exception e) {
            if (!createBackup) {
                createBackup = Util.backupDatabase();
                if (createBackup) {
                    this.delete(false);
                }
            } else {
                this.delete(true);
            }
            plugin.logEvent(new ShopRemoveLog(Util.getNilUniqueId(), "Inventory Invalid", this.saveToInfoStorage()));
            Util.debugLog(
                    "Inventory doesn't exist anymore: " + this + " shop was removed.");
            return null;
        }

        ShopInventoryEvent event = new ShopInventoryEvent(this, inv);
        event.callEvent();
        return event.getInventory();
    }

    /**
     * Returns true if this shop is a double chest, and the other half is selling/buying the same as
     * this is buying/selling.
     *
     * @return true if this shop is a double chest, and the other half is selling/buying the same as
     * this is buying/selling.
     */
    public boolean isDoubleShop() {
        Util.ensureThread(false);
        if (attachedShop == null) {
            return false;
        }
        if (attachedShop.matches(this.getItem())) {
            // They're both trading the same item
            // They're both buying or both selling => Not a double shop,
            // just two shops.
            // One is buying, one is selling.
            return this.getShopType() != attachedShop.getShopType();
        } else {
            return false;
        }
    }

    /**
     * Updates the attachedShop variable to reflect the currently attached shop, if any.
     * Also updates the left shop status.
     */
    @Override
    public void updateAttachedShop() {
        //TODO: Rewrite centering item feature, currently implement is buggy and mess
        Util.ensureThread(false);
        Block attachedChest = Util
                .getSecondHalf(this.getLocation().getBlock());

        Shop preValue = attachedShop;

        //Prevent chain chunk loading
        if (attachedChest == null || !Util.isLoaded(attachedChest.getLocation())) {
            attachedShop = null;
        } else {
            attachedShop = (ContainerShop) plugin.getShopManager().getShop(attachedChest.getLocation());
        }

        if (attachedShop != null && attachedShop.matches(this.getItem())) {
            updateLeftShop();
        } else {
            isLeftShop = false;
        }

        if (!Objects.equals(attachedShop, preValue)) {
            notifyDisplayItemChange();
        }
    }

    /**
     * This function calculates which block of a double chest is the left block,
     * relative to the direction the chest is facing. Left shops don't spawn items since
     * they merge items with the right shop.
     * It also updates the isLeftShop status of this class to reflect the changes.
     */
    private void updateLeftShop() {
        //TODO: Rewrite centering item feature, currently implement is buggy and mess
        if (attachedShop == null) {
            return;
        }
        boolean previousValue = isLeftShop;

        switch (((Chest) getLocation().getBlock().getBlockData()).getFacing()) {
            case WEST:
                // left block has a smaller z value
                isLeftShop = getLocation().getZ() < attachedShop.getLocation().getZ();
                break;
            case EAST:
                // left block has a greater z value
                isLeftShop = getLocation().getZ() > attachedShop.getLocation().getZ();
                break;
            case NORTH:
                // left block has greater x value
                isLeftShop = getLocation().getX() > attachedShop.getLocation().getX();
                break;
            case SOUTH:
                // left block has a smaller x value
                isLeftShop = getLocation().getX() < attachedShop.getLocation().getX();
                break;
            default:
                isLeftShop = false;
        }
        if (isLeftShop != previousValue) {
            notifyDisplayItemChange();
        }
    }

    /**
     * Checks to see if it is a real double without updating anything.
     *
     * @return If the chest is a real double chest, as in it is a double and it has the same item.
     */
    @Override
    public boolean isRealDouble() {
        Util.ensureThread(false);
        if (attachedShop == null) {
            return false;
        }
        return attachedShop.matches(this.getItem());
    }

    @Override
    public boolean isLeftShop() {
        return isLeftShop;
    }

    @Override
    public ContainerShop getAttachedShop() {
        return attachedShop;
    }

    /**
     * Different with isDoubleShop, this method only check the shop is created on the double chest.
     *
     * @return true if create on double chest.
     */
    public boolean isDoubleChestShop() {
        Util.ensureThread(false);
        return Util.isDoubleChest(this.getLocation().getBlock().getBlockData());
    }

    /**
     * Check the container still there and we can keep use it.
     */
    public void checkContainer() {
        Util.ensureThread(false);
        if (!this.isLoaded) {
            return;
        }
        if (!Util.isLoaded(this.getLocation())) {
            return;
        }
        if (!Util.canBeShop(this.getLocation().getBlock())) {
            Util.debugLog("Shop at " + this.getLocation() + "@" + this.getLocation().getBlock()
                    + " container was missing, deleting...");
            plugin.getDatabaseHelper().insertHistoryRecord(new ShopRemoveLog(Util.getNilUniqueId(), "Container invalid", saveToInfoStorage()));
            this.onUnload();
            this.delete(false);
        }
    }

    @Override
    public @NotNull String saveExtraToYaml() {
        return extra.saveToString();
    }

    /**
     * Gets the plugin's k-v map to storage the data. It is spilt by plugin name, different name
     * have different map, the data won't conflict. But if you plugin name is too common, add a
     * prefix will be a good idea.
     *
     * @param plugin Plugin instance
     * @return The data table
     */
    @Override
    public @NotNull ConfigurationSection getExtra(@NotNull Plugin plugin) {
        ConfigurationSection section = extra.getConfigurationSection(plugin.getName());
        if (section == null) {
            section = extra.createSection(plugin.getName());
        }
        return section;
    }


    /**
     * Save the extra data to the shop.
     *
     * @param plugin Plugin instace
     * @param data   The data table
     */
    @Override
    public void setExtra(@NotNull Plugin plugin, @NotNull ConfigurationSection data) {
        setDirty();
        update();
    }

    /**
     * Gets shop status is stacking shop
     *
     * @return The shop stacking status
     */
    @Override
    public boolean isStackingShop() {
        return plugin.isAllowStack() && this.item.getAmount() > 1;
    }

    /**
     * WARNING: This UUID will changed after plugin reload, shop reload or server restart DO NOT USE
     * IT TO STORE DATA!
     *
     * @return Random UUID
     */
    @Override
    public @NotNull UUID getRuntimeRandomUniqueId() {
        return this.runtimeRandomUniqueId;
    }

    /**
     * Gets the currency that shop use
     *
     * @return The currency name
     */
    @Override
    public @Nullable String getCurrency() {
        return this.currency;
    }

    /**
     * Sets the currency that shop use
     *
     * @param currency The currency name; null to use default currency
     */
    @Override
    public void setCurrency(@Nullable String currency) {
        this.currency = currency;
        setDirty();
        this.update();
    }

    @Override
    public void openPreview(@NotNull Player player) {
        if (inventoryPreview == null) {
            inventoryPreview = new InventoryPreview(plugin, getItem().clone(), player.getLocale());
        }
        inventoryPreview.show(player);

    }

    @Override
    public ShopInfoStorage saveToInfoStorage() {
        return new ShopInfoStorage(getLocation().getWorld().getName(), BlockPosition.of(getLocation()), SimpleShopModerator.serialize(getModerator()), getPrice(), Util.serialize(getItem()), isUnlimited() ? 1 : 0, getShopType().toID(), saveExtraToYaml(), getCurrency(), isDisableDisplay(), getTaxAccount());
    }
}
