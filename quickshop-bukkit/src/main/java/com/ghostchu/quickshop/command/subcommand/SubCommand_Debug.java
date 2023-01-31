package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.performance.BulkExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;

public class SubCommand_Debug implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    public SubCommand_Debug(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            switchDebug(sender);
            return;
        }

        switch (cmdArg[0]) {
            case "debug", "dev", "devmode" -> switchDebug(sender);
            case "handlerlist" -> handleHandlerList(sender, ArrayUtils.remove(cmdArg, 0));
            case "signs" -> handleSigns(sender);
            case "database" -> handleDatabase(sender, ArrayUtils.remove(cmdArg, 0));
            case "updateplayersigns" -> handleSignsUpdate(sender, ArrayUtils.remove(cmdArg, 0));
            case "force-shops-reload" -> handleShopsReload(sender, ArrayUtils.remove(cmdArg, 0));
            case "force-shoploader-reload" -> handleShopsLoaderReload(sender, ArrayUtils.remove(cmdArg, 0));
            case "check-shop-status" -> handleShopDebug(sender, ArrayUtils.remove(cmdArg, 0));
            case "toggle-shop-load-status" -> handleShopLoading(sender, ArrayUtils.remove(cmdArg, 0));
            default -> plugin.text().of(sender, "debug.arguments-invalid", cmdArg[0]).send();
        }
    }

    private void handleShopLoading(CommandSender sender, String[] remove) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        if (shop.isLoaded()) {
            shop.onUnload();
        } else {
            shop.onLoad();
        }
        plugin.text().of(sender, "debug.toggle-shop-loaded-status", shop.isLoaded());
    }

    private void handleShopDebug(CommandSender sender, String[] remove) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        plugin.text().of(sender, "debug.shop-internal-data", shop.toString());
    }

    private void handleShopsLoaderReload(CommandSender sender, String[] remove) {
        plugin.text().of(sender, "debug.force-shop-loader-reload").send();
        Set<Shop> loadedShops = plugin.getShopManager().getLoadedShops();
        plugin.text().of(sender, "debug.force-shop-loader-reload-unloading-shops", loadedShops.size()).send();
        plugin.getShopManager().getLoadedShops().forEach(Shop::onUnload);
        List<Shop> allShops = plugin.getShopManager().getAllShops();
        plugin.text().of(sender, "debug.force-shop-loader-reload-unloading-shops-from-memory", allShops.size()).send();
        plugin.getShopManager().getAllShops().forEach(shop -> shop.delete(true));
        plugin.text().of(sender, "debug.force-shop-loader-reload-reloading-shop-loader").send();
        plugin.getShopLoader().loadShops();
        plugin.text().of(sender, "debug.force-shop-loader-reload-complete").send();
    }

    private void handleShopsReload(CommandSender sender, String[] remove) {
        plugin.text().of(sender, "debug.force-shop-reload").send();
        List<Shop> shops = new ArrayList<>(plugin.getShopManager().getLoadedShops());
        shops.forEach(Shop::onUnload);
        shops.forEach(Shop::onLoad);
        plugin.text().of(sender, "debug.force-shop-reload-complete", shops.size()).send();
    }


    public void switchDebug(@NotNull CommandSender sender) {
        final boolean debug = plugin.getConfig().getBoolean("dev-mode");

        if (debug) {
            plugin.reloadConfig();
            plugin.getConfig().set("dev-mode", false);
            plugin.getJavaPlugin().saveConfig();
            plugin.getReloadManager().reload();
            plugin.text().of(sender, "command.now-nolonger-debuging").send();
            return;
        }

        plugin.reloadConfig();
        plugin.getConfig().set("dev-mode", true);
        plugin.getJavaPlugin().saveConfig();
        plugin.getReloadManager().reload();
        plugin.text().of(sender, "command.now-debuging").send();
    }

    private void handleHandlerList(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendDirectMessage(sender, "You must enter an Bukkit Event class");
            plugin.text().of(sender, "debug.handler-list-not-valid-bukkit-event-class", "null");
            return;
        }
        printHandlerList(sender, cmdArg[0]);
    }

    private void handleSigns(@NotNull CommandSender sender) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        shop.getSigns().forEach(sign -> plugin.text().of(sender, "debug.sign-located", sign.getLocation()).send());
    }

    private void handleDatabase(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of("debug.operation-missing");
            return;
        }
        plugin.text().of(sender, "debug.operation-invalid", cmdArg[0]).send();
    }

    private void handleSignsUpdate(CommandSender sender, String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "debug.update-player-shops-signs-no-username-given").send();
            return;
        }
        final int tasksInStack = 15;
        plugin.text().of(sender, "debug.update-player-shops-signs-create-async-task").send();
        Util.asyncThreadRun(() -> {
            UUID uuid = plugin.getPlayerFinder().name2Uuid(cmdArg[0]);
            plugin.text().of(sender, "debug.update-player-shops-player-selected", uuid).send();
            List<Shop> shops = plugin.getShopManager().getPlayerAllShops(uuid);
            plugin.text().of(sender, "debug.update-player-shops-player-shops", shops.size()).send();
            plugin.text().of(sender, "debug.update-player-shops-per-tick-threshold", tasksInStack);
            BulkExecutor bulkExecutor = new BulkExecutor(tasksInStack, (exec) -> {
                long usedTime = exec.getStartTime().until(Instant.now(), java.time.temporal.ChronoUnit.MILLIS);
                plugin.text().of(sender, "debug.update-player-shops-complete", usedTime);
            });
            plugin.text().of(sender, "debug.update-player-shops-task-started", shops.size()).send();
            shops.forEach(shop -> bulkExecutor.addTask(shop::setSignText));
            bulkExecutor.runTaskTimer(plugin.getJavaPlugin(), 1, 1);
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
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }

}
