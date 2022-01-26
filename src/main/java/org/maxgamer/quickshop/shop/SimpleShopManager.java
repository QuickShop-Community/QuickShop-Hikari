/*
 * This file is a part of project QuickShop, the name is SimpleShopManager.java
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.economy.AbstractEconomy;
import org.maxgamer.quickshop.api.economy.EconomyTransaction;
import org.maxgamer.quickshop.api.event.*;
import org.maxgamer.quickshop.api.shop.*;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapper;
import org.maxgamer.quickshop.economy.Trader;
import org.maxgamer.quickshop.integration.SimpleIntegrationManager;
import org.maxgamer.quickshop.localization.LocalizedMessagePair;
import org.maxgamer.quickshop.shop.inventory.BukkitInventoryWrapper;
import org.maxgamer.quickshop.util.*;
import org.maxgamer.quickshop.util.economyformatter.EconomyFormatter;
import org.maxgamer.quickshop.util.holder.Result;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Manage a lot of shops.
 */
public class SimpleShopManager implements ShopManager, Reloadable {

    private final Map<String, Map<ShopChunk, Map<Location, Shop>>> shops = Maps.newConcurrentMap();

    private final Set<Shop> loadedShops = Sets.newConcurrentHashSet();

    private final Map<UUID, Info> actions = Maps.newConcurrentMap();

    private final QuickShop plugin;
    private final Cache<UUID, Shop> shopRuntimeUUIDCaching =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(10, TimeUnit.MINUTES)
                    .maximumSize(50)
                    .weakValues()
                    .initialCapacity(50)
                    .build();
    private final EconomyFormatter formatter;
    @Getter
    @Nullable
    private Trader cacheTaxAccount;
    @Getter
    private Trader cacheUnlimitedShopAccount;
    private SimplePriceLimiter priceLimiter;
    private boolean useOldCanBuildAlgorithm;
    private boolean autoSign;


    public SimpleShopManager(@NotNull QuickShop plugin) {
        Util.ensureThread(false);
        this.plugin = plugin;
        this.formatter = new EconomyFormatter(plugin, plugin.getEconomy());
        plugin.getReloadManager().register(this);
        init();
    }

    private void init() {
        Util.debugLog("Loading caching tax account...");
        String taxAccount = plugin.getConfig().getString("tax-account", "tax");
        if (!taxAccount.isEmpty()) {
            if (Util.isUUID(taxAccount)) {
                this.cacheTaxAccount = new Trader(taxAccount,
                        plugin.getServer().getOfflinePlayer(UUID.fromString(taxAccount)));
            } else {
                this.cacheTaxAccount = new Trader(taxAccount,
                        PlayerFinder.findOfflinePlayerByName(taxAccount));
            }
        } else {
            // disable tax account
            cacheTaxAccount = null;
        }
        if (plugin.getConfig().getBoolean("unlimited-shop-owner-change")) {
            String uAccount = plugin.getConfig().getString("unlimited-shop-owner-change-account", "");
            if (uAccount.isEmpty()) {
                uAccount = "quickshop";
                plugin.getLogger().log(Level.WARNING, "unlimited-shop-owner-change-account is empty, default to \"quickshop\"");
            }
            if (Util.isUUID(uAccount)) {
                cacheUnlimitedShopAccount = new Trader(uAccount, Bukkit.getOfflinePlayer(UUID.fromString(uAccount)));
            } else {
                cacheUnlimitedShopAccount = new Trader(uAccount, PlayerFinder.findOfflinePlayerByName(uAccount));
            }
        }
        this.priceLimiter = new SimplePriceLimiter(
                plugin.getConfig().getDouble("shop.minimum-price"),
                plugin.getConfig().getInt("shop.maximum-price"),
                plugin.getConfig().getBoolean("shop.allow-free-shop"),
                plugin.getConfig().getBoolean("whole-number-prices-only"));
        this.useOldCanBuildAlgorithm = plugin.getConfig().getBoolean("limits.old-algorithm");
        this.autoSign = plugin.getConfig().getBoolean("shop.auto-sign");
    }

    @Override
    public ReloadResult reloadModule() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::init);
        return ReloadResult.builder().status(ReloadStatus.SCHEDULED).build();
    }

    /**
     * Checks other plugins to make sure they can use the chest they're making a shop.
     *
     * @param p  The player to check
     * @param b  The block to check
     * @param bf The blockface to check
     * @return True if they're allowed to place a shop there.
     */
    @Override
    public boolean canBuildShop(@NotNull Player p, @NotNull Block b, @NotNull BlockFace bf) {
        Util.ensureThread(false);
        if (plugin.isLimit()) {
            int owned = 0;
            if (useOldCanBuildAlgorithm) {
                owned = getPlayerAllShops(p.getUniqueId()).size();
            } else {
                for (final Shop shop : getPlayerAllShops(p.getUniqueId())) {
                    if (!shop.isUnlimited()) {
                        owned++;
                    }
                }
            }
            int max = plugin.getShopLimit(p);
            if (owned + 1 > max) {
                plugin.text().of(p, "reached-maximum-can-create", String.valueOf(owned), String.valueOf(max)).send();
                return false;
            }
        }
        ShopPreCreateEvent spce = new ShopPreCreateEvent(p, b.getLocation());
        return !Util.fireCancellableEvent(spce);
    }

    /**
     * Returns a map of World - Chunk - Shop
     *
     * @return a map of World - Chunk - Shop
     */
    @Override
    public @NotNull Map<String, Map<ShopChunk, Map<Location, Shop>>> getShops() {
        return this.shops;
    }

    /**
     * Returns a new shop iterator object, allowing iteration over shops easily, instead of sorting
     * through a 3D map.
     *
     * @return a new shop iterator object.
     */
    @Override
    public @NotNull Iterator<Shop> getShopIterator() {
        return new ShopIterator();
    }

    /**
     * Removes all shops from memory and the world. Does not delete them from the database. Call
     * this on plugin disable ONLY.
     */
    @Override
    public void clear() {
        Util.ensureThread(false);
        if (plugin.isDisplayEnabled()) {
            for (World world : plugin.getServer().getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    Map<Location, Shop> inChunk = this.getShops(chunk);
                    if (inChunk == null || inChunk.isEmpty()) {
                        continue;
                    }
                    for (Shop shop : inChunk.values()) {
                        if (shop.isLoaded()) {
                            shop.onUnload();
                        }
                    }
                }
            }
        }
        this.actions.clear();
        this.shops.clear();
    }

    /**
     * Returns a map of Shops
     *
     * @param c The chunk to search. Referencing doesn't matter, only coordinates and world are
     *          used.
     * @return Shops
     */
    @Override
    public @Nullable Map<Location, Shop> getShops(@NotNull Chunk c) {
        return getShops(c.getWorld().getName(), c.getX(), c.getZ());
    }

    @Override
    public @Nullable Map<Location, Shop> getShops(@NotNull String world, int chunkX, int chunkZ) {
        final Map<ShopChunk, Map<Location, Shop>> inWorld = this.getShops(world);
        if (inWorld == null) {
            return null;
        }
        return inWorld.get(new SimpleShopChunk(world, chunkX, chunkZ));
    }

    /**
     * Returns a map of Chunk - Shop
     *
     * @param world The name of the world (case sensitive) to get the list of shops from
     * @return a map of Chunk - Shop
     */
    @Override
    public @Nullable Map<ShopChunk, Map<Location, Shop>> getShops(@NotNull String world) {
        return this.shops.get(world);
    }

    private void processWaterLoggedSign(@NotNull Block container, @NotNull Block signBlock) {
        boolean signIsWatered = signBlock.getType() == Material.WATER;
        signBlock.setType(Util.getSignMaterial());
        BlockState signBlockState = signBlock.getState();
        BlockData signBlockData = signBlockState.getBlockData();
        if (signIsWatered && (signBlockData instanceof Waterlogged)) {
            Waterlogged waterable = (Waterlogged) signBlockData;
            waterable.setWaterlogged(true); // Looks like sign directly put in water
        }
        if (signBlockData instanceof WallSign) {
            WallSign wallSignBlockData = (WallSign) signBlockData;
            BlockFace bf = container.getFace(signBlock);
            if (bf != null) {
                wallSignBlockData.setFacing(bf);
                signBlockState.setBlockData(wallSignBlockData);
            }
        } else {
            plugin.getLogger().warning(
                    "Sign material "
                            + signBlockState.getType().name()
                            + " not a WallSign, make sure you using correct sign material.");
        }
        signBlockState.update(true);
    }

    /**
     * Create a shop use Shop and Info object.
     *
     * @param shop The shop object
     * @param info The info object
     */
    @Override
    public void createShop(@NotNull Shop shop, @NotNull Info info) {
        Util.ensureThread(false);
        Player player = plugin.getServer().getPlayer(shop.getOwner());
        if (player == null) {
            throw new IllegalStateException("The owner creating the shop is offline or not exist");
        }
        if (info.getSignBlock() != null && autoSign) {
            if (info.getSignBlock().getType().isAir() || info.getSignBlock().getType() == Material.WATER) {
                this.processWaterLoggedSign(shop.getLocation().getBlock(), info.getSignBlock());
            } else {
                if (!plugin.getConfig().getBoolean("shop.allow-shop-without-space-for-sign")) {
                    plugin.text().of(player, "failed-to-put-sign").send();
                    Util.debugLog("Sign cannot placed cause no enough space(Not air block)");
                    return;
                }
            }
        }
        // sync add to prevent compete issue
        addShop(shop.getLocation().getWorld().getName(), shop);
        // load the shop finally
        shop.onLoad();
        // first init
        shop.setSignText();
        // save to database
        plugin.getDatabaseHelper().createShop(shop, null, e ->
                Util.mainThreadRun(() -> {
                    // also remove from memory when failed
                    shop.delete(true);
                    plugin.getLogger()
                            .log(Level.WARNING, "Shop create failed, trying to auto fix the database...", e);
                    boolean backupSuccess = Util.backupDatabase();
                    if (backupSuccess) {
                        plugin.getDatabaseHelper().removeShop(shop);
                    } else {
                        plugin.getLogger().warning(
                                "Failed to backup the database, all changes will revert after a reboot.");
                    }
                    plugin.getDatabaseHelper().createShop(shop, null, e2 -> {
                        plugin.getLogger()
                                .log(Level.SEVERE, "Shop create failed, auto fix failed, the changes may won't commit to database.", e2);
                        // MsgUtil.sendMessage(player, "shop-creation-failed");
                        plugin.text().of(player, "shop-creation-failed").send();
                        Util.mainThreadRun(() -> {
                            shop.onUnload();
                            removeShop(shop);
                            shop.delete();
                        });
                    });
                }));
    }

    /**
     * Format the price use economy system
     *
     * @param d price
     * @return formated price
     */
    @Override
    public @Nullable String format(double d, @NotNull World world, @Nullable String currency) {
        return plugin.getEconomy().format(d, world, currency);
    }

    /**
     * Format the price use economy system
     *
     * @param d price
     * @return formated price
     */
    @Override
    public @Nullable String format(double d, @NotNull Shop shop) {
        return plugin.getEconomy().format(d, shop.getLocation().getWorld(), shop.getCurrency());
    }

    /**
     * Gets a shop in a specific location
     *
     * @param loc The location to get the shop from
     * @return The shop at that location
     */
    @Override
    public @Nullable Shop getShop(@NotNull Location loc) {
        return getShop(loc, false);
    }

    /**
     * Gets a shop in a specific location
     *
     * @param loc                  The location to get the shop from
     * @param skipShopableChecking whether to check is shopable
     * @return The shop at that location
     */
    @Override
    public @Nullable Shop getShop(@NotNull Location loc, boolean skipShopableChecking) {
        if (!skipShopableChecking && !Util.isShoppables(loc.getBlock().getType())) {
            return null;
        }
        final Map<Location, Shop> inChunk = getShops(loc.getChunk());
        if (inChunk == null) {
            return null;
        }
        loc = loc.clone();
        // Fix double chest XYZ issue
        loc.setX(loc.getBlockX());
        loc.setY(loc.getBlockY());
        loc.setZ(loc.getBlockZ());
        // We can do this because WorldListener updates the world reference so
        // the world in loc is the same as world in inChunk.get(loc)
        return inChunk.get(loc);
    }

    /**
     * Gets a shop in a specific location Include the attached shop, e.g DoubleChest shop.
     *
     * @param loc The location to get the shop from
     * @return The shop at that location
     */
    @Override
    public @Nullable Shop getShopIncludeAttached(@Nullable Location loc) {
        return getShopIncludeAttached(loc, true);
    }

    /**
     * Gets a shop in a specific location Include the attached shop, e.g DoubleChest shop.
     *
     * @param loc      The location to get the shop from
     * @param useCache whether to use cache
     * @return The shop at that location
     */
    @Override
    public @Nullable Shop getShopIncludeAttached(@Nullable Location loc, boolean useCache) {
        if (loc == null) {
            Util.debugLog("Location is null.");
            return null;
        }
        if (useCache) {
            if (plugin.getShopCache() != null) {
                return plugin.getShopCache().find(loc, true);
            }
        }
        return findShopIncludeAttached(loc, false);
    }

    @Override
    public void bakeShopRuntimeRandomUniqueIdCache(@NotNull Shop shop) {
        shopRuntimeUUIDCaching.put(shop.getRuntimeRandomUniqueId(), shop);
    }

    @Override
    @Nullable
    public Shop getShopFromRuntimeRandomUniqueId(@NotNull UUID runtimeRandomUniqueId) {
        return getShopFromRuntimeRandomUniqueId(runtimeRandomUniqueId, false);
    }

    @Override
    @Nullable
    public Shop getShopFromRuntimeRandomUniqueId(
            @NotNull UUID runtimeRandomUniqueId, boolean includeInvalid) {
        Shop shop = shopRuntimeUUIDCaching.getIfPresent(runtimeRandomUniqueId);
        if (shop == null) {
            for (Shop shopWithoutCache : this.getLoadedShops()) {
                if (shopWithoutCache.getRuntimeRandomUniqueId().equals(runtimeRandomUniqueId)) {
                    return shopWithoutCache;
                }
            }
            return null;
        }
        if (includeInvalid) {
            return shop;
        }
        if (shop.isValid()) {
            return shop;
        }
        return null;
    }

    @Override
    public void handleChat(@NotNull Player p, @NotNull String msg) {
        if (!plugin.getShopManager().getActions().containsKey(p.getUniqueId())) {
            return;
        }
        String message = net.md_5.bungee.api.ChatColor.stripColor(msg);
        message = ChatColor.stripColor(message);
        QSHandleChatEvent qsHandleChatEvent = new QSHandleChatEvent(p, message);
        qsHandleChatEvent.callEvent();
        message = qsHandleChatEvent.getMessage();
        // Use from the main thread, because Bukkit hates life
        String finalMessage = message;

        Util.mainThreadRun(() -> {
            Map<UUID, Info> actions = getActions();
            // They wanted to do something.
            Info info = actions.remove(p.getUniqueId());
            if (info == null) {
                return; // multithreaded means this can happen
            }
            if (info.getLocation().getWorld() != p.getLocation().getWorld()
                    || info.getLocation().distanceSquared(p.getLocation()) > 25) {
                plugin.text().of(p, "not-looking-at-shop").send();
                return;
            }
            if (info.getAction() == ShopAction.CREATE) {
                actionCreate(p, info, finalMessage);
            }
            if (info.getAction() == ShopAction.BUY) {
                actionTrade(p, info, finalMessage);
            }
        });
    }

    /**
     * Load shop method for loading shop into mapping, so getShops method will can find it. It also
     * effects a lots of feature, make sure load it after create it.
     *
     * @param world The world the shop is in
     * @param shop  The shop to load
     */
    @Override
    public void loadShop(@NotNull String world, @NotNull Shop shop) {
        this.addShop(world, shop);
    }

    /**
     * Adds a shop to the world. Does NOT require the chunk or world to be loaded Call shop.onLoad
     * by yourself
     *
     * @param world The name of the world
     * @param shop  The shop to add
     */
    @Override
    public void addShop(@NotNull String world, @NotNull Shop shop) {
        Map<ShopChunk, Map<Location, Shop>> inWorld =
                this.getShops()
                        .computeIfAbsent(world, k -> new MapMaker().initialCapacity(3).makeMap());
        // There's no world storage yet. We need to create that map.
        // Put it in the data universe
        // Calculate the chunks coordinates. These are 1,2,3 for each chunk, NOT
        // location rounded to the nearest 16.
        int x = (int) Math.floor((shop.getLocation().getBlockX()) / 16.0);
        int z = (int) Math.floor((shop.getLocation().getBlockZ()) / 16.0);
        // Get the chunk set from the world info
        ShopChunk shopChunk = new SimpleShopChunk(world, x, z);
        Map<Location, Shop> inChunk =
                inWorld.computeIfAbsent(shopChunk, k -> new MapMaker().initialCapacity(1).makeMap());
        // That chunk data hasn't been created yet - Create it!
        // Put it in the world
        // Put the shop in its location in the chunk list.
        inChunk.put(shop.getLocation(), shop);
        // shop.onLoad();

    }

    /**
     * Removes a shop from the world. Does NOT remove it from the database. * REQUIRES * the world
     * to be loaded Call shop.onUnload by your self.
     *
     * @param shop The shop to remove
     */
    @Override
    public void removeShop(@NotNull Shop shop) {
        Location loc = shop.getLocation();
        String world = Objects.requireNonNull(loc.getWorld()).getName();
        Map<ShopChunk, Map<Location, Shop>> inWorld = this.getShops().get(world);
        int x = (int) Math.floor((loc.getBlockX()) / 16.0);
        int z = (int) Math.floor((loc.getBlockZ()) / 16.0);
        ShopChunk shopChunk = new SimpleShopChunk(world, x, z);
        Map<Location, Shop> inChunk = inWorld.get(shopChunk);
        if (inChunk == null) {
            return;
        }
        inChunk.remove(loc);
    }

    /**
     * @return Returns the Map. Info contains what their last question etc was.
     */
    @Override
    public @NotNull Map<UUID, Info> getActions() {
        return this.actions;
    }

    /**
     * Get all loaded shops.
     *
     * @return All loaded shops.
     */
    @Override
    public @NotNull Set<Shop> getLoadedShops() {
        return this.loadedShops;
    }

    /**
     * Get a players all shops.
     *
     * <p>Make sure you have caching this, because this need a while to get player's all shops
     *
     * @param playerUUID The player's uuid.
     * @return The list have this player's all shops.
     */
    @Override
    public @NotNull List<Shop> getPlayerAllShops(@NotNull UUID playerUUID) {
        final List<Shop> playerShops = new ArrayList<>(10);
        for (final Shop shop : getAllShops()) {
            if (shop.getOwner().equals(playerUUID)) {
                playerShops.add(shop);
            }
        }
        return playerShops;
    }

    /**
     * Returns all shops in the whole database, include unloaded.
     *
     * <p>Make sure you have caching this, because this need a while to get all shops
     *
     * @return All shop in the database
     */
    @Override
    public @NotNull List<Shop> getAllShops() {
        final List<Shop> shops = new ArrayList<>();
        for (final Map<ShopChunk, Map<Location, Shop>> shopMapData : getShops().values()) {
            for (final Map<Location, Shop> shopData : shopMapData.values()) {
                shops.addAll(shopData.values());
            }
        }
        return shops;
    }

    /**
     * Get the all shops in the world.
     *
     * @param world The world you want get the shops.
     * @return The list have this world all shops
     */
    @Override
    public @NotNull List<Shop> getShopsInWorld(@NotNull World world) {
        final List<Shop> worldShops = new ArrayList<>();
        for (final Shop shop : getAllShops()) {
            Location location = shop.getLocation();
            if (location.isWorldLoaded() && Objects.equals(location.getWorld(), world)) {
                worldShops.add(shop);
            }
        }
        return worldShops;
    }

    public void actionBuy(
            @NotNull UUID buyer,
            @NotNull InventoryWrapper buyerInventory,
            @NotNull AbstractEconomy eco,
            @NotNull Info info,
            @NotNull Shop shop,
            int amount) {
        Util.ensureThread(false);
        if (shopIsNotValid(buyer, info, shop)) {
            return;
        }
        int space = shop.getRemainingSpace();
        if (space == -1) {
            space = 10000;
        }
        if (space < amount) {
            plugin.text().of(buyer, "shop-has-no-space", Integer.toString(space), MsgUtil.getTranslateText(shop.getItem())).send();
            return;
        }
        int count = buyerInventory.countItems(shop);
        // Not enough items
        if (amount > count) {
            plugin.text().of(buyer,
                    "you-dont-have-that-many-items",
                    Integer.toString(count),
                    MsgUtil.getTranslateText(shop.getItem())).send();
            return;
        }
        if (amount < 1) {
            // & Dumber
            plugin.text().of(buyer, "negative-amount").send();
            return;
        }

        // Money handling
        // BUYING MODE  Shop Owner -> Player
        double taxModifier = getTax(shop, buyer);
        double total = CalculateUtil.multiply(amount, shop.getPrice());
        ShopPurchaseEvent e = new ShopPurchaseEvent(shop, buyer, buyerInventory, amount, total);
        if (Util.fireCancellableEvent(e)) {
            return; // Cancelled
        } else {
            total = e.getTotal(); // Allow addon to set it
        }
        Trader taxAccount;
        if (shop.getTaxAccount() != null) {
            taxAccount = new Trader(shop.getTaxAccount().toString(), Bukkit.getOfflinePlayer(shop.getTaxAccount()));
        } else {
            taxAccount = this.cacheTaxAccount;
        }
        EconomyTransaction transaction;
        EconomyTransaction.EconomyTransactionBuilder builder = EconomyTransaction.builder()
                .core(eco)
                .amount(total)
                .taxModifier(taxModifier)
                .taxAccount(taxAccount)
                .currency(shop.getCurrency())
                .world(shop.getLocation().getWorld())
                .to(buyer);
        if (!shop.isUnlimited()
                || (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")
                && shop.isUnlimited())) {
            transaction = builder.from(shop.getOwner()).build();
        } else {
            transaction = builder.from(null).build();
        }
        if (!transaction.failSafeCommit()) {
            if (transaction.getSteps() == EconomyTransaction.TransactionSteps.CHECK) {
                plugin.text().of(buyer, "the-owner-cant-afford-to-buy-from-you",
                        Objects.requireNonNull(format(total, shop.getLocation().getWorld(), shop.getCurrency())),
                        Objects.requireNonNull(format(eco.getBalance(shop.getOwner(), shop.getLocation().getWorld(),
                                shop.getCurrency()), shop.getLocation().getWorld(), shop.getCurrency()))).send();
            } else {
                plugin.text().of(buyer, "purchase-failed").send();
                plugin.getLogger().severe("EconomyTransaction Failed, last error:" + transaction.getLastError());
                plugin.getLogger().severe("Tips: If you see any economy plugin name appears above, please don't ask QuickShop support. Contact with developer of economy plugin. QuickShop didn't process the transaction, we only receive the transaction result from your economy plugin.");
            }
            return;
        }

        // Notify the owner of the purchase. //TODO: move to a standalone method
        Player player = plugin.getServer().getPlayer(buyer);


        LocalizedMessagePair msg = LocalizedMessagePair.of("player-sold-to-your-store",
                player != null ? player.getName() : buyer.toString(),
                String.valueOf(amount),
                MsgUtil.getTranslateText(shop.getItem()));

        ShopTransactionMessageContainer shopTransactionMessage = ShopTransactionMessageContainer.ofLocalizedMessageWithItem(msg, Util.serialize(shop.getItem()), null);

        if (plugin.getConfig().getBoolean("shop.sending-stock-message-to-staffs")) {
            for (UUID staff : shop.getModerator().getStaffs()) {
                MsgUtil.send(shop, staff, shopTransactionMessage);
            }
        }
        MsgUtil.send(shop, shop.getOwner(), shopTransactionMessage);

        if (space == amount) {
            msg = LocalizedMessagePair.of("shop-out-of-space",
                    Integer.toString(shop.getLocation().getBlockX()),
                    Integer.toString(shop.getLocation().getBlockY()),
                    Integer.toString(shop.getLocation().getBlockZ()));
            shopTransactionMessage = ShopTransactionMessageContainer.ofLocalizedMessageWithItem(msg, Util.serialize(shop.getItem()), null);

            if (plugin.getConfig().getBoolean("shop.sending-stock-message-to-staffs")) {
                for (UUID staff : shop.getModerator().getStaffs()) {
                    MsgUtil.send(shop, staff, shopTransactionMessage);
                }
            }
            MsgUtil.send(shop, shop.getOwner(), shopTransactionMessage);
        }


        shop.buy(buyer, buyerInventory, player != null ? player.getLocation() : shop.getLocation(), amount);
        sendSellSuccess(buyer, shop, amount);
        ShopSuccessPurchaseEvent se = new ShopSuccessPurchaseEvent(shop, buyer, buyerInventory, amount, total, taxModifier);
        plugin.getServer().getPluginManager().callEvent(se);
        shop.setSignText(); // Update the signs count
    }


    @Deprecated
    public void actionBuy(@NotNull Player p, @NotNull AbstractEconomy eco, @NotNull SimpleInfo info,
                          @NotNull Shop shop, int amount) {
        Util.ensureThread(false);
        actionBuy(p.getUniqueId(), new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, amount);
    }

    @Override
    @Deprecated
    public double getTax(@NotNull Shop shop, @NotNull Player p) {
        return getTax(shop, p.getUniqueId());
    }

    @Override
    public double getTax(@NotNull Shop shop, @NotNull UUID p) {
        Util.ensureThread(false);
        double tax = plugin.getConfig().getDouble("tax");
        Player player = plugin.getServer().getPlayer(p);
        if (player != null) {
            if (QuickShop.getPermissionManager().hasPermission(player, "quickshop.tax")) {
                tax = 0;
                Util.debugLog("Disable the Tax for player " + player + " cause they have permission quickshop.tax");
            }
            if (shop.isUnlimited() && QuickShop.getPermissionManager().hasPermission(player, "quickshop.tax.bypassunlimited")) {
                tax = 0;
                Util.debugLog("Disable the Tax for player " + player + " cause they have permission quickshop.tax.bypassunlimited and shop is unlimited.");
            }
        }
        if (tax >= 1.0) {
            plugin.getLogger().warning("Disable tax due to is invalid, it should be in 0.0-1.0 (current value is " + tax + ")");
            tax = 0;
        }
        if (tax < 0) {
            tax = 0; // Tax was disabled.
        }
        if (shop.getModerator().isModerator(p)) {
            tax = 0; // Is staff or owner, so we won't will take them tax
        }
        ShopTaxEvent taxEvent = new ShopTaxEvent(shop, tax, p);
        taxEvent.callEvent();
        return taxEvent.getTax();
    }

    public void actionCreate(@NotNull Player p, Info info, @NotNull String message) {
        Util.ensureThread(false);
        if (plugin.getEconomy() == null) {
            MsgUtil.sendDirectMessage(p, "Error: Economy system not loaded, type /qs main command to get details.");
            return;
        }
        if (plugin.isAllowStack() && !p.hasPermission("quickshop.create.stacks")) {
            Util.debugLog("Player " + p + " no permission to create stacks shop, forcing creating single item shop");
            info.getItem().setAmount(1);
        }

        // Checking the shop can be created
        Util.debugLog("Calling for protection check...");
        // Fix openInv compatible issue
        if (!info.isBypassed()) {
            Result result = plugin.getPermissionChecker().canBuild(p, info.getLocation());
            if (!result.isSuccess()) {
                plugin.text().of(p, "3rd-plugin-build-check-failed", result.getMessage()).send();
                if (p.hasPermission("quickshop.alert")) {
                    plugin.text().of(p, "3rd-plugin-build-check-failed-admin", result.getMessage(), result.getListener()).send();
                }
                Util.debugLog("Failed to create shop because protection check failed, found:" + result.getMessage());
                return;
            }
        }

        if (plugin.getShopManager().getShop(info.getLocation()) != null) {
            plugin.text().of(p, "shop-already-owned").send();
            return;
        }
        if (Util.isDoubleChest(info.getLocation().getBlock().getBlockData())
                && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.double")) {
            plugin.text().of(p, "no-double-chests").send();
            return;
        }
        if (!Util.canBeShop(info.getLocation().getBlock())) {
            plugin.text().of(p, "chest-was-removed").send();
            return;
        }
        if (info.getLocation().getBlock().getType()
                == Material.ENDER_CHEST) { // FIXME: Need a better impl
            if (!QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.enderchest")) {
                return;
            }
        }

        if (autoSign) {
            if (info.getSignBlock() == null) {
                if (!plugin.getConfig().getBoolean("shop.allow-shop-without-space-for-sign")) {
                    plugin.text().of(p, "failed-to-put-sign").send();
                    return;
                }
            } else {
                Material signType = info.getSignBlock().getType();
                if (signType != Material.WATER
                        && !signType.isAir()
                        && !plugin.getConfig().getBoolean("shop.allow-shop-without-space-for-sign")) {
                    plugin.text().of(p, "failed-to-put-sign").send();
                    return;
                }
            }
        }

        // Price per item
        double price;
        try {
            price = Double.parseDouble(message);
            if (Double.isInfinite(price)) {
                plugin.text().of(p, "exceeded-maximum", message).send();
                return;
            }
            String strFormat = new DecimalFormat("#.#########").format(Math.abs(price))
                    .replace(",", ".");
            String[] processedDouble = strFormat.split("\\.");
            if (processedDouble.length > 1) {
                int maximumDigitsLimit = plugin.getConfig()
                        .getInt("maximum-digits-in-price", -1);
                if (processedDouble[1].length() > maximumDigitsLimit
                        && maximumDigitsLimit != -1) {
                    plugin.text().of(p, "digits-reach-the-limit", String.valueOf(maximumDigitsLimit)).send();
                    return;
                }
            }
        } catch (NumberFormatException ex) {
            Util.debugLog(ex.getMessage());
            plugin.text().of(p, "not-a-number", message).send();
            return;
        }

        // Price limit checking
        boolean decFormat = plugin.getConfig().getBoolean("use-decimal-format");

        PriceLimiterCheckResult priceCheckResult = this.priceLimiter.check(info.getItem(), price);

        switch (priceCheckResult.getStatus()) {
            case REACHED_PRICE_MIN_LIMIT:
                plugin.text().of(p, "price-too-cheap",
                        (decFormat) ? MsgUtil.decimalFormat(this.priceLimiter.getMaxPrice())
                                : Double.toString(this.priceLimiter.getMinPrice())).send();
                return;
            case REACHED_PRICE_MAX_LIMIT:
                plugin.text().of(p, "price-too-high",
                        (decFormat) ? MsgUtil.decimalFormat(this.priceLimiter.getMaxPrice())
                                : Double.toString(this.priceLimiter.getMinPrice())).send();
                return;
            case PRICE_RESTRICTED:
                plugin.text().of(p, "restricted-prices",
                        MsgUtil.getTranslateText(info.getItem()),
                        String.valueOf(priceCheckResult.getMin()),
                        String.valueOf(priceCheckResult.getMax())).send();
                return;
            case NOT_VALID:
                plugin.text().of(p, "not-a-number", message).send();
                return;
            case NOT_A_WHOLE_NUMBER:
                plugin.text().of(p, "not-a-integer", message).send();
                return;
        }

        // Set to 1 when disabled stacking shop
        if (!plugin.isAllowStack()) {
            info.getItem().setAmount(1);
        }
        Inventory inv;
        BlockState state = info.getLocation().getBlock().getState();
        if(state instanceof BlockInventoryHolder){
            inv = ((BlockInventoryHolder) state).getInventory();
        }else{
            plugin.text().of(p, "chest-was-removed").send();
            return;
        }


        // Create the sample shop
        //noinspection ConstantConditions
        ContainerShop shop = new ContainerShop(
                plugin,
                info.getLocation(),
                price,
                info.getItem(),
                new SimpleShopModerator(p.getUniqueId()),
                false,
                ShopType.SELLING,
                new YamlConfiguration(),
                null,
                false,
                null,
                plugin.getName(),
                new BukkitInventoryWrapper(inv));
        if (!info.isBypassed()) {
            Result result = ((SimpleIntegrationManager) plugin.getIntegrationHelper()).callIntegrationsCanCreate(p, info.getLocation());
            if (!result.isSuccess()) {
                plugin.text().of(p, "integrations-check-failed-create", result.getMessage()).send();
                Util.debugLog("Cancelled by integrations: " + result);
                return;
            }
        }

        // Calling ShopCreateEvent
        ShopCreateEvent shopCreateEvent = new ShopCreateEvent(shop, p.getUniqueId());
        if (Util.fireCancellableEvent(shopCreateEvent)) {
            Util.debugLog("Cancelled by plugin");
            return;
        }
        // Handle create cost
        // This must be called after the event has been called.
        // Else, if the event is cancelled, they won't get their
        // money back.
        double createCost = plugin.getConfig().getDouble("shop.cost");
        if (QuickShop.getPermissionManager().hasPermission(p, "quickshop.bypasscreatefee")) {
            createCost = 0;
        }
        if (createCost > 0) {
            EconomyTransaction economyTransaction =
                    EconomyTransaction.builder()
                            .taxAccount(cacheTaxAccount)
                            .taxModifier(0.0)
                            .core(plugin.getEconomy())
                            .from(p.getUniqueId())
                            .to(null)
                            .amount(createCost)
                            .currency(plugin.getCurrency())
                            .world(shop.getLocation().getWorld())
                            .build();
            if (!economyTransaction.failSafeCommit()) {
                if (economyTransaction.getSteps() == EconomyTransaction.TransactionSteps.CHECK) {
                    plugin.text().of(p, "you-cant-afford-a-new-shop",
                            Objects.requireNonNull(format(createCost, shop.getLocation().getWorld(),
                                    shop.getCurrency()))).send();
                } else {
                    plugin.text().of(p, "purchase-failed").send();
                    plugin.getLogger().severe("EconomyTransaction Failed, last error:" + economyTransaction.getLastError());
                    plugin.getLogger().severe("Tips: If you see any economy plugin name appears above, please don't ask QuickShop support. Contact with developer of economy plugin. QuickShop didn't process the transaction, we only receive the transaction result from your economy plugin.");

                }
                return;
            }
        }

        // The shop about successfully created
        createShop(shop, info);
        if (!plugin.getConfig().getBoolean("shop.lock")) {
            plugin.text().of(p, "shops-arent-locked").send();
        }

        // Figures out which way we should put the sign on and
        // sets its text.
        if (shop.isDoubleShop()) {
            Shop nextTo = shop.getAttachedShop();
            if (Objects.requireNonNull(nextTo).getPrice() > shop.getPrice()) {
                // The one next to it must always be a
                // buying shop.
                plugin.text().of(p, "buying-more-than-selling").send();
            }
        }

        // If this is one of two double chests, update its partner too
        if (shop.isRealDouble()) {
            shop.getAttachedShop().refresh();
        }
        // One last refresh to ensure the item shows up
        shop.refresh();
    }

    @Deprecated
    public void actionSell(
            @NotNull Player p, @NotNull AbstractEconomy eco, @NotNull SimpleInfo info, @NotNull Shop shop,
            int amount) {
        Util.ensureThread(false);
        actionSell(p.getUniqueId(), new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, amount);
    }

    public void actionSell(
            @NotNull UUID seller,
            @NotNull InventoryWrapper sellerInventory,
            @NotNull AbstractEconomy eco,
            @NotNull Info info,
            @NotNull Shop shop,
            int amount) {
        Util.ensureThread(false);
        if (shopIsNotValid(seller, info, shop)) {
            return;
        }
        int stock = shop.getRemainingStock();
        if (stock == -1) {
            stock = 10000;
        }
        if (stock < amount) {
            plugin.text().of(seller, "shop-stock-too-low", Integer.toString(stock),
                    MsgUtil.getTranslateText(shop.getItem())).send();
            return;
        }
        if (amount < 1) {
            // & Dumber
            plugin.text().of(seller, "negative-amount").send();
            return;
        }
        int pSpace = sellerInventory.countSpace(shop);
        if (amount > pSpace) {
            plugin.text().of(seller, "not-enough-space", String.valueOf(pSpace)).send();
            return;
        }

        double taxModifier = getTax(shop, seller);
        double total = CalculateUtil.multiply(amount, shop.getPrice());

        ShopPurchaseEvent e = new ShopPurchaseEvent(shop, seller, sellerInventory, amount, total);
        if (Util.fireCancellableEvent(e)) {
            return; // Cancelled
        } else {
            total = e.getTotal(); // Allow addon to set it
        }
        // Money handling
        // SELLING Player -> Shop Owner
        EconomyTransaction transaction;
        Trader taxAccount;
        if (shop.getTaxAccount() != null) {
            taxAccount = new Trader(shop.getTaxAccount().toString(), Bukkit.getOfflinePlayer(shop.getTaxAccount()));
        } else {
            taxAccount = this.cacheTaxAccount;
        }
        EconomyTransaction.EconomyTransactionBuilder builder = EconomyTransaction.builder()
                .allowLoan(plugin.getConfig().getBoolean("shop.allow-economy-loan", false))
                .core(eco)
                .from(seller)
                .amount(total)
                .taxModifier(taxModifier)
                .taxAccount(taxAccount)
                .world(shop.getLocation().getWorld())
                .currency(shop.getCurrency());
        if (!shop.isUnlimited()
                || (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")
                && shop.isUnlimited())) {
            transaction = builder.to(shop.getOwner()).build();
        } else {
            transaction = builder.to(null).build();
        }
        if (!transaction.failSafeCommit()) {
            if (transaction.getSteps() == EconomyTransaction.TransactionSteps.CHECK) {
                plugin.text().of(seller, "you-cant-afford-to-buy",
                        Objects.requireNonNull(
                                format(total, shop.getLocation().getWorld(), shop.getCurrency())),
                        Objects.requireNonNull(format(
                                eco.getBalance(seller, shop.getLocation().getWorld(),
                                        shop.getCurrency()), shop.getLocation().getWorld(),
                                shop.getCurrency()))).send();
            } else {
                plugin.text().of(seller, "purchase-failed").send();
                plugin.getLogger().severe("EconomyTransaction Failed, last error:" + transaction.getLastError());
            }
            return;
        }

        LocalizedMessagePair msg;
        // Notify the shop owner //TODO: move to a standalone method
        Player player = plugin.getServer().getPlayer(seller);
        if (plugin.getConfig().getBoolean("show-tax")) {
            msg = LocalizedMessagePair.of("player-bought-from-your-store-tax",
                    player != null ? player.getName() : seller.toString(),
                    Integer.toString(amount * shop.getItem().getAmount()),
                    MsgUtil.getTranslateText(shop.getItem()),
                    this.formatter.format(CalculateUtil.multiply(CalculateUtil.subtract(1, taxModifier), total), shop),
                    this.formatter.format(CalculateUtil.multiply(taxModifier, total), shop));
        } else {
            msg = LocalizedMessagePair.of("player-bought-from-your-store",
                    player != null ? player.getName() : seller.toString(),
                    Integer.toString(amount * shop.getItem().getAmount()),
                    MsgUtil.getTranslateText(shop.getItem()),
                    this.formatter.format(CalculateUtil.multiply(CalculateUtil.subtract(1, taxModifier), total), shop));
        }

        ShopTransactionMessageContainer shopTransactionMessage = ShopTransactionMessageContainer.ofLocalizedMessageWithItem(msg, Util.serialize(shop.getItem()), null);

        MsgUtil.send(shop, shop.getOwner(), shopTransactionMessage);
        if (plugin.getConfig().getBoolean("shop.sending-stock-message-to-staffs")) {
            for (UUID staff : shop.getModerator().getStaffs()) {
                MsgUtil.send(shop, staff, shopTransactionMessage);
            }
        }
        // Transfers the item from A to B
        if (stock == amount) {
            msg = LocalizedMessagePair.of("shop-out-of-stock",
                    Integer.toString(shop.getLocation().getBlockX()),
                    Integer.toString(shop.getLocation().getBlockY()),
                    Integer.toString(shop.getLocation().getBlockZ()),
                    MsgUtil.getTranslateText(shop.getItem()));
            shopTransactionMessage = ShopTransactionMessageContainer.ofLocalizedMessageWithItem(msg, Util.serialize(shop.getItem()), null);

            MsgUtil.send(shop, shop.getOwner(), shopTransactionMessage);
            if (plugin.getConfig().getBoolean("shop.sending-stock-message-to-staffs")) {
                for (UUID staff : shop.getModerator().getStaffs()) {
                    MsgUtil.send(shop, staff, shopTransactionMessage);
                }
            }
        }


        shop.sell(seller, sellerInventory, player != null ? player.getLocation() : shop.getLocation(), amount);
        sendPurchaseSuccess(seller, shop, amount);
        ShopSuccessPurchaseEvent se = new ShopSuccessPurchaseEvent(shop, seller, sellerInventory, amount, total, taxModifier);
        plugin.getServer().getPluginManager().callEvent(se);
    }

    /**
     * Send a purchaseSuccess message for a player.
     *
     * @param purchaser Target player
     * @param shop      Target shop
     * @param amount    Trading item amounts.
     */
    @Override
    public void sendPurchaseSuccess(@NotNull UUID purchaser, @NotNull Shop shop, int amount) {
        Player sender = Bukkit.getPlayer(purchaser);
        if (sender == null) {
            return;
        }
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(sender);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(plugin.text().of(sender, "menu.successful-purchase").forLocale());
        chatSheetPrinter.printLine(plugin.text().of(sender, "menu.item-name-and-price", Integer.toString(amount * shop.getItem().getAmount()), MsgUtil.getTranslateText(shop.getItem()), format(amount * shop.getPrice(), shop)).forLocale());
        MsgUtil.printEnchantment(sender, shop, chatSheetPrinter);
        chatSheetPrinter.printFooter();
    }

    /**
     * Send a sellSuccess message for a player.
     *
     * @param seller Target player
     * @param shop   Target shop
     * @param amount Trading item amounts.
     */
    @Override
    public void sendSellSuccess(@NotNull UUID seller, @NotNull Shop shop, int amount) {
        Player sender = Bukkit.getPlayer(seller);
        if (sender == null) {
            return;
        }
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(sender);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(plugin.text().of(sender, "menu.successfully-sold").forLocale());
        chatSheetPrinter.printLine(
                plugin.text().of(sender,
                        "menu.item-name-and-price",
                        Integer.toString(amount),
                        MsgUtil.getTranslateText(shop.getItem()),
                        format(amount * shop.getPrice(), shop)).forLocale());
        if (plugin.getConfig().getBoolean("show-tax")) {
            double tax = plugin.getConfig().getDouble("tax");
            double total = amount * shop.getPrice();
            if (tax != 0) {
                if (!seller.equals(shop.getOwner())) {
                    chatSheetPrinter.printLine(
                            plugin.text().of(sender, "menu.sell-tax", format(tax * total, shop)).forLocale());
                } else {
                    chatSheetPrinter.printLine(plugin.text().of(sender, "menu.sell-tax-self").forLocale());
                }
            }
        }
        MsgUtil.printEnchantment(sender, shop, chatSheetPrinter);
        chatSheetPrinter.printFooter();
    }

    /**
     * Send a shop infomation to a player.
     *
     * @param p    Target player
     * @param shop The shop
     */
    @Override
    public void sendShopInfo(@NotNull Player p, @NotNull Shop shop) {
        // Potentially faster with an array?
        ItemStack items = shop.getItem();
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(p);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(plugin.text().of(p, "menu.shop-information").forLocale());
        chatSheetPrinter.printLine(plugin.text().of(p, "menu.owner", shop.ownerName()).forLocale());
        // Enabled
        plugin.getQuickChat().send(p, plugin.getQuickChat().getItemHologramChat(shop, items, p, ChatColor.DARK_PURPLE + plugin.text().of(p, "tableformat.left_begin").forLocale() + plugin.text().of(p, "menu.item", MsgUtil.getTranslateText(shop.getItem())).forLocale() + "  "));
        if (Util.isTool(items.getType())) {
            chatSheetPrinter.printLine(
                    plugin.text().of(p, "menu.damage-percent-remaining", Util.getToolPercentage(items)).forLocale());
        }
        if (shop.isSelling()) {
            if (shop.getRemainingStock() == -1) {
                chatSheetPrinter.printLine(
                        plugin.text().of(p, "menu.stock", plugin.text().of(p, "signs.unlimited").forLocale()).forLocale());
            } else {
                chatSheetPrinter.printLine(
                        plugin.text().of(p, "menu.stock", Integer.toString(shop.getRemainingStock())).forLocale());
            }
        } else {
            if (shop.getRemainingSpace() == -1) {
                chatSheetPrinter.printLine(
                        plugin.text().of(p, "menu.space", plugin.text().of(p, "signs.unlimited").forLocale()).forLocale());
            } else {
                chatSheetPrinter.printLine(
                        plugin.text().of(p, "menu.space", Integer.toString(shop.getRemainingSpace())).forLocale());
            }
        }
        if (shop.getItem().getAmount() == 1) {
            chatSheetPrinter.printLine(plugin.text().of(p, "menu.price-per", MsgUtil.getTranslateText(shop.getItem()), format(shop.getPrice(), shop)).forLocale());
        } else {
            chatSheetPrinter.printLine(plugin.text().of(p, "menu.price-per-stack", MsgUtil.getTranslateText(shop.getItem()), format(shop.getPrice(), shop), Integer.toString(shop.getItem().getAmount())).forLocale());
        }
        if (shop.isBuying()) {
            chatSheetPrinter.printLine(plugin.text().of(p, "menu.this-shop-is-buying").forLocale());
        } else {
            chatSheetPrinter.printLine(plugin.text().of(p, "menu.this-shop-is-selling").forLocale());
        }
        MsgUtil.printEnchantment(p, shop, chatSheetPrinter);
        if (items.getItemMeta() instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) items.getItemMeta();
            PotionEffectType potionEffectType = potionMeta.getBasePotionData().getType().getEffectType();
            if (potionEffectType != null) {
                chatSheetPrinter.printLine(plugin.text().of(p, "menu.effects").forLocale());
                chatSheetPrinter.printLine(ChatColor.YELLOW + MsgUtil.getPotioni18n(potionEffectType));
            }
            for (PotionEffect potionEffect : potionMeta.getCustomEffects()) {
                chatSheetPrinter.printLine(ChatColor.YELLOW + MsgUtil.getPotioni18n(potionEffect.getType()));
            }
        }
        chatSheetPrinter.printFooter();
    }

    @Override
    public boolean shopIsNotValid(@NotNull UUID uuid, @NotNull Info info, @NotNull Shop shop) {
        Player player = plugin.getServer().getPlayer(uuid);
        return shopIsNotValid(player, info, shop);
    }

    private boolean shopIsNotValid(@Nullable Player p, @NotNull Info info, @NotNull Shop shop) {
        if (plugin.getEconomy() == null) {
            MsgUtil.sendDirectMessage(p,
                    "Error: Economy system not loaded, type /qs main command to get details.");
            return true;
        }
        if (!Util.canBeShop(info.getLocation().getBlock())) {
            plugin.text().of(p, "chest-was-removed").send();
            return true;
        }
        if (info.hasChanged(shop)) {
            plugin.text().of(p, "shop-has-changed").send();
            return true;
        }
        return false;
    }

    private void actionTrade(@NotNull Player p, Info info, @NotNull String message) {
        Util.ensureThread(false);
        if (plugin.getEconomy() == null) {
            MsgUtil.sendDirectMessage(p, "Error: Economy system not loaded, type /qs main command to get details.");
            return;
        }
        Result result = ((SimpleIntegrationManager) plugin.getIntegrationHelper())
                .callIntegrationsCanTrade(p, info.getLocation());
        if (!result.isSuccess()) {
            plugin.text().of(p, "integrations-check-failed-trade", result.getMessage()).send();
            Util.debugLog("Cancel by integrations.");
            return;
        }
        AbstractEconomy eco = plugin.getEconomy();

        // Get the shop they interacted with
        Shop shop = plugin.getShopManager().getShop(info.getLocation());
        // It's not valid anymore
        if (shop == null || !Util.canBeShop(info.getLocation().getBlock())) {
            plugin.text().of(p, "chest-was-removed").send();
            return;
        }
        if (p.getGameMode() == GameMode.CREATIVE && plugin.getConfig().getBoolean("shop.disable-creative-mode-trading")) {
            plugin.text().of(p, "trading-in-creative-mode-is-disabled").send();
            return;
        }
        int amount;
        if (info.hasChanged(shop)) {
            plugin.text().of(p, "shop-has-changed").send();
            return;
        }
        if (shop.isBuying()) {
            if (StringUtils.isNumeric(message)) {
                amount = Integer.parseInt(message);
            } else {
                if (message.equalsIgnoreCase(
                        plugin.getConfig().getString("shop.word-for-trade-all-items", "all"))) {

                    int shopHaveSpaces;
                    if(shop.getInventory() == null){
                        shopHaveSpaces = 0;
                        plugin.getLogger().warning("Shop "+shop+" has null Inventory, failed to check spaces.");
                        Util.debugLog("Shop "+shop+" has null Inventory, failed to check spaces.");
                    }else{
                        shopHaveSpaces = shop.getInventory().countSpace(shop);
                    }
                    int invHaveItems = new BukkitInventoryWrapper(p.getInventory()).countItems(shop);
                    // Check if shop owner has enough money
                    double ownerBalance = eco
                            .getBalance(shop.getOwner(), shop.getLocation().getWorld(),
                                    shop.getCurrency());
                    int ownerCanAfford;

                    if (shop.getPrice() != 0) {
                        ownerCanAfford = (int) (ownerBalance / shop.getPrice());
                    } else {
                        ownerCanAfford = Integer.MAX_VALUE;
                    }

                    if (shop.isAlwaysCountingContainer() || !shop.isUnlimited()) {
                        amount = Math.min(shopHaveSpaces, invHaveItems);
                        amount = Math.min(amount, ownerCanAfford);
                    } else {
                        amount = new BukkitInventoryWrapper(p.getInventory()).countItems(shop);
                        // even if the shop is unlimited, the config option pay-unlimited-shop-owners is set to
                        // true,
                        // the unlimited shop owner should have enough money.
                        if (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
                            amount = Math.min(amount, ownerCanAfford);
                        }
                    }
                    if (amount < 1) { // typed 'all' but the auto set amount is 0
                        if (shopHaveSpaces == 0) {
                            // when typed 'all' but the shop doesn't have any empty space
                            plugin.text().of(p, "shop-has-no-space", Integer.toString(shopHaveSpaces),
                                    MsgUtil.getTranslateText(shop.getItem())).send();
                            return;
                        }
                        if (ownerCanAfford == 0
                                && (!shop.isUnlimited()
                                || plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners"))) {
                            // when typed 'all' but the shop owner doesn't have enough money to buy at least 1
                            // item (and shop isn't unlimited or pay-unlimited is true)
                            plugin.text().of(p, "the-owner-cant-afford-to-buy-from-you",
                                    Objects.requireNonNull(
                                            format(shop.getPrice(), shop.getLocation().getWorld(),
                                                    shop.getCurrency())),
                                    Objects.requireNonNull(
                                            format(ownerBalance, shop.getLocation().getWorld(),
                                                    shop.getCurrency()))).send();
                            return;
                        }
                        // when typed 'all' but player doesn't have any items to sell
                        plugin.text().of(p, "you-dont-have-that-many-items",
                                Integer.toString(amount),
                                MsgUtil.getTranslateText(shop.getItem())).send();
                        return;
                    }
                } else {
                    // instead of output cancelled message (when typed neither integer or 'all'), just let
                    // player know that there should be positive number or 'all'
                    plugin.text().of(p, "not-a-integer", message).send();
                    Util.debugLog(
                            "Receive the chat " + message + " and it format failed: " + message);
                    return;
                }
            }
            actionBuy(p.getUniqueId(), new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, amount);
        } else if (shop.isSelling()) {
            if (StringUtils.isNumeric(message)) {
                amount = Integer.parseInt(message);
            } else {
                if (message.equalsIgnoreCase(plugin.getConfig().getString("shop.word-for-trade-all-items", "all"))) {
                    int shopHaveItems = shop.getRemainingStock();
                    int invHaveSpaces = new BukkitInventoryWrapper(p.getInventory()).countSpace(shop);
                    if (shop.isAlwaysCountingContainer() || !shop.isUnlimited()) {
                        amount = Math.min(shopHaveItems, invHaveSpaces);
                    } else {
                        // should check not having items but having empty slots, cause player is trying to buy
                        // items from the shop.
                        amount = new BukkitInventoryWrapper(p.getInventory()).countSpace(shop);
                    }
                    // typed 'all', check if player has enough money than price * amount
                    double price = shop.getPrice();
                    double balance = eco.getBalance(p.getUniqueId(), shop.getLocation().getWorld(),
                            shop.getCurrency());
                    amount = Math.min(amount, (int) Math.floor(balance / price));
                    if (amount < 1) { // typed 'all' but the auto set amount is 0
                        // when typed 'all' but player can't buy any items
                        if ((shop.isAlwaysCountingContainer() || !shop.isUnlimited()) && shopHaveItems < 1) {
                            // but also the shop's stock is 0
                            plugin.text().of(p, "shop-stock-too-low",
                                    Integer.toString(shop.getRemainingStock()),
                                    MsgUtil.getTranslateText(shop.getItem())).send();
                        } else {
                            // when if player's inventory is full
                            if (invHaveSpaces <= 0) {
                                plugin.text().of(p, "not-enough-space",
                                        String.valueOf(invHaveSpaces)).send();
                                return;
                            }
                            plugin.text().of(p, "you-cant-afford-to-buy",
                                    Objects.requireNonNull(
                                            format(price, shop.getLocation().getWorld(),
                                                    shop.getCurrency())),
                                    Objects.requireNonNull(
                                            format(balance, shop.getLocation().getWorld(),
                                                    shop.getCurrency()))).send();
                        }
                        return;
                    }
                } else {
                    // instead of output cancelled message, just let player know that there should be positive
                    // number or 'all'
                    plugin.text().of(p, "not-a-integer", message).send();
                    Util.debugLog(
                            "Receive the chat " + message + " and it format failed: " + message);
                    return;
                }
            }
            actionSell(p.getUniqueId(), new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, amount);
        } else {
            plugin.text().of(p, "shop-purchase-cancelled").send();
            plugin.getLogger().warning("Shop data broken? Loc:" + shop.getLocation());
        }
    }

    @Nullable
    public Shop findShopIncludeAttached(@NotNull Location loc, boolean fromAttach) {
        Shop shop = getShop(loc);

        // failed, get attached shop
        if (shop == null) {
            Block block = loc.getBlock();
            if (!Util.isShoppables(block.getType())) {
                return null;
            }
            final Block currentBlock = loc.getBlock();
            if (!fromAttach) {
                // sign
                if (Util.isWallSign(currentBlock.getType())) {
                    final Block attached = Util.getAttached(currentBlock);
                    if (attached != null) {
                        shop = this.findShopIncludeAttached(attached.getLocation(), true);
                    }
                } else {
                    // optimize for performance
                    BlockState state = PaperLib.getBlockState(currentBlock, false).getState();
                    if (!(state instanceof Container)) {
                        return null;
                    }
                    @Nullable final Block half = Util.getSecondHalf(currentBlock);
                    if (half != null) {
                        shop = getShop(half.getLocation());
                    }
                }
            }
        }
        // add cache if using
        if (plugin.getShopCache() != null) {
            plugin.getShopCache().setCache(loc, shop);
        }
        return shop;
    }

    /**
     * Change the owner to unlimited shop owner.
     * It defined in configuration.
     */
    @Override
    public void migrateOwnerToUnlimitedShopOwner(Shop shop) {
        shop.setOwner(this.cacheUnlimitedShopAccount.getUniqueId());
        shop.setSignText();
    }

    @Override
    public @NotNull PriceLimiter getPriceLimiter() {
        return this.priceLimiter;
    }

    public class ShopIterator implements Iterator<Shop> {

        private final Iterator<Map<ShopChunk, Map<Location, Shop>>> worlds;

        private Iterator<Map<Location, Shop>> chunks;

        private Iterator<Shop> shops;

        public ShopIterator() {
            worlds = getShops().values().iterator();
        }

        /**
         * Returns true if there is still more shops to iterate over.
         */
        @Override
        public boolean hasNext() {
            if (shops == null || !shops.hasNext()) {
                if (chunks == null || !chunks.hasNext()) {
                    if (!worlds.hasNext()) {
                        return false;
                    } else {
                        chunks = worlds.next().values().iterator();
                        return hasNext();
                    }
                } else {
                    shops = chunks.next().values().iterator();
                    return hasNext();
                }
            }
            return true;
        }

        /**
         * Fetches the next shop. Throws NoSuchElementException if there are no more shops.
         */
        @Override
        public @NotNull Shop next() {
            if (shops == null || !shops.hasNext()) {
                if (chunks == null || !chunks.hasNext()) {
                    if (!worlds.hasNext()) {
                        throw new NoSuchElementException("No more shops to iterate over!");
                    }
                    chunks = worlds.next().values().iterator();
                }
                shops = chunks.next().values().iterator();
            }
            if (!shops.hasNext()) {
                return this.next(); // Skip to the next one (Empty iterator?)
            }
            return shops.next();
        }
    }
}
