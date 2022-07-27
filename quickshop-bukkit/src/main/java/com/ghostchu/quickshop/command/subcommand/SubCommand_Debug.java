package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.external.com.ti.ems.jacky.ResultSetToJson;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SubCommand_Debug implements CommandHandler<CommandSender> {
    private final Cache<UUID, String> sqlCachePool = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();

    public SubCommand_Debug(QuickShop plugin) {
        this.plugin = plugin;
    }

    private final QuickShop plugin;

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
            default -> plugin.text().of(sender, "debug.arguments-invalid", cmdArg[0]).send();
        }
    }

    private void handleSigns(@NotNull CommandSender sender) {
        final BlockIterator bIt = new BlockIterator((LivingEntity) sender, 10);
        if (!bIt.hasNext()) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                shop.getSigns().forEach(sign -> plugin.text().of(sender, "debug.sign-located", sign.getLocation()).send());
                break;
            }
        }
    }

    private void handleDatabase(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of("debug.operation-missing");
            return;
        }
        switch (cmdArg[0]) {
            //case "sql" -> handleDatabaseSQL(sender, ArrayUtils.remove(cmdArg, 0));
            default -> plugin.text().of(sender, "debug.operation-invalid", cmdArg[0]).send();
        }
    }


    private void handleDatabaseSQL(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        Util.SysPropertiesParseResult parseResult = Util.parsePackageProperly("enable-sql");
        if (!parseResult.asBoolean()) {
            plugin.text().of(sender, "debug.sql-disabled", parseResult.getParseKey()).send();
            return;
        }
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "debug.invalid-base64-encoded-sql").send();
            return;
        }
        if (cmdArg.length == 1) {
            try {
                byte[] b = Base64.getDecoder().decode(cmdArg[0]);
                String sql = new String(b, StandardCharsets.UTF_8);
                UUID uuid = UUID.randomUUID();
                sqlCachePool.put(uuid, sql);
                plugin.getLogger().warning("An SQL query operation scheduled: uuid=" + uuid + ", sender=" + sender.getName() + ", sql=" + sql);
                plugin.text().of(sender, "debug.warning-sql", uuid, sender.getName()).send();
                Component component = plugin.text().of(sender, "debug.warning-sql-confirm", uuid, sender.getName()).forLocale()
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/qs debug database sql confirm " + uuid))
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, plugin.text().of(sender, "debug.warning-sql-confirm-hover").forLocale()));
                MsgUtil.sendDirectMessage(sender, component);
            } catch (Exception e) {
                plugin.text().of(sender, "debug.invalid-base64-encoded-sql").send();
            }
        }
        if (cmdArg.length == 2) {
            if (cmdArg[0].equals("confirm")) {
                if (Util.isUUID(cmdArg[1])) {
                    UUID uuid = UUID.fromString(cmdArg[1]);
                    String sql = sqlCachePool.getIfPresent(uuid);
                    if (sql == null) {
                        plugin.text().of(sender, "debug.sql-confirm-not-found", cmdArg[1]).send();
                        return;
                    }
                    sqlCachePool.invalidate(uuid);
                    plugin.getLogger().warning("An SQL query executed by " + sender.getName() + " with UUID " + uuid + " and content " + sql);
                    plugin.text().of(sender, "debug.sql-executing", sql, sender.getName()).send();
                    try (Connection connection = plugin.getSqlManager().getConnection()) {
                        Statement statement = connection.createStatement();
                        statement.execute(sql);
                        ResultSet set = statement.getResultSet();
                        MsgUtil.sendDirectMessage(sender, Component.text(ResultSetToJson.resultSetToJsonString(set)));
                        plugin.text().of(sender, "debug.sql-completed", statement.getLargeUpdateCount());
                    } catch (SQLException e) {
                        plugin.text().of(sender, "debug.sql-exception", e.getMessage()).send();
                        plugin.getLogger().log(Level.WARNING, "Failed to execute custom SQL: " + sql, e);
                    }
                } else {
                    plugin.text().of("debug.invalid-base64-encoded-sql").send();
                }
            }
        }
    }

    private void handleHandlerList(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendDirectMessage(sender, Component.text("You must enter an Bukkit Event class"));
            return;
        }
        printHandlerList(sender, cmdArg[1]);
    }

    public void switchDebug(@NotNull CommandSender sender) {
        final boolean debug = plugin.getConfig().getBoolean("dev-mode");

        if (debug) {
            plugin.reloadConfig();
            plugin.getConfig().set("dev-mode", false);
            plugin.saveConfig();
            plugin.getReloadManager().reload();
            plugin.text().of(sender, "command.now-nolonger-debuging").send();
            return;
        }

        plugin.reloadConfig();
        plugin.getConfig().set("dev-mode", true);
        plugin.saveConfig();
        plugin.getReloadManager().reload();
        plugin.text().of(sender, "command.now-debuging").send();
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
            plugin.getLogger().log(Level.WARNING, "An error has occurred while getting the HandlerList", th);
        }
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }

}
