package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.economy.EconomyProvider;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.cache.ShopInventoryCountCache;
import com.ghostchu.quickshop.api.shop.state.ShopState;
import com.ghostchu.quickshop.api.shop.tax.TaxManager;
import com.ghostchu.quickshop.api.shop.trading.TradeService;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The manager that managing shops
 */
@SuppressWarnings("ALL")
public interface ShopManager {

  /**
   * Provides an instance of {@code IShopLayoutProvider} responsible for managing shop layouts.
   *
   * @return an implementation of {@code IShopLayoutProvider} that handles the shop layout configuration.
   */
  IShopLayoutProvider shopLayoutProvider();

  /**
   * Retrieves an instance of the TaxManager class, responsible for handling tax-related computations
   * and operations within the application.
   *
   * @return an instance of TaxManager that manages tax calculations and logic.
   */
  TaxManager taxManager();

  /**
   * Retrieves the TradeService associated with the EconomyManager.
   *
   * @return A non-null instance of TradeService, which provides functionality for executing
   *         and previewing trade operations such as buying from and selling to shops.
   */
  @NotNull
  TradeService tradeService();

  /**
   * Sets the shop layout provider to customize the layout of the shop.
   *
   * @param provider the instance of IShopLayoutProvider that defines the layout of the shop
   */
  void shopLayoutProvider(final IShopLayoutProvider provider);

  /**
   * Retrieves a map of cooldown information where the keys represent unique identifiers (UUIDs),
   * and the values represent the corresponding timestamps indicating when the cooldown expires.
   *
   * @return A map mapping UUIDs to their cooldown expiration timestamps in milliseconds.
   */
  Map<UUID, Long> findCooldown();

  /**
   * Retrieves a map containing shop types.
   *
   * @return a map where the key is an integer representing the shop type ID,
   * and the value is an object implementing the IShopType interface,
   * which provides details about a shop type.
   */
  @NotNull Map<Integer, IShopType> shopTypes();

  /**
   * Adds a shop type to the internal collection of shop types.
   *
   * @param type the shop type to be added. It must contain a valid ID and properties.
   */
  default void addShopType(IShopType type) {
    shopTypes().put(type.id(), type);
  }

  /**
   * Removes the shop type identified by the specified ID from the collection of shop types.
   *
   * @param id the unique identifier of the shop type to be removed
   */
  default void removeShopType(final int id) {
    shopTypes().remove(id);
  }

  /**
   * Retrieves the shop type corresponding to the given identifier.
   *
   * @param id the identifier of the shop type to retrieve
   * @return an Optional containing the shop type if found, otherwise an empty Optional
   */
  default Optional<IShopType> shopType(final int id) {
    return Optional.ofNullable(shopTypes().get(id));
  }

  /**
   * Retrieves the shop type associated with the specified ID.
   * If no shop type is found, returns a default shop type.
   *
   * @param id the identifier for the desired shop type
   * @return the shop type associated with the given ID, or a default shop type if none exists
   */
  @NotNull IShopType shopTypeOrDefault(final int id);

  /**
   * Retrieves an optional shop type based on the provided identifier.
   *
   * @param identifier the unique identifier of the shop type to search for
   * @return an {@code Optional} containing the matching shop type if found, otherwise an empty {@code Optional}
   */
  default Optional<IShopType> shopType(final String identifier) {
    return shopTypes().values().stream().filter(type -> type.identifier().equalsIgnoreCase(identifier)).findFirst();
  }

  /**
   * Retrieves the shop type associated with the given identifier, or returns a default
   * shop type if no match is found.
   *
   * @param identifier the unique identifier for the shop type to retrieve
   * @return the corresponding IShopType if found, or a default IShopType if no match exists
   */
  @NotNull IShopType shopTypeOrDefault(final String identifier);

  /**
   * Retrieves a map of shop states where the keys are shop identifiers and the values are the corresponding shop states.
   *
   * @return a non-null map containing shop identifiers as keys and their corresponding {@link ShopState} objects as values.
   */
  @NotNull Map<String, ShopState> shopStates();

  /**
   * Adds a shop state to the collection by associating its identifier with the given ShopState instance.
   *
   * @param type the ShopState object to be added, which contains the identifier and related state information
   */
  default void addShopState(final ShopState type) {
    shopStates().put(type.identifier().toLowerCase(Locale.ROOT), type);
  }

  /**
   * Removes the shop state associated with the given identifier.
   *
   * @param identifier the unique identifier of the shop state to be removed
   */
  default void removeShopState(final String identifier) {
    shopStates().remove(identifier);
  }


  /**
   * Retrieves the shop state associated with the given identifier.
   *
   * @param identifier the unique identifier of the shop state to retrieve
   * @return an {@code Optional} containing the {@code ShopState} if found, or an empty {@code Optional} if no match is found
   */
  default Optional<ShopState> shopState(final String identifier) {
    return Optional.ofNullable(shopStates().get(identifier.toLowerCase(Locale.ROOT)));
  }

  /**
   * Retrieves the ShopState associated with the specified identifier.
   * If no ShopState is found for the identifier, a default ShopState is returned.
   *
   * @param identifier the unique identifier for the shop state to retrieve
   * @return the ShopState associated with the identifier, or a default ShopState if not found
   */
  @NotNull ShopState shopStateOrDefault(final String identifier);

  /**
   * Handle the player buying
   *
   * @param buyer          The player buying
   * @param buyerInventory The inventory of the player buying
   * @param eco            The economy
   * @param info           The info of the shop
   * @param shop           The shop
   * @param amount         The amount of the item/stack
   *
   * @return If the transaction was successfull
   */
  boolean actionBuying(
          @NotNull Player buyer,
          @NotNull InventoryWrapper buyerInventory,
          @NotNull EconomyProvider eco,
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
   *
   * @return If the transaction was successfull
   */
  boolean actionSelling(
          @NotNull Player seller,
          @NotNull InventoryWrapper sellerInventory,
          @NotNull EconomyProvider eco,
          @NotNull Info info,
          @NotNull Shop shop,
          int amount);

  void bakeShopRuntimeRandomUniqueIdCache(@NotNull Shop shop);

  /**
   * Removes all shops from memory and the world. Does not delete them from the database. Call this
   * on plugin disable ONLY.
   */
  void clear();

  /**
   * Create a shop use Shop and Info object.
   *
   * @param shop               The shop object
   * @param signBlock          The sign block
   * @param bypassProtectCheck Should bypass protection check
   *
   * @throws IllegalStateException If the shop owner offline
   */
  void createShop(@NotNull Shop shop, @Nullable Block signBlock, boolean bypassProtectCheck) throws IllegalStateException;

  /**
   * Format the price use formatter
   *
   * @param d        price
   * @param currency currency
   * @param world    shop world
   *
   * @return formated price
   */
  @NotNull
  String format(double d, @NotNull World world, @Nullable String currency);

  /**
   * Format the price use formatter
   *
   * @param d    price
   * @param shop The shop
   *
   * @return formated price
   */
  @NotNull
  String format(double d, @NotNull Shop shop);

  /**
   * Returns all shops in the whole database, include unloaded.
   *
   * <p>Make sure you have caching this, because this need a while to get all shops
   *
   * @return All shop in the database
   */
  @NotNull
  List<Shop> getAllShops();

  /**
   * Get all loaded shops.
   *
   * @return All loaded shops.
   */
  @NotNull
  Set<Shop> getLoadedShops();

  /**
   * Get a players all shops.
   *
   * <p>Make sure you have caching this, because this need a while to get player's all shops
   *
   * @param playerUUID The player's uuid.
   *
   * @return The list have this player's all shops.
   */
  @NotNull
  List<Shop> getAllShops(@NotNull QUser playerUUID);

  /**
   * Get a players all shops.
   *
   * <p>Make sure you have caching this, because this need a while to get player's all shops
   *
   * @param playerUUID The player's uuid.
   *
   * @return The list have this player's all shops.
   */
  @NotNull
  List<Shop> getAllShops(@NotNull UUID playerUUID);

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
  @Nullable
  Shop getShop(long shopId);

  /**
   * Gets a shop in a specific location ATTENTION: This not include attached shops (double-chest)
   *
   * @param loc The location to get the shop from
   *
   * @return The shop at that location
   */
  @Nullable
  Shop getShop(@NotNull Location loc);

  /**
   * Gets a shop in a specific location but via cache ATTENTION: This not include attached shops
   * (double-chest)
   *
   * @param loc The location to get the shop from
   *
   * @return The shop at that location but via cache
   */
  @Nullable
  Shop getShopViaCache(@NotNull Location loc);

  /**
   * Gets a shop in a specific location ATTENTION: This not include attached shops (double-chest)
   *
   * @param loc                  The location to get the shop from
   * @param skipShopableChecking whether to check is shopable
   *
   * @return The shop at that location
   */
  @Nullable
  Shop getShop(@NotNull Location loc, boolean skipShopableChecking);


  @Nullable
  Shop getShopFromRuntimeRandomUniqueId(@NotNull UUID runtimeRandomUniqueId);

  @Nullable
  Shop getShopFromRuntimeRandomUniqueId(@NotNull UUID runtimeRandomUniqueId, boolean includeInvalid);

  /**
   * Gets a shop in a specific location Include the attached shop, e.g DoubleChest shop.
   *
   * @param loc The location to get the shop from
   *
   * @return The shop at that location
   */
  @Nullable
  Shop getShopIncludeAttached(@Nullable Location loc);

  /**
   * Gets a shop in a specific location Include the attached shop, e.g DoubleChest shop. but via
   * cache
   *
   * @param loc The location to get the shop from
   *
   * @return The shop at that location but via cache
   */
  @Nullable
  Shop getShopIncludeAttachedViaCache(@Nullable Location loc);


  /**
   * Returns a new shop iterator object, allowing iteration over shops easily, instead of sorting
   * through a 3D map.
   *
   * @return a new shop iterator object.
   */
  @NotNull
  Iterator<Shop> getShopIterator();

  /**
   * Returns a map of World - Chunk - Shop
   *
   * @return a map of World - Chunk - Shop
   */
  @NotNull
  Map<String, Map<ShopChunk, Map<Location, Shop>>> getShops();

  /**
   * Returns a map of Shops
   *
   * @param c The chunk to search. Referencing doesn't matter, only coordinates and world are used.
   *
   * @return Shops
   */
  @NotNull
  Map<Location, Shop> getShops(@NotNull Chunk c);

  /**
   * Gets the shop at the world and specific chunk.
   *
   * @param world  The world to get the shop from
   * @param chunkX The chunk x coordinate
   * @param chunkZ The chunk z coordinate
   *
   * @return The shop at the world and specific chunk.
   */
  @NotNull
  Map<Location, Shop> getShops(@NotNull String world, int chunkX, int chunkZ);

  /**
   * Gets the shop at the world and specific chunk.
   *
   * @param shopChunk The shop chunk
   *
   * @return The shop at the world and specific chunk.
   */
  @NotNull
  Map<Location, Shop> getShops(@NotNull ShopChunk shopChunk);

  /**
   * Returns a map of Chunk - Shop
   *
   * @param world The name of the world (case sensitive) to get the list of shops from
   *
   * @return a map of Chunk - Shop
   */
  @NotNull
  Map<ShopChunk, Map<Location, Shop>> getShops(@NotNull String world);

  /**
   * Get the all shops in the world.
   *
   * @param world The world you want get the shops.
   *
   * @return The list have this world all shops
   */
  @NotNull
  List<Shop> getShopsInWorld(@NotNull World world);

  /**
   * Get the all shops in the world.
   *
   * @param worldName The world you want get the shops.
   *
   * @return The list have this world all shops
   */
  @NotNull
  List<Shop> getShopsInWorld(@NotNull String worldName);


  /**
   * Get the tax of the shop
   *
   * @param shop The shop
   * @param p    The player
   *
   * @return The tax of the shop
   * @deprecated no longer apart of the enhanced tax system
   */
  @Deprecated(since = "6.2.0.11", forRemoval = true)
  default double getTax(@NotNull Shop shop, @NotNull QUser p) {
    return 0.0;
  }

  void handleChat(@NotNull Player player, @NotNull String msg);

  /**
   * Checks if player reached the limit of shops
   *
   * @param p       The player to check
   * @param message Should a message be sent to the player if the limit is reached
   *
   * @return True if they're reached the limit.
   */
  boolean isReachedLimit(@NotNull QUser p, boolean message);

  /**
   * Load shop method for loading shop into mapping, so getShops method will can find it. It also
   * effects a lots of feature, make sure load it after create it.
   *
   * @param world The world the shop is in
   * @param shop  The shop to load
   */
  void loadShop(@NotNull Shop shop);

  /**
   * Load shop method for loading shop into mapping, so getShops method will can find it. It also
   * effects a lots of feature, make sure load it after create it.
   *
   * @param world The world the shop is in
   * @param shop  The shop to load
   */
  void unloadShop(@NotNull Shop shop);

  /**
   * Load shop method for loading shop into mapping, so getShops method will can find it. It also
   * effects a lots of feature, make sure load it after create it.
   *
   * @param world          The world the shop is in
   * @param shop           The shop to load
   * @param chunkUnloading If unloadShop called caused by chunk unloading, when this is true,
   *                       QuickShop will try avoid any main-thread opreations to avoid
   *                       load-unload-load loop
   */
  void unloadShop(@NotNull Shop shop, boolean chunkUnloading);

  /**
   * Change the owner to unlimited shop owner. It defined in configuration.
   */
  void migrateOwnerToUnlimitedShopOwner(Shop shop);

  /**
   * Register shop to database.
   *
   * @param info The info object
   *
   * @return True if the shop was register successfully.
   */
  CompletableFuture<?> registerShop(@NotNull Shop shop, boolean persist);

  /**
   * Unregister a shop from database.
   *
   * @param info The info object
   *
   * @return True if the shop was unregister successfully.
   */
  CompletableFuture<?> unregisterShop(@NotNull Shop shop, boolean persist);

  /**
   * Send a purchaseSuccess message for a player.
   *
   * @param purchaser Target player
   * @param shop      Target shop
   * @param amount    Trading item amounts.
   */
  @ApiStatus.Experimental
  void sendPurchaseSuccess(@NotNull QUser purchaser, @NotNull Shop shop, int amount, double total, double tax);

  /**
   * Send a sellSuccess message for a player.
   *
   * @param seller Target player
   * @param shop   Target shop
   * @param amount Trading item amounts.
   */
  @ApiStatus.Experimental
  void sendSellSuccess(@NotNull QUser seller, @NotNull Shop shop, int amount, double total, double tax);

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
   *
   * @return If the shop is not valided for the player
   */
  boolean shopIsNotValid(@NotNull QUser uuid, @NotNull Info info, @NotNull Shop shop);

  /**
   * Gets the InteractiveManager (which former as known getActions())
   *
   * @return InteractiveManager instance
   */
  @NotNull
  ShopManager.InteractiveManager getInteractiveManager();

  @NotNull
  BlockState makeShopSign(@NotNull Block container, @NotNull Block signBlock, @Nullable Material signMaterial);

  @NotNull
  CompletableFuture<@NotNull List<Shop>> queryTaggedShops(@NotNull UUID tagger, @NotNull String tag);

  CompletableFuture<@Nullable Integer> clearShopTags(@NotNull UUID tagger, @NotNull Shop shop);

  CompletableFuture<@Nullable Integer> clearTagFromShops(@NotNull UUID tagger, @NotNull String tag);

  CompletableFuture<@Nullable Integer> removeTag(@NotNull UUID tagger, @NotNull Shop shop, @NotNull String tag);

  CompletableFuture<@Nullable Integer> tagShop(@NotNull UUID tagger, @NotNull Shop shop, @NotNull String tag);

  @NotNull
  List<String> listTags(@NotNull UUID tagger);

  void deleteShop(@NotNull Shop shop);

  @NotNull
  CompletableFuture<@NotNull ShopInventoryCountCache> queryShopInventoryCacheInDatabase(@NotNull Shop shop);

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
