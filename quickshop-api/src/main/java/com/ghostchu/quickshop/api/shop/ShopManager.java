package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.economy.AbstractEconomy;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * The manager that managing shops
 */
@SuppressWarnings("ALL")
public interface ShopManager {

    /**
     * Handle the player buying
     *
     * @param buyer          The player buying
     * @param buyerInventory The inventory of the player buying
     * @param eco            The economy
     * @param info           The info of the shop
     * @param shop           The shop
     * @param amount         The amount of the item/stack
     */
    void actionBuying(
            @NotNull UUID buyer,
            @NotNull InventoryWrapper buyerInventory,
            @NotNull AbstractEconomy eco,
            @NotNull Info info,
            @NotNull Shop shop,
            int amount);

    /**
     * Handle the player shop creating
     *
     * @param p       The player
     * @param info    The info of the shop
     * @param message The message of the shop
     */
    void actionCreate(@NotNull Player p, Info info, @NotNull String message);

    /**
     * Handle the player shop selling
     *
     * @param seller          The player selling
     * @param sellerInventory The inventory of the player selling
     * @param eco             The economy
     * @param info            The info of the shop
     * @param shop            The shop
     * @param amount          The amount of the item/stack
     */
    void actionSelling(
            @NotNull UUID seller,
            @NotNull InventoryWrapper sellerInventory,
            @NotNull AbstractEconomy eco,
            @NotNull Info info,
            @NotNull Shop shop,
            int amount);

    /**
     * Adds a shop to the world. Does NOT require the chunk or world to be loaded Call shop.onLoad
     * by yourself
     *
     * @param world The name of the world
     * @param shop  The shop to add
     */
    void addShop(@NotNull String world, @NotNull Shop shop);

    void bakeShopRuntimeRandomUniqueIdCache(@NotNull Shop shop);

    /**
     * Removes all shops from memory and the world. Does not delete them from the database. Call
     * this on plugin disable ONLY.
     */
    void clear();

    /**
     * Create a shop use Shop and Info object.
     *
     * @param shop               The shop object
     * @param signBlock          The sign block
     * @param bypassProtectCheck Should bypass protection check
     * @throws IllegalStateException If the shop owner offline
     */
    void createShop(@NotNull Shop shop, @Nullable Block signBlock, boolean bypassProtectCheck) throws IllegalStateException;

    /**
     * Format the price use formatter
     *
     * @param d        price
     * @param currency currency
     * @param world    shop world
     * @return formated price
     */
    @NotNull String format(double d, @NotNull World world, @Nullable String currency);

    /**
     * Format the price use formatter
     *
     * @param d    price
     * @param shop The shop
     * @return formated price
     */
    @NotNull String format(double d, @NotNull Shop shop);

    /**
     * @return Returns the Map. Info contains what their last question etc was.
     * @deprecated Use getInteractiveManager() instead.
     */
    @Deprecated(forRemoval = true)
    @NotNull Map<UUID, Info> getActions();

    /**
     * Returns all shops in the whole database, include unloaded.
     *
     * <p>Make sure you have caching this, because this need a while to get all shops
     *
     * @return All shop in the database
     */
    @NotNull List<Shop> getAllShops();

    /**
     * Get all loaded shops.
     *
     * @return All loaded shops.
     */
    @NotNull Set<Shop> getLoadedShops();

    /**
     * Get a players all shops.
     *
     * <p>Make sure you have caching this, because this need a while to get player's all shops
     *
     * @param playerUUID The player's uuid.
     * @return The list have this player's all shops.
     */
    @NotNull List<Shop> getPlayerAllShops(@NotNull UUID playerUUID);

    /**
     * Getting the Shop Price Limiter
     *
     * @return The shop price limiter
     */
    @NotNull
    PriceLimiter getPriceLimiter();

    /**
     * Gets a shop by shop Id
     *
     * @return The shop object
     */
    @Nullable Shop getShop(long shopId);

    /**
     * Gets a shop in a specific location
     * ATTENTION: This not include attached shops (double-chest)
     *
     * @param loc The location to get the shop from
     * @return The shop at that location
     */
    @Nullable Shop getShop(@NotNull Location loc);

    /**
     * Gets a shop in a specific location
     * ATTENTION: This not include attached shops (double-chest)
     *
     * @param loc                  The location to get the shop from
     * @param skipShopableChecking whether to check is shopable
     * @return The shop at that location
     */
    @Nullable Shop getShop(@NotNull Location loc, boolean skipShopableChecking);


    @Nullable Shop getShopFromRuntimeRandomUniqueId(@NotNull UUID runtimeRandomUniqueId);

    @Nullable Shop getShopFromRuntimeRandomUniqueId(@NotNull UUID runtimeRandomUniqueId, boolean includeInvalid);

    /**
     * Gets a shop in a specific location Include the attached shop, e.g DoubleChest shop.
     *
     * @param loc The location to get the shop from
     * @return The shop at that location
     */
    @Nullable Shop getShopIncludeAttached(@Nullable Location loc);

    /**
     * Gets a shop in a specific location Include the attached shop, e.g DoubleChest shop.
     *
     * @param loc      The location to get the shop from
     * @param useCache whether to use cache
     * @return The shop at that location
     */
    @Nullable Shop getShopIncludeAttached(@Nullable Location loc, boolean useCache);

    /**
     * Returns a new shop iterator object, allowing iteration over shops easily, instead of sorting
     * through a 3D map.
     *
     * @return a new shop iterator object.
     */
    @NotNull Iterator<Shop> getShopIterator();

    /**
     * Returns a map of World - Chunk - Shop
     *
     * @return a map of World - Chunk - Shop
     */
    @NotNull Map<String, Map<ShopChunk, Map<Location, Shop>>> getShops();

    /**
     * Returns a map of Shops
     *
     * @param c The chunk to search. Referencing doesn't matter, only coordinates and world are
     *          used.
     * @return Shops
     */
    @Nullable Map<Location, Shop> getShops(@NotNull Chunk c);

    /**
     * Gets the shop at the world and specific chunk.
     *
     * @param world  The world to get the shop from
     * @param chunkX The chunk x coordinate
     * @param chunkZ The chunk z coordinate
     * @return The shop at the world and specific chunk.
     */
    @Nullable Map<Location, Shop> getShops(@NotNull String world, int chunkX, int chunkZ);

    /**
     * Returns a map of Chunk - Shop
     *
     * @param world The name of the world (case sensitive) to get the list of shops from
     * @return a map of Chunk - Shop
     */
    @Nullable Map<ShopChunk, Map<Location, Shop>> getShops(@NotNull String world);

    /**
     * Get the all shops in the world.
     *
     * @param world The world you want get the shops.
     * @return The list have this world all shops
     */
    @NotNull List<Shop> getShopsInWorld(@NotNull World world);

    @Deprecated
    double getTax(@NotNull Shop shop, @NotNull Player p);

    /**
     * Get the tax of the shop
     *
     * @param shop The shop
     * @param p    The player
     * @return The tax of the shop
     */
    double getTax(@NotNull Shop shop, @NotNull UUID p);

    void handleChat(@NotNull Player p, @NotNull String msg);

    /**
     * Checks if player reached the limit of shops
     *
     * @param p The player to check
     * @return True if they're reached the limit.
     */
    boolean isReachedLimit(@NotNull Player p);

    /**
     * Load shop method for loading shop into mapping, so getShops method will can find it. It also
     * effects a lots of feature, make sure load it after create it.
     *
     * @param world The world the shop is in
     * @param shop  The shop to load
     */
    void loadShop(@NotNull String world, @NotNull Shop shop);

    /**
     * Change the owner to unlimited shop owner.
     * It defined in configuration.
     */
    void migrateOwnerToUnlimitedShopOwner(Shop shop);

    /**
     * Register shop to memory and database.
     *
     * @param info The info object
     * @return True if the shop was register successfully.
     */
    void registerShop(@NotNull Shop shop);

    /**
     * Removes a shop from the world. Does NOT remove it from the database. * REQUIRES * the world
     * to be loaded Call shop.onUnload by your self.
     *
     * @param shop The shop to remove
     */
    void removeShop(@NotNull Shop shop);

    /**
     * Send a purchaseSuccess message for a player.
     *
     * @param purchaser Target player
     * @param shop      Target shop
     * @param amount    Trading item amounts.
     */
    @ApiStatus.Experimental
    void sendPurchaseSuccess(@NotNull UUID purchaser, @NotNull Shop shop, int amount, double total, double tax);

    /**
     * Send a sellSuccess message for a player.
     *
     * @param seller Target player
     * @param shop   Target shop
     * @param amount Trading item amounts.
     */
    @ApiStatus.Experimental
    void sendSellSuccess(@NotNull UUID seller, @NotNull Shop shop, int amount, double total, double tax);

    /**
     * Send a shop infomation to a player.
     *
     * @param p    Target player
     * @param shop The shop
     */
    @ApiStatus.Experimental
    void sendShopInfo(@NotNull Player p, @NotNull Shop shop);

    /**
     * Check if shop is not valided for specific player
     *
     * @param uuid The uuid of the player
     * @param info The info of the shop
     * @param shop The shop
     * @return If the shop is not valided for the player
     */
    boolean shopIsNotValid(@NotNull UUID uuid, @NotNull Info info, @NotNull Shop shop);

    /**
     * Gets the InteractiveManager (which former as known getActions())
     *
     * @return InteractiveManager instance
     */
    @NotNull
    ShopManager.InteractiveManager getInteractiveManager();

    @NotNull CompletableFuture<@NotNull List<Shop>> queryTaggedShops(@NotNull UUID tagger, @NotNull String tag);

    CompletableFuture<@Nullable Integer> clearShopTags(@NotNull UUID tagger, @NotNull Shop shop);

    CompletableFuture<@Nullable Integer> clearTagFromShops(@NotNull UUID tagger, @NotNull String tag);

    CompletableFuture<@Nullable Integer> removeTag(@NotNull UUID tagger, @NotNull Shop shop, @NotNull String tag);

    CompletableFuture<@Nullable Integer> tagShop(@NotNull UUID tagger, @NotNull Shop shop, @NotNull String tag);

    @NotNull List<String> listTags(@NotNull UUID tagger);

    /**
     * An getActions() alternative.
     */
    public static interface InteractiveManager {
        public int size();

        public boolean isEmpty();

        public Info put(UUID uuid, Info info);

        @Nullable
        public Info remove(UUID uuid);

        public void reset();

        public Info get(UUID uuid);

        public boolean containsKey(UUID uuid);

        public boolean containsValue(Info info);
    }
}
