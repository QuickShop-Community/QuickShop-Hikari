package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.economy.Benefit;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A shop
 */
public interface Shop {
    NamespacedKey SHOP_NAMESPACED_KEY = new NamespacedKey(QuickShopAPI.getPluginInstance(), "shopsign");

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
     * @deprecated Use {@link #setPlayerGroup(UUID, String)} to set player to {@link BuiltInShopPermissionGroup#STAFF} instead
     */
    @Deprecated(forRemoval = true, since = "2.0.0.0")
    boolean addStaff(@NotNull UUID player);

    /**
     * Execute buy action for player with x items.
     *
     * @param buyer          The player buying
     * @param buyerInventory The buyer inventory ( may not a player inventory )
     * @param loc2Drop       The location to drops items if player inventory are full
     * @param paramInt       How many buyed?
     * @throws Exception Possible exception thrown if anything wrong.
     */
    void buy(@NotNull UUID buyer, @NotNull InventoryWrapper buyerInventory, @NotNull Location loc2Drop, int paramInt) throws Exception;

    /**
     * Check the display location, and teleport, respawn if needs.
     */
    void checkDisplay();

    /**
     * Claim a sign as shop sign (modern method)
     *
     * @param sign The shop sign
     */
    void claimShopSign(@NotNull Sign sign);

    /**
     * Empty moderators team.
     *
     * @deprecated
     */
    @Deprecated(forRemoval = true, since = "2.0.0.0")
    void clearStaffs();

    /**
     * Remove a staff from moderators
     *
     * @param player Staff
     * @return Success
     * @deprecated Use {@link #setPlayerGroup(UUID, String)} to set player to {@link BuiltInShopPermissionGroup#EVERYONE} instead
     */
    @Deprecated(forRemoval = true, since = "2.0.0.0")
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
     * Get shop's item durability, if have.
     *
     * @return Shop's item durability
     */
    short getDurability();

    /**
     * Getting ConfigurationSection (extra data) instance of your plugin namespace)
     *
     * @param plugin The plugin and plugin name will used for namespace
     * @return ExtraSection, save it through Shop#setExtra. If you don't save it, it may randomly lose or save
     */
    @NotNull
    ConfigurationSection getExtra(@NotNull Plugin plugin);

    /**
     * Gets the shop Inventory
     *
     * @return Inventory
     */
    @Nullable InventoryWrapper getInventory();

    /**
     * Gets the InventoryWrapper provider name (the plugin name who register it), usually is QuickShop
     *
     * @return InventoryWrapper
     */
    @NotNull
    String getInventoryWrapperProvider();

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
     * @deprecated Replaced by {@link #playerAuthorize(UUID, BuiltInShopPermission)} ()}
     */
    @Deprecated(forRemoval = true, since = "2.0.0.0")
    @NotNull
    ShopModerator getModerator();

    /**
     * Set new shop's moderators
     *
     * @param shopModerator New moderators team you want set
     * @deprecated Replaced by {@link #setPlayerGroup(UUID, String)}
     */
    @Deprecated(forRemoval = true, since = "2.0.0.0")
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
     * Gets all player and their group on this shop
     *
     * @return Map of UUID and group
     */
    @NotNull
    Map<UUID, String> getPermissionAudiences();

    /**
     * Gets specific player group on specific shop
     *
     * @param player player
     * @return namespaced group
     */
    @NotNull
    String getPlayerGroup(@NotNull UUID player);

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
     * WARNING: This UUID will changed after plugin reload, shop reload or server restart
     * DO NOT USE IT TO STORE DATA!
     *
     * @return Random UUID
     */
    @NotNull UUID getRuntimeRandomUniqueId();

    /**
     * Gets the Shop ID to identify the shop.
     *
     * @return Shop ID -1 if shop in creating state.
     */
    long getShopId();

    /**
     * Internal Only: Give shop that under id_waiting state an ShopId.
     *
     * @param newId The new shop id, once set will cannot change anymore.
     */
    @ApiStatus.Internal
    void setShopId(long newId);

    /**
     * Gets this shop name that set by player
     *
     * @return Shop name, or null if not set
     */
    @Nullable
    String getShopName();

    /**
     * Sets shop name
     *
     * @param shopName shop name, null to remove currently name
     */
    void setShopName(@Nullable String shopName);

    int getShopStackingAmount();

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
     * @param paramShopType New {@link ShopType}
     */
    void setShopType(@NotNull ShopType paramShopType);

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
    default List<Component> getSignText(@NotNull ProxiedLocale locale) {
        //backward support
        throw new UnsupportedOperationException();
    }

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
     * @deprecated Replaced by {@link #playersCanAuthorize(BuiltInShopPermissionGroup)} with {@link BuiltInShopPermissionGroup#STAFF}
     */
    @NotNull
    @Deprecated(forRemoval = true, since = "2.0.0.0")
    List<UUID> getStaffs();

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
     * Check if shop out of space or out of stock
     *
     * @return true if out of space or out of stock
     */
    boolean inventoryAvailable();

    /**
     * Check shop is or not attacked the target block
     *
     * @param paramBlock Target {@link Block}
     * @return isAttached
     */
    boolean isAttached(@NotNull Block paramBlock);

    /**
     * Get shop is or not in buying mode
     *
     * @return yes or no
     */
    boolean isBuying();

    /**
     * Whether Shop is deleted
     *
     * @return status
     */
    boolean isDeleted();

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
     * Check if this shop is free shop
     *
     * @return Free Shop
     */
    boolean isFreeShop();

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
     * Checks if a Sign is a ShopSign
     *
     * @param sign Target {@link Sign}
     * @return Is shop info sign
     */
    boolean isShopSign(@NotNull Sign sign);

    /**
     * Gets shop status is stacking shop
     *
     * @return The shop stacking status
     */
    boolean isStackingShop();

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
     * Check the target ItemStack is matches with this shop's item.
     *
     * @param paramItemStack Target ItemStack.
     * @return Matches
     */
    boolean matches(@NotNull ItemStack paramItemStack);

    /**
     * Execute codes when player click the shop will did things
     */
    void onClick(@NotNull Player clicker);

    /**
     * Load shop to the world
     */
    void onLoad();

    /**
     * Unload shop from world
     */
    void onUnload();

    /**
     * open a preview for shop item
     *
     * @param player The viewer {@link Player}
     */
    void openPreview(@NotNull Player player);

    /**
     * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
     *
     * @param forceUsername Force returns username of shop
     * @param locale        The locale to parse the message
     * @return owner name
     */
    @NotNull
    Component ownerName(boolean forceUsername, @NotNull ProxiedLocale locale);

    /**
     * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
     *
     * @param locale The locale to parse the message
     * @return owner name
     */
    @NotNull
    Component ownerName(@NotNull ProxiedLocale locale);

    /**
     * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
     *
     * @return owner name
     */
    @NotNull
    Component ownerName();

    /**
     * Check if player have authorized for specific permission on specific shop
     *
     * @param player     player
     * @param namespace  permission namespace
     * @param permission permission
     * @return true if player have authorized
     */
    boolean playerAuthorize(@NotNull UUID player, @NotNull Plugin namespace, @NotNull String permission);

    /**
     * Check if player have authorized for specific permission on specific shop
     *
     * @param player     player
     * @param permission namespaced permission
     * @return true if player have authorized
     */
    boolean playerAuthorize(@NotNull UUID player, @NotNull BuiltInShopPermission permission);

    /**
     * Gets the player list of who can authorize specific permission on this shop
     *
     * @param permission permission
     * @return Collection of UUID
     */
    List<UUID> playersCanAuthorize(@NotNull BuiltInShopPermission permission);

    /**
     * Gets the player list of who can authorize specific group on this shop
     *
     * @param permissionGroup group
     * @return Collection of UUID
     */
    List<UUID> playersCanAuthorize(@NotNull BuiltInShopPermissionGroup permissionGroup);

    /**
     * Gets the player list of who can authorize specific permission on this shop
     *
     * @param permission raw permission
     * @param plugin     namespace of permission
     * @return Collection of UUID
     */
    List<UUID> playersCanAuthorize(@NotNull Plugin plugin, @NotNull String permission);

    /**
     * Refresh shop sign and display item
     */
    @Deprecated(forRemoval = true)
    default void refresh() {
    }

    /**
     * Remove x ItemStack from the shop inventory
     *
     * @param paramItemStack Want removed ItemStack
     * @param paramInt       Want remove how many
     */
    void remove(@NotNull ItemStack paramItemStack, int paramInt);

    /**
     * Save the plugin extra data to Json format
     *
     * @return The json string
     */
    @NotNull
    String saveExtraToYaml();

    /**
     * Getting ShopInfoStorage that you can use for storage the shop data
     *
     * @return ShopInfoStorage
     */
    ShopInfoStorage saveToInfoStorage();

    /**
     * Gets the symbol link that created by InventoryWrapperManager
     *
     * @return InventoryWrapper
     */
    @NotNull
    String saveToSymbolLink();

    /**
     * Execute sell action for player with x items.
     *
     * @param seller          Seller
     * @param sellerInventory Seller's inventory ( may not a player inventory )
     * @param loc2Drop        The location to be drop if buyer inventory full ( if player enter a number that < 0, it will turn to buying item)
     * @param paramInt        How many sold?
     * @throws Exception Possible exception thrown if anything wrong.
     */
    void sell(@NotNull UUID seller, @NotNull InventoryWrapper sellerInventory, @NotNull Location loc2Drop, int paramInt) throws Exception;

    /**
     * Sets shop is dirty
     */
    void setDirty();

    /**
     * Save the extra data to the shop.
     *
     * @param plugin Plugin instace
     * @param data   The data table
     */
    void setExtra(@NotNull Plugin plugin, @NotNull ConfigurationSection data);

    void setInventory(@NotNull InventoryWrapper wrapper, @NotNull InventoryWrapperManager manager);

    /**
     * Sets specific player permission on specfic shop
     *
     * @param player player
     * @param group  namespaced group name
     */
    void setPlayerGroup(@NotNull UUID player, @Nullable String group);

    /**
     * Sets specific player permission on specfic shop
     *
     * @param player player
     * @param group  group
     */
    void setPlayerGroup(@NotNull UUID player, @Nullable BuiltInShopPermissionGroup group);

    /**
     * Generate new sign texts on shop's sign.
     */
    void setSignText();

    /**
     * Set texts on shop's sign
     *
     * @param paramArrayOfString The texts you want set
     */
    void setSignText(@NotNull List<Component> paramArrayOfString);

    void setSignText(@NotNull ProxiedLocale locale);

    /**
     * Update shop data to database
     */
    @NotNull
    CompletableFuture<Void> update();

    /**
     * Gets the benefit in this shop
     */
    @NotNull
    Benefit getShopBenefit();

    /**
     * Sets the benefit in this shop
     */
    void setShopBenefit(@NotNull Benefit benefit);

}
