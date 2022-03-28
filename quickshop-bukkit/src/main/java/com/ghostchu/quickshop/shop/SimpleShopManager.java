/*
 *  This file is a part of project QuickShop, the name is SimpleShopManager.java
 *  Copyright (C) Ghost_chu and contributors
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

package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.economy.AbstractEconomy;
import com.ghostchu.quickshop.api.economy.EconomyTransaction;
import com.ghostchu.quickshop.api.event.*;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.shop.*;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapper;
import com.ghostchu.quickshop.util.*;
import com.ghostchu.quickshop.util.economyformatter.EconomyFormatter;
import com.ghostchu.quickshop.util.holder.Result;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private UUID cacheTaxAccount;
    @Getter
    private UUID cacheUnlimitedShopAccount;
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
                this.cacheTaxAccount = UUID.fromString(taxAccount);
            } else {
                this.cacheTaxAccount = Bukkit.getOfflinePlayer(taxAccount).getUniqueId();
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
                cacheUnlimitedShopAccount = UUID.fromString(uAccount);
            } else {
                cacheUnlimitedShopAccount = Bukkit.getOfflinePlayer(uAccount).getUniqueId();
            }
        }
        this.priceLimiter = new SimplePriceLimiter(plugin);
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
                plugin.text().of(p, "reached-maximum-can-create", Component.text(owned), Component.text(max)).send();
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
        if (signIsWatered && (signBlockData instanceof Waterlogged waterable)) {
            waterable.setWaterlogged(true); // Looks like sign directly put in water
        }
        if (signBlockData instanceof WallSign wallSignBlockData) {
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
                    boolean backupSuccess = false; // Database backup
                    if (backupSuccess) {
                        plugin.getDatabaseHelper().removeShop(shop);
                    } else {
                        plugin.getLogger().warning(
                                "Failed to backup the database, all changes will revert after a reboot.");
                    }
                    plugin.getDatabaseHelper().createShop(shop, null, e2 -> {
                        plugin.getLogger()
                                .log(Level.SEVERE, "Shop create failed, auto fix failed, the changes may won't commit to database.", e2);
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
     * Format the price use formatter
     *
     * @param d price
     * @return formatted price
     */
    @Override
    public @NotNull String format(double d, @NotNull World world, @Nullable String currency) {
        return formatter.format(d, world, currency);
    }

    /**
     * Format the price use formatter
     *
     * @param d price
     * @return formatted price
     */
    @Override
    public @NotNull String format(double d, @NotNull Shop shop) {
        return formatter.format(d, shop);
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
            if (info.getAction().isCreating()) {
                actionCreate(p, info, finalMessage);
            }
            if (info.getAction().isTrading()) {
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
     * Adds (register) a shop to the world. Does NOT require the chunk or world to be loaded Call shop.onLoad
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

    @Override
    public void actionBuying(
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
            plugin.text().of(buyer, "shop-has-no-space", Component.text(space), MsgUtil.getTranslateText(shop.getItem())).send();
            return;
        }
        int count = Util.countItems(buyerInventory, shop);
        // Not enough items
        if (amount > count) {
            plugin.text().of(buyer,
                    "you-dont-have-that-many-items",
                    Component.text(count),
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
            plugin.text().of(buyer, "plugin-cancelled", e.getCancelReason()).send();
            return; // Cancelled
        } else {
            total = e.getTotal(); // Allow addon to set it
        }
        UUID taxAccount = null;
        if (shop.getTaxAccount() != null) {
            taxAccount = shop.getTaxAccount();
        } else {
            if (this.cacheTaxAccount != null)
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
        if (!transaction.checkBalance()) {
            plugin.text().of(buyer, "the-owner-cant-afford-to-buy-from-you",
                    Objects.requireNonNull(format(total, shop.getLocation().getWorld(), shop.getCurrency())),
                    Objects.requireNonNull(format(eco.getBalance(shop.getOwner(), shop.getLocation().getWorld(),
                            shop.getCurrency()), shop.getLocation().getWorld(), shop.getCurrency()))).send();
            return;
        }
        if (!transaction.failSafeCommit()) {
            plugin.text().of(buyer, "economy-transaction-failed", transaction.getLastError()).send();
            plugin.getLogger().severe("EconomyTransaction Failed, last error:" + transaction.getLastError());
            plugin.getLogger().severe("Tips: If you see any economy plugin name appears above, please don't ask QuickShop support. Contact with developer of economy plugin. QuickShop didn't process the transaction, we only receive the transaction result from your economy plugin.");
            return;
        }
        Player player = plugin.getServer().getPlayer(buyer);
        try {
            shop.buy(buyer, buyerInventory, player != null ? player.getLocation() : shop.getLocation(), amount);
        } catch (Exception shopError) {
            plugin.getLogger().log(Level.WARNING, "Failed to processing purchase, rolling back...", shopError);
            transaction.rollback(true);
            plugin.text().of(buyer, "shop-transaction-failed", shopError.getMessage()).send();
            return;
        }
        sendSellSuccess(buyer, shop, amount, total, CalculateUtil.subtract(1, taxModifier));
        ShopSuccessPurchaseEvent se = new ShopSuccessPurchaseEvent(shop, buyer, buyerInventory, amount, total, taxModifier);
        plugin.getServer().getPluginManager().callEvent(se);
        shop.setSignText(); // Update the signs count
        notifySold(buyer, shop, amount, space);
    }

    private void notifySold(@NotNull UUID buyer, @NotNull Shop shop, int amount, int space) {
        Player player = plugin.getServer().getPlayer(buyer);
        plugin.getDatabaseHelper().getPlayerLocale(shop.getOwner(), (locale) -> {
            String langCode = MsgUtil.getDefaultGameLanguageCode();
            if (locale.isPresent())
                langCode = locale.get();
            Component msg = plugin.text().of("player-sold-to-your-store", player != null ? player.getName() : buyer.toString(),
                            amount,
                            MsgUtil.getTranslateText(shop.getItem())).forLocale(langCode)
                    .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()));
            if (space == amount) {
                if (shop.getShopName() == null) {
                    msg = plugin.text().of("shop-out-of-space",
                                    shop.getLocation().getBlockX(),
                                    shop.getLocation().getBlockY(),
                                    shop.getLocation().getBlockZ()).forLocale(langCode)
                            .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()));
                } else {
                    msg = plugin.text().of("shop-out-of-space-name", shop.getShopName(),
                                    MsgUtil.getTranslateText(shop.getItem())).forLocale(langCode)
                            .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()));
                }
                if (plugin.getConfig().getBoolean("shop.sending-stock-message-to-staffs")) {
                    for (UUID staff : shop.getModerator().getStaffs()) {
                        MsgUtil.send(shop, staff, msg);
                    }
                }
                MsgUtil.send(shop, shop.getOwner(), msg);
            }
            if (plugin.getConfig().getBoolean("shop.sending-stock-message-to-staffs")) {
                for (UUID staff : shop.getModerator().getStaffs()) {
                    MsgUtil.send(shop, staff, msg);
                }
            }
            MsgUtil.send(shop, shop.getOwner(), msg);
        });

    }


    @Deprecated
    public void actionBuying(@NotNull Player p, @NotNull AbstractEconomy eco, @NotNull SimpleInfo info,
                             @NotNull Shop shop, int amount) {
        Util.ensureThread(false);
        actionBuying(p.getUniqueId(), new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, amount);
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

    @Override
    public void actionCreate(@NotNull Player p, Info info, @NotNull String message) {
        Util.ensureThread(false);
        if (plugin.getEconomy() == null) {
            MsgUtil.sendDirectMessage(p, Component.text("Error: Economy system not loaded, type /qs main command to get details.").color(NamedTextColor.RED));
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
        if (info.getLocation().getBlock().getType() == Material.ENDER_CHEST) { // FIXME: Need a better impl
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
                    plugin.text().of(p, "digits-reach-the-limit", Component.text(maximumDigitsLimit)).send();
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

        PriceLimiterCheckResult priceCheckResult = this.priceLimiter.check(p, info.getItem(), plugin.getCurrency(), price);

        switch (priceCheckResult.getStatus()) {
            case REACHED_PRICE_MIN_LIMIT -> {
                plugin.text().of(p, "price-too-cheap",
                        Component.text((decFormat) ? MsgUtil.decimalFormat(priceCheckResult.getMax())
                                : Double.toString(priceCheckResult.getMin()))).send();
                return;
            }
            case REACHED_PRICE_MAX_LIMIT -> {
                plugin.text().of(p, "price-too-high",
                        Component.text((decFormat) ? MsgUtil.decimalFormat(priceCheckResult.getMax())
                                : Double.toString(priceCheckResult.getMin()))).send();
                return;
            }
            case PRICE_RESTRICTED -> {
                plugin.text().of(p, "restricted-prices",
                        MsgUtil.getTranslateText(info.getItem()),
                        Component.text(priceCheckResult.getMin()),
                        Component.text(priceCheckResult.getMax())).send();
                return;
            }
            case NOT_VALID -> {
                plugin.text().of(p, "not-a-number", message).send();
                return;
            }
            case NOT_A_WHOLE_NUMBER -> {
                plugin.text().of(p, "not-a-integer", message).send();
                return;
            }
        }

        // Set to 1 when disabled stacking shop
        if (!plugin.isAllowStack()) {
            info.getItem().setAmount(1);
        }

        // Create the sample shop
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
                plugin.getInventoryWrapperManager().mklink(new BukkitInventoryWrapper(((InventoryHolder) info.getLocation().getBlock().getState()).getInventory())),
                null);

        // Calling ShopCreateEvent
        ShopCreateEvent shopCreateEvent = new ShopCreateEvent(shop, p.getUniqueId());
        if (Util.fireCancellableEvent(shopCreateEvent)) {
            plugin.text().of(p, "plugin-cancelled", shopCreateEvent.getCancelReason()).send();
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
            if (!economyTransaction.checkBalance()) {
                plugin.text().of(p, "you-cant-afford-a-new-shop",
                        Objects.requireNonNull(format(createCost, shop.getLocation().getWorld(),
                                shop.getCurrency()))).send();
                return;
            }
            if (!economyTransaction.failSafeCommit()) {
                plugin.text().of(p, "economy-transaction-failed", economyTransaction.getLastError()).send();
                plugin.getLogger().severe("EconomyTransaction Failed, last error:" + economyTransaction.getLastError());
                plugin.getLogger().severe("Tips: If you see any economy plugin name appears above, please don't ask QuickShop support. Contact with developer of economy plugin. QuickShop didn't process the transaction, we only receive the transaction result from your economy plugin.");
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
            if (Objects.requireNonNull(nextTo).getPrice() > shop.getPrice() && (shop.isBuying() == nextTo.isSelling()) && shop.matches(nextTo.getItem())) { // different type compare
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
    public void actionSelling(
            @NotNull Player p, @NotNull AbstractEconomy eco, @NotNull SimpleInfo info, @NotNull Shop shop,
            int amount) {
        Util.ensureThread(false);
        actionSelling(p.getUniqueId(), new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, amount);
    }

    @Override
    public void actionSelling(
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
            plugin.text().of(seller, "shop-stock-too-low", Component.text(stock),
                    MsgUtil.getTranslateText(shop.getItem())).send();
            return;
        }
        int playerSpace = Util.countSpace(sellerInventory, shop);
        if (playerSpace < amount) {
            plugin.text().of(seller, "inventory-space-full", amount, playerSpace).send();
            return;
        }
        if (amount < 1) {
            // & Dumber
            plugin.text().of(seller, "negative-amount").send();
            return;
        }
        int pSpace = Util.countSpace(sellerInventory, shop);
        if (amount > pSpace) {
            plugin.text().of(seller, "not-enough-space", Component.text(pSpace)).send();
            return;
        }

        double taxModifier = getTax(shop, seller);
        double total = CalculateUtil.multiply(amount, shop.getPrice());

        ShopPurchaseEvent e = new ShopPurchaseEvent(shop, seller, sellerInventory, amount, total);
        if (Util.fireCancellableEvent(e)) {
            plugin.text().of(seller, "plugin-cancelled", e.getCancelReason()).send();
            return; // Cancelled
        } else {
            total = e.getTotal(); // Allow addon to set it
        }
        // Money handling
        // SELLING Player -> Shop Owner
        EconomyTransaction transaction;
        UUID taxAccount = null;
        if (shop.getTaxAccount() != null) {
            taxAccount = shop.getTaxAccount();
        } else {
            if (this.cacheTaxAccount != null) {
                taxAccount = this.cacheTaxAccount;
            }
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
        if (!transaction.checkBalance()) {
            plugin.text().of(seller, "you-cant-afford-to-buy",
                    Objects.requireNonNull(
                            format(total, shop.getLocation().getWorld(), shop.getCurrency())),
                    Objects.requireNonNull(format(
                            eco.getBalance(seller, shop.getLocation().getWorld(),
                                    shop.getCurrency()), shop.getLocation().getWorld(),
                            shop.getCurrency()))).send();
            return;
        }
        if (!transaction.failSafeCommit()) {
            plugin.text().of(seller, "economy-transaction-failed", transaction.getLastError()).send();
            plugin.getLogger().severe("EconomyTransaction Failed, last error:" + transaction.getLastError());
            return;
        }
        Player player = plugin.getServer().getPlayer(seller);
        try {
            shop.sell(seller, sellerInventory, player != null ? player.getLocation() : shop.getLocation(), amount);
        } catch (Exception shopError) {
            plugin.getLogger().log(Level.WARNING, "Failed to processing purchase, rolling back...", shopError);
            transaction.rollback(true);
            plugin.text().of(seller, "shop-transaction-failed", shopError.getMessage()).send();
            return;
        }
        sendPurchaseSuccess(seller, shop, amount, total, CalculateUtil.subtract(1, taxModifier));
        ShopSuccessPurchaseEvent se = new ShopSuccessPurchaseEvent(shop, seller, sellerInventory, amount, total, taxModifier);
        plugin.getServer().getPluginManager().callEvent(se);
        notifyBought(seller, shop, amount, stock, taxModifier, total);
    }

    private void notifyBought(@NotNull UUID seller, @NotNull Shop shop, int amount, int stock, double taxModifier, double total) {
        Player player = plugin.getServer().getPlayer(seller);
        plugin.getDatabaseHelper().getPlayerLocale(shop.getOwner(), (locale) -> {
            String langCode = MsgUtil.getDefaultGameLanguageCode();
            if (locale.isPresent())
                langCode = locale.get();
            Component msg;
            if (plugin.getConfig().getBoolean("show-tax")) {
                msg = plugin.text().of("player-bought-from-your-store-tax",
                                player != null ? player.getName() : seller.toString(),
                                amount * shop.getItem().getAmount(),
                                shop.getItem(),
                                this.formatter.format(CalculateUtil.multiply(CalculateUtil.subtract(1, taxModifier), total), shop),
                                this.formatter.format(CalculateUtil.multiply(taxModifier, total), shop)).forLocale(langCode)
                        .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()));
            } else {
                msg = plugin.text().of("player-bought-from-your-store",
                                player != null ? player.getName() : seller.toString(),
                                amount * shop.getItem().getAmount(),
                                MsgUtil.getTranslateText(shop.getItem()),
                                this.formatter.format(CalculateUtil.multiply(CalculateUtil.subtract(1, taxModifier), total), shop)).forLocale(langCode)
                        .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()));
            }

            MsgUtil.send(shop, shop.getOwner(), msg);
            if (plugin.getConfig().getBoolean("shop.sending-stock-message-to-staffs")) {
                for (UUID staff : shop.getModerator().getStaffs()) {
                    MsgUtil.send(shop, staff, msg);
                }
            }
            // Transfers the item from A to B
            if (stock == amount) {
                if (shop.getShopName() == null) {
                    msg = plugin.text().of("shop-out-of-stock",
                                    shop.getLocation().getBlockX(),
                                    shop.getLocation().getBlockY(),
                                    shop.getLocation().getBlockZ(),
                                    MsgUtil.getTranslateText(shop.getItem())).forLocale(langCode)
                            .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()));
                } else {
                    msg = plugin.text().of("shop-out-of-stock-name", shop.getShopName(),
                                    MsgUtil.getTranslateText(shop.getItem())).forLocale(langCode)
                            .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()));
                }
                MsgUtil.send(shop, shop.getOwner(), msg);
                if (plugin.getConfig().getBoolean("shop.sending-stock-message-to-staffs")) {
                    for (UUID staff : shop.getModerator().getStaffs()) {
                        MsgUtil.send(shop, staff, msg);
                    }
                }
            }
        });
    }

    /**
     * Send a purchaseSuccess message for a player.
     *
     * @param purchaser Target player
     * @param shop      Target shop
     * @param amount    Trading item amounts.
     */
    @Override
    public void sendPurchaseSuccess(@NotNull UUID purchaser, @NotNull Shop shop, int amount, double total, double tax) {
        Player sender = Bukkit.getPlayer(purchaser);
        if (sender == null) {
            return;
        }
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(sender);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(plugin.text().of(sender, "menu.successful-purchase").forLocale());
        chatSheetPrinter.printLine(plugin.text().of(sender, "menu.item-name-and-price", Component.text(amount * shop.getItem().getAmount()), MsgUtil.getTranslateText(shop.getItem()), Objects.requireNonNull(format(total, shop))).forLocale());
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
    public void sendSellSuccess(@NotNull UUID seller, @NotNull Shop shop, int amount, double total, double tax) {
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
                        amount,
                        MsgUtil.getTranslateText(shop.getItem()),
                        Objects.requireNonNull(format(total, shop))).forLocale());
        if (plugin.getConfig().getBoolean("show-tax")) {
            if (tax != 0) {
                if (!seller.equals(shop.getOwner())) {
                    chatSheetPrinter.printLine(
                            plugin.text().of(sender, "menu.sell-tax", Objects.requireNonNull(format(tax * total, shop))).forLocale());
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
        chatSheetPrinter.printLine(plugin.text().of(p, "menu.item", MsgUtil.getTranslateText(shop.getItem())).forLocale()
                .append(Component.text("   "))
                .append(plugin.text().of(p, "menu.preview", Component.text(shop.getItem().getAmount())).forLocale())
                .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, MsgUtil.fillArgs("/qs silentpreview {0}", shop.getRuntimeRandomUniqueId().toString())))
        );
        if (Util.isTool(items.getType())) {
            chatSheetPrinter.printLine(
                    plugin.text().of(p, "menu.damage-percent-remaining", Component.text(Util.getToolPercentage(items))).forLocale());
        }
        if (shop.isSelling()) {
            if (shop.getRemainingStock() == -1) {
                chatSheetPrinter.printLine(
                        plugin.text().of(p, "menu.stock", plugin.text().of(p, "signs.unlimited").forLocale()).forLocale());
            } else {
                chatSheetPrinter.printLine(
                        plugin.text().of(p, "menu.stock", Component.text(shop.getRemainingStock())).forLocale());
            }
        } else {
            if (shop.getRemainingSpace() == -1) {
                chatSheetPrinter.printLine(
                        plugin.text().of(p, "menu.space", plugin.text().of(p, "signs.unlimited").forLocale()).forLocale());
            } else {
                chatSheetPrinter.printLine(
                        plugin.text().of(p, "menu.space", Component.text(shop.getRemainingSpace())).forLocale());
            }
        }
        if (shop.getItem().getAmount() == 1) {
            chatSheetPrinter.printLine(plugin.text().of(p, "menu.price-per", MsgUtil.getTranslateText(shop.getItem()), format(shop.getPrice(), shop)).forLocale());
        } else {
            chatSheetPrinter.printLine(plugin.text().of(p, "menu.price-per-stack", MsgUtil.getTranslateText(shop.getItem()), format(shop.getPrice(), shop), shop.getItem().getAmount()).forLocale());
        }
        if (shop.isBuying()) {
            chatSheetPrinter.printLine(plugin.text().of(p, "menu.this-shop-is-buying").forLocale());
        } else {
            chatSheetPrinter.printLine(plugin.text().of(p, "menu.this-shop-is-selling").forLocale());
        }
        MsgUtil.printEnchantment(p, shop, chatSheetPrinter);
        if (items.getItemMeta() instanceof PotionMeta potionMeta) {
            PotionData potionData = potionMeta.getBasePotionData();
            PotionEffectType potionEffectType = potionData.getType().getEffectType();
            if (potionEffectType != null) {
                chatSheetPrinter.printLine(plugin.text().of(p, "menu.effects").forLocale());
                //Because the bukkit API limit, we can't get the actual effect level
                chatSheetPrinter.printLine(Component.empty()
                        .color(NamedTextColor.YELLOW)
                        .append(plugin.getPlatform().getTranslation(potionEffectType))
                );
            }
            if (potionMeta.hasCustomEffects()) {
                for (PotionEffect potionEffect : potionMeta.getCustomEffects()) {
                    int level = potionEffect.getAmplifier();
                    chatSheetPrinter.printLine(Component.empty()
                            .color(NamedTextColor.YELLOW)
                            .append(plugin.getPlatform().getTranslation(potionEffect.getType())).append(LegacyComponentSerializer.legacySection().deserialize(" " + (level <= 10 ? RomanNumber.toRoman(level) : level))));
                }
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
            MsgUtil.sendDirectMessage(p, Component.text("Error: Economy system not loaded, type /qs main command to get details.").color(NamedTextColor.RED));
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
            MsgUtil.sendDirectMessage(p, Component.text("Error: Economy system not loaded, type /qs main command to get details.").color(NamedTextColor.RED));
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
                    amount = buyingShopAllCalc(eco, shop, p);
                } else {
                    // instead of output cancelled message (when typed neither integer or 'all'), just let
                    // player know that there should be positive number or 'all'
                    plugin.text().of(p, "not-a-integer", message).send();
                    Util.debugLog(
                            "Receive the chat " + message + " and it format failed: " + message);
                    return;
                }
            }
            actionBuying(p.getUniqueId(), new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, amount);
        } else if (shop.isSelling()) {
            if (StringUtils.isNumeric(message)) {
                amount = Integer.parseInt(message);
            } else {
                if (message.equalsIgnoreCase(plugin.getConfig().getString("shop.word-for-trade-all-items", "all"))) {
                    amount = sellingShopAllCalc(eco, shop, p);
                } else {
                    // instead of output cancelled message, just let player know that there should be positive
                    // number or 'all'
                    plugin.text().of(p, "not-a-integer", message).send();
                    Util.debugLog(
                            "Receive the chat " + message + " and it format failed: " + message);
                    return;
                }
            }
            actionSelling(p.getUniqueId(), new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, amount);
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

    private int sellingShopAllCalc(@NotNull AbstractEconomy eco, @NotNull Shop shop, @NotNull Player p) {
        int amount;
        int shopHaveItems = shop.getRemainingStock();
        int invHaveSpaces = Util.countSpace(new BukkitInventoryWrapper(p.getInventory()), shop);
        if (shop.isAlwaysCountingContainer() || !shop.isUnlimited()) {
            amount = Math.min(shopHaveItems, invHaveSpaces);
        } else {
            // should check not having items but having empty slots, cause player is trying to buy
            // items from the shop.
            amount = Util.countSpace(new BukkitInventoryWrapper(p.getInventory()), shop);
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
                        Component.text(shop.getRemainingStock()),
                        MsgUtil.getTranslateText(shop.getItem())).send();
                return 0;
            } else {
                // when if player's inventory is full
                if (invHaveSpaces <= 0) {
                    plugin.text().of(p, "not-enough-space",
                            Component.text(invHaveSpaces)).send();
                    return 0;
                }
                plugin.text().of(p, "you-cant-afford-to-buy",
                        Objects.requireNonNull(
                                plugin.getShopManager().format(price, shop.getLocation().getWorld(),
                                        shop.getCurrency())),
                        Objects.requireNonNull(
                                plugin.getShopManager().format(balance, shop.getLocation().getWorld(),
                                        shop.getCurrency()))).send();
            }
            return 0;
        }
        return amount;
    }

    private int buyingShopAllCalc(@NotNull AbstractEconomy eco, @NotNull Shop shop, @NotNull Player p) {
        int amount;
        int shopHaveSpaces =
                Util.countSpace(shop.getInventory(), shop);
        int invHaveItems = Util.countItems(new BukkitInventoryWrapper(p.getInventory()), shop);
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
            amount = Util.countItems(new BukkitInventoryWrapper(p.getInventory()), shop);
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
                plugin.text().of(p, "shop-has-no-space", Component.text(shopHaveSpaces),
                        MsgUtil.getTranslateText(shop.getItem())).send();
                return 0;
            }
            if (ownerCanAfford == 0
                    && (!shop.isUnlimited()
                    || plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners"))) {
                // when typed 'all' but the shop owner doesn't have enough money to buy at least 1
                // item (and shop isn't unlimited or pay-unlimited is true)
                plugin.text().of(p, "the-owner-cant-afford-to-buy-from-you",
                        Objects.requireNonNull(
                                plugin.getShopManager().format(shop.getPrice(), shop.getLocation().getWorld(),
                                        shop.getCurrency())),
                        Objects.requireNonNull(
                                plugin.getShopManager().format(ownerBalance, shop.getLocation().getWorld(),
                                        shop.getCurrency()))).send();
                return 0;
            }
            // when typed 'all' but player doesn't have any items to sell
            plugin.text().of(p, "you-dont-have-that-many-items",
                    Component.text(amount),
                    MsgUtil.getTranslateText(shop.getItem())).send();
            return 0;
        }
        return amount;
    }

    /**
     * Change the owner to unlimited shop owner.
     * It defined in configuration.
     */
    @Override
    public void migrateOwnerToUnlimitedShopOwner(Shop shop) {
        shop.setOwner(this.cacheUnlimitedShopAccount);
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
