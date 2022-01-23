/*
 * This file is a part of project QuickShop, the name is Shop.java
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

package org.maxgamer.quickshop.api.shop;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.chat.ComponentPackage;
import org.maxgamer.quickshop.api.localization.text.TextManager;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapper;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapperManager;
import org.maxgamer.quickshop.shop.ShopSignPersistentDataType;
import org.maxgamer.quickshop.shop.ShopSignStorage;

import java.util.List;
import java.util.UUID;

/**
 * A shop
 */
public interface Shop {
    NamespacedKey SHOP_NAMESPACED_KEY = new NamespacedKey(QuickShop.getInstance(), "shopsign");
    String SHOP_SIGN_PATTERN = "§d§o ";

    /**
     * Add x ItemStack to the shop inventory
     *
     * @param paramItemStack The ItemStack you want add
     * @param paramInt       How many you want add
     */
    void add(@NotNull ItemStack paramItemStack, int paramInt);

    /**
     * Add new staff to the moderators
     *
     * @param player New staff
     * @return Success
     */
    boolean addStaff(@NotNull UUID player);

    /**
     * Execute buy action for player with x items.
     *
     * @param buyer          The player buying
     * @param buyerInventory The buyer inventory ( may not a player inventory )
     * @param loc2Drop       The location to drops items if player inventory are full
     * @param paramInt       How many buyed?
     */
    void buy(@NotNull UUID buyer, @NotNull InventoryWrapper buyerInventory, @NotNull Location loc2Drop, int paramInt);

    /**
     * Check the display location, and teleport, respawn if needs.
     */
    void checkDisplay();

    /**
     * Empty moderators team.
     */
    void clearStaffs();

    /**
     * Clone new shop object. Not a deep clone.
     *
     * @return New shop object
     */
    @NotNull
    Shop clone();

    /**
     * Remove a staff from moderators
     *
     * @param player Staff
     * @return Success
     */
    boolean delStaff(@NotNull UUID player);

    /**
     * Delete shop from ram, and database.
     */
    void delete();

    /**
     * Delete shop from ram or ram and database
     *
     * @param memoryOnly true = only delete from ram, false = delete from both ram and database
     */
    void delete(boolean memoryOnly);

    /**
     * Check shop is or not attacked the target block
     *
     * @param paramBlock Target block
     * @return isAttached
     */
    boolean isAttached(@NotNull Block paramBlock);

    /**
     * Check the target ItemStack is matches with this shop's item.
     *
     * @param paramItemStack Target ItemStack.
     * @return Matches
     */
    boolean matches(@NotNull ItemStack paramItemStack);

    /**
     * Execute codes when player click the shop will did things
     */
    void onClick();

    /**
     * Load shop to the world
     */
    void onLoad();

    /**
     * Unload shop from world
     */
    void onUnload();

    /**
     * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
     *
     * @param forceUsername Force returns username of shop
     * @return owner name
     */
    @NotNull
    String ownerName(boolean forceUsername);

    /**
     * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
     *
     * @return owner name
     */
    @NotNull
    String ownerName();

    /**
     * Remove x ItemStack from the shop inventory
     *
     * @param paramItemStack Want removed ItemStack
     * @param paramInt       Want remove how many
     */
    void remove(@NotNull ItemStack paramItemStack, int paramInt);

    /**
     * Execute sell action for player with x items.
     *
     * @param seller          Seller
     * @param sellerInventory Seller's inventory ( may not a player inventory )
     * @param loc2Drop        The location to be drop if buyer inventory full ( if player enter a number that < 0, it will turn to buying item)
     * @param paramInt        How many sold?
     */
    void sell(@NotNull UUID seller, @NotNull InventoryWrapper sellerInventory, @NotNull Location loc2Drop, int paramInt);

    /**
     * Generate new sign texts on shop's sign.
     */
    void setSignText();

    /**
     * Get sign texts on shop's sign.
     *
     * @param locale The locale to be created for
     * @return String arrays represents sign texts:
     * Index | Content
     * Line 0: Header
     * Line 1: Shop Type
     * Line 2: Shop Item Name
     * Line 3: Price
     */
    default List<ComponentPackage> getSignText(String locale) {
        //backward support
        throw new UnsupportedOperationException();
    }

    /**
     * Set texts on shop's sign
     *
     * @param paramArrayOfString The texts you want set
     */
    void setSignText(@NotNull List<ComponentPackage> paramArrayOfString);

    /**
     * Update shop data to database
     */
    void update();

    /**
     * Get shop's item durability, if have.
     *
     * @return Shop's item durability
     */
    short getDurability();

    /**
     * Get shop item's ItemStack
     *
     * @return The shop's ItemStack
     */
    @NotNull
    ItemStack getItem();

    /**
     * Set shop item's ItemStack
     *
     * @param item ItemStack to set
     */
    void setItem(@NotNull ItemStack item);

    /**
     * Refresh shop sign and display item
     */
    void refresh();

    /**
     * Get shop's location
     *
     * @return Shop's location
     */
    @NotNull
    Location getLocation();

    /**
     * Return this shop's moderators
     *
     * @return Shop moderators
     */
    @NotNull
    ShopModerator getModerator();

    /**
     * Set new shop's moderators
     *
     * @param shopModerator New moderators team you want set
     */
    void setModerator(@NotNull ShopModerator shopModerator);

    /**
     * Get shop's owner UUID
     *
     * @return Shop's owner UUID, can use Bukkit.getOfflinePlayer to convert to the OfflinePlayer.
     */
    @NotNull
    UUID getOwner();

    /**
     * Set new owner to the shop's owner
     *
     * @param paramString New owner UUID
     */
    void setOwner(@NotNull UUID paramString);

    /**
     * Get shop's price
     *
     * @return Price
     */
    double getPrice();

    /**
     * Set shop's new price
     *
     * @param paramDouble New price
     */
    void setPrice(double paramDouble);

    /**
     * Get shop remaining space.
     *
     * @return Remaining space.
     */
    int getRemainingSpace();

    /**
     * Get shop remaining stock.
     *
     * @return Remaining stock.
     */
    int getRemainingStock();

    /**
     * Get shop type
     *
     * @return shop type
     */
    @NotNull
    ShopType getShopType();

    /**
     * Set new shop type for this shop
     *
     * @param paramShopType New shop type
     */
    void setShopType(@NotNull ShopType paramShopType);

    /**
     * Get shop signs, may have multi signs
     *
     * @return Signs for the shop
     */
    @NotNull
    List<Sign> getSigns();

    /**
     * Directly get all staffs.
     *
     * @return staffs
     */
    @NotNull
    List<UUID> getStaffs();

    /**
     * Get shop is or not in buying mode
     *
     * @return yes or no
     */
    boolean isBuying();

    /**
     * Get this container shop is loaded or unloaded.
     *
     * @return Loaded
     */
    boolean isLoaded();

    /**
     * Get shop is or not in selling mode
     *
     * @return yes or no
     */
    boolean isSelling();

    /**
     * Get shop is or not in Unlimited Mode (Admin Shop)
     *
     * @return yes or not
     */
    boolean isUnlimited();

    /**
     * Set shop is or not Unlimited Mode (Admin Shop)
     *
     * @param paramBoolean status
     */
    void setUnlimited(boolean paramBoolean);

    /**
     * Whether Shop is valid
     *
     * @return status
     */
    boolean isValid();

    /**
     * Whether Shop is deleted
     *
     * @return status
     */
    boolean isDeleted();

    /**
     * Get the shop display entity
     *
     * @return The entity for shop display.
     */
    @Nullable
    AbstractDisplayItem getDisplay();

    /**
     * Gets if shop is dirty
     * (so shop will be save)
     *
     * @return Is dirty
     */
    boolean isDirty();

    /**
     * Sets dirty status
     *
     * @param isDirty Shop is dirty
     */
    void setDirty(boolean isDirty);

    /**
     * Sets shop is dirty
     */
    void setDirty();


    /**
     * Save the plugin extra data to Json format
     *
     * @return The json string
     */
    @NotNull
    String saveExtraToYaml();

    /**
     * Getting ConfigurationSection (extra data) instance of your plugin namespace)
     *
     * @param plugin The plugin and plugin name will used for namespace
     * @return ExtraSection, save it through Shop#setExtra. If you don't save it, it may randomly loose or save
     */
    @NotNull
    ConfigurationSection getExtra(@NotNull Plugin plugin);

    /**
     * Save the extra data to the shop.
     *
     * @param plugin Plugin instace
     * @param data   The data table
     */
    void setExtra(@NotNull Plugin plugin, @NotNull ConfigurationSection data);

    /**
     * Gets shop status is stacking shop
     *
     * @return The shop stacking status
     */
    boolean isStackingShop();

    /**
     * WARNING: This UUID will changed after plugin reload, shop reload or server restart
     * DO NOT USE IT TO STORE DATA!
     *
     * @return Random UUID
     */
    @NotNull UUID getRuntimeRandomUniqueId();

    /**
     * Gets the currency that shop use
     *
     * @return The currency name
     */
    @Nullable
    String getCurrency();

    /**
     * Sets the currency that shop use
     *
     * @param currency The currency name; null to use default currency
     */
    void setCurrency(@Nullable String currency);

    /**
     * open a preview for shop item
     *
     * @param player The viewer
     */
    void openPreview(@NotNull Player player);

    /**
     * Returns the current cached isLeftShop state of the Shop
     *
     * @return if the shop is a left shop
     */
    boolean isLeftShop();

    /**
     * Returns the current cached isRealDouble state of the Shop
     *
     * @return if the shop is a RealDouble
     */
    boolean isRealDouble();

    /**
     * Updates the attachedShop variable to reflect the currently attached shop, if any.
     * Also updates the left shop status.
     */
    void updateAttachedShop();

    /**
     * Returns the attached shop object if any, otherwise null.
     *
     * @return Shop or null
     */
    Shop getAttachedShop();

    /**
     * Getting ShopInfoStorage that you can use for storage the shop data
     *
     * @return ShopInfoStorage
     */
    ShopInfoStorage saveToInfoStorage();

    /**
     * Getting if this shop has been disabled the display
     *
     * @return Does display has been disabled
     */
    boolean isDisableDisplay();

    /**
     * Set the display disable state
     *
     * @param disabled Has been disabled
     */
    void setDisableDisplay(boolean disabled);

    /**
     * Getting the shop tax account for using, it can be specific uuid or general tax account
     *
     * @return Shop Tax Account or fallback to general tax account
     */
    @Nullable
    UUID getTaxAccount();

    /**
     * Sets shop taxAccount
     *
     * @param taxAccount tax account, null to use general tax account
     */
    void setTaxAccount(@Nullable UUID taxAccount);

    /**
     * Getting the shop tax account, it can be specific uuid or general tax account
     *
     * @return Shop Tax Account, null if use general tax account
     */

    @Nullable
    UUID getTaxAccountActual();

    /**
     * Claim a sign as shop sign (modern method)
     *
     * @param sign The shop sign
     */
    void claimShopSign(@NotNull Sign sign);

    /**
     * Gets the shop Inventory
     * @return Inventory
     */
    @Nullable
    InventoryWrapper getInventory();

    /**
     * Checks if a Sign is a ShopSign
     *
     * @param sign Target sign
     * @return Is shop info sign
     */
    default boolean isShopSign(@NotNull Sign sign) {
        // Check for new shop sign
        String[] lines = sign.getLines();
        // Can be claim
        if (lines[0].isEmpty() && lines[1].isEmpty() && lines[2].isEmpty() && lines[3].isEmpty()) {
            return true;
        }

        // Check for exists shop sign (modern)
        ShopSignStorage shopSignStorage = sign.getPersistentDataContainer().get(SHOP_NAMESPACED_KEY, ShopSignPersistentDataType.INSTANCE);
        if (shopSignStorage != null) {
            return shopSignStorage.equals(getLocation().getWorld().getName(), getLocation().getBlockX(), getLocation().getBlockY(), getLocation().getBlockZ());
        }

        // Check for exists shop sign (legacy upgrade)
        //Check if attached with the shop block
        if (!isAttached(sign.getBlock())) {
            return false;
        }
        //Check sign content
        if (lines[1].startsWith(SHOP_SIGN_PATTERN)) {
            return true;
        } else {
            if (!QuickShop.getInstance().getConfig().getBoolean("legacy-updater.shop-sign", false)) {
                return false;
            }
            String header = lines[0];
            TextManager textManager = QuickShop.getInstance().text();
            String ownerName = this.ownerName(true);
            //Raw text matching
            String adminShopHeader = textManager.of("signs.header", textManager.of("admin-shop").forLocale()).forLocale();
            String userShopHeader = textManager.of("signs.header", ownerName).forLocale();
            if (header.contains(adminShopHeader) || header.contains(userShopHeader)) {
                return true;
                //TEXT SIGN
                //continue
            } else {
                //Try no color matching
                //arg[0] is name, arg[1] is color code
                adminShopHeader = textManager.of("signs.header", textManager.of("admin-shop").forLocale(), "").forLocale();
                userShopHeader = textManager.of("signs.header", ownerName, "").forLocale();
                adminShopHeader = ChatColor.stripColor(adminShopHeader);
                userShopHeader = ChatColor.stripColor(userShopHeader);
                return header.contains(adminShopHeader) || header.contains(userShopHeader);
            }
        }
    }

    /**
     * Check if this shop is free shop
     *
     * @return Free Shop
     */
    boolean isFreeShop();

    /**
     * If this shop is always counting space or stocks, even is unlimited
     */
    default boolean isAlwaysCountingContainer() {
        return false;
    }

    /**
     * Set if this shop is always a limited shop, even is unlimited
     *
     * @param value is always counting space or stocks
     */
    default void setAlwaysCountingContainer(boolean value) {
        //For back-ward compatibility
        throw new UnsupportedOperationException("setAlwaysCountingContainer is not implemented");
    }

    /**
     * Gets the InventoryWrapper provider name (the plugin name who register it), usually is QuickShop
     * @return InventoryWrapper
     */
    @NotNull
    String getInventoryWrapperProvider();

    /**
     * Gets the symbol link that created by InventoryWrapperManager
     * @return InventoryWrapper
     */
    @NotNull
    String saveToSymbolLink();

    /**
     * Sets shop's Inventory to a new Inventory
     * @param wrapper Inventory
     * @param manager InventoryWrapperManager
     */
    void setInventory(@NotNull InventoryWrapper wrapper, @NotNull InventoryWrapperManager manager);
}
