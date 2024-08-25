package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Random;

public class UpdateWatcher implements Listener {
    private final Random random = new Random();
    private final QuickShop plugin = QuickShop.getInstance();
    private WrappedTask cronTask = null;

    public void init() {
        cronTask = QuickShop.folia().getImpl().runTimerAsync(() -> {
            if (!plugin.getNexusManager().isLatest()) {
                plugin.logger().info("A new version of QuickShop has been released! [{}]", plugin.getNexusManager().getLatestVersion());
                plugin.logger().info("Update here: https://modrinth.com/plugin/quickshop-hikari");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (plugin.perm().hasPermission(player, "quickshop.alerts")) {
                        MsgUtil.sendDirectMessage(player, ChatColor.GREEN + "---------------------------------------------------");
                        MsgUtil.sendDirectMessage(player, ChatColor.GREEN + LegacyComponentSerializer.legacySection().serialize(pickRandomMessage(player)));
//                        MsgUtil.sendDirectMessage(player, ChatColor.GREEN + "Type command " + ChatColor.YELLOW + "/quickshop update" + ChatColor.GREEN + " or click the link below to update QuickShop :)");
                        MsgUtil.sendDirectMessage(player, Component.text("https://modrinth.com/plugin/quickshop-hikari").color(NamedTextColor.AQUA).clickEvent(ClickEvent.openUrl("https://modrinth.com/plugin/quickshop-hikari")));
                        MsgUtil.sendDirectMessage(player, ChatColor.GREEN + "---------------------------------------------------");
                    }
                }
            }
        }, 1, 20L * 60L * 60L);
    }

    private Component pickRandomMessage(CommandSender sender) {
        List<Component> messages = plugin.text().ofList(sender, "updatenotify.list").forLocale();
        int notifyNum = -1;
        if (messages.size() > 1) {
            notifyNum = random.nextInt(messages.size());
        }
        Component notify;
        if (notifyNum > 0) { // Translate bug.
            notify = messages.get(notifyNum);
        } else {
            notify = Component.text("New update {0} now available! Please update!");
        }
        return MsgUtil.fillArgs(notify, Component.text(plugin.getNexusManager().getLatestVersion()), Component.text(plugin.getVersion()));
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        Util.asyncThreadRun(() -> {
            if (!plugin.perm().hasPermission(e.getPlayer(), "quickshop.alerts") || plugin.getNexusManager().isLatest()) {
                return;
            }
            MsgUtil.sendDirectMessage(e.getPlayer(), ChatColor.GREEN + "---------------------------------------------------");
            MsgUtil.sendDirectMessage(e.getPlayer(), ChatColor.GREEN + LegacyComponentSerializer.legacySection().serialize(pickRandomMessage(e.getPlayer())));
            MsgUtil.sendDirectMessage(e.getPlayer(), ChatColor.AQUA + " https://modrinth.com/plugin/quickshop-hikari");
            MsgUtil.sendDirectMessage(e.getPlayer(), ChatColor.GREEN + "---------------------------------------------------");
        });
    }

    public void uninit() {
        if (cronTask == null) {
            return;
        }
        cronTask.cancel();
    }
}
