package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SubCommand_CleanGhost implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    public SubCommand_CleanGhost(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendDirectMessage(sender, Component.text("This command will purge all shops that: have corrupted data / are created in disallowed or unloaded worlds / trade with blacklisted items! Please make sure you have a backup of your shops data! Use /qs cleanghost to confirm the purge.").color(NamedTextColor.YELLOW));
            return;
        }

        if (!"confirm".equalsIgnoreCase(cmdArg[0])) {
            MsgUtil.sendDirectMessage(sender, Component.text("This command will purge all shops that: have corrupted data / are created in disallowed or unloaded worlds / trade with blacklisted items! Please make sure you have a backup of your shops data! Use /qs cleanghost to confirm the purge.").color(NamedTextColor.YELLOW));
            return;
        }

        MsgUtil.sendDirectMessage(sender, Component.text("Starting to check for ghost shops (missing container blocks). All non-existing shops will be removed...").color(NamedTextColor.GREEN));


        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            MsgUtil.sendDirectMessage(sender, Component.text("Starting async thread, please wait...").color(NamedTextColor.GREEN));
            //   Util.backupDatabase(); // Already warn the user, don't care about backup result.
            for (Shop shop : plugin.getShopManager().getAllShops()) {
                MsgUtil.sendDirectMessage(sender, Component.text("Checking the shop "
                        + shop
                        + " metadata and location block state...").color(NamedTextColor.GRAY));
                if (shop == null) {
                    continue; // WTF
                }
                if (shop.getItem().getType() == Material.AIR) {
                    MsgUtil.sendDirectMessage(sender, Component.text("Deleting shop " + shop + " because of corrupted item data.").color(NamedTextColor.YELLOW));
                    plugin.logEvent(new ShopRemoveLog(Util.getSenderUniqueId(sender), "/qs cleanghost command", shop.saveToInfoStorage()));
                    Util.mainThreadRun(shop::delete);
                    continue;
                }
                if (!shop.getLocation().isWorldLoaded()) {
                    MsgUtil.sendDirectMessage(sender, Component.text("Deleting shop " + shop + " because it's world not loaded.").color(NamedTextColor.YELLOW));
                    Util.mainThreadRun(shop::delete);
                    plugin.logEvent(new ShopRemoveLog(Util.getSenderUniqueId(sender), "/qs cleanghost command", shop.saveToInfoStorage()));
                    continue;
                }
                //noinspection ConstantConditions
                if (shop.getOwner() == null) {
                    MsgUtil.sendDirectMessage(sender, Component.text("Deleting shop " + shop + " because of corrupted owner data.").color(NamedTextColor.YELLOW));
                    Util.mainThreadRun(shop::delete);
                    plugin.logEvent(new ShopRemoveLog(Util.getSenderUniqueId(sender), "/qs cleanghost command", shop.saveToInfoStorage()));
                    continue;
                }
                // Shop exist check
                Util.mainThreadRun(() -> {
                    Log.debug(
                            "Posted to main server thread to continue accessing Bukkit API for shop "
                                    + shop);
                    if (!Util.canBeShop(shop.getLocation().getBlock())) {
                        MsgUtil.sendDirectMessage(sender, Component.text("Deleting shop "
                                + shop
                                + " because it is no longer on the target location or it is not allowed to create shops in this location.").color(NamedTextColor.YELLOW));
                        shop.delete();
                    }
                }); // Post to server main thread to check.
                try {
                    Thread.sleep(20); // Have a rest, don't blow up the main server thread.
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            MsgUtil.sendDirectMessage(sender, Component.text("All shops have been checked!").color(NamedTextColor.GREEN));
        });
    }


}
