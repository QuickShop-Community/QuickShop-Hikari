package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import net.kyori.adventure.text.Component;
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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

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
            default -> plugin.text().of(sender, "debug.arguments-invalid", cmdArg[0]).send();
        }
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

    private void handleHandlerList(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendDirectMessage(sender, Component.text("You must enter an Bukkit Event class"));
            return;
        }
        printHandlerList(sender, cmdArg[1]);
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
        plugin.text().of(sender, "debug.operation-invalid", cmdArg[0]).send();
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
