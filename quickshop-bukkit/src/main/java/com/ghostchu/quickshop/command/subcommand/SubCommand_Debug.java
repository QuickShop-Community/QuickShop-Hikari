package com.ghostchu.quickshop.command.subcommand;

import cc.carm.lib.easysql.EasySQL;
import cc.carm.lib.easysql.api.SQLQuery;
import cc.carm.lib.easysql.hikari.HikariDataSource;
import cc.carm.lib.easysql.hikari.pool.HikariPool;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.shop.SimpleShopManager;
import com.ghostchu.quickshop.shop.cache.SimpleShopCache;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.performance.BatchBukkitExecutor;
import com.google.common.cache.Cache;
import com.google.common.hash.Hashing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

public class SubCommand_Debug implements CommandHandler<CommandSender> {

    private final QuickShop plugin;
    private final Map<String, BiConsumer<CommandSender, List<String>>> subParamMapping = new HashMap<>();

    public SubCommand_Debug(QuickShop plugin) {
        this.plugin = plugin;
        subParamMapping.put("debug", (sender, subParams) -> switchDebug(sender));
        subParamMapping.put("dev", (sender, subParams) -> switchDebug(sender));
        subParamMapping.put("devmode", (sender, subParams) -> switchDebug(sender));
        subParamMapping.put("signs", (sender, subParams) -> handleSigns(sender));
        subParamMapping.put("database", this::handleDatabase);
        subParamMapping.put("updateplayersigns", this::handleSignsUpdate);
        subParamMapping.put("force-shops-reload", this::handleShopsReload);
        subParamMapping.put("force-shoploader-reload", this::handleShopsLoaderReload);
        subParamMapping.put("check-shop-status", this::handleShopDebug);
        subParamMapping.put("toggle-shop-load-status", this::handleShopLoading);
        subParamMapping.put("check-shop-debug", this::handleShopInfo);
        subParamMapping.put("set-property", this::handleProperty);
        subParamMapping.put("reset-shop-caches", this::handleShopCacheResetting);
        subParamMapping.put("reset-dbmanager", this::handleDbManagerReset);
        subParamMapping.put("dump-db-connections", this::handleDumpDbConnections);
        subParamMapping.put("stop-db-any-queries", this::handleStopDbQueries);
        subParamMapping.put("toggle-db-debugmode", this::handleToggleDbDebugMode);
        subParamMapping.put("dump-hikaricp-status", this::handleDumpHikariCPStatus);
        subParamMapping.put("set-hikaricp-capacity", this::handleSetHikariCPCapacity);
        subParamMapping.put("item-info", this::handleItemInfo);
        subParamMapping.put("mark-all-shops-dirty", this::handleShopsDirtyAndSave);
        subParamMapping.put("clean-display-entities", this::handleDisplayEntities);
    }

    private void handleDisplayEntities(CommandSender sender, List<String> strings) {
        List<Entity> entities = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item itemEntity) {
                    if (AbstractDisplayItem.checkIsGuardItemStack(itemEntity.getItemStack())) {
                        entities.add(entity);
                    }
                }
                if (entity instanceof ItemDisplay itemDisplay) {
                    if (AbstractDisplayItem.checkIsGuardItemStack(itemDisplay.getItemStack())) {
                        entities.add(entity);
                    }
                }
            }
        }
        entities.forEach(Entity::remove);
        plugin.text().of(sender, "debug.display-removed", entities.size()).send();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().isEmpty()) {
            switchDebug(sender);
            return;
        }
        List<String> subParams = new ArrayList<>(parser.getArgs());
        subParams.remove(0);
        String arg = parser.getArgs().get(0);

        BiConsumer<CommandSender, List<String>> executor = subParamMapping.get(arg);
        if (executor == null) {
            plugin.text().of(sender, "debug.arguments-invalid", parser.getArgs().get(0)).send();
            return;
        }

        executor.accept(sender, subParams);
    }

    private void handleShopsDirtyAndSave(CommandSender sender, List<String> subParams) {
        plugin.getShopManager().getAllShops().forEach(Shop::setDirty);
        plugin.text().of(sender, "debug.marked-as-dirty").send();
    }

    private void handleItemInfo(CommandSender sender, List<String> subParams) {
        if (!(sender instanceof Player player)) {
            return;
        }
        String hand = player.getInventory().getItemInMainHand().getItemMeta().getAsString();
        plugin.text().of(sender, "debug.item-info-hand-as-string", hand, Hashing.crc32().hashString(hand, StandardCharsets.UTF_8).toString()).send();
        Shop shop = getLookingShop(sender);
        if (shop != null) {
            String store = shop.getItem().getItemMeta().getAsString();
            plugin.text().of(sender, "debug.item-info-store-as-string", store, Hashing.crc32().hashString(store, StandardCharsets.UTF_8).toString()).send();
            boolean hand2Store = plugin.getItemMatcher().matches(player.getInventory().getItemInMainHand(), shop.getItem());
            boolean store2Hand = plugin.getItemMatcher().matches(shop.getItem(), player.getInventory().getItemInMainHand());
            plugin.text().of(sender, "debug.item-matching-result", hand2Store, store2Hand).send();
        }
    }

    private void handleSetHikariCPCapacity(CommandSender sender, List<String> subParams) {
        int size = Integer.parseInt(subParams.get(0));
        HikariDataSource hikariDataSource = (HikariDataSource) plugin.getSqlManager().getDataSource();
        hikariDataSource.setMaximumPoolSize(size);
        hikariDataSource.setMinimumIdle(size);
        plugin.text().of(sender, "debug.hikari-cp-size-tweak", size).send();
        ;
    }

    private void handleDbConnectionTest(CommandSender sender, List<String> subParams) {
        plugin.text().of(sender, "debug.hikari-cp-testing").send();
        try {
            CompletableFuture.supplyAsync(() -> {
                try (Connection connection = plugin.getSqlManager().getConnection()) {
                    if (connection.isValid(1000)) {
                        plugin.text().of(sender, "debug.hikari-cp-working").send();
                        ;
                    } else {
                        plugin.text().of(sender, "debug.hikari-cp-not-working");
                    }
                } catch (SQLException e) {
                    plugin.text().of(sender, "internal-error").send();
                    e.printStackTrace();
                }
                return null;
            }).get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            plugin.text().of(sender, "debug.hikari-cp-timeout").send();
        } catch (ExecutionException | InterruptedException e) {
            plugin.text().of(sender, "internal-error").send();
            e.printStackTrace();
        }
    }

    private void handleStopDbQueries(CommandSender sender, List<String> subParams) {
        long stopped = plugin.getSqlManager().getActiveQuery().values().size();
        plugin.getSqlManager().getActiveQuery().values().forEach(SQLQuery::close);
        plugin.text().of(sender, "debug.queries-stopped", stopped).send();
    }

    private void handleDumpHikariCPStatus(CommandSender sender, List<String> subParams) {
        HikariDataSource hikariDataSource = (HikariDataSource) plugin.getSqlManager().getDataSource();

        sender.sendMessage("Catalog: " + hikariDataSource.getCatalog());
        sender.sendMessage("PoolName: " + hikariDataSource.getPoolName());
        sender.sendMessage("Connection Timeout: " + hikariDataSource.getConnectionTimeout());
        sender.sendMessage("Idle Timeout: " + hikariDataSource.getIdleTimeout());
        sender.sendMessage("Leak Detection Threshold: " + hikariDataSource.getLeakDetectionThreshold());
        sender.sendMessage("MaximumPoolSize: " + hikariDataSource.getMaximumPoolSize());
        sender.sendMessage("MinimumIdle: " + hikariDataSource.getMinimumIdle());
        sender.sendMessage("MaxLifeTime: " + hikariDataSource.getMaxLifetime());
        sender.sendMessage("ValidationTimeout: " + hikariDataSource.getValidationTimeout());
        try {
            Field poolField = hikariDataSource.getClass().getDeclaredField("pool");
            poolField.setAccessible(true);
            HikariPool hikariPool = (HikariPool) poolField.get(hikariDataSource);
            sender.sendMessage("Active connections: " + hikariPool.getActiveConnections());
            sender.sendMessage("Idle connections: " + hikariPool.getIdleConnections());
            sender.sendMessage("Total connections: " + hikariPool.getTotalConnections());
            sender.sendMessage("Threads Awaiting connections: " + hikariPool.getThreadsAwaitingConnection());
        } catch (Exception e) {
            plugin.logger().warn("Failed retrieve HikariPool internal state.", e);
        }
    }

    private void handleToggleDbDebugMode(CommandSender sender, List<String> subParams) {
        plugin.getSqlManager().setDebugMode(!plugin.getSqlManager().isDebugMode());
        sender.sendMessage("Db Debug Mode: " + plugin.getSqlManager().isDebugMode());
    }

    private void handleDumpDbConnections(CommandSender sender, List<String> subParams) {
        plugin.text().of(sender, "debug.queries-dumping").send();
        for (Map.Entry<UUID, SQLQuery> e : plugin.getSqlManager().getActiveQuery().entrySet()) {
            sender.sendMessage(e.getKey().toString() + ": " + e.getValue());
        }
    }

    private void handleDbManagerReset(CommandSender sender, List<String> subParams) {
        plugin.text().of(sender, "debug.restart-database-manager").send();
        EasySQL.shutdownManager(this.plugin.getSqlManager());
        plugin.text().of(sender, "debug.restart-database-manager-clear-executors").send();
        QuickExecutor.getHikaricpExecutor().shutdownNow().forEach(r -> plugin.text().of(sender, "debug.restart-database-manager-unfinished-task", r).send());
        QuickExecutor.setHikaricpExecutor(QuickExecutor.provideHikariCPExecutor());
        QuickExecutor.getShopHistoryQueryExecutor().shutdownNow().forEach(r -> plugin.text().of(sender, "debug.restart-database-manager-unfinished-task-history-query", r).send());
        QuickExecutor.setShopHistoryQueryExecutor(QuickExecutor.provideShopHistoryQueryExecutor());
        plugin.text().of(sender, "debug.restart-database-manager-reconnect").send();
        Util.asyncThreadRun(plugin::setupDatabase);
        plugin.text().of(sender, "debug.restart-database-manager-done").send();
    }

    private void handleShopCacheResetting(CommandSender sender, List<String> subParams) {
        SimpleShopManager shopManager = (SimpleShopManager) plugin.getShopManager();
        SimpleShopCache simpleShopCache = (SimpleShopCache) shopManager.getShopCache();
        simpleShopCache.getCaches().values().forEach(Cache::invalidateAll);
        sender.sendMessage("Cleared!");
    }

    private void handleProperty(CommandSender sender, List<String> subParams) {
        if (subParams.isEmpty()) {
            plugin.text().of(sender, "debug.property-incorrect").send();
            return;
        }
        if (subParams.size() > 1) {
            plugin.text().of(sender, "debug.property-incorrect").send();
            return;
        }
        String[] split = subParams.get(0).split("=");
        if (split.length < 1) {
            plugin.text().of(sender, "debug.property-incorrect").send();
            return;
        }
        String key = split[0];
        String value = null;
        if (split.length > 1) {
            value = split[1];
        }
        if (!key.startsWith("com.ghostchu.quickshop") && !key.startsWith("quickshop")) {
            plugin.text().of(sender, "debug.property-security-block").send();
            return;
        }
        if (value == null) {
            System.clearProperty(key);
            plugin.text().of(sender, "debug.property-removed", key).send();
        } else {
            String oldOne = System.setProperty(key, value);
            plugin.text().of(sender, "debug.property-changed", key, oldOne, value).send();
        }
    }

    private void handleShopInfo(CommandSender sender, List<String> subParams) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        MsgUtil.sendDirectMessage(sender, shop.toString());
    }

    private void handleShopLoading(CommandSender sender, List<String> remove) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        if (shop.isLoaded()) {
            plugin.getShopManager().unloadShop(shop);
        } else {
            plugin.getShopManager().loadShop(shop);
        }
        plugin.text().of(sender, "debug.toggle-shop-loaded-status", shop.isLoaded());
    }

    private void handleShopDebug(CommandSender sender, List<String> remove) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        plugin.text().of(sender, "debug.shop-internal-data", shop.toString()).send();
    }

    private void handleShopsLoaderReload(CommandSender sender, List<String> remove) {
        plugin.text().of(sender, "debug.force-shop-loader-reload").send();
        List<Shop> allShops = plugin.getShopManager().getAllShops();
        plugin.text().of(sender, "debug.force-shop-loader-reload-unloading-shops-from-memory", allShops.size()).send();
        plugin.getShopManager().getAllShops().forEach(shop -> plugin.getShopManager().unloadShop(shop));
        plugin.text().of(sender, "debug.force-shop-loader-reload-reloading-shop-loader").send();
        plugin.getShopLoader().loadShops();
        plugin.text().of(sender, "debug.force-shop-loader-reload-complete").send();
    }

    private void handleShopsReload(CommandSender sender, List<String> remove) {
        plugin.text().of(sender, "debug.force-shop-reload").send();
        List<Shop> shops = new ArrayList<>(plugin.getShopManager().getLoadedShops());
        shops.forEach(s -> plugin.getShopManager().unloadShop(s));
        shops.forEach(s -> plugin.getShopManager().loadShop(s));
        plugin.text().of(sender, "debug.force-shop-reload-complete", shops.size()).send();
    }


    public void switchDebug(@NotNull CommandSender sender) {
        final boolean debug = plugin.getConfig().getBoolean("dev-mode");

        if (debug) {
            plugin.getJavaPlugin().reloadConfig();
            plugin.getConfig().set("dev-mode", false);
            plugin.getJavaPlugin().saveConfig();
            plugin.getReloadManager().reload();
            plugin.text().of(sender, "command.now-nolonger-debuging").send();
            return;
        }

        plugin.getJavaPlugin().reloadConfig();
        plugin.getConfig().set("dev-mode", true);
        plugin.getJavaPlugin().saveConfig();
        plugin.getReloadManager().reload();
        plugin.text().of(sender, "command.now-debuging").send();
    }

    private void handleHandlerList(@NotNull CommandSender sender, List<String> remove) {
        if (remove.isEmpty()) {
            plugin.text().of(sender, "debug.handler-list-not-valid-bukkit-event-class", "null");
            return;
        }
        printHandlerList(sender, remove.get(0));
    }

    private void handleSigns(@NotNull CommandSender sender) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        shop.getSigns().forEach(sign -> plugin.text().of(sender, "debug.sign-located", sign.getLocation()).send());
    }

    private void handleDatabase(@NotNull CommandSender sender, @NotNull List<String> remove) {
        if (remove.isEmpty()) {
            plugin.text().of("debug.operation-missing");
            return;
        }
        plugin.text().of(sender, "debug.operation-invalid", remove.get(0)).send();
    }

    private void handleSignsUpdate(CommandSender sender, List<String> remove) {
        if (remove.isEmpty()) {
            plugin.text().of(sender, "debug.update-player-shops-signs-no-username-given").send();
            return;
        }
        plugin.text().of(sender, "debug.update-player-shops-signs-create-async-task").send();
        plugin.getPlayerFinder().name2UuidFuture(remove.get(0)).whenComplete((uuid, throwable) -> {
            if (throwable != null) {
                plugin.text().of(sender, "internal-error", throwable.getMessage()).send();
                return;
            }
            plugin.text().of(sender, "debug.update-player-shops-player-selected", uuid).send();
            List<Shop> shops = plugin.getShopManager().getAllShops(uuid);
            plugin.text().of(sender, "debug.update-player-shops-player-shops", shops.size()).send();
            BatchBukkitExecutor<Shop> updateExecutor = new BatchBukkitExecutor<>();
            updateExecutor.addTasks(shops);
            plugin.text().of(sender, "debug.update-player-shops-task-started", shops.size()).send();
            updateExecutor.startHandle(plugin.getJavaPlugin(), Shop::setSignText).whenComplete((aVoid, th) -> {
                if (th != null) {
                    plugin.text().of(sender, "internal-error", th.getMessage()).send();
                    return;
                }
                long usedTime = updateExecutor.getStartTime().until(Instant.now(), java.time.temporal.ChronoUnit.MILLIS);
                plugin.text().of(sender, "debug.update-player-shops-complete", usedTime);
            });
        });

    }

    public void printHandlerList(@NotNull CommandSender sender, String event) {
        try {
            final Class<?> clazz = Class.forName(event);
            final Method method = clazz.getMethod("getHandlerList");
            final Object[] obj = new Object[0];
            final HandlerList list = (HandlerList) method.invoke(null, obj);

            for (RegisteredListener listener1 : list.getRegisteredListeners()) {
                MsgUtil.sendDirectMessage(sender,
                        LegacyComponentSerializer.legacySection().deserialize(ChatColor.AQUA
                                + listener1.getPlugin().getName()
                                + ChatColor.YELLOW
                                + " # "
                                + ChatColor.GREEN
                                + listener1.getListener().getClass().getCanonicalName()));
            }
        } catch (Exception th) {
            MsgUtil.sendDirectMessage(sender, Component.text("ERR " + th.getMessage()).color(NamedTextColor.RED));
            plugin.logger().warn("An error has occurred while getting the HandlerList", th);
        }
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() == 1) {
            return List.copyOf(subParamMapping.keySet()).stream().filter(s->s.startsWith(parser.getArgs().get(0))).toList();
        }
        return Collections.emptyList();
    }

}
