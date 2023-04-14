package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.economy.AbstractEconomy;
import com.ghostchu.quickshop.api.event.*;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.*;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CalculateUtil;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.common.util.RomanNumber;
import com.ghostchu.quickshop.economy.SimpleBenefit;
import com.ghostchu.quickshop.economy.SimpleEconomyTransaction;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapper;
import com.ghostchu.quickshop.util.ChatSheetPrinter;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.economyformatter.EconomyFormatter;
import com.ghostchu.quickshop.util.holder.Result;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.papermc.lib.PaperLib;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Manage a lot of shops.
 */
public class SimpleShopManager implements ShopManager, Reloadable {

    private static final DecimalFormat STANDARD_FORMATTER = new DecimalFormat("#.#########");
    private final Map<String, Map<ShopChunk, Map<Location, Shop>>> shops = Maps.newConcurrentMap();
    private final Set<Shop> loadedShops = Sets.newConcurrentHashSet();
    private final InteractiveManager interactiveManager;
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
    private int maximumDigitsLimit;
    private boolean allowNoSpaceForSign;
    private boolean useDecFormat;
    private double shopCreateCost;
    private boolean useShopLock;
    private double globalTax;
    private boolean showTax;
    private boolean payUnlimitedShopOwner;
    private String tradeAllKeyword;
    private boolean disableCreativePurchase;
    private boolean sendStockMessageToStaff;

    public SimpleShopManager(@NotNull QuickShop plugin) {
        Util.ensureThread(false);
        this.plugin = plugin;
        this.interactiveManager = new InteractiveManager(plugin);
        this.formatter = new EconomyFormatter(plugin, plugin.getEconomy());
        plugin.getReloadManager().register(this);
        init();
    }

    private void init() {
        Log.debug("Loading caching tax account...");
        String taxAccount = plugin.getConfig().getString("tax-account", "tax");
        if (!taxAccount.isEmpty()) {
            if (CommonUtil.isUUID(taxAccount)) {
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
                plugin.logger().warn("unlimited-shop-owner-change-account is empty, default to \"quickshop\"");
            }
            if (CommonUtil.isUUID(uAccount)) {
                cacheUnlimitedShopAccount = UUID.fromString(uAccount);
            } else {
                cacheUnlimitedShopAccount = plugin.getPlayerFinder().name2Uuid(uAccount);
            }
        }
        this.priceLimiter = new SimplePriceLimiter(plugin);
        this.useOldCanBuildAlgorithm = plugin.getConfig().getBoolean("limits.old-algorithm");
        this.autoSign = plugin.getConfig().getBoolean("shop.auto-sign");
        this.maximumDigitsLimit = plugin.getConfig().getInt("maximum-digits-in-price", -1);
        this.allowNoSpaceForSign = plugin.getConfig().getBoolean("shop.allow-shop-without-space-for-sign");
        this.useDecFormat = plugin.getConfig().getBoolean("use-decimal-format");
        this.shopCreateCost = plugin.getConfig().getDouble("shop.cost");
        this.useShopLock = plugin.getConfig().getBoolean("shop.lock");
        this.globalTax = plugin.getConfig().getDouble("tax");
        this.showTax = plugin.getConfig().getBoolean("show-tax");
        this.payUnlimitedShopOwner = plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners");
        this.tradeAllKeyword = plugin.getConfig().getString("shop.word-for-trade-all-items", "all");
        this.disableCreativePurchase = plugin.getConfig().getBoolean("shop.disable-creative-mode-trading");
        this.sendStockMessageToStaff = plugin.getConfig().getBoolean("shop.sending-stock-message-to-staffs");
    }

    @Deprecated
    public void actionBuying(@NotNull Player p, @NotNull AbstractEconomy eco, @NotNull SimpleInfo info,
                             @NotNull Shop shop, int amount) {
        Util.ensureThread(false);
        actionBuying(p.getUniqueId(), new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, amount);
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

        Player player = Bukkit.getPlayer(buyer);
        if (player != null) {
            if (!plugin.perm().hasPermission(player, "quickshop.other.use") && !shop.playerAuthorize(buyer, BuiltInShopPermission.PURCHASE)) {
                plugin.text().of("no-permission").send();
                return;
            }
        } else {
            if (!shop.playerAuthorize(buyer, BuiltInShopPermission.PURCHASE)) {
                plugin.text().of("no-permission").send();
                return;
            }
        }
        if (shopIsNotValid(buyer, info, shop)) {
            return;
        }
        int space = shop.getRemainingSpace();
        if (space == -1) {
            space = 10000;
        }
        if (space < amount) {
            plugin.text().of(buyer, "shop-has-no-space", Component.text(space), Util.getItemStackName(shop.getItem())).send();
            return;
        }
        int count = Util.countItems(buyerInventory, shop);
        // Not enough items
        if (amount > count) {
            plugin.text().of(buyer,
                    "you-dont-have-that-many-items",
                    Component.text(count),
                    Util.getItemStackName(shop.getItem())).send();
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
            if (this.cacheTaxAccount != null) {
                taxAccount = this.cacheTaxAccount;
            }
        }
        SimpleEconomyTransaction transaction;
        SimpleEconomyTransaction.SimpleEconomyTransactionBuilder builder = SimpleEconomyTransaction.builder()
                .core(eco)
                .amount(total)
                .taxModifier(taxModifier)
                .taxAccount(taxAccount)
                .currency(shop.getCurrency())
                .world(shop.getLocation().getWorld())
                .to(buyer);
        if (shop.isUnlimited() && plugin.getConfig().getBoolean("tax-free-for-unlimited-shop", false)) {
            builder.taxModifier(0.0d);
        }
        if (!shop.isUnlimited()
                || (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")
                && shop.isUnlimited())) {
            transaction = builder.from(shop.getOwner()).build();
        } else {
            transaction = builder.from(null).build();
        }
        if (!transaction.checkBalance()) {
            plugin.text().of(buyer, "the-owner-cant-afford-to-buy-from-you",
                    format(total, shop.getLocation().getWorld(), shop.getCurrency()),
                    format(eco.getBalance(shop.getOwner(), shop.getLocation().getWorld(),
                            shop.getCurrency()), shop.getLocation().getWorld(), shop.getCurrency())).send();
            return;
        }
        if (!transaction.failSafeCommit()) {
            plugin.text().of(buyer, "economy-transaction-failed", transaction.getLastError()).send();
            plugin.logger().error("EconomyTransaction Failed, last error: {}", transaction.getLastError());
            plugin.logger().error("Tips: If you see any economy plugin name appears above, please don't ask QuickShop support. Contact with developer of economy plugin. QuickShop didn't process the transaction, we only receive the transaction result from your economy plugin.");
            return;
        }

        try {
            shop.buy(buyer, buyerInventory, player != null ? player.getLocation() : shop.getLocation(), amount);
        } catch (Exception shopError) {
            plugin.logger().warn("Failed to processing purchase, rolling back...", shopError);
            transaction.rollback(true);
            plugin.text().of(buyer, "shop-transaction-failed", shopError.getMessage()).send();
            return;
        }
        sendSellSuccess(buyer, shop, amount, total, transaction.getTax());
        new ShopSuccessPurchaseEvent(shop, buyer, buyerInventory, amount, total, transaction.getTax()).callEvent();
        shop.setSignText(plugin.text().findRelativeLanguages(buyer)); // Update the signs count
        notifySold(buyer, shop, amount, space);
    }

    private void notifySold(@NotNull UUID buyer, @NotNull Shop shop, int amount, int space) {
        Player player = Bukkit.getPlayer(buyer);
        plugin.getDatabaseHelper().getPlayerLocale(shop.getOwner()).whenCompleteAsync((locale, err) -> {
            String langCode = MsgUtil.getDefaultGameLanguageCode();
            if (locale != null) {
                // Language code override
                langCode = locale;
            }
            Component msg = plugin.text().of("player-sold-to-your-store", player != null ? player.getName() : buyer.toString(),
                            amount,
                            Util.getItemStackName(shop.getItem())).forLocale(langCode)
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
                                    Util.getItemStackName(shop.getItem())).forLocale(langCode)
                            .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()));
                }
                if (sendStockMessageToStaff) {
                    for (UUID recv : shop.playersCanAuthorize(BuiltInShopPermission.RECEIVE_ALERT)) {
                        MsgUtil.send(shop, recv, msg);
                    }
                } else {
                    MsgUtil.send(shop, shop.getOwner(), msg);
                }
            }
            if (sendStockMessageToStaff) {
                for (UUID recv : shop.playersCanAuthorize(BuiltInShopPermission.RECEIVE_ALERT)) {
                    MsgUtil.send(shop, recv, msg);
                }
            } else {
                MsgUtil.send(shop, shop.getOwner(), msg);
            }
        });
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

    @Override
    public void actionCreate(@NotNull Player p, Info info, @NotNull String message) {
        Util.ensureThread(false);
        if (plugin.getEconomy() == null) {
            MsgUtil.sendDirectMessage(p, Component.text("Error: Economy system not loaded, type /qs main command to get details.").color(NamedTextColor.RED));
            return;
        }

        // Price per item
        double price;
        try {
            price = Double.parseDouble(message);
            if (Double.isInfinite(price)) {
                plugin.text().of(p, "exceeded-maximum", message).send();
                return;
            }
            String strFormat = STANDARD_FORMATTER.format(Math.abs(price)).replace(",", ".");
            String[] processedDouble = strFormat.split("\\.");
            if (processedDouble.length > 1) {
                if (processedDouble[1].length() > maximumDigitsLimit && maximumDigitsLimit != -1) {
                    plugin.text().of(p, "digits-reach-the-limit", Component.text(maximumDigitsLimit)).send();
                    return;
                }
            }
        } catch (NumberFormatException ex) {
            Log.debug(ex.getMessage());
            plugin.text().of(p, "not-a-number", message).send();
            return;
        }

        if (info.getLocation().getBlock().getState() instanceof InventoryHolder holder) {
            // Create the basic shop
            ContainerShop shop = new ContainerShop(
                    plugin,
                    -1,
                    info.getLocation(),
                    price,
                    info.getItem(),
                    p.getUniqueId(),
                    false,
                    ShopType.SELLING,
                    new YamlConfiguration(),
                    null,
                    false,
                    null,
                    plugin.getJavaPlugin().getName(),
                    plugin.getInventoryWrapperManager().mklink(new BukkitInventoryWrapper((holder).getInventory())),
                    null,
                    Collections.emptyMap(),
                    new SimpleBenefit());
            createShop(shop, info.getSignBlock(), info.isBypassed());
        } else {
            plugin.text().of(p, "invalid-container").send();
        }
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

        Player player = Bukkit.getPlayer(seller);
        if (player != null) {
            if (!plugin.perm().hasPermission(player, "quickshop.other.use") && !shop.playerAuthorize(seller, BuiltInShopPermission.PURCHASE)) {
                plugin.text().of("no-permission").send();
                return;
            }
        } else {
            if (!shop.playerAuthorize(seller, BuiltInShopPermission.PURCHASE)) {
                plugin.text().of("no-permission").send();
                return;
            }
        }
        if (shopIsNotValid(seller, info, shop)) {
            return;
        }
        int stock = shop.getRemainingStock();
        if (stock == -1) {
            stock = 10000;
        }
        if (stock < amount) {
            plugin.text().of(seller, "shop-stock-too-low", Component.text(stock),
                    Util.getItemStackName(shop.getItem())).send();
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
        SimpleEconomyTransaction transaction;
        UUID taxAccount = null;
        if (shop.getTaxAccount() != null) {
            taxAccount = shop.getTaxAccount();
        } else {
            if (this.cacheTaxAccount != null) {
                taxAccount = this.cacheTaxAccount;
            }
        }
        SimpleEconomyTransaction.SimpleEconomyTransactionBuilder builder = SimpleEconomyTransaction.builder()
                .core(eco)
                .from(seller)
                .amount(total)
                .taxModifier(taxModifier)
                .taxAccount(taxAccount)
                .benefit(shop.getShopBenefit())
                .world(shop.getLocation().getWorld())
                .currency(shop.getCurrency());
        if (shop.isUnlimited() && plugin.getConfig().getBoolean("tax-free-for-unlimited-shop", false)) {
            builder.taxModifier(0.0d);
        }
        if (!shop.isUnlimited()
                || (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")
                && shop.isUnlimited())) {
            transaction = builder.to(shop.getOwner()).build();
        } else {
            transaction = builder.to(null).build();
        }

        if (!transaction.checkBalance()) {
            plugin.text().of(seller, "you-cant-afford-to-buy",
                    format(total, shop.getLocation().getWorld(), shop.getCurrency()),
                    format(eco.getBalance(seller, shop.getLocation().getWorld(),
                                    shop.getCurrency()), shop.getLocation().getWorld(),
                            shop.getCurrency())).send();
            return;
        }
        if (!transaction.failSafeCommit()) {
            plugin.text().of(seller, "economy-transaction-failed", transaction.getLastError()).send();
            plugin.logger().error("EconomyTransaction Failed, last error: {}", transaction.getLastError());
            return;
        }

        try {
            shop.sell(seller, sellerInventory, player != null ? player.getLocation() : shop.getLocation(), amount);
        } catch (Exception shopError) {
            plugin.logger().warn("Failed to processing purchase, rolling back...", shopError);
            transaction.rollback(true);
            plugin.text().of(seller, "shop-transaction-failed", shopError.getMessage()).send();
            return;
        }
        sendPurchaseSuccess(seller, shop, amount, total, transaction.getTax());
        new ShopSuccessPurchaseEvent(shop, seller, sellerInventory, amount, total, transaction.getTax()).callEvent();
        notifyBought(seller, shop, amount, stock, transaction.getTax(), total);
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

    @Override
    public void bakeShopRuntimeRandomUniqueIdCache(@NotNull Shop shop) {
        shopRuntimeUUIDCaching.put(shop.getRuntimeRandomUniqueId(), shop);
    }

    /**
     * Removes all shops from memory and the world. Does not delete them from the database. Call
     * this on plugin disable ONLY.
     */
    @Override
    public void clear() {
        Util.ensureThread(false);
        plugin.logger().info("Unloading loaded shops...");
        getLoadedShops().forEach(Shop::onUnload);
        plugin.logger().info("Saving shops, please allow up to 30 seconds for flush changes into database...");
        CompletableFuture<?> saveTask = CompletableFuture.allOf(
                plugin.getShopManager().getAllShops().stream().filter(Shop::isDirty)
                        .map(Shop::update)
                        .toArray(CompletableFuture[]::new));
        try {
            if (PackageUtil.parsePackageProperly("unlimitedWait").asBoolean()) {
                saveTask.get();
            } else {
                saveTask.get(30, TimeUnit.SECONDS);
            }
        } catch (ExecutionException | TimeoutException e) {
            plugin.logger().warn("Shops saving interrupted, some unsaved data may lost.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.interactiveManager.reset();
        this.shops.clear();
    }

    /**
     * Create a shop use Shop and Info object.
     *
     * @param shop                  The shop object
     * @param signBlock             The sign block
     * @param bypassProtectionCheck Should bypass protection check
     * @throws IllegalStateException If the shop owner offline
     */
    @Override
    public void createShop(@NotNull Shop shop, @Nullable Block signBlock, boolean bypassProtectionCheck) throws IllegalStateException {
        Util.ensureThread(false);
        Player p = Bukkit.getPlayer(shop.getOwner());
        // Player offline check
        if (p == null || !p.isOnline()) {
            throw new IllegalStateException("The owner creating the shop is offline or not exist");
        }

        if (plugin.getEconomy() == null) {
            MsgUtil.sendDirectMessage(p, Component.text("Error: Economy system not loaded, type /qs main command to get details.").color(NamedTextColor.RED));
            return;
        }

        // Check if player has reached the max shop limit
        if (isReachedLimit(p)) {
            plugin.text().of(p, "reached-maximum-create-limit").send();
            return;
        }
        // Check if target block is allowed shop-block
        if (!Util.canBeShop(shop.getLocation().getBlock())) {
            plugin.text().of(p, "chest-was-removed").send();
            return;
        }
        // Check if item has been blacklisted
        if (plugin.getShopItemBlackList().isBlacklisted(shop.getItem())
                && !plugin.perm()
                .hasPermission(p, "quickshop.bypass." + shop.getItem().getType().name().toLowerCase(Locale.ROOT))) {
            plugin.text().of(p, "blacklisted-item").send();
            return;
        }
        // Check if server/player allowed to create stacking shop
        if (plugin.isAllowStack() && !plugin.perm().hasPermission(p, "quickshop.create.stacks")) {
            Log.debug("Player " + p.getName() + " no permission to create stacks shop, forcing creating single item shop");
            shop.getItem().setAmount(1);
        }

        // Checking the shop can be created
        Log.debug("Calling for protection check...");

        // Protection check
        if (!bypassProtectionCheck) {
            Result result = plugin.getPermissionChecker().canBuild(p, shop.getLocation());
            if (!result.isSuccess()) {
                plugin.text().of(p, "3rd-plugin-build-check-failed", result.getMessage()).send();
                if (plugin.perm().hasPermission(p, "quickshop.alert")) {
                    plugin.text().of(p, "3rd-plugin-build-check-failed-admin", result.getMessage(), result.getListener()).send();
                }
                Log.debug("Failed to create shop because protection check failed, found:" + result.getMessage());
                return;
            }
        }

        // Check if the shop is already created
        if (plugin.getShopManager().getShop(shop.getLocation()) != null) {
            plugin.text().of(p, "shop-already-owned").send();
            return;
        }

        // Check if player and server allow double chest shop
        if (Util.isDoubleChest(shop.getLocation().getBlock().getBlockData())
                && !plugin.perm().hasPermission(p, "quickshop.create.double")) {
            plugin.text().of(p, "no-double-chests").send();
            return;
        }

        // Sign check
        if (autoSign) {
            if (signBlock == null) {
                if (!allowNoSpaceForSign) {
                    plugin.text().of(p, "failed-to-put-sign").send();
                    return;
                }
            } else {
                Material signType = signBlock.getType();
                if (signType != Material.WATER
                        && !signType.isAir()
                        && !allowNoSpaceForSign) {
                    plugin.text().of(p, "failed-to-put-sign").send();
                    return;
                }
            }
        }

        // Price limit checking
        PriceLimiterCheckResult priceCheckResult = this.priceLimiter.check(p, shop.getItem(), plugin.getCurrency(), shop.getPrice());
        switch (priceCheckResult.getStatus()) {
            case REACHED_PRICE_MIN_LIMIT -> plugin.text().of(p, "price-too-cheap",
                    Component.text((useDecFormat) ? MsgUtil.decimalFormat(priceCheckResult.getMax())
                            : Double.toString(priceCheckResult.getMin()))).send();
            case REACHED_PRICE_MAX_LIMIT -> plugin.text().of(p, "price-too-high",
                    Component.text((useDecFormat) ? MsgUtil.decimalFormat(priceCheckResult.getMax())
                            : Double.toString(priceCheckResult.getMin()))).send();
            case PRICE_RESTRICTED -> plugin.text().of(p, "restricted-prices",
                    Util.getItemStackName(shop.getItem()),
                    Component.text(priceCheckResult.getMin()),
                    Component.text(priceCheckResult.getMax())).send();
            case NOT_VALID -> plugin.text().of(p, "not-a-number", shop.getPrice()).send();
            case NOT_A_WHOLE_NUMBER -> plugin.text().of(p, "not-a-integer", shop.getPrice()).send();
            case PASS -> {
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
                double createCost = shopCreateCost;
                if (plugin.perm().hasPermission(p, "quickshop.bypasscreatefee")) {
                    createCost = 0;
                }
                if (createCost > 0) {
                    SimpleEconomyTransaction economyTransaction =
                            SimpleEconomyTransaction.builder()
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
                                format(createCost, shop.getLocation().getWorld(),
                                        shop.getCurrency())).send();
                        return;
                    }
                    if (!economyTransaction.failSafeCommit()) {
                        plugin.text().of(p, "economy-transaction-failed", economyTransaction.getLastError()).send();
                        plugin.logger().error("EconomyTransaction Failed, last error:{} ", economyTransaction.getLastError());
                        plugin.logger().error("Tips: If you see any economy plugin name appears above, please don't ask QuickShop support. Contact with developer of economy plugin. QuickShop didn't process the transaction, we only receive the transaction result from your economy plugin.");
                        return;
                    }
                }

                // The shop about successfully created
                if (!useShopLock) {
                    plugin.text().of(p, "shops-arent-locked").send();
                }

                // Shop info sign check
                if (signBlock != null && autoSign) {
                    if (signBlock.getType().isAir() || signBlock.getType() == Material.WATER) {
                        this.processWaterLoggedSign(shop.getLocation().getBlock(), signBlock);
                    }
                }
                registerShop(shop);

            }
        }
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
     * @return Returns the Map. Info contains what their last question etc was.
     * @deprecated Use getInteractiveManager instead. This way won't trigger the BungeeCord notification.
     */
    @SuppressWarnings("removal")
    @Override
    @Deprecated(forRemoval = true)
    public @NotNull Map<UUID, Info> getActions() {
        return this.interactiveManager.actions;
    }

    /**
     * Returns all shops in the memory, include unloaded.
     *
     * <p>Make sure you have caching this, because this need a while to get all shops
     *
     * @return All shop in the database
     */
    @Override
    public @NotNull List<Shop> getAllShops() {
        try (PerfMonitor ignored = new PerfMonitor("Getting all shops")) {
            final List<Shop> shopsCollected = new ArrayList<>();
            for (final Map<ShopChunk, Map<Location, Shop>> shopMapData : getShops().values()) {
                for (final Map<Location, Shop> shopData : shopMapData.values()) {
                    shopsCollected.addAll(shopData.values());
                }
            }
            return shopsCollected;
        }
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

    @Override
    public @NotNull PriceLimiter getPriceLimiter() {
        return this.priceLimiter;
    }

    /**
     * Gets a shop by shop Id
     *
     * @param shopId shop Id
     * @return The shop object
     */
    @Override
    public @Nullable Shop getShop(long shopId) {
        for (Shop shop : getAllShops()) {
            if (shop.getShopId() == shopId) {
                return shop;
            }
        }
        return null;
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
            Log.debug("Location is null.");
            return null;
        }
        if (useCache) {
            if (plugin.getShopCache() != null) {
                return plugin.getShopCache().find(loc, true);
            }
        }
        return findShopIncludeAttached(loc, false);
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
     * Returns a map of World - Chunk - Shop
     *
     * @return a map of World - Chunk - Shop
     */
    @Override
    public @NotNull Map<String, Map<ShopChunk, Map<Location, Shop>>> getShops() {
        return this.shops;
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
    @Deprecated
    public double getTax(@NotNull Shop shop, @NotNull Player p) {
        return getTax(shop, p.getUniqueId());
    }

    @Override
    public double getTax(@NotNull Shop shop, @NotNull UUID p) {
        Util.ensureThread(false);
        double tax = globalTax;
        Player player = Bukkit.getPlayer(p);
        if (player != null) {
            if (plugin.perm().hasPermission(player, "quickshop.tax")) {
                tax = 0;
                Log.debug("Disable the Tax for player " + player + " cause they have permission quickshop.tax");
            }
            if (shop.isUnlimited() && plugin.perm().hasPermission(player, "quickshop.tax.bypassunlimited")) {
                tax = 0;
                Log.debug("Disable the Tax for player " + player + " cause they have permission quickshop.tax.bypassunlimited and shop is unlimited.");
            }
        }
        if (tax >= 1.0) {
            plugin.logger().warn("Disable tax due to is invalid, it should be in >=0.0 and <1.0 (current value is {})", tax);
            tax = 0;
        }
        if (tax < 0) {
            tax = 0; // Tax was disabled.
        }
        if (shop.getOwner().equals(p)) {
            tax = 0; // Is owner, so we won't will take them tax
        }


        ShopTaxEvent taxEvent = new ShopTaxEvent(shop, tax, p);
        taxEvent.callEvent();
        return taxEvent.getTax();
    }

    @Override
    public void handleChat(@NotNull Player p, @NotNull String msg) {
        if (!plugin.getShopManager().getInteractiveManager().containsKey(p.getUniqueId())) {
            return;
        }
        String message = ChatColor.stripColor(msg);
        QSHandleChatEvent qsHandleChatEvent = new QSHandleChatEvent(p, message);
        qsHandleChatEvent.callEvent();
        message = qsHandleChatEvent.getMessage();
        // Use from the main thread, because Bukkit hates life
        String finalMessage = message;

        Util.mainThreadRun(() -> {
            // They wanted to do something.
            Info info = getInteractiveManager().remove(p.getUniqueId());
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
     * Checks other plugins to make sure they can use the chest they're making a shop.
     *
     * @param p The player to check
     * @return True if they're allowed to place a shop there.
     */
    @Override
    public boolean isReachedLimit(@NotNull Player p) {
        Util.ensureThread(false);
        if (plugin.getRankLimiter().isLimit()) {
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
            int max = plugin.getRankLimiter().getShopLimit(p);
            Log.debug("CanBuildShop check for " + p.getName() + " owned: " + owned + "; max: " + max);
            return owned + 1 > max;
        }
        return false;
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
     * Change the owner to unlimited shop owner.
     * It defined in configuration.
     */
    @Override
    public void migrateOwnerToUnlimitedShopOwner(@NotNull Shop shop) {
        shop.setOwner(this.cacheUnlimitedShopAccount);
        shop.setSignText(plugin.text().findRelativeLanguages(shop.getOwner()));
    }

    @Override
    public void registerShop(@NotNull Shop shop) {
        // sync add to prevent compete issue
        addShop(shop.getLocation().getWorld().getName(), shop);
        // load the shop finally
        shop.onLoad();
        // first init
        shop.setSignText(plugin.getTextManager().findRelativeLanguages(shop.getOwner()));
        // save to database
        plugin.getDatabaseHelper().createData(shop).thenCompose(plugin.getDatabaseHelper()::createShop).whenComplete((id, err) -> {
            if (err != null) {
                processCreationFail(shop, shop.getOwner(), err);
                return;
            }
            Log.debug("DEBUG: Setting shop id");
            shop.setShopId(id);
            Log.debug("DEBUG: Creating shop map");
            plugin.getDatabaseHelper().createShopMap(id, shop.getLocation());
            Log.debug("DEBUG: Creating shop successfully");
            new ShopCreateSuccessEvent(shop, shop.getOwner()).callEvent();
        });
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
        if (showTax) {
            chatSheetPrinter.printLine(plugin.text().of(sender, "menu.item-name-and-price-tax",
                    Component.text(amount * shop.getItem().getAmount()),
                    Util.getItemStackName(shop.getItem()),
                    format(total, shop),
                    tax).forLocale());
        } else {
            chatSheetPrinter.printLine(plugin.text().of(sender, "menu.item-name-and-price",
                    Component.text(amount * shop.getItem().getAmount()),
                    Util.getItemStackName(shop.getItem()),
                    format(total, shop)).forLocale());
        }
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
                        Util.getItemStackName(shop.getItem()),
                        format(total, shop)).forLocale());
        if (showTax) {
            if (tax != 0) {
                if (!seller.equals(shop.getOwner())) {
                    chatSheetPrinter.printLine(
                            plugin.text().of(sender, "menu.sell-tax", format(tax, shop)).forLocale());
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
        if (!shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.SHOW_INFORMATION)
                && !plugin.perm().hasPermission(p, "quickshop.other.use")) {
            return;
        }
        if(Util.fireCancellableEvent(new ShopInfoPanelEvent(shop, p.getUniqueId()))){
            Log.debug("ShopInfoPanelEvent cancelled by some plugin");
            return;
        }
        ProxiedLocale locale = plugin.text().findRelativeLanguages(p.getLocale());
        // Potentially faster with an array?
        ItemStack items = shop.getItem();
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(p);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(plugin.text().of(p, "menu.shop-information").forLocale());
        chatSheetPrinter.printLine(plugin.text().of(p, "menu.owner", shop.ownerName(locale)).forLocale());
        // Enabled
        if (shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.PREVIEW_SHOP)
                || plugin.perm().hasPermission(p, "quickshop.other.preview")) {
            chatSheetPrinter.printLine(plugin.text().of(p, "menu.item", Util.getItemStackName(shop.getItem())).forLocale()
                    .append(Component.text("   "))
                    .append(plugin.text().of(p, "menu.preview", Component.text(shop.getItem().getAmount())).forLocale())
                    .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, MsgUtil.fillArgs("/qs silentpreview {0}", shop.getRuntimeRandomUniqueId().toString())))
            );
        } else {
            chatSheetPrinter.printLine(plugin.text().of(p, "menu.item", Util.getItemStackName(shop.getItem())).forLocale()
                    .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()))
            );
        }


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
            chatSheetPrinter.printLine(plugin.text().of(p, "menu.price-per", Util.getItemStackName(shop.getItem()), format(shop.getPrice(), shop)).forLocale());
        } else {
            chatSheetPrinter.printLine(plugin.text().of(p, "menu.price-per-stack", Util.getItemStackName(shop.getItem()), format(shop.getPrice(), shop), shop.getItem().getAmount()).forLocale());
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
                Component translation;
                try {
                    translation = plugin.getPlatform().getTranslation(potionEffectType);
                } catch (Throwable th) {
                    translation = MsgUtil.setHandleFailedHover(p, Component.text(potionEffectType.getName()));
                    plugin.logger().warn("Failed to handle translation for PotionEffect {}", potionEffectType.getKey(), th);
                }
                chatSheetPrinter.printLine(plugin.text().of(p, "menu.effects").forLocale());
                //Because the bukkit API limit, we can't get the actual effect level
                chatSheetPrinter.printLine(Component.empty()
                        .color(NamedTextColor.YELLOW)
                        .append(translation)
                );
            }
            if (potionMeta.hasCustomEffects()) {
                for (PotionEffect potionEffect : potionMeta.getCustomEffects()) {
                    int level = potionEffect.getAmplifier();
                    Component translation;
                    try {
                        translation = plugin.getPlatform().getTranslation(potionEffect.getType());
                    } catch (Throwable th) {
                        translation = MsgUtil.setHandleFailedHover(p, Component.text(potionEffect.getType().getName()));
                        plugin.logger().warn("Failed to handle translation for PotionEffect {}", potionEffect.getType().getKey(), th);
                    }
                    chatSheetPrinter.printLine(Component.empty()
                            .color(NamedTextColor.YELLOW)
                            .append(translation).append(Component.text(" " + (level <= 10 ? RomanNumber.toRoman(level) : level))));
                }
            }
        }
        chatSheetPrinter.printFooter();

    }

    @Override
    public boolean shopIsNotValid(@NotNull UUID uuid, @NotNull Info info, @NotNull Shop shop) {
        Player player = Bukkit.getPlayer(uuid);
        return shopIsNotValid(player, info, shop);
    }

    @Override
    public @NotNull ShopManager.InteractiveManager getInteractiveManager() {
        return this.interactiveManager;
    }

    @Deprecated
    public void actionSelling(
            @NotNull Player p, @NotNull AbstractEconomy eco, @NotNull SimpleInfo info, @NotNull Shop shop,
            int amount) {
        Util.ensureThread(false);
        actionSelling(p.getUniqueId(), new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, amount);
    }

    private void notifyBought(@NotNull UUID seller, @NotNull Shop shop, int amount, int stock, double tax, double total) {
        Player player = Bukkit.getPlayer(seller);
        plugin.getDatabaseHelper().getPlayerLocale(shop.getOwner()).whenCompleteAsync((locale, err) -> {
            String langCode = MsgUtil.getDefaultGameLanguageCode();
            if (locale != null) {
                langCode = locale;
            }
            Component msg;
            if (plugin.getConfig().getBoolean("show-tax")) {
                msg = plugin.text().of("player-bought-from-your-store-tax",
                                player != null ? player.getName() : seller.toString(),
                                amount * shop.getItem().getAmount(),
                                Util.getItemStackName(shop.getItem()),
                                this.formatter.format(total - tax, shop),
                                this.formatter.format(tax, shop)).forLocale(langCode)
                        .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()));
            } else {
                msg = plugin.text().of("player-bought-from-your-store",
                                player != null ? player.getName() : seller.toString(),
                                amount * shop.getItem().getAmount(),
                                Util.getItemStackName(shop.getItem()),
                                this.formatter.format(total - tax, shop)).forLocale(langCode)
                        .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()));
            }

            if (sendStockMessageToStaff) {
                for (UUID recv : shop.playersCanAuthorize(BuiltInShopPermission.RECEIVE_ALERT)) {
                    MsgUtil.send(shop, recv, msg);
                }
            } else {
                MsgUtil.send(shop, shop.getOwner(), msg);
            }
            // Transfers the item from A to B
            if (stock == amount) {
                if (shop.getShopName() == null) {
                    msg = plugin.text().of("shop-out-of-stock",
                                    shop.getLocation().getBlockX(),
                                    shop.getLocation().getBlockY(),
                                    shop.getLocation().getBlockZ(),
                                    Util.getItemStackName(shop.getItem())).forLocale(langCode)
                            .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()));
                } else {
                    msg = plugin.text().of("shop-out-of-stock-name", shop.getShopName(),
                                    Util.getItemStackName(shop.getItem())).forLocale(langCode)
                            .hoverEvent(plugin.getPlatform().getItemStackHoverEvent(shop.getItem()));
                }
                if (sendStockMessageToStaff) {
                    for (UUID recv : shop.playersCanAuthorize(BuiltInShopPermission.RECEIVE_ALERT)) {
                        MsgUtil.send(shop, recv, msg);
                    }
                } else {
                    MsgUtil.send(shop, shop.getOwner(), msg);
                }
            }
        });

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
            plugin.logger().warn(
                    "Sign material {} not a WallSign, make sure you using correct sign material.", signBlockState.getType().name());
        }
        signBlockState.update(true);
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
        if (!shop.isUnlimited()) {
            amount = Math.min(shopHaveSpaces, invHaveItems);
            amount = Math.min(amount, ownerCanAfford);
        } else {
            amount = Util.countItems(new BukkitInventoryWrapper(p.getInventory()), shop);
            // even if the shop is unlimited, the config option pay-unlimited-shop-owners is set to
            // true,
            // the unlimited shop owner should have enough money.
            if (payUnlimitedShopOwner) {
                amount = Math.min(amount, ownerCanAfford);
            }
        }
        if (amount < 1) { // typed 'all' but the auto set amount is 0
            if (shopHaveSpaces == 0) {
                // when typed 'all' but the shop doesn't have any empty space
                plugin.text().of(p, "shop-has-no-space", Component.text(shopHaveSpaces),
                        Util.getItemStackName(shop.getItem())).send();
                return 0;
            }
            if (ownerCanAfford == 0 && (!shop.isUnlimited() || payUnlimitedShopOwner)) {
                // when typed 'all' but the shop owner doesn't have enough money to buy at least 1
                // item (and shop isn't unlimited or pay-unlimited is true)
                plugin.text().of(p, "the-owner-cant-afford-to-buy-from-you",
                        plugin.getShopManager().format(shop.getPrice(), shop.getLocation().getWorld(),
                                shop.getCurrency()),
                        plugin.getShopManager().format(ownerBalance, shop.getLocation().getWorld(),
                                shop.getCurrency())).send();
                return 0;
            }
            // when typed 'all' but player doesn't have any items to sell
            plugin.text().of(p, "you-dont-have-that-many-items",
                    Component.text(amount),
                    Util.getItemStackName(shop.getItem())).send();
            return 0;
        }
        return amount;
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
        if (p.getGameMode() == GameMode.CREATIVE && disableCreativePurchase) {
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
                if (message.equalsIgnoreCase(tradeAllKeyword)) {
                    amount = buyingShopAllCalc(eco, shop, p);
                } else {
                    // instead of output cancelled message (when typed neither integer or 'all'), just let
                    // player know that there should be positive number or 'all'
                    plugin.text().of(p, "not-a-integer", message).send();
                    Log.debug(
                            "Receive the chat " + message + " and it format failed: " + message);
                    return;
                }
            }
            actionBuying(p.getUniqueId(), new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, amount);
        } else if (shop.isSelling()) {
            if (StringUtils.isNumeric(message)) {
                amount = Integer.parseInt(message);
            } else {
                if (message.equalsIgnoreCase(tradeAllKeyword)) {
                    amount = sellingShopAllCalc(eco, shop, p);
                } else {
                    // instead of output cancelled message, just let player know that there should be positive
                    // number or 'all'
                    plugin.text().of(p, "not-a-integer", message).send();
                    Log.debug("Receive the chat " + message + " and it format failed: " + message);
                    return;
                }
            }
            actionSelling(p.getUniqueId(), new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, amount);
        } else {
            plugin.text().of(p, "shop-purchase-cancelled").send();
            plugin.logger().warn("Shop data broken? Loc: {}", shop.getLocation());
        }
    }

    @NotNull
    @Override
    public CompletableFuture<@NotNull List<Shop>> queryTaggedShops(@NotNull UUID tagger, @NotNull String tag) {
        Util.ensureThread(true);
        return CompletableFuture.supplyAsync(() -> plugin.getDatabaseHelper().listShopsTaggedBy(tagger, tag)
                        .stream()
                        .map(this::getShop).toList()
                , QuickExecutor.getDatabaseExecutor());

    }

    @Override
    public CompletableFuture<@Nullable Integer> clearShopTags(@NotNull UUID tagger, @NotNull Shop shop) {
        return plugin.getDatabaseHelper().removeShopAllTag(tagger, shop.getShopId());
    }

    @Override
    public CompletableFuture<@Nullable Integer> clearTagFromShops(@NotNull UUID tagger, @NotNull String tag) {
        tag = tag.trim().toLowerCase(Locale.ROOT);
        tag = tag.replace(" ", "_");
        return plugin.getDatabaseHelper().removeTagFromShops(tagger, tag);
    }

    @Override
    public CompletableFuture<@Nullable Integer> removeTag(@NotNull UUID tagger, @NotNull Shop shop, @NotNull String tag) {
        tag = tag.trim().toLowerCase(Locale.ROOT);
        tag = tag.replace(" ", "_");
        return plugin.getDatabaseHelper().removeShopTag(tagger, shop.getShopId(), tag);
    }

    @Override
    public CompletableFuture<@Nullable Integer> tagShop(@NotNull UUID tagger, @NotNull Shop shop, @NotNull String tag) {
        tag = tag.trim().toLowerCase(Locale.ROOT);
        tag = tag.replace(" ", "_");
        return plugin.getDatabaseHelper().tagShop(tagger, shop.getShopId(), tag);
    }

    @Override
    @NotNull
    public List<String> listTags(@NotNull UUID tagger) {
        Util.ensureThread(true);
        return plugin.getDatabaseHelper().listTags(tagger);
    }

    private void processCreationFail(@NotNull Shop shop, @NotNull UUID owner, @NotNull Throwable e2) {
        plugin.logger().error("Shop create failed, auto fix failed, the changes may won't commit to database.", e2);
        Player player = Bukkit.getPlayer(owner);
        if (player != null) {
            plugin.text().of(player, "shop-creation-failed").send();
        }
        Util.mainThreadRun(() -> {
            shop.onUnload();
            removeShop(shop);
            shop.delete();
        });
    }

    @Override
    public ReloadResult reloadModule() {
        Util.asyncThreadRun(this::init);
        return ReloadResult.builder().status(ReloadStatus.SCHEDULED).build();
    }

    private int sellingShopAllCalc(@NotNull AbstractEconomy eco, @NotNull Shop shop, @NotNull Player p) {
        int amount;
        int shopHaveItems = shop.getRemainingStock();
        int invHaveSpaces = Util.countSpace(new BukkitInventoryWrapper(p.getInventory()), shop);
        if (!shop.isUnlimited()) {
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
            if (!shop.isUnlimited() && shopHaveItems < 1) {
                // but also the shop's stock is 0
                plugin.text().of(p, "shop-stock-too-low",
                        Component.text(shop.getRemainingStock()),
                        Util.getItemStackName(shop.getItem())).send();
                return 0;
            } else {
                // when if player's inventory is full
                if (invHaveSpaces <= 0) {
                    plugin.text().of(p, "not-enough-space",
                            Component.text(invHaveSpaces)).send();
                    return 0;
                }
                plugin.text().of(p, "you-cant-afford-to-buy",
                        plugin.getShopManager().format(price, shop.getLocation().getWorld(),
                                shop.getCurrency()),
                        plugin.getShopManager().format(balance, shop.getLocation().getWorld(),
                                shop.getCurrency())).send();
            }
            return 0;
        }
        return amount;
    }

    public static class InteractiveManager implements ShopManager.InteractiveManager {
        private final Map<UUID, Info> actions = Maps.newConcurrentMap();
        private final QuickShop plugin;

        public InteractiveManager(QuickShop plugin) {
            this.plugin = plugin;
        }

        @Override
        public int size() {
            return this.actions.size();
        }

        @Override
        public boolean isEmpty() {
            return this.actions.isEmpty();
        }

        @Nullable
        @Override
        public Info put(UUID uuid, Info info) {
            sendRequest(uuid);
            return this.actions.put(uuid, info);
        }

        @Nullable
        @Override
        public Info remove(UUID uuid) {
            sendCancel(uuid);
            return this.actions.remove(uuid);
        }

        @Override
        public void reset() {
            this.actions.keySet().forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    if(plugin.getBungeeListener() != null) {
                        plugin.getBungeeListener().notifyForCancel(player);
                    }
                }
            });
            this.actions.clear();
        }

        @Override
        public Info get(UUID uuid) {
            return this.actions.get(uuid);
        }

        @Override
        public boolean containsKey(UUID uuid) {
            return this.actions.containsKey(uuid);
        }

        @Override
        public boolean containsValue(Info info) {
            return this.actions.containsValue(info);
        }

        private void sendCancel(UUID uuid) {
            if (plugin.getBungeeListener() != null) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    Log.debug("Cancel chat forward for player " + p.getName());
                    plugin.getBungeeListener().notifyForCancel(p);
                }
            }
        }

        private void sendRequest(UUID uuid) {
            if (plugin.getBungeeListener() != null) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    Log.debug("Request chat forward for player " + p.getName());
                    plugin.getBungeeListener().notifyForForward(p);
                }
            }
        }
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

    static class TagParser {
        private final List<String> tags;
        private final ShopManager shopManager;
        private final UUID tagger;
        private final Map<String, List<Shop>> singleCaching = new HashMap<>();

        public TagParser(UUID tagger, ShopManager shopManager, List<String> tags) {
            Util.ensureThread(true);
            this.shopManager = shopManager;
            this.tags = tags;
            this.tagger = tagger;
        }
        
        public List<Shop> parseTags(){
            List<Shop> finalShop = new ArrayList<>();
            for (String tag : tags) {
               ParseResult result =  parseSingleTag(tag);
               if(result.getBehavior() == Behavior.INCLUDE){
                   finalShop.addAll(result.getShops());
               }else if (result.getBehavior() == Behavior.EXCLUDE){
                     finalShop.removeAll(result.getShops());
               }
            }
            return finalShop;
        }

        public ParseResult parseSingleTag(String tag) throws IllegalArgumentException {
            Util.ensureThread(true);
            Behavior behavior = Behavior.INCLUDE;
            if (tag.startsWith("-")) {
                behavior = Behavior.EXCLUDE;
            }
            String tagName = tag.substring(1);
            if (tagName.isEmpty()) {
                throw new IllegalArgumentException("Tag name can't be empty");
            }
            List<Shop> shops = singleCaching.computeIfAbsent(tag, (t)->shopManager.queryTaggedShops(tagger, t).join());
            return new ParseResult(behavior, shops);
        }

        @AllArgsConstructor
        @Data
        static class ParseResult {
            private final Behavior behavior;
            private final List<Shop> shops;
        }

        enum Behavior {
            INCLUDE,
            EXCLUDE
        }
    }
}
